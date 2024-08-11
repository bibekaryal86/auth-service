package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
    AppUserEntity appUserEntity = new AppUserEntity();
    BeanUtils.copyProperties(appUserRequest, appUserEntity, "password");
    appUserEntity.setPassword(passwordUtils.hashPassword(appUserRequest.getPassword()));
    return appUserRepository.save(appUserEntity);
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
