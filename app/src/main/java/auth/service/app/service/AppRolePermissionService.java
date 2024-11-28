package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppRolePermissionRequest;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppRolePermissionId;
import auth.service.app.repository.AppRolePermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppRolePermissionService {

  private final AppRolePermissionRepository appRolePermissionRepository;
  private final AppRoleService appRoleService;
  private final AppPermissionService appPermissionService;

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
    appRolePermissionEntity.setId(
        new AppRolePermissionId(appRoleEntity.getId(), appPermissionEntity.getId()));
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
      final String appId, final List<Integer> appRoleIds) {
    log.debug("Read App Role Permissions: [{}], [{}]", appId, appRoleIds);
    if (StringUtils.hasText(appId)) {
      return appRolePermissionRepository
          .findByAppPermissionAppIdAndAppRoleIdInOrderByAppPermissionNameAsc(appId, appRoleIds);
    }
    return appRolePermissionRepository.findByAppRoleIdInOrderByAppPermissionNameAsc(appRoleIds);
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
  @Transactional
  public void deleteAppRolePermission(final int appRoleId, final int appPermissionId) {
    log.info("Delete App Role Permission: [{}], [{}]", appRoleId, appPermissionId);
    final AppRolePermissionEntity appRolePermissionEntity =
        readAppRolePermission(appRoleId, appPermissionId);
    appRolePermissionRepository.delete(appRolePermissionEntity);
  }
}
