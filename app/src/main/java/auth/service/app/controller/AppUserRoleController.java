package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.AppUserRoleRequest;
import auth.service.app.model.dto.AppUserRoleResponse;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.service.AppUserRoleService;
import auth.service.app.service.AuditService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users_roles")
@Validated
public class AppUserRoleController {

  private final AppUserRoleService appUserRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("USER_ROLE_ASSIGN")
  @PostMapping("/user_role")
  public ResponseEntity<AppUserRoleResponse> createAppUserRole(
      @Valid @RequestBody final AppUserRoleRequest appUserRoleRequest,
      final HttpServletRequest request) {
    try {
      final AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.createAppUserRole(appUserRoleRequest);
      runAsync(() -> auditService.auditAppUserAssignRole(request, appUserRoleEntity));
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      log.error("Create App User Role: [{}}", appUserRoleRequest, ex);
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
      log.error("Read App User Roles...", ex);
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
      log.error("Read App User Roles By User ID: [{}]", userId, ex);
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
      log.error("Read App User Roles By User IDs: [{}]", userIds, ex);
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
      log.error("Read App User Role: [{}], [{}]", userId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @CheckPermission("USER_ROLE_UNASSIGN")
  @DeleteMapping("/user_role/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> deleteAppUserRole(
      @PathVariable final int userId,
      @PathVariable final int roleId,
      final HttpServletRequest request) {
    try {
      appUserRoleService.deleteAppUserRole(userId, roleId);
      runAsync(() -> auditService.auditAppUserUnassignRole(request, userId, roleId));
      return entityDtoConvertUtils.getResponseDeleteAppUserRole();
    } catch (Exception ex) {
      log.error("Delete App User Role: [{}], [{}]", userId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }
}
