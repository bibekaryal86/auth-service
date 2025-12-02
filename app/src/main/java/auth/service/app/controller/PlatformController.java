package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PlatformService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/platforms")
@Validated
public class PlatformController {

  private final PlatformService platformService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("AUTHSVC_PLATFORM_CREATE")
  @PostMapping("/platform")
  public ResponseEntity<PlatformResponse> createPlatform(
      @Valid @RequestBody final PlatformRequest platformRequest, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.createPlatform(platformRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_CREATE,
                  String.format(
                      "Platform Create [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          platformEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Create Platform: PlatformRequest=[{}]", platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_READ")
  @GetMapping
  public ResponseEntity<PlatformResponse> readPlatforms(
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted) {
    try {
      final List<PlatformEntity> platformEntities = platformService.readPlatforms(isIncludeDeleted);
      return entityDtoConvertUtils.getResponseMultiplePlatforms(platformEntities);
    } catch (Exception ex) {
      log.error("Read Platforms: IsIncludeDeleted=[{}]", isIncludeDeleted, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_READ")
  @GetMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> readPlatform(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory) {
    try {
      final PlatformEntity platformEntity =
          circularDependencyService.readPlatform(id, isIncludeDeleted);

      List<AuditPlatformEntity> auditPlatformEntities = Collections.emptyList();
      if (isIncludeHistory) {
        auditPlatformEntities = auditService.auditPlatforms(id);
      }

      return entityDtoConvertUtils.getResponseSinglePlatform(
          platformEntity, null, auditPlatformEntities);
    } catch (Exception ex) {
      log.error(
          "Read Platform: Id=[{}], IsIncludeDeleted=[{}], IsIncludeHistory=[{}]",
          id,
          isIncludeDeleted,
          isIncludeHistory,
          ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_UPDATE")
  @PutMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> updatePlatform(
      @PathVariable final long id,
      @Valid @RequestBody final PlatformRequest platformRequest,
      final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.updatePlatform(id, platformRequest);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_UPDATE,
                  String.format(
                      "Platform Update [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          platformEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Update Platform: Id=[{}], PlatformRequest=[{}]", id, platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_SOFTDELETE")
  @DeleteMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> softDeletePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(id, false);
      platformService.softDeletePlatform(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT,
                  String.format(
                      "Platform Delete Soft [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          new PlatformEntity(), responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Soft Delete Platform: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_HARDDELETE")
  @DeleteMapping("/platform/{id}/hard")
  public ResponseEntity<PlatformResponse> hardDeletePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(id, true);
      platformService.hardDeletePlatform(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_DELETE_HARD,
                  String.format(
                      "Platform Delete Hard [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          new PlatformEntity(), responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Hard Delete Platform: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("AUTHSVC_PLATFORM_RESTORE")
  @PatchMapping("/platform/{id}/restore")
  public ResponseEntity<PlatformResponse> restorePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.restoreSoftDeletedPlatform(id);
      CompletableFuture.runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_RESTORE,
                  String.format(
                      "Platform Restore [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          platformEntity, responseCrudInfo, null);
    } catch (Exception ex) {
      log.error("Restore Platform: Id=[{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }
}
