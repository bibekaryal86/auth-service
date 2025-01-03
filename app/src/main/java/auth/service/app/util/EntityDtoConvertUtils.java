package auth.service.app.util;

import static auth.service.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static auth.service.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static auth.service.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import auth.service.app.model.dto.AddressTypeDto;
import auth.service.app.model.dto.AddressTypeResponse;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleDto;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.PlatformRolePermissionDto;
import auth.service.app.model.dto.PlatformRolePermissionResponse;
import auth.service.app.model.dto.ProfileAddressDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileDtoPlatformRole;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleDtoPlatformPermission;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.dto.StatusTypeDto;
import auth.service.app.model.dto.StatusTypeResponse;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.BaseEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.PlatformRolePermissionService;
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
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EntityDtoConvertUtils {
  private final PlatformProfileRoleService platformProfileRoleService;
  private final PlatformRolePermissionService platformRolePermissionService;

  public ResponseEntity<PlatformResponse> getResponseSinglePlatform(
      final PlatformEntity platformEntity) {
    final List<PlatformDto> platformDtos =
        platformEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPlatform(platformEntity));
    return new ResponseEntity<>(
        PlatformResponse.builder()
            .platforms(platformDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(platformEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(platformEntity));
  }

  public ResponseEntity<PlatformResponse> getResponseMultiplePlatforms(
      final List<PlatformEntity> platformEntities) {
    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(convertEntitiesToDtosPlatforms(platformEntities))
            .build());
  }

  public ResponseEntity<PlatformResponse> getResponseDeletePlatform() {
    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<PlatformResponse> getResponseErrorPlatform(final Exception exception) {
    return new ResponseEntity<>(
        PlatformResponse.builder()
            .platforms(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public PlatformDto convertEntityToDtoPlatform(final PlatformEntity platformEntity) {
    if (platformEntity == null) {
      return null;
    }
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    return platformDto;
  }

  public List<PlatformDto> convertEntitiesToDtosPlatforms(
      final List<PlatformEntity> platformEntities) {
    if (CollectionUtils.isEmpty(platformEntities)) {
      return Collections.emptyList();
    }
    return platformEntities.stream().map(this::convertEntityToDtoPlatform).toList();
  }

  public ResponseEntity<PermissionResponse> getResponseSinglePermission(
      final PermissionEntity permissionEntity) {
    final List<PermissionDto> permissionDtos =
        permissionEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPermission(permissionEntity));
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(permissionDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(permissionEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(permissionEntity));
  }

  public ResponseEntity<PermissionResponse> getResponseMultiplePermissions(
      final List<PermissionEntity> permissionEntities) {
    final List<PermissionDto> permissionDtos = convertEntitiesToDtosPermissions(permissionEntities);
    return ResponseEntity.ok(PermissionResponse.builder().permissions(permissionDtos).build());
  }

  public ResponseEntity<PermissionResponse> getResponseDeletePermission() {
    return ResponseEntity.ok(
        PermissionResponse.builder()
            .permissions(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<PermissionResponse> getResponseErrorPermission(final Exception exception) {
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public PermissionDto convertEntityToDtoPermission(final PermissionEntity permissionEntity) {
    if (permissionEntity == null) {
      return null;
    }
    PermissionDto permissionDto = new PermissionDto();
    BeanUtils.copyProperties(permissionEntity, permissionDto);
    return permissionDto;
  }

  public List<PermissionDto> convertEntitiesToDtosPermissions(
      final List<PermissionEntity> permissionEntities) {
    if (CollectionUtils.isEmpty(permissionEntities)) {
      return Collections.emptyList();
    }
    return permissionEntities.stream().map(this::convertEntityToDtoPermission).toList();
  }

  public ResponseEntity<RoleResponse> getResponseSingleRole(final RoleEntity roleEntity) {
    final List<RoleDto> roleDtos =
        roleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoRole(roleEntity, true));
    return new ResponseEntity<>(
        RoleResponse.builder()
            .roles(roleDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(roleEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(roleEntity));
  }

  public ResponseEntity<RoleResponse> getResponseMultipleRoles(
      final List<RoleEntity> roleEntities) {
    final List<RoleDto> roleDtos = convertEntitiesToDtosRoles(roleEntities, true);
    return ResponseEntity.ok(RoleResponse.builder().roles(roleDtos).build());
  }

  public ResponseEntity<RoleResponse> getResponseDeleteRole() {
    return ResponseEntity.ok(
        RoleResponse.builder()
            .roles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<RoleResponse> getResponseErrorRole(final Exception exception) {
    return new ResponseEntity<>(
        RoleResponse.builder()
            .roles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public RoleDto convertEntityToDtoRole(
      final RoleEntity roleEntity, final boolean isIncludePermissions) {
    if (roleEntity == null) {
      return null;
    }

    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    if (!isIncludePermissions) {
      return roleDto;
    }

    final List<PlatformRolePermissionEntity> platformRolePermissionEntities =
        platformRolePermissionService.readPlatformRolePermissionsByRoleId(roleEntity.getId());

    final List<PlatformEntity> platformEntities =
        platformRolePermissionEntities.stream()
            .map(PlatformRolePermissionEntity::getPlatform)
            .distinct()
            .toList();
    final List<PermissionEntity> permissionEntities =
        platformRolePermissionEntities.stream()
            .map(PlatformRolePermissionEntity::getPermission)
            .distinct()
            .toList();

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(Collectors.toMap(PlatformEntity::getId, this::convertEntityToDtoPlatform));

    final Map<Long, PermissionDto> permissionIdDtoMap =
        permissionEntities.stream()
            .collect(Collectors.toMap(PermissionEntity::getId, this::convertEntityToDtoPermission));

    final List<RoleDtoPlatformPermission> platformPermissions =
        platformRolePermissionEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                    Collectors.mapping(
                        prpe -> permissionIdDtoMap.get(prpe.getPermission().getId()),
                        Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new RoleDtoPlatformPermission(entry.getKey(), entry.getValue()))
            .toList();

    roleDto.setPlatformPermissions(platformPermissions);
    return roleDto;
  }

  public List<RoleDto> convertEntitiesToDtosRoles(
      final List<RoleEntity> roleEntities, final boolean isIncludePermissions) {
    if (CollectionUtils.isEmpty(roleEntities)) {
      return Collections.emptyList();
    }

    if (!isIncludePermissions) {
      return roleEntities.stream()
          .map(appRoleEntity -> convertEntityToDtoRole(appRoleEntity, false))
          .toList();
    }

    final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).distinct().toList();
    final List<PlatformRolePermissionEntity> platformRolePermissionEntities =
        platformRolePermissionService.readPlatformRolePermissionsByRoleIds(roleIds);
    final List<PlatformEntity> platformEntities =
        platformRolePermissionEntities.stream()
            .map(PlatformRolePermissionEntity::getPlatform)
            .distinct()
            .toList();
    final List<PermissionEntity> permissionEntities =
        platformRolePermissionEntities.stream()
            .map(PlatformRolePermissionEntity::getPermission)
            .distinct()
            .toList();
    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(Collectors.toMap(PlatformEntity::getId, this::convertEntityToDtoPlatform));
    final Map<Long, PermissionDto> permissionIdDtoMap =
        permissionEntities.stream()
            .collect(Collectors.toMap(PermissionEntity::getId, this::convertEntityToDtoPermission));
    final Map<Long, List<RoleDtoPlatformPermission>> roleIdPlatformPermissionsMap =
        platformRolePermissionEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> prpe.getRole().getId(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                            Collectors.mapping(
                                prpe -> permissionIdDtoMap.get(prpe.getPermission().getId()),
                                Collectors.toList())),
                        map ->
                            map.entrySet().stream()
                                .map(
                                    e ->
                                        RoleDtoPlatformPermission.builder()
                                            .platform(e.getKey())
                                            .permissions(e.getValue())
                                            .build())
                                .collect(Collectors.toList()))));

    return roleEntities.stream()
        .map(
            roleEntity -> {
              RoleDto roleDto = new RoleDto();
              BeanUtils.copyProperties(roleEntity, roleDto);
              final List<RoleDtoPlatformPermission> platformPermissions =
                  roleIdPlatformPermissionsMap.getOrDefault(
                      roleDto.getId(), Collections.emptyList());
              roleDto.setPlatformPermissions(platformPermissions);
              return roleDto;
            })
        .toList();
  }

  public ResponseEntity<ProfileResponse> getResponseSingleProfile(
      final ProfileEntity profileEntity) {
    final List<ProfileDto> profileDtos =
        profileEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoProfile(profileEntity, true));
    return new ResponseEntity<>(
        ProfileResponse.builder()
            .profiles(profileDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(profileEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(profileEntity));
  }

  public ResponseEntity<ProfileResponse> getResponseMultipleProfiles(
      final List<ProfileEntity> profileEntities) {
    final List<ProfileDto> profileDtos = convertEntitiesToDtosProfiles(profileEntities, true);
    return ResponseEntity.ok(ProfileResponse.builder().profiles(profileDtos).build());
  }

  public ResponseEntity<ProfileResponse> getResponseDeleteProfile() {
    return ResponseEntity.ok(
        ProfileResponse.builder()
            .profiles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<ProfileResponse> getResponseErrorProfile(final Exception exception) {
    return new ResponseEntity<>(
        ProfileResponse.builder()
            .profiles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public ProfileDto convertEntityToDtoProfile(
      final ProfileEntity profileEntity, final boolean isIncludeRoles) {
    if (profileEntity == null) {
      return null;
    }
    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "addresses", "status", "roles");

    profileDto.setStatus(convertEntityToDtoStatusType(profileEntity.getStatusType()));

    if (!CollectionUtils.isEmpty(profileEntity.getAddresses())) {
      final List<ProfileAddressDto> profileAddressDtos =
          convertEntitiesToDtosProfileAddress(profileEntity.getAddresses());
      profileDto.setAddresses(profileAddressDtos);
    }

    if (!isIncludeRoles) {
      return profileDto;
    }

    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByProfileId(profileEntity.getId());
    List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(Collectors.toMap(PlatformEntity::getId, this::convertEntityToDtoPlatform));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    RoleEntity::getId, roleEntity -> convertEntityToDtoRole(roleEntity, true)));

    final List<ProfileDtoPlatformRole> platformRoles =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                    Collectors.mapping(
                        prpe -> roleIdDtoMap.get(prpe.getRole().getId()), Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new ProfileDtoPlatformRole(entry.getKey(), entry.getValue()))
            .toList();

    profileDto.setPlatformRoles(platformRoles);
    return profileDto;
  }

  public List<ProfileDto> convertEntitiesToDtosProfiles(
      final List<ProfileEntity> profileEntities, final boolean isIncludeRoles) {
    if (CollectionUtils.isEmpty(profileEntities)) {
      return Collections.emptyList();
    }
    if (!isIncludeRoles) {
      return profileEntities.stream()
          .map(profileEntity -> convertEntityToDtoProfile(profileEntity, false))
          .toList();
    }

    final List<Long> profileIds = profileEntities.stream().map(ProfileEntity::getId).toList();
    final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByProfileIds(profileIds);
    List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(Collectors.toMap(PlatformEntity::getId, this::convertEntityToDtoPlatform));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    BaseEntity::getId, roleEntity -> convertEntityToDtoRole(roleEntity, true)));

    final Map<Long, List<ProfileDtoPlatformRole>> profileIdPlatformRolesMap =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> prpe.getRole().getId(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                            Collectors.mapping(
                                prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                                Collectors.toList())),
                        map ->
                            map.entrySet().stream()
                                .map(
                                    e ->
                                        ProfileDtoPlatformRole.builder()
                                            .platform(e.getKey())
                                            .roles(e.getValue())
                                            .build())
                                .collect(Collectors.toList()))));

    return profileEntities.stream()
        .map(
            profileEntity -> {
              ProfileDto profileDto = new ProfileDto();
              BeanUtils.copyProperties(
                  profileEntity, profileDto, "password", "addresses", "status", "roles");
              List<ProfileDtoPlatformRole> platformRoles =
                  profileIdPlatformRolesMap.getOrDefault(
                      profileDto.getId(), Collections.emptyList());
              profileDto.setPlatformRoles(platformRoles);
              profileDto.setStatus(convertEntityToDtoStatusType(profileEntity.getStatusType()));

              if (!CollectionUtils.isEmpty(profileEntity.getAddresses())) {
                final List<ProfileAddressDto> profileAddressDtos =
                    convertEntitiesToDtosProfileAddress(profileEntity.getAddresses());
                profileDto.setAddresses(profileAddressDtos);
              }

              return profileDto;
            })
        .toList();
  }

  public ResponseEntity<StatusTypeResponse> getResponseSingleStatusType(
      final StatusTypeEntity statusTypeEntity) {
    final List<StatusTypeDto> statusTypeDtos =
        statusTypeEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoStatusType(statusTypeEntity));
    return new ResponseEntity<>(
        StatusTypeResponse.builder()
            .statusTypes(statusTypeDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(statusTypeEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(statusTypeEntity));
  }

  public ResponseEntity<StatusTypeResponse> getResponseMultipleStatusTypes(
      final List<StatusTypeEntity> statusTypeEntities) {
    return ResponseEntity.ok(
        StatusTypeResponse.builder()
            .statusTypes(convertEntitiesToDtosStatusTypes(statusTypeEntities))
            .build());
  }

  public ResponseEntity<StatusTypeResponse> getResponseDeleteStatusType() {
    return ResponseEntity.ok(
        StatusTypeResponse.builder()
            .statusTypes(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<StatusTypeResponse> getResponseErrorStatusType(final Exception exception) {
    return new ResponseEntity<>(
        StatusTypeResponse.builder()
            .statusTypes(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  private StatusTypeDto convertEntityToDtoStatusType(final StatusTypeEntity statusTypeEntity) {
    if (statusTypeEntity == null) {
      return null;
    }
    final StatusTypeDto statusTypeDto = new StatusTypeDto();
    BeanUtils.copyProperties(statusTypeEntity, statusTypeDto);
    return statusTypeDto;
  }

  private List<StatusTypeDto> convertEntitiesToDtosStatusTypes(
      final List<StatusTypeEntity> statusTypeEntities) {
    if (CollectionUtils.isEmpty(statusTypeEntities)) {
      return Collections.emptyList();
    }
    return statusTypeEntities.stream().map(this::convertEntityToDtoStatusType).toList();
  }

  public ResponseEntity<AddressTypeResponse> getResponseSingleAddressType(
      final AddressTypeEntity addressTypeEntity) {
    final List<AddressTypeDto> addressTypeDtos =
        addressTypeEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoAddressType(addressTypeEntity));
    return new ResponseEntity<>(
        AddressTypeResponse.builder()
            .addressTypes(addressTypeDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(getResponseStatusInfoForSingleResponse(addressTypeEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(addressTypeEntity));
  }

  public ResponseEntity<AddressTypeResponse> getResponseMultipleAddressTypes(
      final List<AddressTypeEntity> addressTypeEntities) {
    return ResponseEntity.ok(
        AddressTypeResponse.builder()
            .addressTypes(convertEntitiesToDtosAddressTypes(addressTypeEntities))
            .build());
  }

  public ResponseEntity<AddressTypeResponse> getResponseDeleteAddressType() {
    return ResponseEntity.ok(
        AddressTypeResponse.builder()
            .addressTypes(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<AddressTypeResponse> getResponseErrorAddressType(
      final Exception exception) {
    return new ResponseEntity<>(
        AddressTypeResponse.builder()
            .addressTypes(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  private AddressTypeDto convertEntityToDtoAddressType(final AddressTypeEntity addressTypeEntity) {
    if (addressTypeEntity == null) {
      return null;
    }
    final AddressTypeDto addressTypeDto = new AddressTypeDto();
    BeanUtils.copyProperties(addressTypeEntity, addressTypeDto);
    return addressTypeDto;
  }

  private List<AddressTypeDto> convertEntitiesToDtosAddressTypes(
      final List<AddressTypeEntity> addressTypeEntities) {
    if (CollectionUtils.isEmpty(addressTypeEntities)) {
      return Collections.emptyList();
    }
    return addressTypeEntities.stream().map(this::convertEntityToDtoAddressType).toList();
  }

  private ProfileAddressDto convertEntityToDtoProfileAddress(
      final ProfileAddressEntity profileAddressEntity) {
    if (profileAddressEntity == null) {
      return null;
    }

    final ProfileAddressDto profileAddressDto = new ProfileAddressDto();
    BeanUtils.copyProperties(profileAddressEntity, profileAddressDto, "profile", "type");
    AddressTypeDto addressTypeDto = convertEntityToDtoAddressType(profileAddressEntity.getType());
    profileAddressDto.setType(addressTypeDto);

    return profileAddressDto;
  }

  private List<ProfileAddressDto> convertEntitiesToDtosProfileAddress(
      final List<ProfileAddressEntity> profileAddressEntities) {
    if (CollectionUtils.isEmpty(profileAddressEntities)) {
      return Collections.emptyList();
    }
    return profileAddressEntities.stream().map(this::convertEntityToDtoProfileAddress).toList();
  }

  public ResponseEntity<ProfilePasswordTokenResponse> getResponseErrorProfilePassword(
      final Exception exception) {
    return new ResponseEntity<>(
        ProfilePasswordTokenResponse.builder()
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public ResponseEntity<ResponseMetadata> getResponseErrorResponseMetadata(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    return new ResponseEntity<>(
        ResponseMetadata.builder()
            .responseStatusInfo(ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
            .build(),
        httpStatus);
  }

  public ResponseEntity<Void> getResponseValidateProfile(
      final String redirectUrl, final boolean isValidated) {
    if (!StringUtils.hasText(redirectUrl)) {
      throw new IllegalStateException("Redirect URL cannot be null or empty...");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(
        URI.create(redirectUrl + (isValidated ? "?is_validated=true" : "?is_validated=false")));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  public ResponseEntity<Void> getResponseResetProfile(
      final String redirectUrl, final boolean isReset, final String email) {
    if (!StringUtils.hasText(redirectUrl)) {
      throw new IllegalStateException("Redirect URL cannot be null or empty...");
    }

    final String url =
        redirectUrl + (isReset ? "?is_reset=true&to_reset=" + email : "?is_reset=false");
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(url));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  public ResponseEntity<PlatformProfileRoleResponse> getResponseSinglePlatformProfileRole(
      final PlatformProfileRoleEntity platformProfileRoleEntity) {
    final List<PlatformProfileRoleDto> platformProfileRoleDtos =
        platformProfileRoleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPlatformProfileRole(platformProfileRoleEntity));
    return new ResponseEntity<>(
        PlatformProfileRoleResponse.builder()
            .platformProfileRoles(platformProfileRoleDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        getResponseStatusInfoForSingleResponse(platformProfileRoleEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(platformProfileRoleEntity));
  }

  public ResponseEntity<PlatformProfileRoleResponse> getResponseMultiplePlatformProfileRoles(
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities) {
    return ResponseEntity.ok(
        PlatformProfileRoleResponse.builder()
            .platformProfileRoles(
                convertEntitiesToDtosPlatformProfileRoles(platformProfileRoleEntities))
            .build());
  }

  public ResponseEntity<PlatformProfileRoleResponse> getResponseDeletePlatformProfileRole() {
    return ResponseEntity.ok(
        PlatformProfileRoleResponse.builder()
            .platformProfileRoles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<PlatformProfileRoleResponse> getResponseErrorPlatformProfileRole(
      final Exception exception) {
    return new ResponseEntity<>(
        PlatformProfileRoleResponse.builder()
            .platformProfileRoles(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public PlatformProfileRoleDto convertEntityToDtoPlatformProfileRole(
      final PlatformProfileRoleEntity platformProfileRoleEntity) {
    if (platformProfileRoleEntity == null) {
      return null;
    }
    final PlatformDto platformDto =
        convertEntityToDtoPlatform(platformProfileRoleEntity.getPlatform());
    final RoleDto roleDto = convertEntityToDtoRole(platformProfileRoleEntity.getRole(), false);
    final ProfileDto profileDto =
        convertEntityToDtoProfile(platformProfileRoleEntity.getProfile(), false);
    return PlatformProfileRoleDto.builder()
        .platform(platformDto)
        .profile(profileDto)
        .role(roleDto)
        .build();
  }

  public List<PlatformProfileRoleDto> convertEntitiesToDtosPlatformProfileRoles(
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities) {
    if (CollectionUtils.isEmpty(platformProfileRoleEntities)) {
      return Collections.emptyList();
    }
    return platformProfileRoleEntities.stream()
        .map(this::convertEntityToDtoPlatformProfileRole)
        .toList();
  }

  public ResponseEntity<PlatformRolePermissionResponse> getResponseSinglePlatformRolePermission(
      final PlatformRolePermissionEntity platformRolePermissionEntity) {
    final List<PlatformRolePermissionDto> platformRolePermissionDtos =
        platformRolePermissionEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPlatformRolePermission(platformRolePermissionEntity));
    return new ResponseEntity<>(
        PlatformRolePermissionResponse.builder()
            .platformRolePermissions(platformRolePermissionDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        getResponseStatusInfoForSingleResponse(platformRolePermissionEntity))
                    .build())
            .build(),
        getHttpStatusForSingleResponse(platformRolePermissionEntity));
  }

  public ResponseEntity<PlatformRolePermissionResponse> getResponseMultiplePlatformRolePermissions(
      final List<PlatformRolePermissionEntity> platformRolePermissionEntities) {
    return ResponseEntity.ok(
        PlatformRolePermissionResponse.builder()
            .platformRolePermissions(
                convertEntitiesToDtosPlatformRolePermissions(platformRolePermissionEntities))
            .build());
  }

  public ResponseEntity<PlatformRolePermissionResponse> getResponseDeletePlatformRolePermission() {
    return ResponseEntity.ok(
        PlatformRolePermissionResponse.builder()
            .platformRolePermissions(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseCrudInfo(ResponseCrudInfo.builder().deletedRowsCount(1).build())
                    .build())
            .build());
  }

  public ResponseEntity<PlatformRolePermissionResponse> getResponseErrorPlatformRolePermission(
      final Exception exception) {
    return new ResponseEntity<>(
        PlatformRolePermissionResponse.builder()
            .platformRolePermissions(Collections.emptyList())
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  public PlatformRolePermissionDto convertEntityToDtoPlatformRolePermission(
      final PlatformRolePermissionEntity platformRolePermissionEntity) {
    if (platformRolePermissionEntity == null) {
      return null;
    }
    final PlatformDto platformDto =
        convertEntityToDtoPlatform(platformRolePermissionEntity.getPlatform());
    final RoleDto roleDto = convertEntityToDtoRole(platformRolePermissionEntity.getRole(), false);
    final PermissionDto permissionDto =
        convertEntityToDtoPermission(platformRolePermissionEntity.getPermission());
    return PlatformRolePermissionDto.builder()
        .platform(platformDto)
        .role(roleDto)
        .permission(permissionDto)
        .build();
  }

  public List<PlatformRolePermissionDto> convertEntitiesToDtosPlatformRolePermissions(
      final List<PlatformRolePermissionEntity> platformRolePermissionEntities) {
    if (CollectionUtils.isEmpty(platformRolePermissionEntities)) {
      return Collections.emptyList();
    }
    return platformRolePermissionEntities.stream()
        .map(this::convertEntityToDtoPlatformRolePermission)
        .toList();
  }
}
