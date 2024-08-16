package user.management.system.app.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AppRolePermissionRequest;
import user.management.system.app.model.dto.AppRolePermissionResponse;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/api/v1/app_roles_permissions")
public class AppRolePermissionController {

  private final AppRolePermissionService appRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppRolePermissionController(
      final AppRolePermissionService appRolePermissionService,
      final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appRolePermissionService = appRolePermissionService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @PostMapping("/role_permission")
  public ResponseEntity<AppRolePermissionResponse> createAppRolePermission(
      @RequestBody final AppRolePermissionRequest appRolePermissionRequest) {
    try {
      final AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionService.createAppRolePermission(appRolePermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppRolePermission(appRolePermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissions() {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions();
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @GetMapping("/role/{roleId}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissionsByRoleId(
      @PathVariable final int roleId) {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(roleId);
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @GetMapping("/app/{app}/roles/{roleIds}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissionsByRoleIds(
      @PathVariable final String app, @PathVariable final List<Integer> roleIds) {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(app, roleIds);
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @GetMapping("/role_permission/{roleId}/{permissionId}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermission(
      @PathVariable final int roleId, @PathVariable final int permissionId) {
    try {
      final AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionService.readAppRolePermission(roleId, permissionId);
      return entityDtoConvertUtils.getResponseSingleAppRolePermission(appRolePermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @DeleteMapping("/role_permission/{roleId}/{permissionId}")
  public ResponseEntity<AppRolePermissionResponse> deleteAppRolePermission(
      @PathVariable final int roleId, @PathVariable final int permissionId) {
    try {
      appRolePermissionService.deleteAppRolePermission(roleId, permissionId);
      return entityDtoConvertUtils.getResponseDeleteAppRolePermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }
}
