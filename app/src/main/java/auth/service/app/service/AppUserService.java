package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.APP_ROLE_NAME_GUEST;
import static auth.service.app.util.ConstantUtils.APP_ROLE_NAME_STANDARD;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppUserAddressDto;
import auth.service.app.model.dto.AppUserRequest;
import auth.service.app.model.dto.UserLoginRequest;
import auth.service.app.model.dto.UserUpdateEmailRequest;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppUserAddressEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.model.entity.AppUserRoleId;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsAppUserId;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.events.AppUserCreatedEvent;
import auth.service.app.model.events.AppUserUpdatedEvent;
import auth.service.app.repository.AppRoleRepository;
import auth.service.app.repository.AppUserAddressRepository;
import auth.service.app.repository.AppUserRepository;
import auth.service.app.repository.AppUserRoleRepository;
import auth.service.app.repository.AppsAppUserRepository;
import auth.service.app.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.List;
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
public class AppUserService {

  private final AppUserRepository appUserRepository;
  private final AppUserAddressRepository appUserAddressRepository;
  private final AppsAppUserRepository appsAppUserRepository;
  private final AppRoleRepository appRoleRepository;
  private final AppUserRoleRepository appUserRoleRepository;
  private final PasswordUtils passwordUtils;
  private final ApplicationEventPublisher applicationEventPublisher;

  // CREATE
  @Transactional
  public AppUserEntity createAppUser(
      final AppsEntity appsEntity,
      final AppUserRequest appUserRequest,
      final String baseUrlForEmail) {
    log.debug("Create App User: [{}], [{}]", appUserRequest, baseUrlForEmail);
    validateCreateAppUser(appUserRequest);

    AppUserEntity appUserEntity = new AppUserEntity();
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password", "addresses");
    appUserEntity.setPassword(passwordUtils.hashPassword(appUserRequest.getPassword()));
    appUserEntity.setIsValidated(false);
    appUserEntity = appUserRepository.save(appUserEntity);

    // save addresses
    if (!CollectionUtils.isEmpty(appUserRequest.getAddresses())) {
      List<AppUserAddressEntity> appUserAddressEntities =
          convertAddressRequestToEntity(appUserRequest.getAddresses(), appUserEntity);
      appUserAddressRepository.saveAll(appUserAddressEntities);
    }

    // save apps user
    AppsAppUserEntity appsAppUserEntity = new AppsAppUserEntity();
    appsAppUserEntity.setApp(appsEntity);
    appsAppUserEntity.setAppUser(appUserEntity);
    appsAppUserEntity.setAssignedDate(LocalDateTime.now());
    appsAppUserEntity.setId(new AppsAppUserId(appsEntity.getId(), appUserEntity.getId()));
    appsAppUserRepository.save(appsAppUserEntity);

    // save app role
    final AppRoleEntity appRoleEntity = getAppRoleEntityToCreate(appUserRequest.isGuestUser());
    AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
    appUserRoleEntity.setAppUser(appUserEntity);
    appUserRoleEntity.setAppRole(appRoleEntity);
    appUserRoleEntity.setAssignedDate(LocalDateTime.now());
    appUserRoleEntity.setId(new AppUserRoleId(appUserEntity.getId(), appRoleEntity.getId()));
    appUserRoleRepository.save(appUserRoleEntity);

    // @see EmailService
    applicationEventPublisher.publishEvent(
        new AppUserCreatedEvent(this, appUserEntity, appsEntity, baseUrlForEmail));
    return appUserEntity;
  }

  private void validateCreateAppUser(final AppUserRequest appUserRequest) {
    // password and app are required for create user
    if (!StringUtils.hasText(appUserRequest.getPassword())) {
      throw new ElementMissingException("User", "password");
    }
  }

  private AppRoleEntity getAppRoleEntityToCreate(final boolean isGuestUser) {
    final String roleName = isGuestUser ? APP_ROLE_NAME_GUEST : APP_ROLE_NAME_STANDARD;
    return appRoleRepository
        .findByName(roleName)
        .orElseThrow(() -> new ElementNotFoundException("Role", roleName));
  }

