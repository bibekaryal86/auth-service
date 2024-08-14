package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.util.PasswordUtils;

@Slf4j
@Service
public class AppUserService {

  private final AppUserRepository appUserRepository;
  private final PasswordUtils passwordUtils;

  public AppUserService(
      final AppUserRepository appUserRepository, final PasswordUtils passwordUtils) {
    this.appUserRepository = appUserRepository;
    this.passwordUtils = passwordUtils;
  }

  // CREATE
  public AppUserEntity createAppUser(final AppUserRequest appUserRequest) {
    log.debug("Create App User: [{}]", appUserRequest);
    if (!StringUtils.hasText(appUserRequest.getPassword())) {
      // password is required for create, optional for update
      throw new ElementMissingException("User", "password");
    }

    AppUserEntity appUserEntity = new AppUserEntity();
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
    appUserEntity.setPassword(passwordUtils.hashPassword(appUserRequest.getPassword()));
    appUserEntity.setIsValidated(false);

    // TODO add guest role or standard user role
    // TODO initiate validation email

    return appUserRepository.save(appUserEntity);
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
            .orElseThrow(() -> new ElementNotFoundException("User", String.format("%s,%s", app, email)));
  }

  // UPDATE
  public AppUserEntity updateAppUser(final int id, final AppUserRequest appUserRequest) {
    log.debug("Update App User: [{}], [{}]", id, appUserRequest);
    AppUserEntity appUserEntity = readAppUser(id);
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
    return appUserRepository.save(appUserEntity);
  }

  public AppUserEntity updateAppUserPassword(final int id, final AppUserRequest appUserRequest) {
    log.debug("Update App User Password: [{}], [{}]", id, appUserRequest);
    AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setPassword(passwordUtils.hashPassword(appUserRequest.getPassword()));
    return appUserRepository.save(appUserEntity);
  }

  // DELETE
  public AppUserEntity softDeleteAppUser(final int id) {
    log.info("Soft Delete App User: [{}]", id);
    AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setDeletedDate(LocalDateTime.now());
    return appUserRepository.save(appUserEntity);
  }

  public void hardDeleteAppUser(final int id) {
    log.info("Hard Delete App User: [{}]", id);
    AppUserEntity appUserEntity = readAppUser(id);
    appUserRepository.delete(appUserEntity);
  }

  // RESTORE
  public AppUserEntity restoreSoftDeletedAppUser(final int id) {
    log.info("Restore Soft Deleted App User: [{}]", id);
    AppUserEntity appUserEntity = readAppUser(id);
    appUserEntity.setDeletedDate(null);
    return appUserRepository.save(appUserEntity);
  }
}
