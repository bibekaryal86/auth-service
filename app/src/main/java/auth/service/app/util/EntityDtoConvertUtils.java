package auth.service.app.util;

import static auth.service.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static auth.service.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static auth.service.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileAddressDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileDtoPlatformRole;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.BaseEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.PlatformProfileRoleService;
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

  // permission
  private PermissionDto convertEntityToDtoPermission(final PermissionEntity permissionEntity) {
    if (permissionEntity == null) {
      return null;
    }
    PermissionDto permissionDto = new PermissionDto();
    BeanUtils.copyProperties(permissionEntity, permissionDto);
    return permissionDto;
  }

  private List<PermissionDto> convertEntitiesToDtosPermissions(
      final List<PermissionEntity> permissionEntities) {
    if (CollectionUtils.isEmpty(permissionEntities)) {
      return Collections.emptyList();
    }
    return permissionEntities.stream().map(this::convertEntityToDtoPermission).toList();
  }

  public ResponseEntity<PermissionResponse> getResponseSinglePermission(
      final PermissionEntity permissionEntity, final ResponseCrudInfo responseCrudInfo) {
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(
                        responseCrudInfo == null
                            ? CommonUtils.emptyResponseCrudInfo()
                            : responseCrudInfo)
                    .build())
            .build(),
        getHttpStatusForSingleResponse(permissionEntity));
  }

  public ResponseEntity<PermissionResponse> getResponseMultiplePermissions(
      final List<PermissionEntity> permissionEntities, final ResponsePageInfo responsePageInfo) {
    final List<PermissionDto> permissionDtos = convertEntitiesToDtosPermissions(permissionEntities);
    return ResponseEntity.ok(
        PermissionResponse.builder()
            .permissions(permissionDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responsePageInfo(responsePageInfo)
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // role
  private RoleDto convertEntityToDtoRole(
      final RoleEntity roleEntity, final boolean isIncludePermissions) {
    if (roleEntity == null) {
      return null;
    }

    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    if (isIncludePermissions) {
      List<PermissionDto> permissionDtos =
          convertEntitiesToDtosPermissions(roleEntity.getPermissionEntities());
      roleDto.setPermissions(permissionDtos);
    } else {
      roleDto.setPermissions(Collections.emptyList());
    }

    return roleDto;
  }

  private List<RoleDto> convertEntitiesToDtosRoles(
      final List<RoleEntity> roleEntities, final boolean isIncludePermissions) {
    if (CollectionUtils.isEmpty(roleEntities)) {
      return Collections.emptyList();
    }

    return roleEntities.stream()
        .map(appRoleEntity -> convertEntityToDtoRole(appRoleEntity, isIncludePermissions))
        .toList();
  }

  public ResponseEntity<RoleResponse> getResponseSingleRole(
      final RoleEntity roleEntity, final ResponseCrudInfo responseCrudInfo) {
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(
                        responseCrudInfo == null
                            ? CommonUtils.emptyResponseCrudInfo()
                            : responseCrudInfo)
                    .build())
            .build(),
        getHttpStatusForSingleResponse(roleEntity));
  }

  public ResponseEntity<RoleResponse> getResponseMultipleRoles(
      final List<RoleEntity> roleEntities,
      final boolean isIncludePermissions,
      final ResponsePageInfo responsePageInfo) {
    final List<RoleDto> roleDtos = convertEntitiesToDtosRoles(roleEntities, isIncludePermissions);
    return ResponseEntity.ok(
        RoleResponse.builder()
            .roles(roleDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responsePageInfo(responsePageInfo)
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // platform
  private PlatformDto convertEntityToDtoPlatform(final PlatformEntity platformEntity) {
    if (platformEntity == null) {
      return null;
    }
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    return platformDto;
  }

  private List<PlatformDto> convertEntitiesToDtosPlatforms(
      final List<PlatformEntity> platformEntities) {
    if (CollectionUtils.isEmpty(platformEntities)) {
      return Collections.emptyList();
    }
    return platformEntities.stream().map(this::convertEntityToDtoPlatform).toList();
  }

  public ResponseEntity<PlatformResponse> getResponseSinglePlatform(
      final PlatformEntity platformEntity, final ResponseCrudInfo responseCrudInfo) {
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(
                        responseCrudInfo == null
                            ? CommonUtils.emptyResponseCrudInfo()
                            : responseCrudInfo)
                    .build())
            .build(),
        getHttpStatusForSingleResponse(platformEntity));
  }

  public ResponseEntity<PlatformResponse> getResponseMultiplePlatforms(
      final List<PlatformEntity> platformEntities, final ResponsePageInfo responsePageInfo) {
    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(convertEntitiesToDtosPlatforms(platformEntities))
            .responseMetadata(
                ResponseMetadata.builder()
                    .responsePageInfo(responsePageInfo)
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // profile, profile_address
  private ProfileAddressDto convertEntityToDtoProfileAddress(
      final ProfileAddressEntity profileAddressEntity) {
    if (profileAddressEntity == null) {
      return null;
    }

    final ProfileAddressDto profileAddressDto = new ProfileAddressDto();
    BeanUtils.copyProperties(profileAddressEntity, profileAddressDto, "profile");
    return profileAddressDto;
  }

  private List<ProfileAddressDto> convertEntitiesToDtosProfileAddress(
      final List<ProfileAddressEntity> profileAddressEntities) {
    if (CollectionUtils.isEmpty(profileAddressEntities)) {
      return Collections.emptyList();
    }
    return profileAddressEntities.stream().map(this::convertEntityToDtoProfileAddress).toList();
  }

  public ProfileDto convertEntityToDtoProfile(
      final ProfileEntity profileEntity, final boolean isIncludeRoles) {
    if (profileEntity == null) {
      return null;
    }
    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "profileAddress");

    if (profileEntity.getProfileAddress() != null) {
      profileDto.setProfileAddress(
          convertEntityToDtoProfileAddress(profileEntity.getProfileAddress()));
    }

    if (!isIncludeRoles) {
      profileDto.setPlatformRoles(Collections.emptyList());
      return profileDto;
    }

    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByProfileIds(
            List.of(profileEntity.getId()));
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

  private List<ProfileDto> convertEntitiesToDtosProfiles(
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
              ProfileDto profileDto = convertEntityToDtoProfile(profileEntity, isIncludeRoles);

              List<ProfileDtoPlatformRole> platformRoles =
                  profileIdPlatformRolesMap.getOrDefault(
                      profileDto.getId(), Collections.emptyList());
              profileDto.setPlatformRoles(platformRoles);

              return profileDto;
            })
        .toList();
  }

  public ResponseEntity<ProfileResponse> getResponseSingleProfile(
      final ProfileEntity profileEntity, final ResponseCrudInfo responseCrudInfo) {
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(
                        responseCrudInfo == null
                            ? CommonUtils.emptyResponseCrudInfo()
                            : responseCrudInfo)
                    .build())
            .build(),
        getHttpStatusForSingleResponse(profileEntity));
  }

  public ResponseEntity<ProfileResponse> getResponseMultipleProfiles(
      final List<ProfileEntity> profileEntities,
      final boolean isIncludeRoles,
      final ResponsePageInfo responsePageInfo) {
    final List<ProfileDto> profileDtos =
        convertEntitiesToDtosProfiles(profileEntities, isIncludeRoles);
    return ResponseEntity.ok(
        ProfileResponse.builder()
            .profiles(profileDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .responsePageInfo(responsePageInfo)
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
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // others
  public ResponseEntity<ProfilePasswordTokenResponse> getResponseErrorProfilePassword(
      final Exception exception) {
    return new ResponseEntity<>(
        ProfilePasswordTokenResponse.builder()
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
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
            .responsePageInfo(CommonUtils.emptyResponsePageInfo())
            .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
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

  public ResponseEntity<PlatformProfileRoleResponse> getResponseErrorPlatformProfileRole(
      final Exception exception) {
    return new ResponseEntity<>(
        PlatformProfileRoleResponse.builder()
            .responseMetadata(
                ResponseMetadata.builder()
                    .responseStatusInfo(
                        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build())
                    .responsePageInfo(CommonUtils.emptyResponsePageInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .build(),
        getHttpStatusForErrorResponse(exception));
  }
}
