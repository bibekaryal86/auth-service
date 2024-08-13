package user.management.system.app.controller;

import java.util.List;
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
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.service.AppRoleService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/app_roles")
public class AppRoleController {

  private final AppRoleService appRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppRoleController(final AppRoleService appRoleService, final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appRoleService = appRoleService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @PostMapping
  public ResponseEntity<AppRoleResponse> createAppRole(
      @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppRoleResponse> readAppRoles() {
    try {
      List<AppRoleEntity> appRoleEntities = appRoleService.readAppRoles();
      return entityDtoConvertUtils.getResponseMultipleAppRole(appRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppRoleResponse> readAppRole(@PathVariable final int id) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.readAppRole(id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppRoleResponse> updateAppRole(
      @PathVariable final int id, @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.updateAppRole(id, appRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<AppRoleResponse> softDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.softDeleteAppRole(id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<AppRoleResponse> hardDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.hardDeleteAppRole(id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @PatchMapping("/{id}/restore")
  public ResponseEntity<AppRoleResponse> restoreAppRole(@PathVariable final int id) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }
}
