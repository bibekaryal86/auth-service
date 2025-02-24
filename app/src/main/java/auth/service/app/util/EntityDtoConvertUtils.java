package auth.service.app.util;

import static auth.service.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static auth.service.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static auth.service.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformDtoProfileRole;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileAddressDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileDtoPlatformRole;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleDtoPlatformProfile;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.PermissionService;
import auth.service.app.service.PlatformProfileRoleService;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private final PermissionService permissionService;

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
      final List<PermissionEntity> permissionEntities,
      final ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
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
            .requestMetadata(requestMetadata)
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
      final RoleEntity roleEntity,
      final List<PermissionEntity> permissionEntitiesRole,
      final boolean isIncludePlatforms) {
    if (roleEntity == null) {
      return null;
    }

    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);
    roleDto.setPermissions(convertEntitiesToDtosPermissions(permissionEntitiesRole));

    if (!isIncludePlatforms) {
      roleDto.setPlatformProfiles(Collections.emptyList());
      return roleDto;
    }

    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByRoleIds(List.of(roleEntity.getId()));
    List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    List<ProfileEntity> profileEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getProfile)
            .distinct()
            .toList();

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(
                Collectors.toMap(
                    PlatformEntity::getId,
                    platformEntity -> convertEntityToDtoPlatform(platformEntity, false)));
    final Map<Long, ProfileDto> profileIdDtoMap =
        profileEntities.stream()
            .collect(
                Collectors.toMap(
                    ProfileEntity::getId,
                    profileEntity -> convertEntityToDtoProfile(profileEntity, false)));

    final List<RoleDtoPlatformProfile> platformProfiles =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                    Collectors.mapping(
                        prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                        Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new RoleDtoPlatformProfile(entry.getKey(), entry.getValue()))
            .toList();

    roleDto.setPlatformProfiles(platformProfiles);
    return roleDto;
  }

  private List<RoleDto> convertEntitiesToDtosRoles(
      final List<RoleEntity> roleEntities,
      final boolean isIncludePermissions,
      final boolean isIncludePlatforms) {
    if (CollectionUtils.isEmpty(roleEntities)) {
      return Collections.emptyList();
    }

    final List<PermissionEntity> permissionEntities;
    if (isIncludePermissions) {
      final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
      permissionEntities = permissionService.readPermissionsByRoleIds(roleIds);
    } else {
      permissionEntities = Collections.emptyList();
    }

    if (!isIncludePlatforms) {
      return roleEntities.stream()
          .map(
              roleEntity -> {
                final List<PermissionEntity> permissionEntitiesRole =
                    permissionEntities.stream()
                        .filter(
                            permissionEntity ->
                                Objects.equals(
                                    permissionEntity.getRole().getId(), roleEntity.getId()))
                        .toList();
                return convertEntityToDtoRole(roleEntity, permissionEntitiesRole, false);
              })
          .toList();
    }

    final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
    final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByRoleIds(roleIds);
    List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    List<ProfileEntity> profileEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getProfile)
            .distinct()
            .toList();

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(
                Collectors.toMap(
                    PlatformEntity::getId,
                    platformEntity -> convertEntityToDtoPlatform(platformEntity, false)));
    final Map<Long, ProfileDto> profileIdDtoMap =
        profileEntities.stream()
            .collect(
                Collectors.toMap(
                    ProfileEntity::getId,
                    profileEntity -> convertEntityToDtoProfile(profileEntity, true)));

    final Map<Long, List<RoleDtoPlatformProfile>> roleIdPlatformProfilesMap =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> prpe.getRole().getId(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                            Collectors.mapping(
                                prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                                Collectors.toList())),
                        map ->
                            map.entrySet().stream()
                                .map(
                                    e ->
                                        RoleDtoPlatformProfile.builder()
                                            .platform(e.getKey())
                                            .profiles(e.getValue())
                                            .build())
                                .collect(Collectors.toList()))));
    return roleEntities.stream()
        .map(
            roleEntity -> {
              final List<PermissionEntity> permissionEntitiesRole =
                  permissionEntities.stream()
                      .filter(
                          permissionEntity ->
                              Objects.equals(
                                  permissionEntity.getRole().getId(), roleEntity.getId()))
                      .toList();
              final RoleDto roleDto =
                  convertEntityToDtoRole(roleEntity, permissionEntitiesRole, false);
              final List<RoleDtoPlatformProfile> platformProfiles =
                  roleIdPlatformProfilesMap.getOrDefault(roleDto.getId(), Collections.emptyList());
              roleDto.setPlatformProfiles(platformProfiles);
              return roleDto;
            })
        .toList();
  }

  public ResponseEntity<RoleResponse> getResponseSingleRole(
      final RoleEntity roleEntity, final ResponseCrudInfo responseCrudInfo) {
    final List<PermissionEntity> permissionEntitiesRole =
        roleEntity == null
            ? Collections.emptyList()
            : permissionService.readPermissionsByRoleIds(List.of(roleEntity.getId()));
    final List<RoleDto> roleDtos =
        roleEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDtoRole(roleEntity, permissionEntitiesRole, true));
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
      final boolean isIncludePlatforms,
      final ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    final List<RoleDto> roleDtos =
        convertEntitiesToDtosRoles(roleEntities, isIncludePermissions, isIncludePlatforms);
    return ResponseEntity.ok(
        RoleResponse.builder()
            .roles(roleDtos)
            .responseMetadata(
                ResponseMetadata.builder()
                    .responsePageInfo(responsePageInfo)
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .requestMetadata(requestMetadata)
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
  private PlatformDto convertEntityToDtoPlatform(
      final PlatformEntity platformEntity, final boolean isIncludeProfiles) {
    if (platformEntity == null) {
      return null;
    }
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);

    if (!isIncludeProfiles) {
      platformDto.setProfileRoles(Collections.emptyList());
      return platformDto;
    }

    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByPlatformIds(
            List.of(platformEntity.getId()));
    List<ProfileEntity> profileEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getProfile)
            .distinct()
            .toList();
    List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();

    final Map<Long, ProfileDto> profileIdDtoMap =
        profileEntities.stream()
            .collect(
                Collectors.toMap(
                    ProfileEntity::getId,
                    profileEntity -> convertEntityToDtoProfile(profileEntity, false)));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    RoleEntity::getId,
                    roleEntity ->
                        convertEntityToDtoRole(roleEntity, Collections.emptyList(), false)));

    final List<PlatformDtoProfileRole> profileRoles =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                    Collectors.mapping(
                        prpe -> roleIdDtoMap.get(prpe.getRole().getId()), Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new PlatformDtoProfileRole(entry.getKey(), entry.getValue()))
            .toList();

    platformDto.setProfileRoles(profileRoles);
    return platformDto;
  }

  private List<PlatformDto> convertEntitiesToDtosPlatforms(
      final List<PlatformEntity> platformEntities, final boolean isIncludeProfiles) {
    if (CollectionUtils.isEmpty(platformEntities)) {
      return Collections.emptyList();
    }

    if (!isIncludeProfiles) {
      return platformEntities.stream()
          .map(platformEntity -> convertEntityToDtoPlatform(platformEntity, false))
          .toList();
    }

    final List<Long> platformIds = platformEntities.stream().map(PlatformEntity::getId).toList();
    final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByPlatformIds(platformIds);
    final List<ProfileEntity> profileEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getProfile)
            .distinct()
            .toList();
    final List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();

    final Map<Long, ProfileDto> profileIdDtoMap =
        profileEntities.stream()
            .collect(
                Collectors.toMap(
                    ProfileEntity::getId,
                    profileEntity -> convertEntityToDtoProfile(profileEntity, false)));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    RoleEntity::getId,
                    roleEntity ->
                        convertEntityToDtoRole(roleEntity, Collections.emptyList(), false)));
    final Map<Long, List<PlatformDtoProfileRole>> platformIdProfileRolesMap =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> prpe.getPlatform().getId(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                            Collectors.mapping(
                                prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                                Collectors.toList())),
                        map ->
                            map.entrySet().stream()
                                .map(
                                    e ->
                                        PlatformDtoProfileRole.builder()
                                            .profile(e.getKey())
                                            .roles(e.getValue())
                                            .build())
                                .collect(Collectors.toList()))));

    return platformEntities.stream()
        .map(
            platformEntity -> {
              final PlatformDto platformDto = convertEntityToDtoPlatform(platformEntity, false);
              final List<PlatformDtoProfileRole> profileRoles =
                  platformIdProfileRolesMap.getOrDefault(
                      platformDto.getId(), Collections.emptyList());
              platformDto.setProfileRoles(profileRoles);
              return platformDto;
            })
        .toList();
  }

  public ResponseEntity<PlatformResponse> getResponseSinglePlatform(
      final PlatformEntity platformEntity, final ResponseCrudInfo responseCrudInfo) {
    final List<PlatformDto> platformDtos =
        (platformEntity == null || platformEntity.getId() == null)
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPlatform(platformEntity, true));
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
      final List<PlatformEntity> platformEntities,
      final boolean isIncludeProfiles,
      final ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(convertEntitiesToDtosPlatforms(platformEntities, isIncludeProfiles))
            .responseMetadata(
                ResponseMetadata.builder()
                    .responsePageInfo(responsePageInfo)
                    .responseStatusInfo(CommonUtils.emptyResponseStatusInfo())
                    .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
                    .build())
            .requestMetadata(requestMetadata)
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

    final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByProfileIds(
            List.of(profileEntity.getId()));
    final List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    final List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();
    final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
    final List<PermissionEntity> permissionEntities =
        permissionService.readPermissionsByRoleIds(roleIds);

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(
                Collectors.toMap(
                    PlatformEntity::getId,
                    platformEntity -> convertEntityToDtoPlatform(platformEntity, false)));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    RoleEntity::getId,
                    roleEntity -> {
                      final List<PermissionEntity> permissionEntitiesRole =
                          permissionEntities.stream()
                              .filter(
                                  permissionEntity ->
                                      Objects.equals(
                                          permissionEntity.getRole().getId(), roleEntity.getId()))
                              .toList();
                      return convertEntityToDtoRole(roleEntity, permissionEntitiesRole, false);
                    }));

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
    final List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();
    final List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();
    final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
    final List<PermissionEntity> permissionEntities =
        permissionService.readPermissionsByRoleIds(roleIds);

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .collect(
                Collectors.toMap(
                    PlatformEntity::getId,
                    platformEntity -> convertEntityToDtoPlatform(platformEntity, false)));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .collect(
                Collectors.toMap(
                    RoleEntity::getId,
                    roleEntity -> {
                      final List<PermissionEntity> permissionEntitiesRole =
                          permissionEntities.stream()
                              .filter(
                                  permissionEntity ->
                                      Objects.equals(
                                          permissionEntity.getRole().getId(), roleEntity.getId()))
                              .toList();
                      return convertEntityToDtoRole(roleEntity, permissionEntitiesRole, false);
                    }));

    final Map<Long, List<ProfileDtoPlatformRole>> profileIdPlatformRolesMap =
        platformProfileRoleEntities.stream()
            .collect(
                Collectors.groupingBy(
                    prpe -> prpe.getProfile().getId(),
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
              // send isIncludeRoles as false here, roles already included
              final ProfileDto profileDto = convertEntityToDtoProfile(profileEntity, false);
              final List<ProfileDtoPlatformRole> platformRoles =
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
      final ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
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
            .requestMetadata(requestMetadata)
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
