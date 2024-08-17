package user.management.system.app.util;

import static user.management.system.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static user.management.system.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static user.management.system.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import user.management.system.app.model.dto.AppPermissionDto;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.dto.AppRoleDto;
import user.management.system.app.model.dto.AppRolePermissionDto;
import user.management.system.app.model.dto.AppRolePermissionResponse;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.AppUserRoleDto;
import user.management.system.app.model.dto.AppUserRoleResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.service.AppUserRoleService;

@Component
@RequiredArgsConstructor
public class EntityDtoConvertUtils {
  private final AppUserRoleService appUserRoleService;
  private final AppRolePermissionService appRolePermissionService;

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
            : List.of(convertEntityToDtoAppRole(appRoleEntity, true));
    return new ResponseEntity<>(
        new AppRoleResponse(appPermissionDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppRoleResponse> getResponseMultipleAppRole(
      final List<AppRoleEntity> appRoleEntities) {
    final List<AppRoleDto> appRoleDtos = convertEntitiesToDtosAppRole(appRoleEntities, true);
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

  public AppRoleDto convertEntityToDtoAppRole(
      final AppRoleEntity appRoleEntity, final boolean isIncludePermissions) {
    if (appRoleEntity == null) {
      return null;
    }

    AppRoleDto appRoleDto =
        new AppRoleDto(
            appRoleEntity.getId(), appRoleEntity.getName(), appRoleEntity.getDescription());

    if (isIncludePermissions) {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(appRoleDto.getId());
      final List<AppRolePermissionDto> appRolePermissionDtos =
          convertEntitiesToDtosAppRolePermission(appRolePermissionEntities);
      final List<AppPermissionDto> appPermissionDtos =
          appRolePermissionDtos.stream().map(AppRolePermissionDto::getPermission).toList();
      appRoleDto.setPermissions(appPermissionDtos);
    }

    return appRoleDto;
  }

  public List<AppRoleDto> convertEntitiesToDtosAppRole(
      final List<AppRoleEntity> appRoleEntities, boolean isIncludePermissions) {
    if (CollectionUtils.isEmpty(appRoleEntities)) {
      return Collections.emptyList();
    }

    if (!isIncludePermissions) {
      return appRoleEntities.stream()
          .map(appRoleEntity -> convertEntityToDtoAppRole(appRoleEntity, false))
          .toList();
    }

    final List<Integer> appRoleIds = appRoleEntities.stream().map(AppRoleEntity::getId).toList();
    final List<AppRolePermissionEntity> appRolePermissionEntities =
        appRolePermissionService.readAppRolePermissions(null, appRoleIds);
    final List<AppRolePermissionDto> appRolePermissionDtos =
        convertEntitiesToDtosAppRolePermission(appRolePermissionEntities);

    final Map<Integer, List<AppPermissionDto>> rolePermissionsMap =
        appRolePermissionDtos.stream()
            .collect(
                Collectors.groupingBy(
                    rolePermission -> rolePermission.getRole().getId(),
                    Collectors.mapping(AppRolePermissionDto::getPermission, Collectors.toList())));

    return appRoleEntities.stream()
        .map(
            appRoleEntity -> {
              AppRoleDto roleDto =
                  new AppRoleDto(
                      appRoleEntity.getId(),
                      appRoleEntity.getName(),
                      appRoleEntity.getDescription());
              List<AppPermissionDto> permissions =
                  rolePermissionsMap.getOrDefault(roleDto.getId(), Collections.emptyList());
              roleDto.setPermissions(permissions);
              return roleDto;
            })
        .toList();
  }

  public ResponseEntity<AppUserResponse> getResponseSingleAppUser(
      final AppUserEntity appUserEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appUserEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appUserEntity);
    final List<AppUserDto> appUserDtos =
        appUserEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppUser(appUserEntity, true));
    return new ResponseEntity<>(
        new AppUserResponse(appUserDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppUserResponse> getResponseMultipleAppUser(
      final List<AppUserEntity> appUserEntities) {
    final List<AppUserDto> appUserDtos = convertEntitiesToDtosAppUser(appUserEntities, true);
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

  public AppUserDto convertEntityToDtoAppUser(
      final AppUserEntity appUserEntity, final boolean isIncludeRoles) {
    if (appUserEntity == null) {
      return null;
    }
    final AppUserDto appUserDto = new AppUserDto();
    BeanUtils.copyProperties(appUserEntity, appUserDto, "password");

    if (isIncludeRoles) {
      final List<AppUserRoleEntity> appUserRoleEntities =
          appUserRoleService.readAppUserRoles(appUserEntity.getId());
      final List<AppRoleEntity> appRoleEntities =
          appUserRoleEntities.stream().map(AppUserRoleEntity::getAppRole).toList();
      final List<AppRoleDto> appRoleDtos = convertEntitiesToDtosAppRole(appRoleEntities, true);
      appUserDto.setRoles(appRoleDtos);
    }

    return appUserDto;
  }

  public List<AppUserDto> convertEntitiesToDtosAppUser(
      final List<AppUserEntity> appUserEntities, final boolean isIncludeRoles) {
    if (CollectionUtils.isEmpty(appUserEntities)) {
      return Collections.emptyList();
    }
    if (!isIncludeRoles) {
      return appUserEntities.stream()
          .map(appUserEntity -> convertEntityToDtoAppUser(appUserEntity, false))
          .toList();
    }

    final List<Integer> appUserIds = appUserEntities.stream().map(AppUserEntity::getId).toList();
    final List<AppUserRoleEntity> appUserRoleEntities =
        appUserRoleService.readAppUserRoles(appUserIds);
    final List<Integer> appRoleIds =
        appUserRoleEntities.stream()
            .map(appUserRoleEntity -> appUserRoleEntity.getAppRole().getId())
            .toList();
    final List<AppRolePermissionEntity> appRolePermissionEntities =
        appRolePermissionService.readAppRolePermissions("", appRoleIds);

    Map<Integer, List<AppUserRoleEntity>> userRolesMap =
        appUserRoleEntities.stream()
            .collect(Collectors.groupingBy(userRole -> userRole.getAppUser().getId()));
    Map<Integer, List<AppRolePermissionEntity>> rolePermissionsMap =
        appRolePermissionEntities.stream()
            .collect(Collectors.groupingBy(rolePermission -> rolePermission.getAppRole().getId()));

    return appUserEntities.stream()
        .map(
            appUserEntity -> {
              List<AppRoleDto> appRoleDtos =
                  userRolesMap.getOrDefault(appUserEntity.getId(), Collections.emptyList()).stream()
                      .map(
                          appUserRoleEntity ->
                              convertEntityToDtoAppRole(appUserRoleEntity.getAppRole(), false))
                      .toList();

              appRoleDtos.forEach(
                  roleDto -> {
                    List<AppPermissionDto> appPermissionDtos =
                        rolePermissionsMap
                            .getOrDefault(roleDto.getId(), Collections.emptyList())
                            .stream()
                            .map(
                                appRolePermissionEntity ->
                                    convertEntityToDtoAppPermission(
                                        appRolePermissionEntity.getAppPermission()))
                            .toList();
                    roleDto.setPermissions(appPermissionDtos);
                  });

              AppUserDto appUserDto = convertEntityToDtoAppUser(appUserEntity, false);
              appUserDto.setRoles(appRoleDtos);
              return appUserDto;
            })
        .toList();
  }

  public ResponseEntity<AppRolePermissionResponse> getResponseSingleAppRolePermission(
      final AppRolePermissionEntity appRolePermissionEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appRolePermissionEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appRolePermissionEntity);
    final List<AppRolePermissionDto> appRolePermissionDtos =
        appRolePermissionEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppRolePermission(appRolePermissionEntity));
    return new ResponseEntity<>(
        new AppRolePermissionResponse(appRolePermissionDtos, null, null, responseStatusInfo),
        httpStatus);
  }

  public ResponseEntity<AppRolePermissionResponse> getResponseMultipleAppRolePermission(
      final List<AppRolePermissionEntity> appRolePermissionEntities) {
    final List<AppRolePermissionDto> appRolePermissionDtos =
        convertEntitiesToDtosAppRolePermission(appRolePermissionEntities);
    return ResponseEntity.ok(
        new AppRolePermissionResponse(appRolePermissionDtos, null, null, null));
  }

  public ResponseEntity<AppRolePermissionResponse> getResponseDeleteAppRolePermission() {
    return ResponseEntity.ok(
        new AppRolePermissionResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  public ResponseEntity<AppRolePermissionResponse> getResponseErrorAppRolePermission(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppRolePermissionResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  public AppRolePermissionDto convertEntityToDtoAppRolePermission(
      final AppRolePermissionEntity appRolePermissionEntity) {
    if (appRolePermissionEntity == null) {
      return null;
    }
    AppRoleDto appRoleDto = convertEntityToDtoAppRole(appRolePermissionEntity.getAppRole(), false);
    AppPermissionDto appPermissionDto =
        convertEntityToDtoAppPermission(appRolePermissionEntity.getAppPermission());
    return new AppRolePermissionDto(appRoleDto, appPermissionDto);
  }

  public List<AppRolePermissionDto> convertEntitiesToDtosAppRolePermission(
      final List<AppRolePermissionEntity> appRolePermissionEntities) {
    if (CollectionUtils.isEmpty(appRolePermissionEntities)) {
      return Collections.emptyList();
    }
    return appRolePermissionEntities.stream()
        .map(this::convertEntityToDtoAppRolePermission)
        .toList();
  }

  public ResponseEntity<AppUserRoleResponse> getResponseSingleAppUserRole(
      final AppUserRoleEntity appUserRoleEntity) {
    final HttpStatus httpStatus = getHttpStatusForSingleResponse(appUserRoleEntity);
    final ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(appUserRoleEntity);
    final List<AppUserRoleDto> appUserRoleDtos =
        appUserRoleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAppUserRole(appUserRoleEntity));
    return new ResponseEntity<>(
        new AppUserRoleResponse(appUserRoleDtos, null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<AppUserRoleResponse> getResponseMultipleAppUserRole(
      final List<AppUserRoleEntity> appUserRoleEntities) {
    final List<AppUserRoleDto> appUserRoleDtos =
        convertEntitiesToDtosAppUserRole(appUserRoleEntities);
    return ResponseEntity.ok(new AppUserRoleResponse(appUserRoleDtos, null, null, null));
  }

  public ResponseEntity<AppUserRoleResponse> getResponseDeleteAppUserRole() {
    return ResponseEntity.ok(
        new AppUserRoleResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  public ResponseEntity<AppUserRoleResponse> getResponseErrorAppUserRole(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AppUserRoleResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  public AppUserRoleDto convertEntityToDtoAppUserRole(final AppUserRoleEntity appUserRoleEntity) {
    if (appUserRoleEntity == null) {
      return null;
    }
    final AppUserDto appUserDto = convertEntityToDtoAppUser(appUserRoleEntity.getAppUser(), false);
    final AppRoleDto appRoleDto = convertEntityToDtoAppRole(appUserRoleEntity.getAppRole(), true);
    return new AppUserRoleDto(appUserDto, appRoleDto);
  }

  public List<AppUserRoleDto> convertEntitiesToDtosAppUserRole(
      final List<AppUserRoleEntity> appUserRoleEntities) {
    if (CollectionUtils.isEmpty(appUserRoleEntities)) {
      return Collections.emptyList();
    }
    return appUserRoleEntities.stream().map(this::convertEntityToDtoAppUserRole).toList();
  }

  public ResponseEntity<UserLoginResponse> getResponseErrorAppUserLogin(final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(new UserLoginResponse(null, null, responseStatusInfo), httpStatus);
  }

  public ResponseEntity<ResponseStatusInfo> getResponseErrorValidateReset(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(responseStatusInfo, httpStatus);
  }

  public ResponseEntity<Void> getResponseValidateUser(
      final String redirectUrl, final boolean isValidated) {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(
        URI.create(redirectUrl + (isValidated ? "?is_validated=true" : "?is_validated=false")));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  public ResponseEntity<Void> getResponseResetUser(
      final String redirectUrl, final boolean isReset, final String email) {
    String url = redirectUrl + (isReset ? "?is_reset=true&to_reset=" + email : "?is_reset=false");
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(url));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}
