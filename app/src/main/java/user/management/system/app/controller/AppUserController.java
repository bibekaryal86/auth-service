package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static user.management.system.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static user.management.system.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.BeanUtils;
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
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserService;

@RestController
@RequestMapping("/app_users")
public class AppUserController {

  private final AppUserService appUserService;

  public AppUserController(final AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @PostMapping
  public ResponseEntity<AppUserResponse> createAppUser(
      @RequestBody final AppUserRequest appUserRequest) {
    try {
      AppUserEntity appUserEntity = appUserService.createAppUser(appUserRequest);
      return getResponseSingle(appUserEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AppUserResponse> readAppUsers() {
    try {
      List<AppUserEntity> appUserEntities = appUserService.readAppUsers();
      return getResponseMultiple(appUserEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final int id) {
    try {
      AppUserEntity appUserEntity = appUserService.readAppUser(id);
      return getResponseSingle(appUserEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppUserResponse> updateAppUser(
      @PathVariable final int id, @RequestBody final AppUserRequest appUserRequest) {
    try {
      AppUserEntity appUserEntity = appUserService.updateAppUser(id, appUserRequest);
      return getResponseSingle(appUserEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/hard/{id}")
  public ResponseEntity<AppUserResponse> hardDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.hardDeleteAppUser(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/soft/{id}")
  public ResponseEntity<AppUserResponse> softDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.softDeleteAppUser(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<AppUserResponse> restoreAppUser(@PathVariable final int id) {
    try {
      AppUserEntity appUserEntity = appUserService.restoreSoftDeletedAppUser(id);
      return getResponseSingle(appUserEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<AppUserResponse> getResponseSingle(final AppUserEntity appUserEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(appUserEntity);
    ResponseStatusInfo responseStatusInfo = getResponseStatusInfoForSingleResponse(appUserEntity);
    List<AppUserDto> appUserDtos =
        appUserEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(appUserEntity));
    return new ResponseEntity<>(
        new AppUserResponse(appUserDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<AppUserResponse> getResponseMultiple(
      final List<AppUserEntity> appUserEntities) {
    List<AppUserDto> appUserDtos = convertEntitiesToDtos(appUserEntities);
    return ResponseEntity.ok(new AppUserResponse(appUserDtos, null, null, null));
  }

  private ResponseEntity<AppUserResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new AppUserResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<AppUserResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppUserResponse(Collections.emptyList(), null, null, responseStatusInfo), httpStatus);
  }

  private AppUserDto convertEntityToDto(final AppUserEntity appUserEntity) {
    if (appUserEntity == null) {
      return null;
    }
    AppUserDto appUserDto = new AppUserDto();
    BeanUtils.copyProperties(appUserEntity, appUserDto, "password");
    return appUserDto;
  }

  private List<AppUserDto> convertEntitiesToDtos(final List<AppUserEntity> appUserEntities) {
    if (CollectionUtils.isEmpty(appUserEntities)) {
      return Collections.emptyList();
    }
    return appUserEntities.stream().map(this::convertEntityToDto).toList();
  }
}
