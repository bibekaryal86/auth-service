package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;
import user.management.system.app.util.PermissionCheck;

@Tag(name = "Users Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users")
public class AppUserController {

  private final AppUserService appUserService;
  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;

  @GetMapping
  public ResponseEntity<AppUserResponse> readAppUsers() {
    try {
      final List<AppUserEntity> appUserEntities = appUserService.readAppUsers();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/{appId}")
  public ResponseEntity<AppUserResponse> readAppUsers(@PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appId);
      final List<AppUserEntity> appUserEntities =
          appsAppUserEntities.stream().map(AppsAppUserEntity::getAppUser).toList();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final int id) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity = appUserService.readAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/user/email/{email}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final String email) {
    try {
      permissionCheck.canUserAccessAppUser(email, 0);
      final AppUserEntity appUserEntity = appUserService.readAppUser(email);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> updateAppUser(
      @PathVariable final int id, @RequestBody final AppUserRequest appUserRequest) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity = appUserService.updateAppUser(id, appUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("/user/{id}/password")
  public ResponseEntity<AppUserResponse> updateAppUserPassword(
      @PathVariable final int id, @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity =
          appUserService.updateAppUserPassword(id, userLoginRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN SOFT DELETE USER")
  @DeleteMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> softDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.softDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/user/{id}/hard")
  public ResponseEntity<AppUserResponse> hardDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.hardDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/user/{id}/restore")
  public ResponseEntity<AppUserResponse> restoreAppUser(@PathVariable final int id) {
    try {
      final AppUserEntity appUserEntity = appUserService.restoreSoftDeletedAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }
}
