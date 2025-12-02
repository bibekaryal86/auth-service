package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@Validated
public class RoleController {

  private final RoleService roleService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("AUTHSVC_ROLE_CREATE")
  @PostMapping("/role")
  public ResponseEntity<RoleResponse> createRole(
      @Valid @RequestBody final RoleRequest roleRequest, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.createRole(roleRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  roleEntity,
                  AuditEnums.AuditRole.ROLE_CREATE,
                  String.format(
                      "Role Create [Id: %s] - [Name: %s]",
                      roleEntity.getId(), roleEntity.getRoleName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Create Role: RoleRequest=[{}]", roleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_READ")
  @GetMapping
  public ResponseEntity<RoleResponse> readRoles(
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted) {
    try {
      final List<RoleEntity> roleEntities = roleService.readRoles(isIncludeDeleted);
      return entityDtoConvertUtils.getResponseMultipleRoles(roleEntities);
    } catch (Exception ex) {
      log.error("Read Roles: IsIncludeDeleted=[{}]", isIncludeDeleted, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_READ")
  @GetMapping("/role/{id}")
  public ResponseEntity<RoleResponse> readRole(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory) {
    try {
      final RoleEntity roleEntity = circularDependencyService.readRole(id, isIncludeDeleted);
      List<AuditRoleEntity> auditRoleEntities = Collections.emptyList();
      if (isIncludeHistory) {
        auditRoleEntities = auditService.auditRoles(id);
      }

      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, null, auditRoleEntities);
    } catch (Exception ex) {
      log.error(
          "Read Role: Id=[{}], IsIncludeDeleted=[{}], IsIncludeHistory=[{}]",
          id,
          isIncludeDeleted,
          isIncludeHistory,
          ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_UPDATE")
  @PutMapping("/role/{id}")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable final long id,
      @Valid @RequestBody final RoleRequest roleRequest,
      final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.updateRole(id, roleRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  roleEntity,
                  AuditEnums.AuditRole.ROLE_UPDATE,
                  String.format(
                      "Role Update [Id: %s] - [Name: %s]",
                      roleEntity.getId(), roleEntity.getRoleName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Update Role: Id=[{}], RoleRequest=[{}]", id, roleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_SOFTDELETE")
  @DeleteMapping("/role/{id}")
  public ResponseEntity<RoleResponse> softDeleteRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = circularDependencyService.readRole(id, Boolean.FALSE);
      roleService.softDeleteRole(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  roleEntity,
                  AuditEnums.AuditRole.ROLE_DELETE_SOFT,
                  String.format(
                      "Role Delete Soft [Id: %s] - [Name: %s]",
                      roleEntity.getId(), roleEntity.getRoleName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Soft Delete Role: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_HARDDELETE")
  @DeleteMapping("/role/{id}/hard")
  public ResponseEntity<RoleResponse> hardDeleteRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = circularDependencyService.readRole(id, true);
      roleService.hardDeleteRole(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  roleEntity,
                  AuditEnums.AuditRole.ROLE_DELETE_HARD,
                  String.format(
                      "Role Delete Hard [Id: %s] - [Name: %s]",
                      roleEntity.getId(), roleEntity.getRoleName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Hard Delete Role: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("AUTHSVC_ROLE_RESTORE")
  @PatchMapping("/role/{id}/restore")
  public ResponseEntity<RoleResponse> restoreRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.restoreSoftDeletedRole(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  roleEntity,
                  AuditEnums.AuditRole.ROLE_RESTORE,
                  String.format(
                      "Role Restore [Id: %s] - [Name: %s]",
                      roleEntity.getId(), roleEntity.getRoleName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Restore Role: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }
}
