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
import user.management.system.app.model.dto.AppRoleDto;
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.service.AppRoleService;

@RestController
@RequestMapping("/app_roles")
public class AppRoleController {

  private final AppRoleService appRoleService;

  public AppRoleController(final AppRoleService appRoleService) {
    this.appRoleService = appRoleService;
  }

  @PostMapping
  public ResponseEntity<AppRoleResponse> createAppRole(
      @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
      return getResponseSingle(appRoleEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppRoleResponse> readAppRoles() {
    try {
      List<AppRoleEntity> appRoleEntities = appRoleService.readAppRoles();
      return getResponseMultiple(appRoleEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppRoleResponse> readAppRole(@PathVariable final int id) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.readAppRole(id);
      return getResponseSingle(appRoleEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppRoleResponse> updateAppRole(
      @PathVariable final int id, @RequestBody final AppRoleRequest appRoleRequest) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.updateAppRole(id, appRoleRequest);
      return getResponseSingle(appRoleEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<AppRoleResponse> softDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.softDeleteAppRole(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<AppRoleResponse> hardDeleteAppRole(@PathVariable final int id) {
    try {
      appRoleService.hardDeleteAppRole(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PatchMapping("/{id}/restore")
  public ResponseEntity<AppRoleResponse> restoreAppRole(@PathVariable final int id) {
    try {
      AppRoleEntity appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
      return getResponseSingle(appRoleEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<AppRoleResponse> getResponseSingle(final AppRoleEntity appRoleEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(appRoleEntity);
    ResponseStatusInfo responseStatusInfo = getResponseStatusInfoForSingleResponse(appRoleEntity);
    List<AppRoleDto> appPermissionDtos =
        appRoleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(appRoleEntity));
    return new ResponseEntity<>(
        new AppRoleResponse(appPermissionDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<AppRoleResponse> getResponseMultiple(
      final List<AppRoleEntity> appRoleEntities) {
    List<AppRoleDto> appRoleDtos = convertEntitiesToDtos(appRoleEntities);
    return ResponseEntity.ok(new AppRoleResponse(appRoleDtos, null, null, null));
  }

  private ResponseEntity<AppRoleResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new AppRoleResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<AppRoleResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppRoleResponse(Collections.emptyList(), null, null, responseStatusInfo), httpStatus);
  }

  private AppRoleDto convertEntityToDto(final AppRoleEntity appRoleEntity) {
    if (appRoleEntity == null) {
      return null;
    }
    return new AppRoleDto(
        appRoleEntity.getId(), appRoleEntity.getName(), appRoleEntity.getDescription());
  }

  private List<AppRoleDto> convertEntitiesToDtos(final List<AppRoleEntity> appRoleEntities) {
    if (CollectionUtils.isEmpty(appRoleEntities)) {
      return Collections.emptyList();
    }
    return appRoleEntities.stream().map(this::convertEntityToDto).toList();
  }
}
