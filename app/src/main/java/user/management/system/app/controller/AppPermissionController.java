package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static user.management.system.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static user.management.system.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AppPermissionDto;
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.service.AppPermissionService;

@RestController
@RequestMapping("/app_permissions")
public class AppPermissionController {

  private final AppPermissionService appPermissionService;

  public AppPermissionController(final AppPermissionService appPermissionService) {
    this.appPermissionService = appPermissionService;
  }

  @PostMapping
  public ResponseEntity<AppPermissionResponse> createAppPermission(
      @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.createAppPermission(appPermissionRequest);
      return getResponseSingle(appPermissionEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppPermissionResponse> readAppPermissions() {
    try {
      List<AppPermissionEntity> appPermissionEntities = appPermissionService.readAppPermissions();
      return getResponseMultiple(appPermissionEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> readAppPermission(@PathVariable final int id) {
    try {
      AppPermissionEntity appPermissionEntity = appPermissionService.readAppPermission(id);
      return getResponseSingle(appPermissionEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> updateAppPermission(
      @PathVariable final int id, @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.updateAppPermission(id, appPermissionRequest);
      return getResponseSingle(appPermissionEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/hard/{id}")
  public ResponseEntity<AppPermissionResponse> hardDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.hardDeleteAppPermission(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/soft/{id}")
  public ResponseEntity<AppPermissionResponse> softDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.softDeleteAppPermission(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<AppPermissionResponse> restoreAppPermission(@PathVariable final int id) {
    try {
      AppPermissionEntity appPermissionEntity =
          appPermissionService.restoreSoftDeletedAppPermission(id);
      return getResponseSingle(appPermissionEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<AppPermissionResponse> getResponseSingle(
      final AppPermissionEntity appPermissionEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(appPermissionEntity);
    ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appPermissionEntity);
    List<AppPermissionDto> appPermissionDtos =
        appPermissionEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(appPermissionEntity));
    return new ResponseEntity<>(
        new AppPermissionResponse(appPermissionDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<AppPermissionResponse> getResponseMultiple(
      final List<AppPermissionEntity> appPermissionEntities) {
    List<AppPermissionDto> appPermissionDtos = convertEntitiesToDtos(appPermissionEntities);
    return ResponseEntity.ok(new AppPermissionResponse(appPermissionDtos, null, null, null));
  }

  private ResponseEntity<AppPermissionResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new AppPermissionResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<AppPermissionResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppPermissionResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  private AppPermissionDto convertEntityToDto(final AppPermissionEntity appPermissionEntity) {
    if (appPermissionEntity == null) {
      return null;
    }
    return new AppPermissionDto(
        appPermissionEntity.getId(),
        appPermissionEntity.getApp(),
        appPermissionEntity.getName(),
        appPermissionEntity.getDescription());
  }

  private List<AppPermissionDto> convertEntitiesToDtos(
      final List<AppPermissionEntity> appPermissionEntities) {
    if (CollectionUtils.isEmpty(appPermissionEntities)) {
      return Collections.emptyList();
    }
    return appPermissionEntities.stream().map(this::convertEntityToDto).toList();
  }
}
