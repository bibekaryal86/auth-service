package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.PlatformService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps")
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
      // TODO audit
      // runAsync(() -> auditService.auditAppsCreate(request, platformEntity));
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity);
    } catch (Exception ex) {
      log.error("Create App: [{}]", platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ PLATFORM")
  @GetMapping
  public ResponseEntity<PlatformResponse> readPlatforms() {
    try {
      final List<PlatformEntity> platformEntities = platformService.readPlatforms();
      return entityDtoConvertUtils.getResponseMultiplePlatforms(platformEntities);
    } catch (Exception ex) {
      log.error("Read Platforms...", ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ PLATFORM")
  @GetMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> readPlatform(@PathVariable final long id) {
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(id);
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity);
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
      // TODO audit
      // runAsync(() -> auditService.auditAppsUpdate(request, platformEntity));
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity);
    } catch (Exception ex) {
      log.error("Update App: [{}] | [{}]", id, platformRequest, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN DELETE PLATFORM")
  @DeleteMapping("/platform/{id}")
  public ResponseEntity<PlatformResponse> softDeletePlatform(
      @PathVariable final long id, final HttpServletRequest request) {
    try {
      platformService.softDeletePlatform(id);
      // TODO audit
      // runAsync(() -> auditService.auditAppsDeleteSoft(request, id));
      return entityDtoConvertUtils.getResponseDeletePlatform();
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
      platformService.hardDeletePlatform(id);
      // TODO audit
      // runAsync(() -> auditService.auditAppsDeleteHard(request, id));
      return entityDtoConvertUtils.getResponseDeletePlatform();
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
      // TODO audit
      // runAsync(() -> auditService.auditAppsRestore(request, id));
      return entityDtoConvertUtils.getResponseSinglePlatform(platformEntity);
    } catch (Exception ex) {
      log.error("Restore Platform: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorPlatform(ex);
    }
  }
}
