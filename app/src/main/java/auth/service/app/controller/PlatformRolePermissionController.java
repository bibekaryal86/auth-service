package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.PlatformRolePermissionService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @CheckPermission("AUTHSVC_PRP_ASSIGN")
  @PostMapping
  public ResponseEntity<ResponseWithMetadata> assignPlatformRolePermission(
      @Valid @RequestBody final PlatformRolePermissionRequest platformRolePermissionRequest,
      final HttpServletRequest request) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.assignPlatformRolePermission(platformRolePermissionRequest);

      CompletableFuture.runAsync(
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

      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);

      return ResponseEntity.ok(
          new ResponseWithMetadata(
              new ResponseMetadata(
                  ResponseMetadata.emptyResponseStatusInfo(),
                  responseCrudInfo,
                  ResponseMetadata.emptyResponsePageInfo())));
    } catch (Exception ex) {
      log.error("Create Platform Role Permission: [{}}", platformRolePermissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @CheckPermission("AUTHSVC_PRP_UNASSIGN")
  @DeleteMapping("/platform/{platformId}/role/{roleId}/permission/{permissionId}")
  public ResponseEntity<ResponseWithMetadata> unassignPlatformRolePermission(
      @PathVariable final long platformId,
      @PathVariable final long roleId,
      @PathVariable final long permissionId,
      final HttpServletRequest request) {
    try {
      final PlatformRolePermissionEntity platformRolePermissionEntity =
          platformRolePermissionService.unassignPlatformRolePermission(
              platformId, roleId, permissionId);

      CompletableFuture.runAsync(
          () ->
              auditService.auditRole(
                  request,
                  platformRolePermissionEntity.getRole(),
                  AuditEnums.AuditRole.UNASSIGN_PLATFORM_PERMISSION,
                  String.format(
                      "Platform Profile Role UnAssign [Platform: %s] - [Role: %s] - [Permission: %s]",
                      platformRolePermissionEntity.getPlatform().getId(),
                      platformRolePermissionEntity.getRole().getId(),
                      platformRolePermissionEntity.getPermission().getId())));

      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return ResponseEntity.ok(
          new ResponseWithMetadata(
              new ResponseMetadata(
                  ResponseMetadata.emptyResponseStatusInfo(),
                  responseCrudInfo,
                  ResponseMetadata.emptyResponsePageInfo())));
    } catch (Exception ex) {
      log.error(
          "Unassign Platform Role Permission: [{}], [{}], [{}]",
          platformId,
          roleId,
          permissionId,
          ex);
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @CheckPermission("AUTHSVC_PRP_HARDDELETE")
  @DeleteMapping("/platform/{platformId}/role/{roleId}/permission/{permissionId}/hard")
  public ResponseEntity<ResponseWithMetadata> hardDeletePlatformRolePermission(
      @PathVariable final long platformId,
      @PathVariable final long roleId,
      @PathVariable final long permissionId) {
    try {
      platformRolePermissionService.hardDeletePlatformRolePermission(
          platformId, roleId, permissionId);

      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return ResponseEntity.ok(
          new ResponseWithMetadata(
              new ResponseMetadata(
                  ResponseMetadata.emptyResponseStatusInfo(),
                  responseCrudInfo,
                  ResponseMetadata.emptyResponsePageInfo())));
    } catch (Exception ex) {
      log.error(
          "Hard Delete Platform Role Permission: [{}], [{}], [{}]",
          platformId,
          roleId,
          permissionId,
          ex);
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }
}
