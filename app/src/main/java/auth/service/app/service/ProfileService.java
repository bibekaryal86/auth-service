package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_GUEST;
import static auth.service.app.util.ConstantUtils.ROLE_NAME_STANDARD;
import static auth.service.app.util.JwtUtils.decodeEmailAddress;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.ProfileAddressRequest;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.ProfileAddressRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.JpaDataUtils;
import auth.service.app.util.PasswordUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    log.debug("Create App User: [{}], [{}]", profileRequest, baseUrlForEmail);
    validateCreateProfile(profileRequest);

    // profile
    ProfileEntity profileEntity = new ProfileEntity();
    BeanUtils.copyProperties(profileRequest, profileEntity, "password", "addresses");
    profileEntity.setPassword(passwordUtils.hashPassword(profileRequest.getPassword()));
    profileEntity.setIsValidated(false);

    // profile_addresses
    ProfileAddressEntity profileAddressEntity =
        convertAddressRequestToEntity(profileRequest.getAddressRequest(), profileEntity, true);

    // platform_profile_role
    final RoleEntity roleEntity = getRoleEntityToCreate(profileRequest.isGuestUser());
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId());

    // save profile
    profileEntity = profileRepository.save(profileEntity);
    // save profile address
    if (profileAddressEntity != null) {
      profileAddressRepository.save(profileAddressEntity);
    }
    // save platform profile role
    platformProfileRoleService.createPlatformProfileRole(platformProfileRoleRequest);

    // publish event for validation email
    applicationEventPublisher.publishEvent(
        new ProfileEvent(
            this, TypeEnums.EventType.CREATE, profileEntity, platformEntity, baseUrlForEmail));
    return profileEntity;
  }

  private void validateCreateProfile(final ProfileRequest profileRequest) {
    // password and app are required for create user
    if (!StringUtils.hasText(profileRequest.getPassword())) {
      throw new ElementMissingException("Profile", "password");
    }
  }

  private RoleEntity getRoleEntityToCreate(final boolean isGuestUser) {
    final String roleName = isGuestUser ? ROLE_NAME_GUEST : ROLE_NAME_STANDARD;
    return circularDependencyService.readRoleByName(roleName, false);
  }

  // READ
  public Page<ProfileEntity> readProfiles(final RequestMetadata requestMetadata) {
    log.debug("Read Profiles: [{}]", requestMetadata);
    if (CommonUtils.isRequestMetadataIncluded(requestMetadata)) {
      Specification<ProfileEntity> specification =
          JpaDataUtils.getQuerySpecification(requestMetadata);
      Pageable pageable = JpaDataUtils.getQueryPageable(requestMetadata, "lastName");
      return profileRepository.findAll(specification, pageable);
    }
    return new PageImpl<>(profileRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName")));
  }

  /** Use {@link CircularDependencyService#readProfile(Long, Boolean)} */
  private ProfileEntity readProfile(final Long id) {
    log.debug("Read Profile: [{}]", id);
    return profileRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Profile", String.valueOf(id)));
  }

  public ProfileEntity readProfileByEmail(final String email) {
    log.debug("Read Profile By Email: [{}]", email);
    return profileRepository
        .findByEmail(email)
        .orElseThrow(() -> new ElementNotFoundException("Profile", email));
  }

  public ProfileEntity readProfileByEmailNoException(final String email) {
    log.debug("Read Profile By Email No Exception: [{}]", email);
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
    log.debug("Update Profiler: [{}], [{}]", id, profileRequest);
    ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    // profile
    BeanUtils.copyProperties(profileRequest, profileEntity, "email", "password", "addresses");
    // profile_address
    ProfileAddressEntity profileAddressEntity =
        convertAddressRequestToEntity(profileRequest.getAddressRequest(), profileEntity, false);

    // save profile
    profileEntity = updateProfile(profileEntity);
    // save profile_address
    profileAddressRepository.save(profileAddressEntity);

    return profileEntity;
  }

  @Transactional
  public ProfileEntity updateProfileEmail(
      final Long id,
      final ProfileEmailRequest profileEmailRequest,
      final PlatformEntity platformEntity,
      final String baseUrlForEmail) {
    log.debug(
        "Update Profile Email: platform-[{}], profile-[{}], [{}]",
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
        "Update Profile Password: platform-[{}], profile-[{}], [{}]",
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
    log.info("Soft Delete Profile: [{}]", id);
    final ProfileEntity profileEntity = readProfile(id);

    if (profileEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }

    profileEntity.setDeletedDate(LocalDateTime.now());
    return profileRepository.save(profileEntity);
  }

  public void hardDeleteProfile(final Long id) {
    log.info("Hard Delete Profile: [{}]", id);
    final ProfileEntity profileEntity = readProfile(id);
    profileRepository.delete(profileEntity);
  }

  // RESTORE
  public ProfileEntity restoreSoftDeletedProfile(final Long id) {
    log.info("Restore Soft Deleted Profile: [{}]", id);
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
        "Login Profile: [{}] [{}] [{}]", platformId, profilePasswordRequest.getEmail(), ipAddress);
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, profilePasswordRequest.getEmail());
    final PlatformEntity platformEntity = platformProfileRoleEntity.getPlatform();
    final ProfileEntity profileEntity =
        getProfileEntity(platformId, platformProfileRoleEntity, platformEntity);

    final boolean isLoginSuccess =
        passwordUtils.verifyPassword(
            profilePasswordRequest.getPassword(), profileEntity.getPassword());

    if (!isLoginSuccess) {
      throw new ProfileNotAuthorizedException();
    }

    return tokenService.saveToken(null, null, platformEntity, profileEntity, ipAddress);
  }

  @NotNull
  private static ProfileEntity getProfileEntity(
      Long platformId,
      PlatformProfileRoleEntity platformProfileRoleEntity,
      PlatformEntity platformEntity) {
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();

    if (platformEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Platform", String.valueOf(platformId));
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

    return profileEntity;
  }

  public ProfileEntity resetProfile(
      final Long platformId, final ProfilePasswordRequest profilePasswordRequest) {
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, profilePasswordRequest.getEmail());
    CommonUtils.validatePlatformProfileRoleNotDeleted(platformProfileRoleEntity);
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();
    profileEntity.setPassword(passwordUtils.hashPassword(profilePasswordRequest.getPassword()));
    return updateProfile(profileEntity);
  }

  public ProfileEntity validateAndResetProfile(
      final Long platformId, final String encodedEmail, final boolean isValidate) {
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformId, decodeEmailAddress(encodedEmail));
    CommonUtils.validatePlatformProfileRoleNotDeleted(platformProfileRoleEntity);
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();

    if (isValidate) {
      profileEntity.setIsValidated(true);
      return updateProfile(profileEntity);
    }

    return profileEntity;
  }

  private ProfileAddressEntity convertAddressRequestToEntity(
      final ProfileAddressRequest request,
      final ProfileEntity profileEntity,
      final boolean isIgnoreId) {
    if (request == null) {
      return null;
    }

    ProfileAddressEntity entity = new ProfileAddressEntity();
    if (isIgnoreId) {
      BeanUtils.copyProperties(request, entity, "id", "profileId", "typeId");
    } else {
      BeanUtils.copyProperties(request, entity, "profileId", "typeId");
    }
    entity.setProfile(profileEntity);
    return entity;
  }
}
