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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppsAppUserRequest;
import user.management.system.app.model.dto.AppsAppUserResponse;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps_app_user")
@Validated
public class AppsAppUserController {

  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("ONLY SUPERUSER CAN ASSIGN USER TO APPS")
  @PostMapping("/apps_user")
  public ResponseEntity<AppsAppUserResponse> createAppsAppUser(
      @Valid @RequestBody final AppsAppUserRequest appsAppUserRequest,
      final HttpServletRequest request) {
    try {
      final AppsAppUserEntity appsAppUserEntity =
          appsAppUserService.createAppsAppUser(appsAppUserRequest);
      auditService.auditAppUserAssignApp(request, appsAppUserEntity);
      return entityDtoConvertUtils.getResponseSingleAppsAppUser(appsAppUserEntity);
    } catch (Exception ex) {
      log.error("Create Apps App User: [{}]", appsAppUserRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsers() {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities = appsAppUserService.readAppsAppUsers();
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appsAppUserEntities);
    } catch (Exception ex) {
      log.error("Read Apps App Users...", ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByAppId(
      @PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities =
          appsAppUserService.readAppsAppUsersByAppId(appId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appsAppUserEntities);
    } catch (Exception ex) {
      log.error("Read Apps App Users By App Id: [{}]", appId, ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/user/{appUserId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByUserId(
      @PathVariable final int appUserId) {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities =
          appsAppUserService.readAppsAppUsersByUserId(appUserId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appsAppUserEntities);
    } catch (Exception ex) {
      log.error("Read Apps App Users By User Id: [{}]", appUserId, ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/app/{appId}/user/{appUserEmail}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByAppIdAndUserEmail(
      @PathVariable final String appId, @PathVariable final String appUserEmail) {
    try {
      final AppsAppUserEntity appsAppUserEntity =
          appsAppUserService.readAppsAppUser(appId, appUserEmail);
      return entityDtoConvertUtils.getResponseSingleAppsAppUser(appsAppUserEntity);
    } catch (Exception ex) {
      log.error(
          "Read Apps App Users By App Id And User Email: [{}], [{}]", appId, appUserEmail, ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN UNASSIGN USER FROM APP")
  @DeleteMapping("/apps_user/{appId}/{appUserEmail}")
  public ResponseEntity<AppsAppUserResponse> deleteAppsAppUser(
      @PathVariable final String appId,
      @PathVariable final String appUserEmail,
      final HttpServletRequest request) {
    try {
      appsAppUserService.deleteAppsAppUser(appId, appUserEmail);
      auditService.auditAppUserUnassignApp(request, appUserEmail, appId);
      return entityDtoConvertUtils.getResponseDeleteAppsAppUser();
    } catch (Exception ex) {
      log.error("Delete Apps App User: [{}], [{}]", appId, appUserEmail, ex);
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }
}
