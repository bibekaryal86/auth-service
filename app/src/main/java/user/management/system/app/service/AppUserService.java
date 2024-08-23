package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserAddressDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserAddressEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsAppUserId;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.events.AppUserCreatedEvent;
import user.management.system.app.repository.AppUserAddressRepository;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.repository.AppsAppUserRepository;
import user.management.system.app.util.PasswordUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {

  private final AppUserRepository appUserRepository;
  private final AppUserAddressRepository appUserAddressRepository;
  private final AppsAppUserRepository appsAppUserRepository;
  private final PasswordUtils passwordUtils;
  private final ApplicationEventPublisher applicationEventPublisher;

  // CREATE
  public AppUserEntity createAppUser(
      final AppsEntity appsEntity,
      final AppUserRequest appUserRequest,
      final String baseUrlForEmail) {
    log.debug("Create App User: [{}], [{}]", appUserRequest, baseUrlForEmail);
    validateCreateAppUser(appUserRequest);

    AppUserEntity appUserEntity = new AppUserEntity();
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
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

    // @see EmailService, AppUserRoleService
    applicationEventPublisher.publishEvent(
        new AppUserCreatedEvent(
            this, appUserEntity, appsEntity, appUserRequest.isGuestUser(), baseUrlForEmail));
    return appUserEntity;
  }

  private void validateCreateAppUser(final AppUserRequest appUserRequest) {
    // password and app are required for create user
    if (!StringUtils.hasText(appUserRequest.getPassword())) {
      throw new ElementMissingException("User", "password");
    }
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
  public AppUserEntity updateAppUser(final int id, final AppUserRequest appUserRequest) {
    log.debug("Update App User: [{}], [{}]", id, appUserRequest);
    AppUserEntity appUserEntity = readAppUser(id);
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
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

  public AppUserEntity updateAppUserPassword(
      final int id, final UserLoginRequest userLoginRequest) {
    log.debug("Update App User Password: [{}], [{}]", id, userLoginRequest);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setPassword(passwordUtils.hashPassword(userLoginRequest.getPassword()));
    return appUserRepository.save(appUserEntity);
  }

  // DELETE
  public AppUserEntity softDeleteAppUser(final int id) {
    log.info("Soft Delete App User: [{}]", id);
    final AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setDeletedDate(LocalDateTime.now());
    return appUserRepository.save(appUserEntity);
  }

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
