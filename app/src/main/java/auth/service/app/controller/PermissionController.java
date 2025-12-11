package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PermissionService;
import auth.service.app.service.PlatformRolePermissionService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionController {

  private final PermissionService permissionService;
  private final CircularDependencyService circularDependencyService;
  private final PlatformRolePermissionService platformRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("AUTHSVC_PERMISSION_CREATE")
  @PostMapping("/permission")
  public ResponseEntity<PermissionResponse> createPermission(
      @Valid @RequestBody final PermissionRequest permissionRequest,
      final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity =
          permissionService.createPermission(permissionRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_CREATE,
                  String.format(
                      "Permission Create [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePermission(
          permissionEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Create Permission: PermissionRequest=[{}]", permissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_READ")
  @GetMapping
  public ResponseEntity<PermissionResponse> readPermissions(
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "") final String platformId,
      @RequestParam(required = false, defaultValue = "") final String roleId) {
    try {
      final Long platformIdLong = CommonUtils.getValidId(platformId);
      final Long roleIdLong = CommonUtils.getValidId(roleId);
      final boolean isSuperUser = CommonUtils.isSuperUser(CommonUtils.getAuthentication());

      List<PermissionEntity> permissionEntities;
      if (platformIdLong == null && roleIdLong == null) {
        permissionEntities = permissionService.readPermissions(isIncludeDeleted && isSuperUser);
      } else {
        final List<PlatformRolePermissionEntity> prpEntities =
            platformRolePermissionService.readPlatformRolePermissions(
                platformIdLong, roleIdLong, isIncludeDeleted && isSuperUser);
        permissionEntities =
            prpEntities.stream().map(PlatformRolePermissionEntity::getPermission).toList();
      }

      return entityDtoConvertUtils.getResponseMultiplePermissions(permissionEntities);
    } catch (Exception ex) {
      log.error(
          "Read Permissions: IsIncludeDeleted=[{}], PlatformId=[{}], RoleId=[{}]",
          isIncludeDeleted,
          platformId,
          roleId,
          ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_READ")
  @GetMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> readPermission(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory) {
    try {
      final boolean isSuperUser = CommonUtils.isSuperUser(CommonUtils.getAuthentication());
      final PermissionEntity permissionEntity =
          circularDependencyService.readPermission(id, isIncludeDeleted && isSuperUser);
      List<AuditPermissionEntity> auditPermissionEntities = Collections.emptyList();
      if (isIncludeHistory) {
        auditPermissionEntities = auditService.auditPermissions(id);
      }

      return entityDtoConvertUtils.getResponseSinglePermission(
          permissionEntity, null, auditPermissionEntities);
    } catch (Exception ex) {
      log.error(
          "Read Permission: Id=[{}], IsIncludeDeleted=[{}], IsIncludeHistory=[{}]",
          id,
          isIncludeDeleted,
          isIncludeHistory,
          ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_UPDATE")
  @PutMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> updatePermission(
      @PathVariable final long id,
      @Valid @RequestBody final PermissionRequest permissionRequest,
      final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity =
          permissionService.updatePermission(id, permissionRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_UPDATE,
                  String.format(
                      "Permission Update [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePermission(
          permissionEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Update Permission: Id=[{}], PermissionRequest=[{}]", id, permissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_SOFTDELETE")
  @DeleteMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> softDeletePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity = permissionService.softDeletePermission(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_DELETE_SOFT,
                  String.format(
                      "Permission Delete Soft [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePermission(
          permissionEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Soft Delete Permission: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_HARDDELETE")
  @DeleteMapping("/permission/{id}/hard")
  public ResponseEntity<PermissionResponse> hardDeletePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity =
          circularDependencyService.readPermission(id, Boolean.TRUE);
      permissionService.hardDeletePermission(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_DELETE_HARD,
                  String.format(
                      "Permission Delete Hard [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePermission(
          new PermissionEntity(), responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Hard Delete Permission: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("AUTHSVC_PERMISSION_RESTORE")
  @PatchMapping("/permission/{id}/restore")
  public ResponseEntity<PermissionResponse> restorePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity = permissionService.restoreSoftDeletedPermission(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_RESTORE,
                  String.format(
                      "Permission Restore [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1);
      return entityDtoConvertUtils.getResponseSinglePermission(
          permissionEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Restore Permission: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }
}
