package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.PROFILE_STATUS_NAME_ACTIVE;
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
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.ProfileAddressRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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

    final StatusTypeEntity statusTypeEntity =
        circularDependencyService.readStatusType(profileRequest.getStatusId());
    ProfileEntity profileEntity = new ProfileEntity();
    BeanUtils.copyProperties(profileRequest, profileEntity, "password", "addresses");
    profileEntity.setPassword(passwordUtils.hashPassword(profileRequest.getPassword()));
    profileEntity.setIsValidated(false);
    profileEntity.setStatusType(statusTypeEntity);

    profileEntity = profileRepository.save(profileEntity);

    // save addresses
    if (!CollectionUtils.isEmpty(profileRequest.getAddresses())) {
      List<ProfileAddressEntity> profileAddressEntities =
          convertAddressRequestToEntity(profileRequest.getAddresses(), profileEntity, true);
      profileAddressRepository.saveAll(profileAddressEntities);
    }

    // save platform profile role
    final RoleEntity roleEntity = getRoleEntityToCreate(profileRequest.isGuestUser());
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId());
    platformProfileRoleService.createPlatformProfileRole(platformProfileRoleRequest);

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
    return circularDependencyService.readRoleByName(roleName);
  }

  // READ
  public List<ProfileEntity> readProfiles() {
    log.debug("Read App Users...");
    return profileRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
  }

  /** Use {@link CircularDependencyService#readProfile(Long)} */
  public ProfileEntity readProfile(final Long id) {
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
  @Transactional
  public ProfileEntity updateProfile(final Long id, final ProfileRequest profileRequest) {
    log.debug("Update Profiler: [{}], [{}]", id, profileRequest);
    ProfileEntity profileEntity = readProfile(id);
    BeanUtils.copyProperties(profileRequest, profileEntity, "email", "password", "addresses");
    profileEntity = updateProfile(profileEntity);

    if (!Objects.equals(profileRequest.getStatusId(), profileEntity.getStatusType().getId())) {
      profileEntity.setStatusType(
          circularDependencyService.readStatusType(profileRequest.getStatusId()));
    }

    // save addresses
    if (!CollectionUtils.isEmpty(profileRequest.getAddresses())) {
      List<ProfileAddressEntity> profileAddressEntities =
          convertAddressRequestToEntity(profileRequest.getAddresses(), profileEntity, false);
      profileAddressRepository.saveAll(profileAddressEntities);
    }

    return profileEntity;
  }

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

    return profileEntityUpdated;
  }

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
    profileEntity.setPassword(passwordUtils.hashPassword(profilePasswordRequest.getPassword()));

    final ProfileEntity profileEntityUpdated = updateProfile(profileEntity);

    applicationEventPublisher.publishEvent(
        new ProfileEvent(
            this, TypeEnums.EventType.UPDATE_PASSWORD, profileEntity, platformEntity, ""));

    return profileEntityUpdated;
  }

  @Transactional
  public ProfileEntity deleteProfileAddress(final Long profileId, final Long profileAddressId) {
    log.info("Delete Profile Address: [{}], [{}]", profileId, profileAddressId);
    final ProfileEntity profileEntity = readProfile(profileId);
    final ProfileAddressEntity profileAddressEntity =
        profileEntity.getAddresses().stream()
            .filter(address -> Objects.equals(address.getId(), profileAddressId))
            .findFirst()
            .orElseThrow(() -> new ElementNotFoundException("Profile Address", "id"));
    profileEntity.getAddresses().remove(profileAddressEntity);
    return profileEntity;
  }

  // DELETE
  public ProfileEntity softDeleteProfile(final Long id) {
    log.info("Soft Delete Profile: [{}]", id);
    final ProfileEntity profileEntity = readProfile(id);
    profileEntity.setDeletedDate(LocalDateTime.now());
    return profileRepository.save(profileEntity);
  }

  @Transactional
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
  @Transactional
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

    if (!Objects.equals(
        profileEntity.getStatusType().getStatusName().toUpperCase(), PROFILE_STATUS_NAME_ACTIVE)) {
      throw new ProfileNotActiveException();
    }

    if (profileEntity.getLoginAttempts() >= 5) {
      throw new ProfileLockedException();
    }

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
            platformId, decodeEmailAddress(encodedEmail));
    final ProfileEntity profileEntity = platformProfileRoleEntity.getProfile();

    if (isValidate) {
      profileEntity.setIsValidated(true);
      return updateProfile(profileEntity);
    }

    return profileEntity;
  }

  private ProfileEntity updateProfile(final ProfileEntity profileEntity) {
    return profileRepository.save(profileEntity);
  }

  private List<ProfileAddressEntity> convertAddressRequestToEntity(
      final List<ProfileAddressRequest> requests,
      final ProfileEntity profileEntity,
      final boolean isIgnoreId) {
    return requests.stream()
        .map(
            request -> {
              ProfileAddressEntity entity = new ProfileAddressEntity();
              if (isIgnoreId) {
                BeanUtils.copyProperties(request, entity, "id", "profileId", "typeId");
              } else {
                BeanUtils.copyProperties(request, entity, "profileId", "typeId");
              }
              entity.setProfile(profileEntity);
              entity.setType(circularDependencyService.readAddressType(request.getTypeId()));
              return entity;
            })
        .toList();
  }
}
