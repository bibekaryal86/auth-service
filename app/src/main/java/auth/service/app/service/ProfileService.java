package auth.service.app.service;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.ProfileAddressRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.JwtUtils;
import auth.service.app.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final ProfileAddressRepository profileAddressRepository;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final CircularDependencyService circularDependencyService;
  private final TokenService tokenService;
  private final PasswordUtils passwordUtils;
  private final ApplicationEventPublisher applicationEventPublisher;

  // CREATE
  @Transactional
  public ProfileEntity createProfile(
      final PlatformEntity platformEntity,
      final ProfileRequest profileRequest,
      final String baseUrlForEmail) {
    log.debug(
        "Create Profile: ProfileRequest=[{}], BaseUrlForEmail=[{}]",
        profileRequest,
        baseUrlForEmail);
    createProfileValidate(profileRequest);

    // profile
    ProfileEntity profileEntity = new ProfileEntity();
    BeanUtils.copyProperties(profileRequest, profileEntity, "password", "profileAddress");
    profileEntity.setPassword(passwordUtils.hashPassword(profileRequest.getPassword()));
    profileEntity.setIsValidated(false);

    // profile_address
    ProfileAddressEntity profileAddressEntity = null;

    if (profileRequest.getAddressRequest() != null) {
      profileEntity.setProfileAddress(null);
    } else {
      profileAddressEntity = new ProfileAddressEntity();
      BeanUtils.copyProperties(
          profileRequest.getAddressRequest(), profileAddressEntity, "id", "profileId");
      profileAddressEntity.setProfile(profileEntity);
    }

    // platform_profile_role
    final String roleName =
        profileRequest.isGuestUser()
            ? ConstantUtils.ROLE_NAME_GUEST
            : ConstantUtils.ROLE_NAME_STANDARD;
    final RoleEntity roleEntity = circularDependencyService.readRoleByName(roleName, Boolean.FALSE);

    // save profile
    profileEntity = profileRepository.save(profileEntity);
    // save profile address
    if (profileAddressEntity != null) {
      profileAddressRepository.save(profileAddressEntity);
    }
    // save platform profile role
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId());
    platformProfileRoleService.assignPlatformProfileRole(platformProfileRoleRequest);

    // publish event for validation email
    applicationEventPublisher.publishEvent(
        new ProfileEvent(
            this, TypeEnums.EventType.CREATE, profileEntity, platformEntity, baseUrlForEmail));
    return profileEntity;
  }

  // READ
  public List<ProfileEntity> readProfiles(final boolean isIncludeDeleted) {
    log.debug("Read Profiles: IsIncludeDeleted=[{}]", isIncludeDeleted);
    return profileRepository.findAllProfiles(isIncludeDeleted);
  }

  /** Use {@link CircularDependencyService#readProfile(Long, boolean)} */
  private ProfileEntity readProfile(final Long id) {
    log.debug("Read Profile: Id=[{}]", id);
    return profileRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Profile", String.valueOf(id)));
  }

  public ProfileEntity readProfileByEmail(final String email) {
    log.debug("Read Profile By Email: Email=[{}]", email);
    return profileRepository
        .findByEmail(email)
        .orElseThrow(() -> new ElementNotFoundException("Profile", email));
  }

  public ProfileEntity readProfileByEmailNoException(final String email) {
    log.debug("Read Profile By Email No Exception: Email=[{}]", email);
    try {
      return readProfileByEmail(email);
    } catch (Exception ignored) {
      return null;
    }
  }

  // UPDATE
  public ProfileEntity updateProfile(final ProfileEntity profileEntity) {
    return profileRepository.save(profileEntity);
  }

  @Transactional
  public ProfileEntity updateProfile(final Long id, final ProfileRequest profileRequest) {
    log.debug("Update Profile: Id=[{}], ProfileRequest=[{}]", id, profileRequest);
    ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    // profile
    BeanUtils.copyProperties(profileRequest, profileEntity, "email", "password", "profileAddress");

    // profile_address
    ProfileAddressEntity profileAddressEntity = null;
    if (profileRequest.getAddressRequest() != null) {
      profileAddressEntity = profileEntity.getProfileAddress();
      BeanUtils.copyProperties(
          profileRequest.getAddressRequest(), profileAddressEntity, "profileId");
    }

    // save profile
    profileEntity = updateProfile(profileEntity);
    // save profile_address
    if (profileAddressEntity != null) {
      if (profileRequest.getAddressRequest().isDeleteAddress()) {
        profileAddressRepository.deleteById(profileRequest.getAddressRequest().getId());
        profileEntity.setProfileAddress(null);
      } else {
        profileAddressEntity = profileAddressRepository.save(profileAddressEntity);
        profileEntity.setProfileAddress(profileAddressEntity);
      }
    }

    return profileEntity;
  }

  @Transactional
  public ProfileEntity updateProfileEmail(
      final Long id,
      final ProfileEmailRequest profileEmailRequest,
      final PlatformEntity platformEntity,
      final String baseUrlForEmail) {
    log.debug(
        "Update Profile Email: PlatformId=[{}], ProfileId=[{}], ProfileEmailRequest=[{}]",
        platformEntity.getId(),
        id,
        profileEmailRequest);
    final ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    profileEntity.setEmail(profileEmailRequest.getNewEmail());
    profileEntity.setIsValidated(false);
    final ProfileEntity profileEntityUpdated = updateProfile(profileEntity);

    applicationEventPublisher.publishEvent(
        new ProfileEvent(
            this,
            TypeEnums.EventType.UPDATE_EMAIL,
            profileEntity,
            platformEntity,
            baseUrlForEmail));

    // set tokens as deleted, checked during refresh token only
    tokenService.setTokenDeletedDateByProfileId(id);

    return profileEntityUpdated;
  }

  @Transactional
  public ProfileEntity updateProfilePassword(
      final Long id,
      final ProfilePasswordRequest profilePasswordRequest,
      final PlatformEntity platformEntity) {
    log.debug(
        "Update Profile Password: PlatformId=[{}], ProfileId=[{}], ProfilePasswordRequest=[{}]",
        platformEntity.getId(),
        id,
        profilePasswordRequest);
    final ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    profileEntity.setPassword(passwordUtils.hashPassword(profilePasswordRequest.getPassword()));

    final ProfileEntity profileEntityUpdated = updateProfile(profileEntity);

    applicationEventPublisher.publishEvent(
        new ProfileEvent(
            this, TypeEnums.EventType.UPDATE_PASSWORD, profileEntity, platformEntity, ""));

    // set tokens as deleted, checked during refresh token only
    tokenService.setTokenDeletedDateByProfileId(id);

    return profileEntityUpdated;
  }

  // DELETE
  public ProfileEntity softDeleteProfile(final Long id) {
    log.info("Soft Delete Profile: Id=[{}]", id);
    final ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    profileEntity.setDeletedDate(LocalDateTime.now());
    return profileRepository.save(profileEntity);
  }

  @Transactional
  public void hardDeleteProfile(final Long id) {
    log.info("Hard Delete Profile: Id=[{}]", id);
    final ProfileEntity profileEntity = readProfile(id);

    // before Profile can be deleted, we need to delete entities in PlatformProfileRole
    platformProfileRoleService.hardDeletePlatformProfileRolesByProfileIds(List.of(id));
    // also delete ProfileAddress
    if (profileEntity.getProfileAddress() != null
        && profileEntity.getProfileAddress().getId() != null) {
      profileAddressRepository.deleteById(profileEntity.getProfileAddress().getId());
    }

    profileRepository.delete(profileEntity);
  }

  // RESTORE
  public ProfileEntity restoreSoftDeletedProfile(final Long id) {
    log.info("Restore Soft Deleted Profile: Id=[{}]", id);
    final ProfileEntity profileEntity = readProfile(id);
    profileEntity.setDeletedDate(null);
    return profileRepository.save(profileEntity);
  }

  // OTHERS
  public ProfilePasswordTokenResponse loginProfile(
      final Long platformId,
      final ProfilePasswordRequest profilePasswordRequest,
      final String ipAddress) {
    log.info(
        "Login Profile: PlatformId=[{}], Email=[{}], IdAddress=[{}]",
        platformId,
        profilePasswordRequest.getEmail(),
        ipAddress);
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, profilePasswordRequest.getEmail());
    final PlatformEntity platformEntity = platformProfileRoleEntity.getPlatform();
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();
    loginProfileValidate(platformEntity, profileEntity);

    final boolean isLoginSuccess =
        passwordUtils.verifyPassword(
            profilePasswordRequest.getPassword(), profileEntity.getPassword());

    if (!isLoginSuccess) {
      throw new ProfileNotAuthorizedException();
    }

    return tokenService.saveToken(null, null, platformEntity, profileEntity, ipAddress);
  }

  public ProfileEntity resetProfile(
      final Long platformId, final ProfilePasswordRequest profilePasswordRequest) {
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, profilePasswordRequest.getEmail());
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();
    profileEntity.setPassword(passwordUtils.hashPassword(profilePasswordRequest.getPassword()));
    return updateProfile(profileEntity);
  }

  public ProfileEntity validateAndResetProfile(
      final Long platformId, final String encodedEmail, final boolean isValidate) {
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, JwtUtils.decodeEmailAddress(encodedEmail));
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();

    if (isValidate) {
      profileEntity.setIsValidated(true);
      return updateProfile(profileEntity);
    }

    return profileEntity;
  }

  private void createProfileValidate(final ProfileRequest profileRequest) {
    // password required for create profile
    if (!StringUtils.hasText(profileRequest.getPassword())) {
      throw new ElementMissingException("Profile", "password");
    }
  }

  private void loginProfileValidate(
      final PlatformEntity platformEntity, final ProfileEntity profileEntity) {
    if (platformEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Platform", String.valueOf(platformEntity.getId()));
    }

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", profileEntity.getEmail());
    }

    if (!profileEntity.getIsValidated()) {
      throw new ProfileNotValidatedException();
    }

    if (profileEntity.getLoginAttempts() != null && profileEntity.getLoginAttempts() >= 5) {
      throw new ProfileLockedException();
    }

    if (profileEntity.getLastLogin() != null
        && profileEntity.getLastLogin().isBefore(LocalDateTime.now().minusDays(45))) {
      throw new ProfileNotActiveException();
    }
  }
}
