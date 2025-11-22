package auth.service.app.util;

import static auth.service.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static auth.service.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static auth.service.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import auth.service.app.model.dto.AuditPermissionDto;
import auth.service.app.model.dto.AuditPlatformDto;
import auth.service.app.model.dto.AuditProfileDto;
import auth.service.app.model.dto.AuditResponse;
import auth.service.app.model.dto.AuditRoleDto;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformDtoProfileRole;
import auth.service.app.model.dto.PlatformDtoRoleProfile;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileAddressDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileDtoPlatformRole;
import auth.service.app.model.dto.ProfileDtoRolePlatform;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleDtoPlatformProfile;
import auth.service.app.model.dto.RoleDtoProfilePlatform;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.PermissionService;
import auth.service.app.service.PlatformProfileRoleService;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EntityDtoConvertUtils {
  private final PlatformProfileRoleService platformProfileRoleService;
  private final PermissionService permissionService;
  private final CookieService cookieService;

  // permission
  private PermissionDto convertEntityToDtoPermission(
      final PermissionEntity permissionEntity, final AuditResponse auditResponse) {
    if (permissionEntity == null) {
      return null;
    }
    PermissionDto permissionDto = new PermissionDto();
    BeanUtils.copyProperties(permissionEntity, permissionDto, "role");

    permissionDto.setAuditResponse(auditResponse);
    permissionDto.setRole(
        convertEntityToDtoRole(
            permissionEntity.getRole(),
            Collections.emptyList(),
            Boolean.FALSE,
            Boolean.FALSE,
            null));

    return permissionDto;
  }

  private List<PermissionDto> convertEntitiesToDtosPermissions(
      final List<PermissionEntity> permissionEntities) {
    if (CollectionUtils.isEmpty(permissionEntities)) {
      return Collections.emptyList();
    }
    return permissionEntities.stream()
        .map(permissionEntity -> convertEntityToDtoPermission(permissionEntity, null))
        .toList();
  }

  public ResponseEntity<PermissionResponse> getResponseSinglePermission(
      final PermissionEntity permissionEntity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final RequestMetadata requestMetadata,
      final AuditResponse auditResponse) {
    final List<PermissionDto> permissionDtos =
        (permissionEntity == null || permissionEntity.getId() == null)
            ? Collections.emptyList()
            : List.of(convertEntityToDtoPermission(permissionEntity, auditResponse));
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(permissionDtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(permissionEntity),
                    responseCrudInfo == null
                        ? ResponseMetadata.emptyResponseCrudInfo()
                        : responseCrudInfo,
                    ResponseMetadata.emptyResponsePageInfo()))
            .requestMetadata(requestMetadata)
            .build(),
        getHttpStatusForSingleResponse(permissionEntity));
  }

  public ResponseEntity<PermissionResponse> getResponseMultiplePermissions(
      final List<PermissionEntity> permissionEntities,
      final ResponseMetadata.ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    final List<PermissionDto> permissionDtos = convertEntitiesToDtosPermissions(permissionEntities);
    return ResponseEntity.ok(
        PermissionResponse.builder()
            .permissions(permissionDtos)
            .responseMetadata(
                new ResponseMetadata(
                    ResponseMetadata.emptyResponseStatusInfo(),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    responsePageInfo))
            .requestMetadata(requestMetadata)
            .build());
  }

  public ResponseEntity<PermissionResponse> getResponseErrorPermission(final Exception exception) {
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(Collections.emptyList())
            .responseMetadata(
                new ResponseMetadata(
                    new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // role
  private RoleDto convertEntityToDtoRole(
      final RoleEntity roleEntity,
      final List<PermissionEntity> permissionEntitiesRole,
      final boolean isIncludePlatforms,
      final boolean isIncludeProfiles,
      final AuditResponse auditResponse) {
    if (roleEntity == null) {
      return null;
    }

    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);
    roleDto.setAuditResponse(auditResponse);

    if (CommonUtils.canReadPermissions()) {
      roleDto.setPermissions(convertEntitiesToDtosPermissions(permissionEntitiesRole));
    } else {
      roleDto.setPermissions(Collections.emptyList());
    }

    if ((isIncludePlatforms || isIncludeProfiles)
        && (CommonUtils.canReadPlatforms() || CommonUtils.canReadProfiles())) {
      List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByRoleIds(List.of(roleEntity.getId()));

      List<PlatformEntity> platformEntities = Collections.emptyList();
      List<ProfileEntity> profileEntities = Collections.emptyList();

      final boolean shouldIncludePlatforms = isIncludePlatforms && CommonUtils.canReadPlatforms();
      final boolean shouldIncludeProfiles = isIncludeProfiles && CommonUtils.canReadProfiles();

      final boolean shouldIncludeProfilesDueToPlatforms =
          shouldIncludePlatforms && CommonUtils.canReadProfiles();
      final boolean shouldIncludePlatformsDueToProfiles =
          shouldIncludeProfiles && CommonUtils.canReadPlatforms();

      if (shouldIncludePlatforms || shouldIncludePlatformsDueToProfiles) {
        platformEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getPlatform)
                .distinct()
                .toList();
      }

      if (shouldIncludeProfiles || shouldIncludeProfilesDueToPlatforms) {
        profileEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getProfile)
                .distinct()
                .toList();
      }

      final Map<Long, PlatformDto> platformIdDtoMap =
          platformEntities.isEmpty()
              ? Collections.emptyMap()
              : platformEntities.stream()
                  .collect(
                      Collectors.toMap(
                          PlatformEntity::getId,
                          platformEntity ->
                              convertEntityToDtoPlatform(
                                  platformEntity, Boolean.FALSE, Boolean.FALSE, null)));
      final Map<Long, ProfileDto> profileIdDtoMap =
          profileEntities.isEmpty()
              ? Collections.emptyMap()
              : profileEntities.stream()
                  .collect(
                      Collectors.toMap(
                          ProfileEntity::getId,
                          profileEntity ->
                              convertEntityToDtoProfile(
                                  profileEntity,
                                  Boolean.FALSE,
                                  Boolean.FALSE,
                                  Boolean.TRUE,
                                  null)));

      final List<RoleDtoPlatformProfile> platformProfiles =
          shouldIncludePlatforms
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> platformIdDtoMap.containsKey(prpe.getPlatform().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                          Collectors.mapping(
                              prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      e ->
                          RoleDtoPlatformProfile.builder()
                              .platform(e.getKey())
                              .profiles(e.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      final List<RoleDtoProfilePlatform> profilePlatforms =
          shouldIncludeProfiles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> profileIdDtoMap.containsKey(prpe.getProfile().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                          Collectors.mapping(
                              prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      e ->
                          RoleDtoProfilePlatform.builder()
                              .profile(e.getKey())
                              .platforms(e.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      roleDto.setPlatformProfiles(platformProfiles);
      roleDto.setProfilePlatforms(profilePlatforms);
    } else {
      roleDto.setPlatformProfiles(Collections.emptyList());
      roleDto.setProfilePlatforms(Collections.emptyList());
    }

    return roleDto;
  }

  private List<RoleDto> convertEntitiesToDtosRoles(
      final List<RoleEntity> roleEntities,
      final boolean isIncludePermissions,
      final boolean isIncludePlatforms,
      final boolean isIncludeProfiles) {
    if (CollectionUtils.isEmpty(roleEntities)) {
      return Collections.emptyList();
    }

    final List<PermissionEntity> permissionEntities;
    if (isIncludePermissions && CommonUtils.canReadPermissions()) {
      final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
      permissionEntities = permissionService.readPermissionsByRoleIds(roleIds);
    } else {
      permissionEntities = Collections.emptyList();
    }

    if ((isIncludePlatforms || isIncludeProfiles)
        && (CommonUtils.canReadPlatforms() || CommonUtils.canReadPermissions())) {
      final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByRoleIds(roleIds);

      List<PlatformEntity> platformEntities = Collections.emptyList();
      List<ProfileEntity> profileEntities = Collections.emptyList();

      final boolean shouldIncludePlatforms = isIncludePlatforms && CommonUtils.canReadPlatforms();
      final boolean shouldIncludeProfiles = isIncludeProfiles && CommonUtils.canReadProfiles();

      final boolean shouldIncludeProfilesDueToPlatforms =
          shouldIncludePlatforms && CommonUtils.canReadProfiles();
      final boolean shouldIncludePlatformsDueToProfiles =
          shouldIncludeProfiles && CommonUtils.canReadPlatforms();

      if (shouldIncludePlatforms || shouldIncludePlatformsDueToProfiles) {
        platformEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getPlatform)
                .distinct()
                .toList();
      }

      if (shouldIncludeProfiles || shouldIncludeProfilesDueToPlatforms) {
        profileEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getProfile)
                .distinct()
                .toList();
      }

      final Map<Long, PlatformDto> platformIdDtoMap =
          platformEntities.stream()
              .collect(
                  Collectors.toMap(
                      PlatformEntity::getId,
                      platformEntity ->
                          convertEntityToDtoPlatform(
                              platformEntity, Boolean.FALSE, Boolean.FALSE, null)));
      final Map<Long, ProfileDto> profileIdDtoMap =
          profileEntities.stream()
              .collect(
                  Collectors.toMap(
                      ProfileEntity::getId,
                      profileEntity ->
                          convertEntityToDtoProfile(
                              profileEntity, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, null)));

      final Map<Long, List<RoleDtoPlatformProfile>> roleIdPlatformProfilesMap =
          shouldIncludePlatforms
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> platformIdDtoMap.containsKey(prpe.getPlatform().getId()))
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
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

      final Map<Long, List<RoleDtoProfilePlatform>> roleIdProfilePlatformsMap =
          shouldIncludeProfiles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> profileIdDtoMap.containsKey(prpe.getProfile().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> prpe.getRole().getId(),
                          Collectors.collectingAndThen(
                              Collectors.groupingBy(
                                  prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                                  Collectors.mapping(
                                      prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                                      Collectors.toList())),
                              map ->
                                  map.entrySet().stream()
                                      .map(
                                          e ->
                                              RoleDtoProfilePlatform.builder()
                                                  .profile(e.getKey())
                                                  .platforms(e.getValue())
                                                  .build())
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

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
                    convertEntityToDtoRole(
                        roleEntity, permissionEntitiesRole, Boolean.FALSE, Boolean.FALSE, null);
                final List<RoleDtoPlatformProfile> platformProfiles =
                    roleIdPlatformProfilesMap.getOrDefault(
                        roleDto.getId(), Collections.emptyList());
                final List<RoleDtoProfilePlatform> profilePlatforms =
                    roleIdProfilePlatformsMap.getOrDefault(
                        roleDto.getId(), Collections.emptyList());
                roleDto.setPlatformProfiles(platformProfiles);
                roleDto.setProfilePlatforms(profilePlatforms);
                return roleDto;
              })
          .toList();
    } else {
      return roleEntities.stream()
          .map(
              roleEntity -> {
                final List<PermissionEntity> permissionEntitiesRole =
                    CommonUtils.canReadPermissions()
                        ? permissionEntities.stream()
                            .filter(
                                permissionEntity ->
                                    Objects.equals(
                                        permissionEntity.getRole().getId(), roleEntity.getId()))
                            .toList()
                        : Collections.emptyList();
                return convertEntityToDtoRole(
                    roleEntity, permissionEntitiesRole, Boolean.FALSE, Boolean.FALSE, null);
              })
          .toList();
    }
  }

  public ResponseEntity<RoleResponse> getResponseSingleRole(
      final RoleEntity roleEntity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final RequestMetadata requestMetadata,
      final AuditResponse auditResponse) {
    final List<PermissionEntity> permissionEntitiesRole =
        (roleEntity == null || roleEntity.getId() == null)
            ? Collections.emptyList()
            : permissionService.readPermissionsByRoleIds(List.of(roleEntity.getId()));
    final List<RoleDto> roleDtos =
        (roleEntity == null || roleEntity.getId() == null)
            ? Collections.emptyList()
            : List.of(
                convertEntityToDtoRole(
                    roleEntity, permissionEntitiesRole, Boolean.TRUE, Boolean.TRUE, auditResponse));
    return new ResponseEntity<>(
        RoleResponse.builder()
            .roles(roleDtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(roleEntity),
                    responseCrudInfo == null
                        ? ResponseMetadata.emptyResponseCrudInfo()
                        : responseCrudInfo,
                    ResponseMetadata.emptyResponsePageInfo()))
            .requestMetadata(requestMetadata)
            .build(),
        getHttpStatusForSingleResponse(roleEntity));
  }

  public ResponseEntity<RoleResponse> getResponseMultipleRoles(
      final List<RoleEntity> roleEntities,
      final boolean isIncludePermissions,
      final boolean isIncludePlatforms,
      final boolean isIncludeProfiles,
      final ResponseMetadata.ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    final List<RoleDto> roleDtos =
        convertEntitiesToDtosRoles(
            roleEntities, isIncludePermissions, isIncludePlatforms, isIncludeProfiles);
    return ResponseEntity.ok(
        RoleResponse.builder()
            .roles(roleDtos)
            .responseMetadata(
                new ResponseMetadata(
                    ResponseMetadata.emptyResponseStatusInfo(),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    responsePageInfo))
            .requestMetadata(requestMetadata)
            .build());
  }

  public ResponseEntity<RoleResponse> getResponseErrorRole(final Exception exception) {
    return new ResponseEntity<>(
        RoleResponse.builder()
            .roles(Collections.emptyList())
            .responseMetadata(
                new ResponseMetadata(
                    new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // platform
  private PlatformDto convertEntityToDtoPlatform(
      final PlatformEntity platformEntity,
      final boolean isIncludeProfiles,
      final boolean isIncludeRoles,
      final AuditResponse auditResponse) {
    if (platformEntity == null) {
      return null;
    }

    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    platformDto.setAuditResponse(auditResponse);

    if ((isIncludeProfiles || isIncludeRoles)
        && (CommonUtils.canReadProfiles() || CommonUtils.canReadRoles())) {
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByPlatformIds(
              List.of(platformEntity.getId()));

      List<ProfileEntity> profileEntities = Collections.emptyList();
      List<RoleEntity> roleEntities = Collections.emptyList();

      final boolean shouldIncludeProfiles = isIncludeProfiles && CommonUtils.canReadProfiles();
      final boolean shouldIncludeRoles = isIncludeRoles && CommonUtils.canReadRoles();

      final boolean shouldIncludeProfilesDueToRoles =
          shouldIncludeRoles && CommonUtils.canReadProfiles();
      final boolean shouldIncludeRolesDueToProfiles =
          shouldIncludeProfiles && CommonUtils.canReadRoles();

      if (shouldIncludeProfiles || shouldIncludeProfilesDueToRoles) {
        profileEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getProfile)
                .distinct()
                .toList();
      }

      if (shouldIncludeRoles || shouldIncludeRolesDueToProfiles) {
        roleEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getRole)
                .distinct()
                .toList();
      }

      final Map<Long, ProfileDto> profileIdDtoMap =
          profileEntities.stream()
              .collect(
                  Collectors.toMap(
                      ProfileEntity::getId,
                      profileEntity ->
                          convertEntityToDtoProfile(
                              profileEntity, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null)));
      final Map<Long, RoleDto> roleIdDtoMap =
          roleEntities.stream()
              .collect(
                  Collectors.toMap(
                      RoleEntity::getId,
                      roleEntity ->
                          convertEntityToDtoRole(
                              roleEntity,
                              Collections.emptyList(),
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null)));

      final List<PlatformDtoProfileRole> profileRoles =
          shouldIncludeProfiles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> profileIdDtoMap.containsKey(prpe.getProfile().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                          Collectors.mapping(
                              prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      e ->
                          PlatformDtoProfileRole.builder()
                              .profile(e.getKey())
                              .roles(e.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      final List<PlatformDtoRoleProfile> roleProfiles =
          shouldIncludeRoles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> roleIdDtoMap.containsKey(prpe.getRole().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                          Collectors.mapping(
                              prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      e ->
                          PlatformDtoRoleProfile.builder()
                              .role(e.getKey())
                              .profiles(e.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      platformDto.setProfileRoles(profileRoles);
      platformDto.setRoleProfiles(roleProfiles);
    } else {
      platformDto.setProfileRoles(Collections.emptyList());
      platformDto.setRoleProfiles(Collections.emptyList());
    }

    return platformDto;
  }

  private List<PlatformDto> convertEntitiesToDtosPlatforms(
      final List<PlatformEntity> platformEntities,
      final boolean isIncludeProfiles,
      final boolean isIncludeRoles) {
    if (CollectionUtils.isEmpty(platformEntities)) {
      return Collections.emptyList();
    }

    if ((isIncludeProfiles || isIncludeRoles)
        && (CommonUtils.canReadProfiles() || CommonUtils.canReadRoles())) {
      final List<Long> platformIds = platformEntities.stream().map(PlatformEntity::getId).toList();
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByPlatformIds(platformIds);

      List<ProfileEntity> profileEntities = Collections.emptyList();
      List<RoleEntity> roleEntities = Collections.emptyList();

      final boolean shouldIncludeProfiles = isIncludeProfiles && CommonUtils.canReadProfiles();
      final boolean shouldIncludeRoles = isIncludeRoles && CommonUtils.canReadRoles();

      final boolean shouldIncludeProfilesDueToRoles =
          shouldIncludeRoles && CommonUtils.canReadProfiles();
      final boolean shouldIncludeRolesDueToProfiles =
          shouldIncludeProfiles && CommonUtils.canReadRoles();

      if (shouldIncludeProfiles || shouldIncludeProfilesDueToRoles) {
        profileEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getProfile)
                .distinct()
                .toList();
      }

      if (shouldIncludeRoles || shouldIncludeRolesDueToProfiles) {
        roleEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getRole)
                .distinct()
                .toList();
      }

      final Map<Long, ProfileDto> profileIdDtoMap =
          profileEntities.stream()
              .collect(
                  Collectors.toMap(
                      ProfileEntity::getId,
                      profileEntity ->
                          convertEntityToDtoProfile(
                              profileEntity, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null)));
      final Map<Long, RoleDto> roleIdDtoMap =
          roleEntities.stream()
              .collect(
                  Collectors.toMap(
                      RoleEntity::getId,
                      roleEntity ->
                          convertEntityToDtoRole(
                              roleEntity,
                              Collections.emptyList(),
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null)));
      final Map<Long, List<PlatformDtoProfileRole>> platformIdProfileRolesMap =
          shouldIncludeProfiles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> profileIdDtoMap.containsKey(prpe.getProfile().getId()))
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
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

      final Map<Long, List<PlatformDtoRoleProfile>> platformIdRoleProfilesMap =
          shouldIncludeRoles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> roleIdDtoMap.containsKey(prpe.getRole().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> prpe.getPlatform().getId(),
                          Collectors.collectingAndThen(
                              Collectors.groupingBy(
                                  prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                                  Collectors.mapping(
                                      prpe -> profileIdDtoMap.get(prpe.getProfile().getId()),
                                      Collectors.toList())),
                              map ->
                                  map.entrySet().stream()
                                      .map(
                                          e ->
                                              PlatformDtoRoleProfile.builder()
                                                  .role(e.getKey())
                                                  .profiles(e.getValue())
                                                  .build())
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

      return platformEntities.stream()
          .map(
              platformEntity -> {
                final PlatformDto platformDto =
                    convertEntityToDtoPlatform(platformEntity, Boolean.FALSE, Boolean.FALSE, null);
                final List<PlatformDtoProfileRole> profileRoles =
                    platformIdProfileRolesMap.getOrDefault(
                        platformDto.getId(), Collections.emptyList());
                final List<PlatformDtoRoleProfile> roleProfiles =
                    platformIdRoleProfilesMap.getOrDefault(
                        platformDto.getId(), Collections.emptyList());
                platformDto.setProfileRoles(profileRoles);
                platformDto.setRoleProfiles(roleProfiles);
                return platformDto;
              })
          .toList();
    } else {
      return platformEntities.stream()
          .map(
              platformEntity ->
                  convertEntityToDtoPlatform(platformEntity, Boolean.FALSE, Boolean.FALSE, null))
          .toList();
    }
  }

  public ResponseEntity<PlatformResponse> getResponseSinglePlatform(
      final PlatformEntity platformEntity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final RequestMetadata requestMetadata,
      final AuditResponse auditResponse) {
    final List<PlatformDto> platformDtos =
        (platformEntity == null || platformEntity.getId() == null)
            ? Collections.emptyList()
            : List.of(
                convertEntityToDtoPlatform(
                    platformEntity, Boolean.TRUE, Boolean.TRUE, auditResponse));
    return new ResponseEntity<>(
        PlatformResponse.builder()
            .platforms(platformDtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(platformEntity),
                    responseCrudInfo == null
                        ? ResponseMetadata.emptyResponseCrudInfo()
                        : responseCrudInfo,
                    ResponseMetadata.emptyResponsePageInfo()))
            .requestMetadata(requestMetadata)
            .build(),
        getHttpStatusForSingleResponse(platformEntity));
  }

  public ResponseEntity<PlatformResponse> getResponseMultiplePlatforms(
      final List<PlatformEntity> platformEntities,
      final boolean isIncludeProfiles,
      final boolean isIncludeRoles,
      final ResponseMetadata.ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(
                convertEntitiesToDtosPlatforms(platformEntities, isIncludeProfiles, isIncludeRoles))
            .responseMetadata(
                new ResponseMetadata(
                    ResponseMetadata.emptyResponseStatusInfo(),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    responsePageInfo))
            .requestMetadata(requestMetadata)
            .build());
  }

  public ResponseEntity<PlatformResponse> getResponseErrorPlatform(final Exception exception) {
    return new ResponseEntity<>(
        PlatformResponse.builder()
            .platforms(Collections.emptyList())
            .responseMetadata(
                new ResponseMetadata(
                    new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    ResponseMetadata.emptyResponsePageInfo()))
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

  private ProfileDto convertEntityToDtoProfile(
      final ProfileEntity profileEntity,
      final boolean isIncludeRoles,
      final boolean isIncludePlatforms,
      final boolean isIncludeAddress,
      final AuditResponse auditResponse) {
    if (profileEntity == null) {
      return null;
    }
    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "profileAddress");
    profileDto.setAuditResponse(auditResponse);

    if (isIncludeAddress && profileEntity.getProfileAddress() != null) {
      profileDto.setProfileAddress(
          convertEntityToDtoProfileAddress(profileEntity.getProfileAddress()));
    }

    if ((isIncludeRoles || isIncludePlatforms)
        && (CommonUtils.canReadRoles() || CommonUtils.canReadPlatforms())) {
      List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByProfileIds(
              List.of(profileEntity.getId()));

      List<RoleEntity> roleEntities = Collections.emptyList();
      List<PlatformEntity> platformEntities = Collections.emptyList();

      final boolean shouldIncludeRoles = isIncludeRoles && CommonUtils.canReadRoles();
      final boolean shouldIncludePlatforms = isIncludePlatforms && CommonUtils.canReadPlatforms();

      final boolean shouldIncludeRolesDueToPlatforms =
          shouldIncludePlatforms && CommonUtils.canReadRoles();
      final boolean shouldIncludePlatformsDueToRoles =
          shouldIncludeRoles && CommonUtils.canReadPlatforms();

      if (shouldIncludeRoles || shouldIncludeRolesDueToPlatforms) {
        roleEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getRole)
                .distinct()
                .toList();
      }

      if (shouldIncludePlatforms || shouldIncludePlatformsDueToRoles) {
        platformEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getPlatform)
                .distinct()
                .toList();
      }

      final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
      final List<PermissionEntity> permissionEntities =
          roleIds.isEmpty()
              ? Collections.emptyList()
              : permissionService.readPermissionsByRoleIds(roleIds);

      final Map<Long, PlatformDto> platformIdDtoMap =
          platformEntities.stream()
              .collect(
                  Collectors.toMap(
                      PlatformEntity::getId,
                      platformEntity ->
                          convertEntityToDtoPlatform(
                              platformEntity, Boolean.FALSE, Boolean.FALSE, null)));
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
                        return convertEntityToDtoRole(
                            roleEntity, permissionEntitiesRole, Boolean.FALSE, Boolean.FALSE, null);
                      }));

      final List<ProfileDtoPlatformRole> platformRoles =
          shouldIncludePlatforms
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> platformIdDtoMap.containsKey(prpe.getPlatform().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                          Collectors.mapping(
                              prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      entry ->
                          ProfileDtoPlatformRole.builder()
                              .platform(entry.getKey())
                              .roles(entry.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      final List<ProfileDtoRolePlatform> rolePlatforms =
          shouldIncludeRoles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> roleIdDtoMap.containsKey(prpe.getRole().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                          Collectors.mapping(
                              prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                              Collectors.toList())))
                  .entrySet()
                  .stream()
                  .map(
                      entry ->
                          ProfileDtoRolePlatform.builder()
                              .role(entry.getKey())
                              .platforms(entry.getValue())
                              .build())
                  .toList()
              : Collections.emptyList();

      profileDto.setPlatformRoles(platformRoles);
      profileDto.setRolePlatforms(rolePlatforms);
    } else {
      profileDto.setPlatformRoles(Collections.emptyList());
      profileDto.setRolePlatforms(Collections.emptyList());
    }

    return profileDto;
  }

  private List<ProfileDto> convertEntitiesToDtosProfiles(
      final List<ProfileEntity> profileEntities,
      final boolean isIncludeRoles,
      final boolean isIncludePlatforms,
      final boolean isIncludeAddress) {
    if (CollectionUtils.isEmpty(profileEntities)) {
      return Collections.emptyList();
    }

    if ((isIncludeRoles || isIncludePlatforms)
        && (CommonUtils.canReadRoles() || CommonUtils.canReadPlatforms())) {
      final List<Long> profileIds = profileEntities.stream().map(ProfileEntity::getId).toList();
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleService.readPlatformProfileRolesByProfileIds(profileIds);

      List<RoleEntity> roleEntities = Collections.emptyList();
      List<PlatformEntity> platformEntities = Collections.emptyList();

      final boolean shouldIncludeRoles = isIncludeRoles && CommonUtils.canReadRoles();
      final boolean shouldIncludePlatforms = isIncludePlatforms && CommonUtils.canReadPlatforms();

      final boolean shouldIncludeRolesDueToPlatforms =
          shouldIncludePlatforms && CommonUtils.canReadRoles();
      final boolean shouldIncludePlatformsDueToRoles =
          shouldIncludeRoles && CommonUtils.canReadPlatforms();

      if (shouldIncludeRoles || shouldIncludeRolesDueToPlatforms) {
        roleEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getRole)
                .distinct()
                .toList();
      }

      if (shouldIncludePlatforms || shouldIncludePlatformsDueToRoles) {
        platformEntities =
            platformProfileRoleEntities.stream()
                .map(PlatformProfileRoleEntity::getPlatform)
                .distinct()
                .toList();
      }

      final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
      final List<PermissionEntity> permissionEntities =
          roleIds.isEmpty()
              ? Collections.emptyList()
              : permissionService.readPermissionsByRoleIds(roleIds);

      final Map<Long, PlatformDto> platformIdDtoMap =
          platformEntities.stream()
              .collect(
                  Collectors.toMap(
                      PlatformEntity::getId,
                      platformEntity ->
                          convertEntityToDtoPlatform(
                              platformEntity, Boolean.FALSE, Boolean.FALSE, null)));
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
                        return convertEntityToDtoRole(
                            roleEntity, permissionEntitiesRole, Boolean.FALSE, Boolean.FALSE, null);
                      }));

      final Map<Long, List<ProfileDtoPlatformRole>> profileIdPlatformRolesMap =
          shouldIncludePlatforms
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> platformIdDtoMap.containsKey(prpe.getPlatform().getId()))
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
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

      final Map<Long, List<ProfileDtoRolePlatform>> profileIdRolePlatformsMap =
          shouldIncludeRoles
              ? platformProfileRoleEntities.stream()
                  .filter(prpe -> roleIdDtoMap.containsKey(prpe.getRole().getId()))
                  .collect(
                      Collectors.groupingBy(
                          prpe -> prpe.getProfile().getId(),
                          Collectors.collectingAndThen(
                              Collectors.groupingBy(
                                  prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                                  Collectors.mapping(
                                      prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                                      Collectors.toList())),
                              map ->
                                  map.entrySet().stream()
                                      .map(
                                          e ->
                                              ProfileDtoRolePlatform.builder()
                                                  .role(e.getKey())
                                                  .platforms(e.getValue())
                                                  .build())
                                      .collect(Collectors.toList()))))
              : Collections.emptyMap();

      return profileEntities.stream()
          .map(
              profileEntity -> {
                // send isIncludeRoles as Boolean.FALSE here, roles already included
                final ProfileDto profileDto =
                    convertEntityToDtoProfile(
                        profileEntity, Boolean.FALSE, Boolean.FALSE, isIncludeAddress, null);
                final List<ProfileDtoPlatformRole> platformRoles =
                    profileIdPlatformRolesMap.getOrDefault(
                        profileDto.getId(), Collections.emptyList());
                final List<ProfileDtoRolePlatform> rolePlatforms =
                    profileIdRolePlatformsMap.getOrDefault(
                        profileDto.getId(), Collections.emptyList());
                profileDto.setPlatformRoles(platformRoles);
                profileDto.setRolePlatforms(rolePlatforms);
                return profileDto;
              })
          .toList();
    } else {
      return profileEntities.stream()
          .map(
              profileEntity ->
                  convertEntityToDtoProfile(
                      profileEntity, Boolean.FALSE, Boolean.FALSE, isIncludeAddress, null))
          .toList();
    }
  }

  public ResponseEntity<ProfileResponse> getResponseSingleProfile(
      final ProfileEntity profileEntity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final RequestMetadata requestMetadata,
      final AuditResponse auditResponse) {
    final List<ProfileDto> profileDtos =
        (profileEntity == null || profileEntity.getId() == null)
            ? Collections.emptyList()
            : List.of(
                convertEntityToDtoProfile(
                    profileEntity, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, auditResponse));
    return new ResponseEntity<>(
        ProfileResponse.builder()
            .profiles(profileDtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(profileEntity),
                    responseCrudInfo == null
                        ? ResponseMetadata.emptyResponseCrudInfo()
                        : responseCrudInfo,
                    ResponseMetadata.emptyResponsePageInfo()))
            .requestMetadata(requestMetadata)
            .build(),
        getHttpStatusForSingleResponse(profileEntity));
  }

  public ResponseEntity<ProfileResponse> getResponseMultipleProfiles(
      final List<ProfileEntity> profileEntities,
      final boolean isIncludeRoles,
      final boolean isIncludePlatforms,
      final boolean isIncludeAddress,
      final ResponseMetadata.ResponsePageInfo responsePageInfo,
      final RequestMetadata requestMetadata) {
    final List<ProfileDto> profileDtos =
        convertEntitiesToDtosProfiles(
            profileEntities, isIncludeRoles, isIncludePlatforms, isIncludeAddress);
    return ResponseEntity.ok(
        ProfileResponse.builder()
            .profiles(profileDtos)
            .responseMetadata(
                new ResponseMetadata(
                    ResponseMetadata.emptyResponseStatusInfo(),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    responsePageInfo))
            .requestMetadata(requestMetadata)
            .build());
  }

  public ResponseEntity<ProfileResponse> getResponseErrorProfile(final Exception exception) {
    return new ResponseEntity<>(
        ProfileResponse.builder()
            .profiles(Collections.emptyList())
            .responseMetadata(
                new ResponseMetadata(
                    new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // others
  public ResponseEntity<ProfilePasswordTokenResponse> getResponseErrorProfilePassword(
      final Exception exception) {
      final ResponseCookie refreshTokenCookieResponse = cookieService.buildRefreshCookie("", 0);
      final ResponseCookie csrfTokenCookieResponse = cookieService.buildCsrfCookie("", 0);
      final ProfilePasswordTokenResponse profilePasswordTokenResponse = ProfilePasswordTokenResponse.builder()
              .responseMetadata(
                      new ResponseMetadata(
                              new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                              ResponseMetadata.emptyResponseCrudInfo(),
                              ResponseMetadata.emptyResponsePageInfo()))
              .build();
      final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
      return ResponseEntity
              .status(httpStatus)
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookieResponse.toString())
              .header(HttpHeaders.SET_COOKIE, csrfTokenCookieResponse.toString())
              .body(profilePasswordTokenResponse);
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

  public ResponseEntity<ResponseWithMetadata> getResponseErrorResponseMetadata(
      final Exception exception) {
    final HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    return new ResponseEntity<>(
        new ResponseWithMetadata(
            new ResponseMetadata(
                new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                ResponseMetadata.emptyResponseCrudInfo(),
                ResponseMetadata.emptyResponsePageInfo())),
        httpStatus);
  }

  // TODO The following need to be consolidated with above methods to avoid duplication
  public ProfileDto convertEntityToDtoProfileBasic(
      final ProfileEntity profileEntity, final Long platformId) {
    if (profileEntity == null) {
      return null;
    }

    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "profileAddress");

    if (profileEntity.getProfileAddress() != null) {
      profileDto.setProfileAddress(
          convertEntityToDtoProfileAddress(profileEntity.getProfileAddress()));
    }

    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleService.readPlatformProfileRolesByPlatformIdAndProfileId(
            platformId, profileEntity.getId());
    List<RoleEntity> roleEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getRole)
            .distinct()
            .toList();
    List<PlatformEntity> platformEntities =
        platformProfileRoleEntities.stream()
            .map(PlatformProfileRoleEntity::getPlatform)
            .distinct()
            .toList();

    final List<Long> roleIds = roleEntities.stream().map(RoleEntity::getId).toList();
    final List<PermissionEntity> permissionEntities =
        roleIds.isEmpty()
            ? Collections.emptyList()
            : permissionService.readPermissionsByRoleIds(roleIds);

    final Map<Long, PlatformDto> platformIdDtoMap =
        platformEntities.stream()
            .filter(Objects::nonNull)
            .collect(
                Collectors.toMap(PlatformEntity::getId, this::convertEntityToDtoPlatformBasic));
    final Map<Long, RoleDto> roleIdDtoMap =
        roleEntities.stream()
            .filter(Objects::nonNull)
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
                      return convertEntityToDtoRoleBasic(roleEntity, permissionEntitiesRole);
                    }));

    final List<ProfileDtoPlatformRole> platformRoles =
        platformProfileRoleEntities.stream()
            .filter(prpe -> platformIdDtoMap.containsKey(prpe.getPlatform().getId()))
            .collect(
                Collectors.groupingBy(
                    prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                    Collectors.mapping(
                        prpe -> roleIdDtoMap.get(prpe.getRole().getId()), Collectors.toList())))
            .entrySet()
            .stream()
            .map(
                entry ->
                    ProfileDtoPlatformRole.builder()
                        .platform(entry.getKey())
                        .roles(entry.getValue())
                        .build())
            .toList();

    final List<ProfileDtoRolePlatform> rolePlatforms =
        platformProfileRoleEntities.stream()
            .filter(prpe -> roleIdDtoMap.containsKey(prpe.getRole().getId()))
            .collect(
                Collectors.groupingBy(
                    prpe -> roleIdDtoMap.get(prpe.getRole().getId()),
                    Collectors.mapping(
                        prpe -> platformIdDtoMap.get(prpe.getPlatform().getId()),
                        Collectors.toList())))
            .entrySet()
            .stream()
            .map(
                entry ->
                    ProfileDtoRolePlatform.builder()
                        .role(entry.getKey())
                        .platforms(entry.getValue())
                        .build())
            .toList();

    profileDto.setPlatformRoles(platformRoles);
    profileDto.setRolePlatforms(rolePlatforms);

    return profileDto;
  }

  private PlatformDto convertEntityToDtoPlatformBasic(final PlatformEntity platformEntity) {
    if (platformEntity == null) {
      return null;
    }

    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    platformDto.setProfileRoles(Collections.emptyList());
    platformDto.setRoleProfiles(Collections.emptyList());

    return platformDto;
  }

  private RoleDto convertEntityToDtoRoleBasic(
      final RoleEntity roleEntity, List<PermissionEntity> permissionEntitiesRole) {
    if (roleEntity == null) {
      return null;
    }

    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    roleDto.setPermissions(
        permissionEntitiesRole.stream().map(this::convertEntityToDtoPermissionBasic).toList());
    roleDto.setPlatformProfiles(Collections.emptyList());
    roleDto.setProfilePlatforms(Collections.emptyList());

    return roleDto;
  }

  private PermissionDto convertEntityToDtoPermissionBasic(final PermissionEntity permissionEntity) {
    if (permissionEntity == null) {
      return null;
    }

    PermissionDto permissionDto = new PermissionDto();
    BeanUtils.copyProperties(permissionEntity, permissionDto, "role");
    permissionDto.setRole(
        convertEntityToDtoRoleBasic(permissionEntity.getRole(), Collections.emptyList()));

    return permissionDto;
  }

  // Audit
  private AuditPermissionDto convertEntityToDtoPermissionAudit(
      final AuditPermissionEntity auditPermissionEntity) {
    if (auditPermissionEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(
            auditPermissionEntity.getCreatedBy(),
            Boolean.FALSE,
            Boolean.FALSE,
            Boolean.FALSE,
            null);
    final PermissionDto eventData =
        convertEntityToDtoPermission(auditPermissionEntity.getEventData(), null);
    final PermissionDto permission =
        convertEntityToDtoPermission(auditPermissionEntity.getPermission(), null);
    return new AuditPermissionDto(
        auditPermissionEntity.getId(),
        auditPermissionEntity.getEventType(),
        auditPermissionEntity.getEventDesc(),
        auditPermissionEntity.getCreatedAt(),
        createdBy,
        auditPermissionEntity.getIpAddress(),
        auditPermissionEntity.getUserAgent(),
        eventData,
        permission);
  }

  public List<AuditPermissionDto> convertEntityToDtoPermissionsAudit(
      final List<AuditPermissionEntity> auditPermissionEntities) {
    if (CommonUtilities.isEmpty(auditPermissionEntities)) {
      return Collections.emptyList();
    }
    return auditPermissionEntities.stream().map(this::convertEntityToDtoPermissionAudit).toList();
  }

  private AuditRoleDto convertEntityToDtoRoleAudit(final AuditRoleEntity auditRoleEntity) {
    if (auditRoleEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(
            auditRoleEntity.getCreatedBy(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    final RoleDto eventData =
        convertEntityToDtoRole(
            auditRoleEntity.getEventData(),
            Collections.emptyList(),
            Boolean.FALSE,
            Boolean.FALSE,
            null);
    final RoleDto role =
        convertEntityToDtoRole(
            auditRoleEntity.getRole(), Collections.emptyList(), Boolean.FALSE, Boolean.FALSE, null);
    return new AuditRoleDto(
        auditRoleEntity.getId(),
        auditRoleEntity.getEventType(),
        auditRoleEntity.getEventDesc(),
        auditRoleEntity.getCreatedAt(),
        createdBy,
        auditRoleEntity.getIpAddress(),
        auditRoleEntity.getUserAgent(),
        eventData,
        role);
  }

  public List<AuditRoleDto> convertEntityToDtoRolesAudit(
      final List<AuditRoleEntity> auditRoleEntities) {
    if (CommonUtilities.isEmpty(auditRoleEntities)) {
      return Collections.emptyList();
    }
    return auditRoleEntities.stream().map(this::convertEntityToDtoRoleAudit).toList();
  }

  private AuditPlatformDto convertEntityToDtoPlatformAudit(
      final AuditPlatformEntity auditPlatformEntity) {
    if (auditPlatformEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(
            auditPlatformEntity.getCreatedBy(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    final PlatformDto eventData =
        convertEntityToDtoPlatform(
            auditPlatformEntity.getEventData(), Boolean.FALSE, Boolean.FALSE, null);
    final PlatformDto platform =
        convertEntityToDtoPlatform(
            auditPlatformEntity.getPlatform(), Boolean.FALSE, Boolean.FALSE, null);
    return new AuditPlatformDto(
        auditPlatformEntity.getId(),
        auditPlatformEntity.getEventType(),
        auditPlatformEntity.getEventDesc(),
        auditPlatformEntity.getCreatedAt(),
        createdBy,
        auditPlatformEntity.getIpAddress(),
        auditPlatformEntity.getUserAgent(),
        eventData,
        platform);
  }

  public List<AuditPlatformDto> convertEntityToDtoPlatformsAudit(
      final List<AuditPlatformEntity> auditPlatformEntities) {
    if (CommonUtilities.isEmpty(auditPlatformEntities)) {
      return Collections.emptyList();
    }
    return auditPlatformEntities.stream().map(this::convertEntityToDtoPlatformAudit).toList();
  }

  private AuditProfileDto convertEntityToDtoProfileAudit(
      final AuditProfileEntity auditProfileEntity) {
    if (auditProfileEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(
            auditProfileEntity.getCreatedBy(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    final ProfileDto eventData =
        convertEntityToDtoProfile(
            auditProfileEntity.getEventData(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    final ProfileDto profile =
        convertEntityToDtoProfile(
            auditProfileEntity.getProfile(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    return new AuditProfileDto(
        auditProfileEntity.getId(),
        auditProfileEntity.getEventType(),
        auditProfileEntity.getEventDesc(),
        auditProfileEntity.getCreatedAt(),
        createdBy,
        auditProfileEntity.getIpAddress(),
        auditProfileEntity.getUserAgent(),
        eventData,
        profile);
  }

  public List<AuditProfileDto> convertEntityToDtoProfilesAudit(
      final List<AuditProfileEntity> auditProfileEntities) {
    if (CommonUtilities.isEmpty(auditProfileEntities)) {
      return Collections.emptyList();
    }
    return auditProfileEntities.stream().map(this::convertEntityToDtoProfileAudit).toList();
  }
}
