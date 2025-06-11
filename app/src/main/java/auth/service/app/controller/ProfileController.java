package auth.service.app.controller;

import static auth.service.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.AuditResponse;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeRoles,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludePlatforms,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory,
      @RequestParam(required = false, defaultValue = "1") final int pageNumber,
      @RequestParam(required = false, defaultValue = "100") final int perPage,
      @RequestParam(required = false, defaultValue = "") final String sortColumn,
      @RequestParam(required = false, defaultValue = "ASC") final Sort.Direction sortDirection) {
    try {
      final RequestMetadata requestMetadata =
          RequestMetadata.builder()
              .isIncludeRoles(isIncludeRoles)
              .isIncludePlatforms(isIncludePlatforms)
              .isIncludeDeleted(isIncludeDeleted)
              .isIncludeHistory(isIncludeHistory)
              .pageNumber(pageNumber)
              .perPage((perPage < 10 || perPage > 1000) ? 100 : perPage)
              .sortColumn(sortColumn.isEmpty() ? "lastName" : sortColumn)
              .sortDirection(sortDirection)
              .build();

      final Page<ProfileEntity> profileEntityPage = profileService.readProfiles(requestMetadata);
      final List<ProfileEntity> filteredProfileEntities =
          permissionCheck.filterProfileListByAccess(profileEntityPage.toList());
      final ResponseMetadata.ResponsePageInfo responsePageInfo =
          CommonUtils.defaultResponsePageInfo(profileEntityPage);
      return entityDtoConvertUtils.getResponseMultipleProfiles(
          filteredProfileEntities,
          isIncludeRoles,
          isIncludePlatforms,
          responsePageInfo,
          requestMetadata);
    } catch (Exception ex) {
      log.error("Read Profiles...", ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/platform/{platformId}")
  public ResponseEntity<ProfileResponse> readProfilesByPlatformId(
      @PathVariable final Long platformId,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeRoles,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludePlatforms,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory,
      @RequestParam(required = false, defaultValue = "1") final int pageNumber,
      @RequestParam(required = false, defaultValue = "100") final int perPage,
      @RequestParam(required = false, defaultValue = "") final String sortColumn,
      @RequestParam(required = false, defaultValue = "ASC") final Sort.Direction sortDirection) {
    try {
      final RequestMetadata requestMetadata =
          RequestMetadata.builder()
              .isIncludeRoles(isIncludeRoles)
              .isIncludePlatforms(isIncludePlatforms)
              .isIncludeDeleted(isIncludeDeleted)
              .isIncludeHistory(isIncludeHistory)
              .pageNumber(pageNumber)
              .perPage((perPage < 10 || perPage > 1000) ? 100 : perPage)
              .sortColumn(sortColumn.isEmpty() ? "lastName" : sortColumn)
              .sortDirection(sortDirection)
              .build();

      final Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage =
          platformProfileRoleService.readPlatformProfileRolesByPlatformId(
              platformId, requestMetadata);
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleEntityPage.toList();
      final List<ProfileEntity> profileEntities =
          platformProfileRoleEntities.stream().map(PlatformProfileRoleEntity::getProfile).toList();
      final List<ProfileEntity> filteredProfileEntities =
          permissionCheck.filterProfileListByAccess(profileEntities);
      final ResponseMetadata.ResponsePageInfo responsePageInfo =
          CommonUtils.defaultResponsePageInfo(platformProfileRoleEntityPage);
      return entityDtoConvertUtils.getResponseMultipleProfiles(
          filteredProfileEntities,
          isIncludeRoles,
          isIncludePlatforms,
          responsePageInfo,
          requestMetadata);
    } catch (Exception ex) {
      log.error("Read Profiles By Platform Id: [{}]", platformId, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> readProfile(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory,
      @RequestParam(required = false, defaultValue = "1") final int historyPage,
      @RequestParam(required = false, defaultValue = "100") final int historySize) {
    try {
      permissionCheck.checkProfileAccess("", id);
      final ProfileEntity profileEntity =
          circularDependencyService.readProfile(id, isIncludeDeleted);

      RequestMetadata requestMetadata = null;
      AuditResponse auditResponse = null;
      if (isIncludeHistory) {
        requestMetadata =
            RequestMetadata.builder()
                .sortColumn("permissionName")
                .sortDirection(Sort.Direction.ASC)
                .isIncludeDeleted(isIncludeDeleted)
                .isIncludeHistory(Boolean.TRUE)
                .pageNumber(1)
                .perPage(100)
                .historyPage(historyPage)
                .historySize(historySize)
                .build();
        auditResponse = auditService.auditProfiles(requestMetadata, id);
      }

      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, null, requestMetadata, auditResponse);
    } catch (Exception ex) {
      log.error("Read Profile: [{}]", id, ex);
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
      runAsync(
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
      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error("Update Profile: [{}] | [{}]", id, profileRequest, ex);
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
        baseUrlForLinkInEmail = getBaseUrlForLinkInEmail(request);
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
      runAsync(
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
      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error(
          "Update Profile Email: [{}] | [{}] | [{}]", platformId, id, profileEmailRequest, ex);
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
      runAsync(
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
      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error(
          "Update Profile Password: [{}] | [{}] | [{}]",
          platformId,
          id,
          profilePasswordRequest,
          ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN SOFT DELETE PROFILE")
  @DeleteMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> softDeleteProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = circularDependencyService.readProfile(id, false);
      profileService.softDeleteProfile(id);
      runAsync(
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
      return entityDtoConvertUtils.getResponseSingleProfile(
          new ProfileEntity(), responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error("Soft Delete Profile: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/profile/{id}/hard")
  public ResponseEntity<ProfileResponse> hardDeleteProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = circularDependencyService.readProfile(id, true);
      profileService.hardDeleteProfile(id);
      runAsync(
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
          new ProfileEntity(), responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error("Hard Delete Profile: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/profile/{id}/restore")
  public ResponseEntity<ProfileResponse> restoreProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity = profileService.restoreSoftDeletedProfile(id);
      runAsync(
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
      return entityDtoConvertUtils.getResponseSingleProfile(
          profileEntity, responseCrudInfo, null, null);
    } catch (Exception ex) {
      log.error("Restore Profile: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }
}
