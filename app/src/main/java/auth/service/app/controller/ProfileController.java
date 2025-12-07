package auth.service.app.controller;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.ProfileService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.PermissionCheck;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profiles")
@Validated
public class ProfileController {

  private final ProfileService profileService;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;
  private final AuditService auditService;
  private final EnvServiceConnector envServiceConnector;

  @GetMapping
  public ResponseEntity<ProfileResponse> readProfiles(
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "") final String platformId,
      @RequestParam(required = false, defaultValue = "") final String roleId) {
    try {
      final Long platformIdLong = CommonUtils.getValidId(platformId);
      final Long roleIdLong = CommonUtils.getValidId(roleId);
      final boolean isSuperUser = CommonUtils.isSuperUser(CommonUtils.getAuthentication());

      List<ProfileEntity> profileEntities;
      if (platformIdLong == null && roleIdLong == null) {
        profileEntities = profileService.readProfiles(isIncludeDeleted && isSuperUser);
      } else {
        final List<PlatformProfileRoleEntity> pprEntities =
            platformProfileRoleService.readPlatformProfileRoles(
                platformIdLong, roleIdLong, isIncludeDeleted && isSuperUser);
        profileEntities = pprEntities.stream().map(PlatformProfileRoleEntity::getProfile).toList();
      }

      final List<ProfileEntity> filteredProfileEntities =
          permissionCheck.filterProfileListByAccess(profileEntities);
      return entityDtoConvertUtils.getResponseMultipleProfiles(
          filteredProfileEntities, Boolean.TRUE);
    } catch (Exception ex) {
      log.error("Read Profiles: IsIncludeDeleted=[{}]", isIncludeDeleted, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> readProfile(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory) {
    try {
      permissionCheck.checkProfileAccess("", id);
      final boolean isSuperUser = CommonUtils.isSuperUser(CommonUtils.getAuthentication());
      final ProfileEntity profileEntity =
          circularDependencyService.readProfile(id, isIncludeDeleted && isSuperUser);

      List<AuditProfileEntity> auditProfileEntities = Collections.emptyList();
      if (isIncludeHistory) {
        auditProfileEntities = auditService.auditProfiles(id);
      }

      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, null, auditProfileEntities);
    } catch (Exception ex) {
      log.error(
          "Read Profile: Id=[{}], IsIncludeDeleted=[{}], IsIncludeHistory=[{}]",
          id,
          isIncludeDeleted,
          isIncludeHistory,
          ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @PutMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> updateProfile(
      @PathVariable final long id,
      @Valid @RequestBody final ProfileRequest profileRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.checkProfileAccess("", id);
      ProfileEntity profileEntity = profileService.updateProfile(id, profileRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_UPDATE,
                  String.format(
                      "Profile Update [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
        return entityDtoConvertUtils.getResponseSingleProfile(profileEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Update Profile: Id=[{}], ProfileRequest=[{}]", id, profileRequest, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @PutMapping("/platform/{platformId}/profile/{id}/email")
  public ResponseEntity<ProfileResponse> updateProfileEmail(
      @PathVariable final long platformId,
      @PathVariable final long id,
      @Valid @RequestBody final ProfileEmailRequest profileEmailRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.checkProfileAccess("", id);
      String baseUrlForLinkInEmail = envServiceConnector.getBaseUrlForLinkInEmail();
      if (baseUrlForLinkInEmail == null) {
        baseUrlForLinkInEmail = CommonUtils.getBaseUrlForLinkInEmail(request);
      }

      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(
              platformId, profileEmailRequest.getOldEmail());

      final ProfileEntity profileEntity =
          profileService.updateProfileEmail(
              id,
              profileEmailRequest,
              platformProfileRoleEntity.getPlatform(),
              baseUrlForLinkInEmail);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_EMAIL_UPDATE,
                  String.format(
                      "Profile Update Email [PlatformId: %s] - [Id: %s] - [OldEmail: %s] - [NewEmail: %s]",
                      platformId,
                      profileEntity.getId(),
                      profileEmailRequest.getOldEmail(),
                      profileEmailRequest.getNewEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error(
          "Update Profile Email: PlatformId=[{}], Id=[{}], ProfileEmailRequest=[{}]",
          platformId,
          id,
          profileEmailRequest,
          ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @PutMapping("/platform/{platformId}/profile/{id}/password")
  public ResponseEntity<ProfileResponse> updateProfilePassword(
      @PathVariable final long platformId,
      @PathVariable final long id,
      @Valid @RequestBody final ProfilePasswordRequest profilePasswordRequest,
      final HttpServletRequest request) {
    try {
      permissionCheck.checkProfileAccess("", id);
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(
              platformId, profilePasswordRequest.getEmail());

      final ProfileEntity profileEntity =
          profileService.updateProfilePassword(
              id, profilePasswordRequest, platformProfileRoleEntity.getPlatform());
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_PASSWORD_UPDATE,
                  String.format(
                      "Profile Update Password [PlatformId: %s] - [Id: %s] - [Email: %s]",
                      platformId, profileEntity.getId(), profileEntity.getEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error(
          "Update Profile Password: PlatformId=[{}] | Id=[{}] | ProfilePasswordRequest=[{}]",
          platformId,
          id,
          profilePasswordRequest,
          ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("AUTHSVC_PROFILE_SOFTDELETE")
  @DeleteMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> softDeleteProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = profileService.softDeleteProfile(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_DELETE_SOFT,
                  String.format(
                      "Profile Delete Soft [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Soft Delete Profile: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("AUTHSVC_PROFILE_HARDDELETE")
  @DeleteMapping("/profile/{id}/hard")
  public ResponseEntity<ProfileResponse> hardDeleteProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = circularDependencyService.readProfile(id, Boolean.TRUE);
      profileService.hardDeleteProfile(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_DELETE_HARD,
                  String.format(
                      "Profile Delete Hard [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSingleProfile(
          new ProfileEntity(), responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Hard Delete Profile: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("AUTHSVC_PROFILE_RESTORE")
  @PatchMapping("/profile/{id}/restore")
  public ResponseEntity<ProfileResponse> restoreProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = profileService.restoreSoftDeletedProfile(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESTORE,
                  String.format(
                      "Profile Restore [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Restore Profile: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }
}
