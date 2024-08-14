package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.repository.AppUserRoleRepository;

@Slf4j
@Service
public class AppUserRoleService {

  private final AppUserRoleRepository appUserRoleRepository;
  private final AppUserService appUserService;
  private final AppRoleService appRoleService;

  public AppUserRoleService(
      final AppUserRoleRepository appUserRoleRepository,
      final AppUserService appUserService,
      final AppRoleService appRoleService) {
    this.appUserRoleRepository = appUserRoleRepository;
    this.appUserService = appUserService;
    this.appRoleService = appRoleService;
  }

  // CREATE
  public AppUserRoleEntity createAppUserRole(final AppUserRoleRequest appUserRoleRequest) {
    log.debug("Create App User Role: [{}]", appUserRoleRequest);
    AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
    final AppUserEntity appUserEntity = appUserService.readAppUser(appUserRoleRequest.getUserId());
    final AppRoleEntity appRoleEntity = appRoleService.readAppRole(appUserRoleRequest.getRoleId());
    appUserRoleEntity.setAppUser(appUserEntity);
    appUserRoleEntity.setAppRole(appRoleEntity);
    appUserRoleEntity.setAssignedDate(LocalDateTime.now());
    return appUserRoleRepository.save(appUserRoleEntity);
  }

  // READ
  public List<AppUserRoleEntity> readAppUserRoles() {
    log.debug("Read App User Roles...");
    return appUserRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "appUser.name"));
  }

  public List<AppUserRoleEntity> readAppUserRoles(final int appUserId) {
    log.debug("Read App User Roles: [{}]", appUserId);
    return appUserRoleRepository.findByIdAppUserIdOrderByAppRoleNameAsc(appUserId);
  }

  public AppUserRoleEntity readAppUserRole(final int appUserId, final int appRoleId) {
    log.debug("Read App User Role: [{}], [{}]", appUserId, appRoleId);
    return appUserRoleRepository
        .findById(new AppUserRoleId(appUserId, appRoleId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "App User Role", String.format("%s,%s", appUserId, appRoleId)));
  }

  // UPDATE
  // not provided

  // DELETE
  public void deleteAppUserRole(final int appUserId, final int appRoleId) {
    log.info("Delete App User Role: [{}], [{}]", appUserId, appRoleId);
    final AppUserRoleEntity appUserRoleEntity = readAppUserRole(appUserId, appRoleId);
    appUserRoleRepository.delete(appUserRoleEntity);
  }
}
