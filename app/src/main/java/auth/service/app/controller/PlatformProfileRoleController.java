package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.PlatformProfileRoleService;
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
@RequestMapping("/api/v1/ppr")
@Validated
public class PlatformProfileRoleController {

  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("AUTHSVC_PLATFORM_PROFILE_ROLE_ASSIGN")
  @PostMapping
  public ResponseEntity<ResponseWithMetadata> assignPlatformProfileRole(
      @Valid @RequestBody final PlatformProfileRoleRequest platformProfileRoleRequest,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.assignPlatformProfileRole(platformProfileRoleRequest);

      CompletableFuture.runAsync(
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

      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);

      return ResponseEntity.ok(
          new ResponseWithMetadata(
              new ResponseMetadata(
                  ResponseMetadata.emptyResponseStatusInfo(),
                  responseCrudInfo,
                  ResponseMetadata.emptyResponsePageInfo())));
    } catch (Exception ex) {
      log.error("Create Platform Profile Role: [{}}", platformProfileRoleRequest, ex);
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_PROFILE_ROLE_UNASSIGN")
  @DeleteMapping("/platform/{platformId}/profile/{profileId}/role/{roleId}")
  public ResponseEntity<ResponseWithMetadata> unassignPlatformProfileRole(
      @PathVariable final long platformId,
      @PathVariable final long profileId,
      @PathVariable final long roleId,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.unassignPlatformProfileRole(platformId, profileId, roleId);

      CompletableFuture.runAsync(
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
          "Delete Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId, ex);
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }
}
