package auth.service.app.controller;

import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.AppPermissionRequest;
import auth.service.app.model.dto.AppPermissionResponse;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.service.AppPermissionService;
import auth.service.app.service.AuditService;
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
@RequestMapping("/api/v1/app_permissions")
@Validated
public class AppPermissionController {

  private final AppPermissionService appPermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @CheckPermission("PERMISSION_CREATE")
  @PostMapping("/{appId}/permission")
  public ResponseEntity<AppPermissionResponse> createAppPermission(
      @PathVariable final String appId,
      @Valid @RequestBody final AppPermissionRequest appPermissionRequest,
      final HttpServletRequest request) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.createAppPermission(appId, appPermissionRequest);
      runAsync(() -> auditService.auditAppPermissionCreate(request, appId, appPermissionEntity));
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      log.error("Create App Permission: [{}] | [{}]", appId, appPermissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping
  public ResponseEntity<AppPermissionResponse> readAppPermissions() {
    try {
      final List<AppPermissionEntity> appPermissionEntities =
          appPermissionService.readAppPermissions();
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
      log.error("Read App Permissions...", ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> readAppPermission(@PathVariable final int id) {
    try {
      final AppPermissionEntity appPermissionEntity = appPermissionService.readAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      log.error("Read App Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppPermissionResponse> readAppPermissionsByAppId(
      @PathVariable final String appId) {
    try {
      final List<AppPermissionEntity> appPermissionEntities =
          appPermissionService.readAppPermissions(appId);
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
      log.error("Read App Permissions By App ID: [{}]", appId, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_UPDATE")
  @PutMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> updateAppPermission(
      @PathVariable final int id,
      @Valid @RequestBody final AppPermissionRequest appPermissionRequest,
      final HttpServletRequest request) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.updateAppPermission(id, appPermissionRequest);
      runAsync(() -> auditService.auditAppPermissionUpdate(request, appPermissionEntity));
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      log.error("Update App Permission: [{}] | [{}]", id, appPermissionRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_DELETE")
  @DeleteMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> softDeleteAppPermission(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appPermissionService.softDeleteAppPermission(id);
      runAsync(() -> auditService.auditAppPermissionDeleteSoft(request, id));
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      log.error("Soft Delete App Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/permission/{id}/hard")
  public ResponseEntity<AppPermissionResponse> hardDeleteAppPermission(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appPermissionService.hardDeleteAppPermission(id);
      runAsync(() -> auditService.auditAppPermissionDeleteHard(request, id));
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      log.error("Hard Delete App Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/permission/{id}/restore")
  public ResponseEntity<AppPermissionResponse> restoreAppPermission(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.restoreSoftDeletedAppPermission(id);
      runAsync(() -> auditService.auditAppPermissionRestore(request, id));
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      log.error("Restore App Permission: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }
}