  // READ
  public List<AppUserEntity> readAppUsers() {
    log.debug("Read App Users...");
    return appUserRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
  }

  public AppUserEntity readAppUser(final int id) {
    log.debug("Read App User: [{}]", id);
    return appUserRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("User", String.valueOf(id)));
  }

  public AppUserEntity readAppUser(final String email) {
    log.debug("Read App User: [{}]", email);
    return appUserRepository
        .findByEmail(email)
        .orElseThrow(() -> new ElementNotFoundException("User", email));
  }

  // UPDATE
  @Transactional
  public AppUserEntity updateAppUser(final int id, final AppUserRequest appUserRequest) {
    log.debug("Update App User: [{}], [{}]", id, appUserRequest);
    AppUserEntity appUserEntity = readAppUser(id);
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "email", "password", "addresses");
    appUserEntity = updateAppUser(appUserEntity);

    // save addresses
    if (!CollectionUtils.isEmpty(appUserRequest.getAddresses())) {
      List<AppUserAddressEntity> appUserAddressEntities =
          convertAddressRequestToEntity(appUserRequest.getAddresses(), appUserEntity);
      appUserAddressRepository.saveAll(appUserAddressEntities);
    }

    return appUserEntity;
  }

  public AppUserEntity updateAppUser(final AppUserEntity appUserEntity) {
    return appUserRepository.save(appUserEntity);
  }

  public AppUserEntity updateAppUserEmail(
      final int id,
      final UserUpdateEmailRequest updateEmailRequest,
      final AppsEntity appsEntity,
      final String baseUrlForEmail) {
    log.debug(
        "Update App User Email: [{}], [{}], [{}]", appsEntity.getId(), id, updateEmailRequest);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setEmail(updateEmailRequest.getNewEmail());
    appUserEntity.setIsValidated(false);
    final AppUserEntity appUserEntityUpdated = updateAppUser(appUserEntity);
    // @see EmailService
    applicationEventPublisher.publishEvent(
        new AppUserUpdatedEvent(this, appUserEntity, appsEntity, baseUrlForEmail));
    return appUserEntityUpdated;
  }

  public AppUserEntity updateAppUserPassword(
      final int id, final UserLoginRequest userLoginRequest) {
    log.debug("Update App User Password: [{}], [{}]", id, userLoginRequest);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setPassword(passwordUtils.hashPassword(userLoginRequest.getPassword()));
    return updateAppUser(appUserEntity);
  }

  @Transactional
  public AppUserEntity deleteAppUserAddress(final int userId, final int addressId) {
    log.info("Delete App User Address: [{}], [{}]", userId, addressId);
    final AppUserEntity appUserEntity = readAppUser(userId);
    final AppUserAddressEntity appUserAddressEntity =
        appUserEntity.getAddresses().stream()
            .filter(address -> address.getId() == addressId)
            .findFirst()
            .orElseThrow(() -> new ElementNotFoundException("User Address", "id"));
    appUserEntity.getAddresses().remove(appUserAddressEntity);
    return appUserEntity;
  }

  // DELETE
  public AppUserEntity softDeleteAppUser(final int id) {
    log.info("Soft Delete App User: [{}]", id);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setDeletedDate(LocalDateTime.now());
    return appUserRepository.save(appUserEntity);
  }

  @Transactional
  public void hardDeleteAppUser(final int id) {
    log.info("Hard Delete App User: [{}]", id);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserRepository.delete(appUserEntity);
  }

  // RESTORE
  public AppUserEntity restoreSoftDeletedAppUser(final int id) {
    log.info("Restore Soft Deleted App User: [{}]", id);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setDeletedDate(null);
    return appUserRepository.save(appUserEntity);
  }

  // others
  private List<AppUserAddressEntity> convertAddressRequestToEntity(
      final List<AppUserAddressDto> requests, final AppUserEntity appUser) {
    return requests.stream()
        .map(
            request -> {
              AppUserAddressEntity entity = new AppUserAddressEntity();
              BeanUtils.copyProperties(request, entity);
              entity.setAppUser(appUser);
              return entity;
            })
        .toList();
  }
}
