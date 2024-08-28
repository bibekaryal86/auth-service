package user.management.system.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles_permissions")
@Validated
public class AppRolePermissionController {

  private final AppRolePermissionService appRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("ROLE_PERMISSION_ASSIGN")
  @PostMapping("/role_permission")
  public ResponseEntity<AppRolePermissionResponse> createAppRolePermission(
      @Valid @RequestBody final AppRolePermissionRequest appRolePermissionRequest,
      final HttpServletRequest request) {
    try {
      final AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionService.createAppRolePermission(appRolePermissionRequest);
      runAsync(() -> auditService.auditAppRoleAssignPermission(request, appRolePermissionEntity));
      return entityDtoConvertUtils.getResponseSingleAppRolePermission(appRolePermissionEntity);
    } catch (Exception ex) {
      log.error("Create App Role Permission: [{}]", appRolePermissionRequest, ex);
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
      log.error("Read App Role Permissions...", ex);
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
      log.error("Read App Role Permissions By Role Id: [{}]", roleId, ex);
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
      log.error("Read App Role Permissions By Role Ids: [{}], [{}]", appId, roleIds, ex);
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
      log.error("Read App Role Permission: [{}], [{}]", roleId, permissionId, ex);
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @CheckPermission("ROLE_PERMISSION_UNASSIGN")
  @DeleteMapping("/role_permission/{roleId}/{permissionId}")
  public ResponseEntity<AppRolePermissionResponse> deleteAppRolePermission(
      @PathVariable final int roleId,
      @PathVariable final int permissionId,
      final HttpServletRequest request) {
    try {
      appRolePermissionService.deleteAppRolePermission(roleId, permissionId);
      runAsync(() -> auditService.auditAppRoleUnassignPermission(request, roleId, permissionId));
      return entityDtoConvertUtils.getResponseDeleteAppRolePermission();
    } catch (Exception ex) {
      log.error("Delete App Role Permission: [{}], [{}]", roleId, permissionId, ex);
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }
}
