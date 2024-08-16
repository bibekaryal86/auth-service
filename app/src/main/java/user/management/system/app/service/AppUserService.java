package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.events.AppUserCreatedEvent;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.util.PasswordUtils;

@Slf4j
@Service
public class AppUserService {

  private final AppUserRepository appUserRepository;
  private final PasswordUtils passwordUtils;
  private final ApplicationEventPublisher applicationEventPublisher;

  public AppUserService(
      final AppUserRepository appUserRepository,
      final PasswordUtils passwordUtils,
      final ApplicationEventPublisher applicationEventPublisher) {
    this.appUserRepository = appUserRepository;
    this.passwordUtils = passwordUtils;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  // CREATE
  public AppUserEntity createAppUser(
      final AppUserRequest appUserRequest, final String baseUrlForEmail) {
    log.debug("Create App User: [{}], [{}]", appUserRequest, baseUrlForEmail);
    AppUserEntity appUserEntity = new AppUserEntity();
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
    appUserEntity.setPassword(passwordUtils.hashPassword(appUserRequest.getPassword()));
    appUserEntity.setIsValidated(false);
    appUserEntity = appUserRepository.save(appUserEntity);
    // @see EmailService, AppUserRoleService
    applicationEventPublisher.publishEvent(
        new AppUserCreatedEvent(
            this, appUserEntity, appUserRequest.isGuestUser(), baseUrlForEmail));
    return appUserEntity;
  }

  private void validateCreateAppUser(final AppUserRequest appUserRequest) {
    // password and app are required for create user
    if (!StringUtils.hasText(appUserRequest.getPassword())) {
      throw new ElementMissingException("User", "password");
    } else if (!StringUtils.hasText(appUserRequest.getApp())) {
      throw new ElementMissingException("User", "app");
    }
  }

  // READ
  public List<AppUserEntity> readAppUsers() {
    log.debug("Read App Users...");
    return appUserRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
  }

  public List<AppUserEntity> readAppUsers(final String email) {
    log.debug("Read App Users: [{}]", email);
    return appUserRepository.findAllByEmailOrderByApp(email);
  }

  public AppUserEntity readAppUser(final int id) {
    log.debug("Read App User: [{}]", id);
    return appUserRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("User", String.valueOf(id)));
  }

  public AppUserEntity readAppUser(final String app, final String email) {
    log.debug("Read App User: [{}], [{}]", app, email);
    return appUserRepository
        .findByAppAndEmail(app, email)
        .orElseThrow(UserNotAuthorizedException::new);
  }

  // UPDATE
  public AppUserEntity updateAppUser(final int id, final AppUserRequest appUserRequest) {
    log.debug("Update App User: [{}], [{}]", id, appUserRequest);
    final AppUserEntity appUserEntity = readAppUser(id);
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
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
}
