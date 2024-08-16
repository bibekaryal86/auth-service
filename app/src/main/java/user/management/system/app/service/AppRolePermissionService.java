package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppRolePermissionRequest;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppRolePermissionId;
import user.management.system.app.repository.AppRolePermissionRepository;

@Slf4j
@Service
public class AppRolePermissionService {

  private final AppRolePermissionRepository appRolePermissionRepository;
  private final AppRoleService appRoleService;
  private final AppPermissionService appPermissionService;

  public AppRolePermissionService(
      final AppRolePermissionRepository appRolePermissionRepository,
      final AppRoleService appRoleService,
      final AppPermissionService appPermissionService) {
    this.appRolePermissionRepository = appRolePermissionRepository;
    this.appRoleService = appRoleService;
    this.appPermissionService = appPermissionService;
  }

  // CREATE
  public AppRolePermissionEntity createAppRolePermission(
      final AppRolePermissionRequest appRolePermissionRequest) {
    log.debug("Create App Role Permission: [{}]", appRolePermissionRequest);
    AppRolePermissionEntity appRolePermissionEntity = new AppRolePermissionEntity();
    final AppRoleEntity appRoleEntity =
        appRoleService.readAppRole(appRolePermissionRequest.getRoleId());
    final AppPermissionEntity appPermissionEntity =
        appPermissionService.readAppPermission(appRolePermissionRequest.getPermissionId());
    appRolePermissionEntity.setAppRole(appRoleEntity);
    appRolePermissionEntity.setAppPermission(appPermissionEntity);
    appRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    return appRolePermissionRepository.save(appRolePermissionEntity);
  }

  // READ
  public List<AppRolePermissionEntity> readAppRolePermissions() {
    log.debug("Read App Role Permissions...");
    return appRolePermissionRepository.findAll(Sort.by(Sort.Direction.ASC, "appRole.name"));
  }

  public List<AppRolePermissionEntity> readAppRolePermissions(final int appRoleId) {
    log.debug("Read App Role Permissions: [{}]", appRoleId);
    return appRolePermissionRepository.findByAppRoleIdOrderByAppPermissionNameAsc(appRoleId);
  }

  public List<AppRolePermissionEntity> readAppRolePermissions(
      final String app, final List<Integer> appRoleIds) {
    log.debug("Read App Role Permissions: [{}], [{}]", app, appRoleIds);
    return appRolePermissionRepository
        .findByAppPermissionAppAndAppRoleIdInOrderByAppPermissionNameAsc(app, appRoleIds);
  }

  public AppRolePermissionEntity readAppRolePermission(
      final int appRoleId, final int appPermissionId) {
    log.debug("Read App Role Permission: [{}], [{}]", appRoleId, appPermissionId);
    return appRolePermissionRepository
        .findById(new AppRolePermissionId(appRoleId, appPermissionId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "App Role Permission", String.format("%s,%s", appRoleId, appPermissionId)));
  }

  // UPDATE
  // not provided

  // DELETE
  public void deleteAppRolePermission(final int appRoleId, final int appPermissionId) {
    log.info("Delete App Role Permission: [{}], [{}]", appRoleId, appPermissionId);
    final AppRolePermissionEntity appRolePermissionEntity =
        readAppRolePermission(appRoleId, appPermissionId);
    appRolePermissionRepository.delete(appRolePermissionEntity);
  }
}
