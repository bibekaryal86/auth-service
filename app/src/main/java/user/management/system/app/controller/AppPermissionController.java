package user.management.system.app.controller;

import java.util.List;
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
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.service.AppPermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/api/v1/app_permissions")
public class AppPermissionController {

  private final AppPermissionService appPermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppPermissionController(
      final AppPermissionService appPermissionService,
      final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appPermissionService = appPermissionService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @PostMapping
  public ResponseEntity<AppPermissionResponse> createAppPermission(
      @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.createAppPermission(appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppPermissionResponse> readAppPermissions() {
    try {
      List<AppPermissionEntity> appPermissionEntities = appPermissionService.readAppPermissions();
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> readAppPermission(@PathVariable final int id) {
    try {
      AppPermissionEntity appPermissionEntity = appPermissionService.readAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> updateAppPermission(
      @PathVariable final int id, @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.updateAppPermission(id, appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> softDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.softDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<AppPermissionResponse> hardDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.hardDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @PatchMapping("/{id}/restore")
  public ResponseEntity<AppPermissionResponse> restoreAppPermission(@PathVariable final int id) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.restoreSoftDeletedAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }
}
