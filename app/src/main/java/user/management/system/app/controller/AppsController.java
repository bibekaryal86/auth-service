package user.management.system.app.controller;

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
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.dto.AppsResponse;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.service.AppsService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps")
@Validated
public class AppsController {

  private final AppsService appsService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("ONLY SUPERUSER CAN CREATE APP")
  @PostMapping("/app")
  public ResponseEntity<AppsResponse> createApp(
      @Valid @RequestBody final AppsRequest appsRequest, final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.createApp(appsRequest);
      auditService.auditAppsCreate(request, appsEntity);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Create App: [{}]", appsRequest, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APP")
  @GetMapping
  public ResponseEntity<AppsResponse> readApps() {
    try {
      final List<AppsEntity> appsEntities = appsService.readApps();
      return entityDtoConvertUtils.getResponseMultipleApps(appsEntities);
    } catch (Exception ex) {
      log.error("Read Apps...", ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN READ APP")
  @GetMapping("/app/{id}")
  public ResponseEntity<AppsResponse> readApp(@PathVariable final String id) {
    try {
      final AppsEntity appsEntity = appsService.readApp(id);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Read App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN UPDATE APP")
  @PutMapping("/app/{id}")
  public ResponseEntity<AppsResponse> updateApp(
      @PathVariable final String id,
      @Valid @RequestBody final AppsRequest appsRequest,
      final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.updateApps(id, appsRequest);
      auditService.auditAppsUpdate(request, appsEntity);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Update App: [{}] | [{}]", id, appsRequest, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN DELETE APP")
  @DeleteMapping("/app/{id}")
  public ResponseEntity<AppsResponse> softDeleteApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      appsService.softDeleteApps(id);
      auditService.auditAppsDeleteSoft(request, id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      log.error("Soft Delete App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/app/{id}/hard")
  public ResponseEntity<AppsResponse> hardDeleteApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      appsService.hardDeleteApps(id);
      auditService.auditAppsDeleteHard(request, id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      log.error("Hard Delete App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/app/{id}/restore")
  public ResponseEntity<AppsResponse> restoreApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.restoreSoftDeletedApps(id);
      auditService.auditAppsRestore(request, id);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Restore App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }
}
