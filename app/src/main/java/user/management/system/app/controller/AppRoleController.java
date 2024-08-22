package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Roles Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles")
public class AppRoleController {

  private final AppRoleService appRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("ROLE_CREATE")
  @PostMapping("/role")
  public ResponseEntity<AppRoleResponse> createAppRole(
      @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
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
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_UPDATE")
  @PutMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> updateAppRole(
      @PathVariable final int id, @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.updateAppRole(id, appRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ROLE_DELETE")
  @DeleteMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> softDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.softDeleteAppRole(id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/role/{id}/hard")
  public ResponseEntity<AppRoleResponse> hardDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.hardDeleteAppRole(id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/role/{id}/restore")
  public ResponseEntity<AppRoleResponse> restoreAppRole(@PathVariable final int id) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }
}
