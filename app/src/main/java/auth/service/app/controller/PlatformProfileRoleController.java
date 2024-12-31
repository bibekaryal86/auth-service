package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.PlatformProfileRoleService;
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
@RequestMapping("/api/v1/ppr")
@Validated
public class PlatformProfileRoleController {

  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("PLATFORM_PROFILE_ROLE_ASSIGN")
  @PostMapping
  public ResponseEntity<PlatformProfileRoleResponse> createPlatformProfileRole(
      @Valid @RequestBody final PlatformProfileRoleRequest platformProfileRoleRequest,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.createPlatformProfileRole(platformProfileRoleRequest);

      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  platformProfileRoleEntity.getProfile(),
                  AuditEnums.AuditProfile.ASSIGN_PLATFORM_ROLE,
                  String.format(
                      "Platform Profile Role Assign [Platform: %s] - [Profile: %s] - [Role: %s]",
                      platformProfileRoleEntity.getPlatform().getId(),
                      platformProfileRoleEntity.getProfile().getId(),
                      platformProfileRoleEntity.getRole().getId())));

      return entityDtoConvertUtils.getResponseSinglePlatformProfileRole(platformProfileRoleEntity);
    } catch (Exception ex) {
      log.error("Create Platform Profile Role: [{}}", platformProfileRoleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformProfileRole(ex);
    }
  }

  @CheckPermission("PLATFORM_PROFILE_ROLE_READ")
  @GetMapping
  public ResponseEntity<PlatformProfileRoleResponse> readPlatformProfileRoles() {
    try {
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByProfileId();
      return entityDtoConvertUtils.getResponseMultiplePlatformProfileRoles(
          platformProfileRoleEntities);
    } catch (Exception ex) {
      log.error("Read Platform Profile Roles...", ex);
      return entityDtoConvertUtils.getResponseErrorPlatformProfileRole(ex);
    }
  }

  @CheckPermission("PLATFORM_PROFILE_ROLE_READ")
  @GetMapping("/platform/{platformId}/profile/{profileId}/role/{roleId}")
  public ResponseEntity<PlatformProfileRoleResponse> readPlatformProfileRole(
      @PathVariable final long platformId,
      @PathVariable final long profileId,
      @PathVariable final long roleId) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(platformId, profileId, roleId);
      return entityDtoConvertUtils.getResponseSinglePlatformProfileRole(platformProfileRoleEntity);
    } catch (Exception ex) {
      log.error("Read Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformProfileRole(ex);
    }
  }

  @CheckPermission("PLATFORM_PROFILE_ROLE_UNASSIGN")
  @DeleteMapping("/platform/{platformId}/profile/{profileId}/role/{roleId}")
  public ResponseEntity<PlatformProfileRoleResponse> deletePlatformProfileRole(
      @PathVariable final long platformId,
      @PathVariable final long profileId,
      @PathVariable final long roleId,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(platformId, profileId, roleId);
      platformProfileRoleService.deletePlatformProfileRole(platformId, profileId, roleId);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  platformProfileRoleEntity.getProfile(),
                  AuditEnums.AuditProfile.UNASSIGN_PLATFORM_ROLE,
                  String.format(
                      "Platform Profile Role UnAssign [Platform: %s] - [Profile: %s] - [Role: %s]",
                      platformProfileRoleEntity.getPlatform().getId(),
                      platformProfileRoleEntity.getProfile().getId(),
                      platformProfileRoleEntity.getRole().getId())));
      return entityDtoConvertUtils.getResponseDeletePlatformProfileRole();
    } catch (Exception ex) {
      log.error(
          "Delete Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformProfileRole(ex);
    }
  }
}
