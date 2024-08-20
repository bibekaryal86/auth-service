package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.service.AppPermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(
    name = "App Permissions Controller",
    description = "Create, View and Manage App User Permissions")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_permissions")
public class AppPermissionController {

  private final AppPermissionService appPermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("PERMISSION_CREATE")
  @PostMapping("/{appId}/permission")
  public ResponseEntity<AppPermissionResponse> createAppPermission(
      @PathVariable final String appId,
      @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.createAppPermission(appId, appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
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
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_READ")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppPermissionResponse> readAppPermissionsByAppName(
      @PathVariable final String appId) {
    try {
      final List<AppPermissionEntity> appPermissionEntities =
          appPermissionService.readAppPermissions(appId);
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
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
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_UPDATE")
  @PutMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> updateAppPermission(
      @PathVariable final int id, @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.updateAppPermission(id, appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("PERMISSION_DELETE")
  @DeleteMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> softDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.softDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/permission/{id}/hard")
  public ResponseEntity<AppPermissionResponse> hardDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.hardDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/permission/{id}/restore")
  public ResponseEntity<AppPermissionResponse> restoreAppPermission(@PathVariable final int id) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.restoreSoftDeletedAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }
}
