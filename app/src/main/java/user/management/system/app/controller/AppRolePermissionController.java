package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppRolePermissionRequest;
import user.management.system.app.model.dto.AppRolePermissionResponse;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(
    name = "App Role Permission Controller",
    description = "View, Assign and Unassign permissions from roles")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles_permissions")
public class AppRolePermissionController {

  private final AppRolePermissionService appRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("ROLE_PERMISSION_ASSIGN")
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

  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
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

  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
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

  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
  @GetMapping("/app/{appId}/roles/{roleIds}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissionsByRoleIds(
      @PathVariable final String appId, @PathVariable final List<Integer> roleIds) {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(appId, roleIds);
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
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

  @CheckPermission("ROLE_PERMISSION_UNASSIGN")
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
