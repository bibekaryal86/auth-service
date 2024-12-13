package auth.service.app.controller;

import static auth.service.app.util.CommonUtils.getBaseUrlForLinkInEmail;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.service.AuditService;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.ProfileService;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.PermissionCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profiles")
@Validated
public class ProfileController {

  private final ProfileService profileService;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;
  private final AuditService auditService;

  @GetMapping
  public ResponseEntity<ProfileResponse> readProfiles() {
    try {
      final List<ProfileEntity> profileEntities = profileService.readProfiles();
      final List<ProfileEntity> filteredProfileEntities =
          permissionCheck.filterProfileListByAccess(profileEntities);
      return entityDtoConvertUtils.getResponseMultipleProfiles(filteredProfileEntities);
    } catch (Exception ex) {
      log.error("Read Profiles...", ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/platform/{platformId}")
  public ResponseEntity<ProfileResponse> readProfilesByPlatformId(
      @PathVariable final Long platformId) {
    try {
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRoles(platformId);
      final List<ProfileEntity> profileEntities =
          platformProfileRoleEntities.stream().map(PlatformProfileRoleEntity::getProfile).toList();
      final List<ProfileEntity> filteredProfileEntities =
          permissionCheck.filterProfileListByAccess(profileEntities);
      return entityDtoConvertUtils.getResponseMultipleProfiles(filteredProfileEntities);
    } catch (Exception ex) {
      log.error("Read Profiles By Platform Id: [{}]", platformId, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> readProfile(@PathVariable final long id) {
    try {
      permissionCheck.checkProfileAccess("", id);
      final ProfileEntity profileEntity = profileService.readProfile(id);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Read Profile: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @GetMapping("/profile/email/{email}")
  public ResponseEntity<ProfileResponse> readProfileByEmail(@PathVariable final String email) {
    try {
      permissionCheck.checkProfileAccess(email, 0);
      final ProfileEntity profileEntity = profileService.readProfileByEmail(email);
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Read Profile By Email: [{}]", email, ex);
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
      if (!CollectionUtils.isEmpty(profileRequest.getAddresses())) {
        profileEntity = profileService.readProfile(profileEntity.getId());
      }
      final ProfileEntity finalProfileEntity = profileEntity;
      // TODO audit
      // runAsync(() -> auditService.auditProfileUpdate(request, finalProfileEntity));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
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
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          profileService.readPlatformProfileRole(platformId, profileEmailRequest.getOldEmail());
      final ProfileEntity profileEntity =
          profileService.updateProfileEmail(
              id, profileEmailRequest, platformProfileRoleEntity.getPlatform(), baseUrl);
      // TODO audit
      // runAsync(() -> auditService.auditProfileUpdateEmail(request, profileEntity, platformId));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Update Profile Email: [{}] | [{}]", id, profileEmailRequest, ex);
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
          profileService.readPlatformProfileRole(platformId, profilePasswordRequest.getEmail());
      final ProfileEntity profileEntity =
          profileService.updateProfilePassword(
              id, profilePasswordRequest, platformProfileRoleEntity.getPlatform());
      // TODO audit
      // runAsync(() -> auditService.auditProfileUpdatePassword(request, profileEntity));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
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

  @DeleteMapping("/profile/{profileId}/address/{addressId}")
  public ResponseEntity<ProfileResponse> deleteProfileAddress(
      @PathVariable final long profileId,
      @PathVariable final long addressId,
      final HttpServletRequest request) {
    try {
      permissionCheck.checkProfileAccess("", profileId);
      final ProfileEntity profileEntity = profileService.deleteProfileAddress(profileId, addressId);
      // TODO audit
      // unAsync(() -> auditService.auditProfileDeleteAddress(request, profileEntity));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Delete Profile Address: [{}] | [{}]", profileId, addressId, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN SOFT DELETE USER")
  @DeleteMapping("/profile/{id}")
  public ResponseEntity<ProfileResponse> softDeleteProfile(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      profileService.softDeleteProfile(id);
      // TODO audit
      // runAsync(() -> auditService.auditProfileDeleteSoft(request, id));
      return entityDtoConvertUtils.getResponseDeleteProfile();
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
      profileService.hardDeleteProfile(id);
      // TODO audit
      // runAsync(() -> auditService.auditProfileDeleteHard(request, id));
      return entityDtoConvertUtils.getResponseDeleteProfile();
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
      // TODO audit
      // runAsync(() -> auditService.auditProfileRestore(request, id));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Restore Profile: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }
}
