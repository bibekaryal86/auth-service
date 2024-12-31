package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.dto.PlatformRolePermissionResponse;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.enums.AuditEnums;
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
  @PostMapping
  public ResponseEntity<PlatformRolePermissionResponse> createPlatformRolePermission(
      @Valid @RequestBody final PlatformRolePermissionRequest platformRolePermissionRequest,
      final HttpServletRequest request) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.createPlatformRolePermission(platformRolePermissionRequest);
      runAsync(
          () ->
              auditService.auditRole(
                  request,
                  platformRolePermissionEntity.getRole(),
                  AuditEnums.AuditRole.ASSIGN_PLATFORM_PERMISSION,
                  String.format(
                      "Platform Role Permission Assign [Platform: %s] - [Role: %s] - [Permission: %s]",
                      platformRolePermissionEntity.getPlatform().getId(),
                      platformRolePermissionEntity.getRole().getId(),
                      platformRolePermissionEntity.getPermission().getId())));
      return entityDtoConvertUtils.getResponseSinglePlatformRolePermission(
          platformRolePermissionEntity);
    } catch (Exception ex) {
      log.error("Create Platform Role Permission: [{}}", platformRolePermissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatformRolePermission(ex);
    }
  }

  @CheckPermission("PLATFORM_ROLE_PERMISSION_READ")
  @GetMapping
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
  public ResponseEntity<PlatformRolePermissionResponse> deletePlatformRolePermission(
      @PathVariable final long platformId,
      @PathVariable final long roleId,
      @PathVariable final long permissionId,
      final HttpServletRequest request) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.readPlatformRolePermission(
              platformId, roleId, permissionId);
      platformRolePermissionService.deletePlatformRolePermission(platformId, roleId, permissionId);
      runAsync(
          () ->
              auditService.auditRole(
                  request,
                  platformRolePermissionEntity.getRole(),
                  AuditEnums.AuditRole.UNASSIGN_PLATFORM_PERMISSION,
                  String.format(
                      "Platform Role Permission Unassign [Platform: %s] - [Role: %s] - [Permission: %s]",
                      platformRolePermissionEntity.getPlatform().getId(),
                      platformRolePermissionEntity.getRole().getId(),
                      platformRolePermissionEntity.getPermission().getId())));
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
