package user.management.system.app.controller;

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
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.service.AppRoleService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles")
@Validated
public class AppRoleController {

  private final AppRoleService appRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("ROLE_CREATE")
  @PostMapping("/role")
  public ResponseEntity<AppRoleResponse> createAppRole(
      @Valid @RequestBody final AppRoleRequest appRoleRequest, final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
      auditService.auditAppRoleCreate(request, appRoleEntity);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      log.error("Create App Role: [{}]", appRoleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_READ")
  @GetMapping
  public ResponseEntity<AppRoleResponse> readAppRoles() {
    try {
      final List<AppRoleEntity> appRoleEntities = appRoleService.readAppRoles();
      return entityDtoConvertUtils.getResponseMultipleAppRole(appRoleEntities);
    } catch (Exception ex) {
      log.error("Read App Roles...", ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_READ")
  @GetMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> readAppRole(@PathVariable final int id) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.readAppRole(id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      log.error("Read App Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_UPDATE")
  @PutMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> updateAppRole(
      @PathVariable final int id,
      @Valid @RequestBody final AppRoleRequest appRoleRequest,
      final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.updateAppRole(id, appRoleRequest);
      auditService.auditAppRoleUpdate(request, appRoleEntity);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      log.error("Update App Role: [{}] | [{}]", id, appRoleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_DELETE")
  @DeleteMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> softDeleteAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appRoleService.softDeleteAppRole(id);
      auditService.auditAppRoleDeleteSoft(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      log.error("Soft Delete App Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/role/{id}/hard")
  public ResponseEntity<AppRoleResponse> hardDeleteAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appRoleService.hardDeleteAppRole(id);
      auditService.auditAppRoleDeleteHard(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      log.error("Hard Delete App Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/role/{id}/restore")
  public ResponseEntity<AppRoleResponse> restoreAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
      auditService.auditAppRoleRestore(request, id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      log.error("Restore App Role: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }
}
