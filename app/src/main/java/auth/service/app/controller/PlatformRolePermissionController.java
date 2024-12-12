package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.dto.PlatformRolePermissionResponse;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.service.AuditService;
import auth.service.app.service.PlatformRolePermissionService;
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
@RequestMapping("/api/v1/prp")
@Validated
public class PlatformRolePermissionController {

  private final PlatformRolePermissionService platformRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("PLATFORM_ROLE_PERMISSION_ASSIGN")
  @PostMapping("/")
  public ResponseEntity<PlatformRolePermissionResponse> createPlatformRolePermission(
      @Valid @RequestBody final PlatformRolePermissionRequest platformRolePermissionRequest,
      final HttpServletRequest request) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.createPlatformRolePermission(platformRolePermissionRequest);

      // TODO audit
      // runAsync(() -> auditService.auditAppUserAssignRole(request, appUserRoleEntity));

      return entityDtoConvertUtils.getResponseSinglePlatformRolePermission(
          platformRolePermissionEntity);
    } catch (Exception ex) {
      log.error("Create Platform Role Permission: [{}}", platformRolePermissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_READ")
  @GetMapping("/")
  public ResponseEntity<PlatformRolePermissionResponse> readPlatformRolePermissions() {
    try {
      final List<PlatformRolePermissionEntity> platformRolePermissionEntities =
          platformRolePermissionService.readPlatformRolePermissions();
      return entityDtoConvertUtils.getResponseMultiplePlatformRolePermissions(
          platformRolePermissionEntities);
    } catch (Exception ex) {
      log.error("Read Platform Role Permissions...", ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_READ")
  @GetMapping("/platform/{platformId}/role/{roleId}")
  public ResponseEntity<PlatformRolePermissionResponse> readPlatformRolePermissionsByRoleId(
      @PathVariable final long platformId, @PathVariable final long roleId) {
    try {
      final List<PlatformRolePermissionEntity> platformRolePermissionEntities =
          platformRolePermissionService.readPlatformRolePermissions(platformId, roleId);
      return entityDtoConvertUtils.getResponseMultiplePlatformRolePermissions(
          platformRolePermissionEntities);
    } catch (Exception ex) {
      log.error("Read Platform Role Permissions By Role ID: [{}], [{}]", platformId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_READ")
  @GetMapping("/platform/{platformId}/roles/{roleIds}")
  public ResponseEntity<PlatformRolePermissionResponse> readPlatformRolePermissionsByRoleIds(
      @PathVariable final long platformId, @PathVariable final List<Long> roleIds) {
    try {
      final List<PlatformRolePermissionEntity> platformRolePermissionEntities =
          platformRolePermissionService.readPlatformRolePermissions(platformId, roleIds);
      return entityDtoConvertUtils.getResponseMultiplePlatformRolePermissions(
          platformRolePermissionEntities);
    } catch (Exception ex) {
      log.error("Read Platform Role Permissions By Role IDs: [{}], [{}]", platformId, roleIds, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_READ")
  @GetMapping("/platform/{platformId}/role/{roleId}/permission/{permissionId}")
  public ResponseEntity<PlatformRolePermissionResponse> readPlatformRolePermission(
      @PathVariable final long platformId,
      @PathVariable final long roleId,
      @PathVariable final long permissionId) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.readPlatformRolePermission(
              platformId, roleId, permissionId);
      return entityDtoConvertUtils.getResponseSinglePlatformRolePermission(
          platformRolePermissionEntity);
    } catch (Exception ex) {
      log.error(
          "Read Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_UNASSIGN")
  @DeleteMapping("/platform/{platformId}/role/{roleId}/permission/{permissionId}")
  public ResponseEntity<PlatformRolePermissionResponse> deleteAppUserRole(
      @PathVariable final long platformId,
      @PathVariable final long roleId,
      @PathVariable final long permissionId,
      final HttpServletRequest request) {
    try {
      platformRolePermissionService.deletePlatformRolePermission(platformId, roleId, permissionId);
      // TODO audit
      // runAsync(() -> auditService.auditAppUserUnassignRole(request, userId, roleId));
      return entityDtoConvertUtils.getResponseDeletePlatformRolePermission();
    } catch (Exception ex) {
      log.error(
          "Delete Platform Role Permission: [{}], [{}], [{}]",
          platformId,
          roleId,
          permissionId,
          ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }
}
