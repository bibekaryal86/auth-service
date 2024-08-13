package user.management.system.app.util;

import static user.management.system.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static user.management.system.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static user.management.system.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import user.management.system.app.model.dto.AppPermissionDto;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.dto.AppRoleDto;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppUserEntity;

@Component
public class EntityDtoConvertUtils {

  public ResponseEntity<AppPermissionResponse> getResponseSingleAppPermission(
      final AppPermissionEntity appPermissionEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appPermissionEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appPermissionEntity);
    final List<AppPermissionDto> appPermissionDtos =
        appPermissionEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppPermission(appPermissionEntity));
    return new ResponseEntity<>(
        new AppPermissionResponse(appPermissionDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppPermissionResponse> getResponseMultipleAppPermission(
      final List<AppPermissionEntity> appPermissionEntities) {
    final List<AppPermissionDto> appPermissionDtos =
        convertEntitiesToDtosAppPermission(appPermissionEntities);
    return ResponseEntity.ok(new AppPermissionResponse(appPermissionDtos, null, null, null));
  }

  public ResponseEntity<AppPermissionResponse> getResponseDeleteAppPermission() {
    return ResponseEntity.ok(
        new AppPermissionResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  public ResponseEntity<AppPermissionResponse> getResponseErrorAppPermission(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppPermissionResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  public AppPermissionDto convertEntityToDtoAppPermission(
      final AppPermissionEntity appPermissionEntity) {
    if (appPermissionEntity == null) {
      return null;
    }
    return new AppPermissionDto(
        appPermissionEntity.getId(),
        appPermissionEntity.getApp(),
        appPermissionEntity.getName(),
        appPermissionEntity.getDescription());
  }

  public List<AppPermissionDto> convertEntitiesToDtosAppPermission(
      final List<AppPermissionEntity> appPermissionEntities) {
    if (CollectionUtils.isEmpty(appPermissionEntities)) {
      return Collections.emptyList();
    }
    return appPermissionEntities.stream().map(this::convertEntityToDtoAppPermission).toList();
  }

  public ResponseEntity<AppRoleResponse> getResponseSingleAppRole(
      final AppRoleEntity appRoleEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appRoleEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appRoleEntity);
    final List<AppRoleDto> appPermissionDtos =
        appRoleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppRole(appRoleEntity));
    return new ResponseEntity<>(
        new AppRoleResponse(appPermissionDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppRoleResponse> getResponseMultipleAppRole(
      final List<AppRoleEntity> appRoleEntities) {
    final List<AppRoleDto> appRoleDtos = convertEntitiesToDtosAppRole(appRoleEntities);
    return ResponseEntity.ok(new AppRoleResponse(appRoleDtos, null, null, null));
  }

  public ResponseEntity<AppRoleResponse> getResponseDeleteAppRole() {
    return ResponseEntity.ok(
        new AppRoleResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  public ResponseEntity<AppRoleResponse> getResponseErrorAppRole(final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppRoleResponse(Collections.emptyList(), null, null, responseStatusInfo), httpStatus);
  }

  public AppRoleDto convertEntityToDtoAppRole(final AppRoleEntity appRoleEntity) {
    if (appRoleEntity == null) {
      return null;
    }
    return new AppRoleDto(
        appRoleEntity.getId(), appRoleEntity.getName(), appRoleEntity.getDescription());
  }

  public List<AppRoleDto> convertEntitiesToDtosAppRole(final List<AppRoleEntity> appRoleEntities) {
    if (CollectionUtils.isEmpty(appRoleEntities)) {
      return Collections.emptyList();
    }
    return appRoleEntities.stream().map(this::convertEntityToDtoAppRole).toList();
  }

  public ResponseEntity<AppUserResponse> getResponseSingleAppUser(
      final AppUserEntity appUserEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appUserEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appUserEntity);
    final List<AppUserDto> appUserDtos =
        appUserEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppUser(appUserEntity));
    return new ResponseEntity<>(
        new AppUserResponse(appUserDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppUserResponse> getResponseMultipleAppUser(
      final List<AppUserEntity> appUserEntities) {
    final List<AppUserDto> appUserDtos = convertEntitiesToDtosAppUser(appUserEntities);
    return ResponseEntity.ok(new AppUserResponse(appUserDtos, null, null, null));
  }

  public ResponseEntity<AppUserResponse> getResponseDeleteAppUser() {
    return ResponseEntity.ok(
        new AppUserResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  public ResponseEntity<AppUserResponse> getResponseErrorAppUser(final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppUserResponse(Collections.emptyList(), null, null, responseStatusInfo), httpStatus);
  }

  public AppUserDto convertEntityToDtoAppUser(final AppUserEntity appUserEntity) {
    if (appUserEntity == null) {
      return null;
    }
    final AppUserDto appUserDto = new AppUserDto();
    BeanUtils.copyProperties(appUserEntity, appUserDto, "password");
    return appUserDto;
  }

  public List<AppUserDto> convertEntitiesToDtosAppUser(final List<AppUserEntity> appUserEntities) {
    if (CollectionUtils.isEmpty(appUserEntities)) {
      return Collections.emptyList();
    }
    return appUserEntities.stream().map(this::convertEntityToDtoAppUser).toList();
  }
}
