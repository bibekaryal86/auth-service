package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserUpdateEmailRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;
import user.management.system.app.util.PermissionCheck;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users")
@Validated
public class AppUserController {

  private final AppUserService appUserService;
  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;
  private final AuditService auditService;

  @GetMapping
  public ResponseEntity<AppUserResponse> readAppUsers() {
    try {
      final List<AppUserEntity> appUserEntities = appUserService.readAppUsers();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      log.error("Read App Users...", ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/{appId}")
  public ResponseEntity<AppUserResponse> readAppUsersByAppId(@PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities =
          appsAppUserService.readAppsAppUsersByAppId(appId);
      final List<AppUserEntity> appUserEntities =
          appsAppUserEntities.stream().map(AppsAppUserEntity::getAppUser).toList();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      log.error("Read App Users By App Id: [{}]", appId, ex);
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
      log.error("Read App User: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/user/email/{email}")
  public ResponseEntity<AppUserResponse> readAppUserByEmail(@PathVariable final String email) {
    try {
      permissionCheck.canUserAccessAppUser(email, 0);
      final AppUserEntity appUserEntity = appUserService.readAppUser(email);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Read App User By Email: [{}]", email, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> updateAppUser(
      @PathVariable final int id,
      @Valid @RequestBody final AppUserRequest appUserRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      AppUserEntity appUserEntity = appUserService.updateAppUser(id, appUserRequest);
      if (!appUserRequest.getAddresses().isEmpty()) {
        appUserEntity = appUserService.readAppUser(appUserEntity.getId());
      }
      auditService.auditAppUserUpdate(request, appUserEntity);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Update App User: [{}] | [{}]", id, appUserRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("{appId}/user/{id}/email")
  public ResponseEntity<AppUserResponse> updateAppUserEmail(
      @PathVariable final String appId,
      @PathVariable final int id,
      @Valid @RequestBody final UserUpdateEmailRequest userUpdateEmailRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final AppsAppUserEntity appsAppUserEntity =
          appsAppUserService.readAppsAppUser(appId, userUpdateEmailRequest.getOldEmail());
      final AppUserEntity appUserEntity =
          appUserService.updateAppUserEmail(
              id, userUpdateEmailRequest, appsAppUserEntity.getApp(), baseUrl);
      auditService.auditAppUserUpdateEmail(request, appUserEntity);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Update App User Email: [{}] | [{}]", id, userUpdateEmailRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("/user/{id}/password")
  public ResponseEntity<AppUserResponse> updateAppUserPassword(
      @PathVariable final int id,
      @Valid @RequestBody final UserLoginRequest userLoginRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity =
          appUserService.updateAppUserPassword(id, userLoginRequest);
      auditService.auditAppUserUpdatePassword(request, appUserEntity);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Update App User Password: [{}] | [{}]", id, userLoginRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @DeleteMapping("/user/{userId}/address/{addressId}")
  public ResponseEntity<AppUserResponse> deleteAppUserAddress(
      @PathVariable final int userId,
      @PathVariable final int addressId,
      final HttpServletRequest request) {
    try {
      permissionCheck.canUserAccessAppUser("", userId);
      final AppUserEntity appUserEntity = appUserService.deleteAppUserAddress(userId, addressId);
      auditService.auditAppUserDeleteAddress(request, appUserEntity);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Delete App User Address: [{}] | [{}]", userId, addressId, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN SOFT DELETE USER")
  @DeleteMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> softDeleteAppUser(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appUserService.softDeleteAppUser(id);
      auditService.auditAppUserDeleteSoft(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      log.error("Soft Delete App User: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/user/{id}/hard")
  public ResponseEntity<AppUserResponse> hardDeleteAppUser(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appUserService.hardDeleteAppUser(id);
      auditService.auditAppUserDeleteHard(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      log.error("Hard Delete App User: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/user/{id}/restore")
  public ResponseEntity<AppUserResponse> restoreAppUser(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      final AppUserEntity appUserEntity = appUserService.restoreSoftDeletedAppUser(id);
      auditService.auditAppUserRestore(request, id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Restore App User: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }
}
