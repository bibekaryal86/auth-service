package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PlatformService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
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

  @CheckPermission("ONLY SUPERUSER CAN CREATE PLATFORM")
  @PostMapping("/platform")
  public ResponseEntity<PlatformResponse> createPlatform(
      @Valid @RequestBody final PlatformRequest platformRequest, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.createPlatform(platformRequest);
      runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_CREATE,
                  String.format(
                      "Platform Create [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseCrudInfo responseCrudInfo = CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity, responseCrudInfo);
    } catch (Exception ex) {
      log.error("Create Platform: [{}]", platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ PLATFORM")
  @GetMapping
  public ResponseEntity<PlatformResponse> readPlatforms(
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeProfiles,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeHistory,
      @RequestParam(required = false, defaultValue = "0") final int pageNumber,
      @RequestParam(required = false, defaultValue = "100") final int perPage,
      @RequestParam(required = false, defaultValue = "") final String sortColumn,
      @RequestParam(required = false, defaultValue = "ASC") final Sort.Direction sortDirection) {
    try {
      final RequestMetadata requestMetadata =
          RequestMetadata.builder()
              .isIncludeProfiles(isIncludeProfiles)
              .isIncludeDeleted(isIncludeDeleted)
              .isIncludeHistory(isIncludeHistory)
              .pageNumber(pageNumber)
              .perPage((perPage < 10 || perPage > 1000) ? 100 : perPage)
              .sortColumn(sortColumn.isEmpty() ? "platformName" : sortColumn)
              .sortDirection(sortDirection)
              .build();

      final Page<PlatformEntity> platformEntityPage =
          platformService.readPlatforms(requestMetadata);
      final List<PlatformEntity> platformEntities = platformEntityPage.toList();
      final ResponsePageInfo responsePageInfo =
          CommonUtils.defaultResponsePageInfo(platformEntityPage);
      return entityDtoConvertUtils.getResponseMultiplePlatforms(
          platformEntities, isIncludeProfiles, responsePageInfo, requestMetadata);
    } catch (Exception ex) {
      log.error("Read Platforms...", ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ PLATFORM")
  @GetMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> readPlatform(
      @PathVariable final long id,
      @RequestParam(required = false, defaultValue = "false") final boolean isIncludeDeleted) {
    try {
      final PlatformEntity platformEntity =
          circularDependencyService.readPlatform(id, isIncludeDeleted);
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity, null);
    } catch (Exception ex) {
      log.error("Read Platform: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN UPDATE PLATFORM")
  @PutMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> updatePlatform(
      @PathVariable final long id,
      @Valid @RequestBody final PlatformRequest platformRequest,
      final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.updatePlatform(id, platformRequest);
      runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_UPDATE,
                  String.format(
                      "Platform Update [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseCrudInfo responseCrudInfo = CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity, responseCrudInfo);
    } catch (Exception ex) {
      log.error("Update Platform: [{}] | [{}]", id, platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN DELETE PLATFORM")
  @DeleteMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> softDeletePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(id, false);
      platformService.softDeletePlatform(id);
      runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT,
                  String.format(
                      "Platform Delete Soft [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseCrudInfo responseCrudInfo = CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          new PlatformEntity(), responseCrudInfo);
    } catch (Exception ex) {
      log.error("Soft Delete Platform: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/platform/{id}/hard")
  public ResponseEntity<PlatformResponse> hardDeletePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(id, true);
      platformService.hardDeletePlatform(id);
      runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_DELETE_HARD,
                  String.format(
                      "Platform Delete Hard [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseCrudInfo responseCrudInfo = CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0);
      return entityDtoConvertUtils.getResponseSinglePlatform(
          new PlatformEntity(), responseCrudInfo);
    } catch (Exception ex) {
      log.error("Hard Delete Platform: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/platform/{id}/restore")
  public ResponseEntity<PlatformResponse> restorePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      final PlatformEntity platformEntity = platformService.restoreSoftDeletedPlatform(id);
      runAsync(
          () ->
              auditService.auditPlatform(
                  request,
                  platformEntity,
                  AuditEnums.AuditPlatform.PLATFORM_RESTORE,
                  String.format(
                      "Platform Restore [Id: %s] - [Name: %s]",
                      platformEntity.getId(), platformEntity.getPlatformName())));
      final ResponseCrudInfo responseCrudInfo = CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1);
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity, responseCrudInfo);
    } catch (Exception ex) {
      log.error("Restore Platform: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }
}
