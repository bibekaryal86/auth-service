package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.AuditService;
import auth.service.app.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@Validated
public class RoleController {

  private final RoleService roleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("ROLE_CREATE")
  @PostMapping("/role")
  public ResponseEntity<RoleResponse> createRole(
      @Valid @RequestBody final RoleRequest roleRequest, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.createRole(roleRequest);
      // TODO audit
      // runAsync(() -> auditService.auditRoleCreate(request, roleEntity));
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity);
    } catch (Exception ex) {
      log.error("Create Role: [{}]", roleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ROLE_READ")
  @GetMapping
  public ResponseEntity<RoleResponse> readRoles() {
    try {
      final List<RoleEntity> roleEntities = roleService.readRoles();
      return entityDtoConvertUtils.getResponseMultipleRoles(roleEntities);
    } catch (Exception ex) {
      log.error("Read Roles...", ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ROLE_READ")
  @GetMapping("/role/{id}")
  public ResponseEntity<RoleResponse> readRole(@PathVariable final long id) {
    try {
      final RoleEntity roleEntity = roleService.readRole(id);
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity);
    } catch (Exception ex) {
      log.error("Read Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ROLE_UPDATE")
  @PutMapping("/role/{id}")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable final long id,
      @Valid @RequestBody final RoleRequest roleRequest,
      final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.updateRole(id, roleRequest);
      // TODO audit
      // runAsync(() -> auditService.auditRoleUpdate(request, roleEntity));
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity);
    } catch (Exception ex) {
      log.error("Update Role: [{}] | [{}]", id, roleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ROLE_DELETE")
  @DeleteMapping("/role/{id}")
  public ResponseEntity<RoleResponse> softDeleteRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      roleService.softDeleteRole(id);
      // TODO audit
      // runAsync(() -> auditService.auditRoleDeleteSoft(request, id));
      return entityDtoConvertUtils.getResponseDeleteRole();
    } catch (Exception ex) {
      log.error("Soft Delete Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/role/{id}/hard")
  public ResponseEntity<RoleResponse> hardDeleteRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      roleService.hardDeleteRole(id);
      // TODO audit
      // runAsync(() -> auditService.auditRoleDeleteHard(request, id));
      return entityDtoConvertUtils.getResponseDeleteRole();
    } catch (Exception ex) {
      log.error("Hard Delete Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/role/{id}/restore")
  public ResponseEntity<RoleResponse> restoreRole(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final RoleEntity roleEntity = roleService.restoreSoftDeletedRole(id);
      // TODO audit
      // runAsync(() -> auditService.auditRoleRestore(request, id));
      return entityDtoConvertUtils.getResponseSingleRole(roleEntity);
    } catch (Exception ex) {
      log.error("Restore Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorRole(ex);
    }
  }
}
