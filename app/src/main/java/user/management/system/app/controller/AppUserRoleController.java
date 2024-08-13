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
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.dto.AppUserRoleResponse;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppUserRoleService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/api/v1/app_users_roles")
public class AppUserRoleController {

  private final AppUserRoleService appUserRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppUserRoleController(
      final AppUserRoleService appUserRoleService,
      final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appUserRoleService = appUserRoleService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @PostMapping
  public ResponseEntity<AppUserRoleResponse> createAppUserRole(
      @RequestBody final AppUserRoleRequest appUserRoleRequest) {
    try {
      AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.createAppUserRole(appUserRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppUserRoleResponse> readAppUserRoles() {
    try {
      List<AppUserRoleEntity> appUserRoleEntities = appUserRoleService.readAppUserRoles();
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRolesByUserId(
      @PathVariable final int userId) {
    try {
      List<AppUserRoleEntity> appUserRoleEntities = appUserRoleService.readAppUserRoles(userId);
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @GetMapping("/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRole(
      @PathVariable final int userId, @PathVariable final int roleId) {
    try {
      AppUserRoleEntity appUserRoleEntity = appUserRoleService.readAppUserRole(userId, roleId);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @DeleteMapping("/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> deleteAppUserRole(
      @PathVariable final int userId, @PathVariable final int roleId) {
    try {
      appUserRoleService.deleteAppUserRole(userId, roleId);
      return entityDtoConvertUtils.getResponseDeleteAppUserRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }
}
