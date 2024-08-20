package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import user.management.system.app.model.dto.AppsAppUserRequest;
import user.management.system.app.model.dto.AppsAppUserResponse;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(
    name = "App Apps App User Controller",
    description = "View, Assign and Unassign users from apps")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps_app_user")
public class AppsAppUserController {

  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("ONLY SUPERUSER CAN ASSIGN USER TO APPS")
  @PostMapping("/apps_user")
  public ResponseEntity<AppsAppUserResponse> createAppAppsAppUser(
      @RequestBody final AppsAppUserRequest appAppsAppUserRequest) {
    try {
      final AppsAppUserEntity appAppsAppUserEntity =
          appsAppUserService.createAppsAppUser(appAppsAppUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppsAppUser(appAppsAppUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping
  public ResponseEntity<AppsAppUserResponse> readAppAppsAppUsers() {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities = appsAppUserService.readAppsAppUsers();
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByAppId(
      @PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/user/{appUserId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByUserId(
      @PathVariable final int appUserId) {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appUserId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
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
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN UNASSIGN USER FROM APP")
  @DeleteMapping("/apps_user/{appId}/{appUserEmail}")
  public ResponseEntity<AppsAppUserResponse> deleteAppsAppUser(
      @PathVariable final String appId, @PathVariable final String appUserEmail) {
    try {
      appsAppUserService.deleteAppsAppUser(appId, appUserEmail);
      return entityDtoConvertUtils.getResponseDeleteAppsAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }
}
