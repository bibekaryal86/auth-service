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
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.dto.AppsResponse;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.service.AppsService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Apps Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps")
public class AppsController {

  private final AppsService appsService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("ONLY SUPERUSER CAN CREATE APP")
  @PostMapping("/app")
  public ResponseEntity<AppsResponse> createApp(@RequestBody final AppsRequest appsRequest) {
    try {
      final AppsEntity appsEntity = appsService.createApp(appsRequest);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
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
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN UPDATE APP")
  @PutMapping("/app/{id}")
  public ResponseEntity<AppsResponse> updateApp(
      @PathVariable final String id, @RequestBody final AppsRequest appsRequest) {
    try {
      final AppsEntity appsEntity = appsService.updateApps(id, appsRequest);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN DELETE APP")
  @DeleteMapping("/app/{id}")
  public ResponseEntity<AppsResponse> softDeleteApps(@PathVariable final String id) {
    try {
      appsService.softDeleteApps(id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/app/{id}/hard")
  public ResponseEntity<AppsResponse> hardDeleteApps(@PathVariable final String id) {
    try {
      appsService.hardDeleteApps(id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/app/{id}/restore")
  public ResponseEntity<AppsResponse> restoreApps(@PathVariable final String id) {
    try {
      final AppsEntity appsEntity = appsService.restoreSoftDeletedApps(id);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }
}
