package user.management.system.app.controller;

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
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.dto.AppUserRoleResponse;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppUserRoleService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users_roles")
public class AppUserRoleController {

  private final AppUserRoleService appUserRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("USER_ROLE_ASSIGN")
  @PostMapping("/user_role")
  public ResponseEntity<AppUserRoleResponse> createAppUserRole(
      @RequestBody final AppUserRoleRequest appUserRoleRequest) {
    try {
      final AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.createAppUserRole(appUserRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping
  public ResponseEntity<AppUserRoleResponse> readAppUserRoles() {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities = appUserRoleService.readAppUserRoles();
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/user/{userId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRolesByUserId(
      @PathVariable final int userId) {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities =
          appUserRoleService.readAppUserRoles(userId);
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/users/{userIds}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRolesByUserIds(
      @PathVariable final List<Integer> userIds) {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities =
          appUserRoleService.readAppUserRoles(userIds);
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/user_role/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRole(
      @PathVariable final int userId, @PathVariable final int roleId) {
    try {
      final AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.readAppUserRole(userId, roleId);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission("USER_ROLE_UNASSIGN")
  @DeleteMapping("/user_role/{userId}/{roleId}")
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
