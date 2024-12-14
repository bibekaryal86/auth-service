package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PermissionService;
import auth.service.app.util.EntityDtoConvertUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionController {

  private final PermissionService permissionService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("PERMISSION_CREATE")
  @PostMapping("/permission")
  public ResponseEntity<PermissionResponse> createPermission(
      @Valid @RequestBody final PermissionRequest permissionRequest,
      final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity =
          permissionService.createPermission(permissionRequest);
      runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_CREATE,
                  String.format(
                      "Permission Create [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      return entityDtoConvertUtils.getResponseSinglePermission(permissionEntity);
    } catch (Exception ex) {
      log.error("Create Permission: [{}]", permissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping
  public ResponseEntity<PermissionResponse> readPermissions() {
    try {
      final List<PermissionEntity> permissionEntities = permissionService.readPermissions();
      return entityDtoConvertUtils.getResponseMultiplePermissions(permissionEntities);
    } catch (Exception ex) {
      log.error("Read Permissions...", ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> readPermission(@PathVariable final long id) {
    try {
      final PermissionEntity permissionEntity = circularDependencyService.readPermission(id);
      return entityDtoConvertUtils.getResponseSinglePermission(permissionEntity);
    } catch (Exception ex) {
      log.error("Read Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_UPDATE")
  @PutMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> updatePermission(
      @PathVariable final long id,
      @Valid @RequestBody final PermissionRequest permissionRequest,
      final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity =
          permissionService.updatePermission(id, permissionRequest);
      runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_UPDATE,
                  String.format(
                      "Permission Update [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      return entityDtoConvertUtils.getResponseSinglePermission(permissionEntity);
    } catch (Exception ex) {
      log.error("Update Permission: [{}] | [{}]", id, permissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_DELETE")
  @DeleteMapping("/permission/{id}")
  public ResponseEntity<PermissionResponse> softDeletePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity = circularDependencyService.readPermission(id);
      permissionService.softDeletePermission(id);
      runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_DELETE_SOFT,
                  String.format(
                      "Permission Delete Soft [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      return entityDtoConvertUtils.getResponseDeletePermission();
    } catch (Exception ex) {
      log.error("Soft Delete Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/permission/{id}/hard")
  public ResponseEntity<PermissionResponse> hardDeletePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity = circularDependencyService.readPermission(id);
      permissionService.hardDeletePermission(id);
      runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_DELETE_HARD,
                  String.format(
                      "Permission Delete Hard [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      return entityDtoConvertUtils.getResponseDeletePermission();
    } catch (Exception ex) {
      log.error("Hard Delete Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/permission/{id}/restore")
  public ResponseEntity<PermissionResponse> restorePermission(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PermissionEntity permissionEntity = permissionService.restoreSoftDeletedPermission(id);
      runAsync(
          () ->
              auditService.auditPermission(
                  request,
                  permissionEntity,
                  AuditEnums.AuditPermission.PERMISSION_RESTORE,
                  String.format(
                      "Permission Restore [Id: %s] - [Name: %s]",
                      permissionEntity.getId(), permissionEntity.getPermissionName())));
      return entityDtoConvertUtils.getResponseSinglePermission(permissionEntity);
    } catch (Exception ex) {
      log.error("Restore Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPermission(ex);
    }
  }
}
