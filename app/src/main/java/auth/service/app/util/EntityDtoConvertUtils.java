package auth.service.app.util;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.exception.TokenInvalidException;
import auth.service.app.model.dto.AuditPermissionDto;
import auth.service.app.model.dto.AuditPlatformDto;
import auth.service.app.model.dto.AuditProfileDto;
import auth.service.app.model.dto.AuditRoleDto;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleDto;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.PlatformRolePermissionDto;
import auth.service.app.model.dto.ProfileAddressDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.PlatformRolePermissionService;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
public class EntityDtoConvertUtils {

  private final PlatformProfileRoleService pprService;
  private final PlatformRolePermissionService prpService;

  // HELPERS

  public HttpStatus getHttpStatusForErrorResponse(final Exception exception) {
    if (exception instanceof ElementNotFoundException) {
      return HttpStatus.NOT_FOUND;
    } else if (exception instanceof ElementMissingException) {
      return HttpStatus.BAD_REQUEST;
    } else if (exception instanceof ProfileForbiddenException
        || exception instanceof ProfileNotValidatedException
        || exception instanceof ElementNotActiveException
        || exception instanceof ProfileNotActiveException
        || exception instanceof ProfileLockedException
        || exception instanceof CheckPermissionException) {
      return HttpStatus.FORBIDDEN;
    } else if (exception instanceof ProfileNotAuthorizedException
        || exception instanceof TokenInvalidException) {
      return HttpStatus.UNAUTHORIZED;
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public <T> HttpStatus getHttpStatusForSingleResponse(final T object) {
    return ObjectUtils.isEmpty(object) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
  }

  public <T> ResponseMetadata.ResponseStatusInfo getResponseStatusInfoForSingleResponse(
      final T object) {
    return ObjectUtils.isEmpty(object)
        ? new ResponseMetadata.ResponseStatusInfo(ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE)
        : ResponseMetadata.emptyResponseStatusInfo();
  }

  public ResponseMetadata.ResponseCrudInfo getResponseCrudInfoForResponse(
      ResponseMetadata.ResponseCrudInfo responseCrudInfo) {
    return responseCrudInfo == null ? ResponseMetadata.emptyResponseCrudInfo() : responseCrudInfo;
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

  // AUDITS

  private AuditPermissionDto convertEntityToDtoPermissionAudit(
      final AuditPermissionEntity auditEntity) {
    if (auditEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(auditEntity.getCreatedBy(), null, Boolean.FALSE, Boolean.FALSE);
    final PermissionDto eventData =
        convertEntityToDtoPermission(
            auditEntity.getEventData(), null, Boolean.FALSE, Boolean.FALSE);

    return new AuditPermissionDto(
        auditEntity.getId(),
        auditEntity.getEventType(),
        auditEntity.getEventDesc(),
        auditEntity.getCreatedAt(),
        createdBy,
        auditEntity.getIpAddress(),
        auditEntity.getUserAgent(),
        eventData);
  }

  private AuditRoleDto convertEntityToDtoRoleAudit(final AuditRoleEntity auditEntity) {
    if (auditEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(auditEntity.getCreatedBy(), null, Boolean.FALSE, Boolean.FALSE);
    final RoleDto eventData =
        convertEntityToDtoRole(auditEntity.getEventData(), null, Boolean.FALSE, Boolean.FALSE);
    return new AuditRoleDto(
        auditEntity.getId(),
        auditEntity.getEventType(),
        auditEntity.getEventDesc(),
        auditEntity.getCreatedAt(),
        createdBy,
        auditEntity.getIpAddress(),
        auditEntity.getUserAgent(),
        eventData);
  }

  private AuditPlatformDto convertEntityToDtoPlatformAudit(final AuditPlatformEntity auditEntity) {
    if (auditEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(auditEntity.getCreatedBy(), null, Boolean.FALSE, Boolean.FALSE);
    final PlatformDto eventData =
        convertEntityToDtoPlatform(auditEntity.getEventData(), null, Boolean.FALSE, Boolean.FALSE);
    return new AuditPlatformDto(
        auditEntity.getId(),
        auditEntity.getEventType(),
        auditEntity.getEventDesc(),
        auditEntity.getCreatedAt(),
        createdBy,
        auditEntity.getIpAddress(),
        auditEntity.getUserAgent(),
        eventData);
  }

  private AuditProfileDto convertEntityToDtoProfileAudit(final AuditProfileEntity auditEntity) {
    if (auditEntity == null) {
      return null;
    }
    final ProfileDto createdBy =
        convertEntityToDtoProfile(auditEntity.getCreatedBy(), null, Boolean.FALSE, Boolean.FALSE);
    final ProfileDto eventData =
        convertEntityToDtoProfile(auditEntity.getEventData(), null, Boolean.FALSE, Boolean.FALSE);
    return new AuditProfileDto(
        auditEntity.getId(),
        auditEntity.getEventType(),
        auditEntity.getEventDesc(),
        auditEntity.getCreatedAt(),
        createdBy,
        auditEntity.getIpAddress(),
        auditEntity.getUserAgent(),
        eventData);
  }

  // PERMISSIONS

  private PermissionDto convertEntityToDtoPermission(
      final PermissionEntity entity,
      final List<AuditPermissionEntity> auditEntities,
      final boolean isIncludeExtras,
      final boolean isIncludeDeleted) {
    if (entity == null) {
      return null;
    }
    final PermissionDto dto = new PermissionDto();
    BeanUtils.copyProperties(entity, dto);

    if (CommonUtilities.isEmpty(auditEntities)) {
      dto.setHistory(Collections.emptyList());
    } else {
      final List<AuditPermissionDto> auditDtos =
          auditEntities.stream().map(this::convertEntityToDtoPermissionAudit).toList();
      dto.setHistory(auditDtos);
    }

    if (isIncludeExtras) {
      final List<PlatformRolePermissionEntity> prpEntities =
          prpService.readPlatformRolePermissionsByPermissionIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformRolePermissionDto> prpDtos =
          CommonUtilities.isEmpty(prpEntities)
              ? Collections.emptyList()
              : prpEntities.stream()
                  .map(
                      prpEntity -> {
                        final PlatformDto platformDto =
                            convertEntityToDtoPlatform(
                                prpEntity.getPlatform(), null, Boolean.FALSE, Boolean.FALSE);
                        final RoleDto roleDto =
                            convertEntityToDtoRole(
                                prpEntity.getRole(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformRolePermissionDto.builder()
                            .platform(platformDto)
                            .role(roleDto)
                            .assignedDate(prpEntity.getAssignedDate())
                            .unassignedDate(prpEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      dto.setPlatformRolePermissions(prpDtos);
    } else {
      dto.setPlatformRolePermissions(Collections.emptyList());
    }

    return dto;
  }

  public ResponseEntity<PermissionResponse> getResponseSinglePermission(
      final PermissionEntity entity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final List<AuditPermissionEntity> auditEntities) {
    final List<PermissionDto> dtos =
        (entity == null || entity.getId() == null)
            ? Collections.emptyList()
            : List.of(
                convertEntityToDtoPermission(entity, auditEntities, Boolean.TRUE, Boolean.TRUE));
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(dtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(entity),
                    getResponseCrudInfoForResponse(responseCrudInfo),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForSingleResponse(entity));
  }

  public ResponseEntity<PermissionResponse> getResponseMultiplePermissions(
      final List<PermissionEntity> entities) {
    List<PermissionDto> dtos;
    List<Long> platformIds;
    List<Long> roleIds;

    if (CommonUtilities.isEmpty(entities)) {
      dtos = Collections.emptyList();
      platformIds = Collections.emptyList();
      roleIds = Collections.emptyList();
    } else {
      dtos =
          entities.stream()
              .map(
                  entity ->
                      convertEntityToDtoPermission(entity, null, Boolean.FALSE, Boolean.FALSE))
              .toList();

      final List<PlatformRolePermissionEntity> pprEntities =
          prpService.readPlatformRolePermissionsByPermissionIds(
              entities.stream().map(PermissionEntity::getId).toList(), Boolean.TRUE);
      platformIds = pprEntities.stream().map(pprEntity -> pprEntity.getPlatform().getId()).toList();
      roleIds = pprEntities.stream().map(pprEntity -> pprEntity.getRole().getId()).toList();
    }

    return ResponseEntity.ok(
        PermissionResponse.builder()
            .permissions(dtos)
            .platformIds(platformIds)
            .roleIds(roleIds)
            .responseMetadata(ResponseMetadata.emptyResponseMetadata())
            .build());
  }

  public ResponseEntity<PermissionResponse> getResponseErrorPermission(final Exception exception) {
    return new ResponseEntity<>(
        PermissionResponse.builder()
            .permissions(Collections.emptyList())
            .platformIds(Collections.emptyList())
            .roleIds(Collections.emptyList())
            .responseMetadata(
                new ResponseMetadata(
                    new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                    ResponseMetadata.emptyResponseCrudInfo(),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForErrorResponse(exception));
  }

  // ROLE

  private RoleDto convertEntityToDtoRole(
      final RoleEntity entity,
      final List<AuditRoleEntity> auditEntities,
      final boolean isIncludeExtras,
      final boolean isIncludeDeleted) {

    if (entity == null) {
      return null;
    }

    final RoleDto dto = new RoleDto();
    BeanUtils.copyProperties(entity, dto);

    if (CommonUtilities.isEmpty(auditEntities)) {
      dto.setHistory(Collections.emptyList());
    } else {
      final List<AuditRoleDto> auditDtos =
          auditEntities.stream().map(this::convertEntityToDtoRoleAudit).toList();
      dto.setHistory(auditDtos);
    }

    if (isIncludeExtras) {
      final List<PlatformRolePermissionEntity> prpEntities =
          prpService.readPlatformRolePermissionsByRoleIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformProfileRoleEntity> pprEntities =
          pprService.readPlatformProfileRolesByRoleIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformRolePermissionDto> prpDtos =
          CommonUtilities.isEmpty(prpEntities)
              ? Collections.emptyList()
              : prpEntities.stream()
                  .map(
                      prpEntity -> {
                        final PlatformDto platformDto =
                            convertEntityToDtoPlatform(
                                prpEntity.getPlatform(), null, Boolean.FALSE, Boolean.FALSE);
                        final PermissionDto permissionDto =
                            convertEntityToDtoPermission(
                                prpEntity.getPermission(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformRolePermissionDto.builder()
                            .platform(platformDto)
                            .permission(permissionDto)
                            .assignedDate(prpEntity.getAssignedDate())
                            .unassignedDate(prpEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      final List<PlatformProfileRoleDto> pprDtos =
          CommonUtilities.isEmpty(pprEntities)
              ? Collections.emptyList()
              : pprEntities.stream()
                  .map(
                      pprEntity -> {
                        final PlatformDto platformDto =
                            convertEntityToDtoPlatform(
                                pprEntity.getPlatform(), null, Boolean.FALSE, Boolean.FALSE);
                        final ProfileDto profileDto =
                            convertEntityToDtoProfile(
                                pprEntity.getProfile(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformProfileRoleDto.builder()
                            .platform(platformDto)
                            .profile(profileDto)
                            .assignedDate(pprEntity.getAssignedDate())
                            .unassignedDate(pprEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      dto.setPlatformRolePermissions(prpDtos);
      dto.setPlatformProfileRoles(pprDtos);
    } else {
      dto.setPlatformRolePermissions(Collections.emptyList());
      dto.setPlatformProfileRoles(Collections.emptyList());
    }

    return dto;
  }

  public ResponseEntity<RoleResponse> getResponseSingleRole(
      final RoleEntity entity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final List<AuditRoleEntity> auditEntities) {
    final List<RoleDto> dtos =
        (entity == null || entity.getId() == null)
            ? Collections.emptyList()
            : List.of(convertEntityToDtoRole(entity, auditEntities, Boolean.TRUE, Boolean.TRUE));
    return new ResponseEntity<>(
        RoleResponse.builder()
            .roles(dtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(entity),
                    getResponseCrudInfoForResponse(responseCrudInfo),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForSingleResponse(entity));
  }

  public ResponseEntity<RoleResponse> getResponseMultipleRoles(final List<RoleEntity> entities) {
    List<RoleDto> dtos;

    if (CommonUtilities.isEmpty(entities)) {
      dtos = Collections.emptyList();
    } else {
      dtos =
          entities.stream()
              .map(entity -> convertEntityToDtoRole(entity, null, Boolean.FALSE, Boolean.FALSE))
              .toList();
    }

    return ResponseEntity.ok(
        RoleResponse.builder()
            .roles(dtos)
            .responseMetadata(ResponseMetadata.emptyResponseMetadata())
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

  // PLATFORM

  private PlatformDto convertEntityToDtoPlatform(
      final PlatformEntity entity,
      final List<AuditPlatformEntity> auditEntities,
      final boolean isIncludeExtras,
      final boolean isIncludeDeleted) {

    if (entity == null) {
      return null;
    }

    final PlatformDto dto = new PlatformDto();
    BeanUtils.copyProperties(entity, dto);

    if (CommonUtilities.isEmpty(auditEntities)) {
      dto.setHistory(Collections.emptyList());
    } else {
      final List<AuditPlatformDto> auditDtos =
          auditEntities.stream().map(this::convertEntityToDtoPlatformAudit).toList();
      dto.setHistory(auditDtos);
    }

    if (isIncludeExtras) {
      final List<PlatformRolePermissionEntity> prpEntities =
          prpService.readPlatformRolePermissionsByPlatformIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformProfileRoleEntity> pprEntities =
          pprService.readPlatformProfileRolesByPlatformIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformRolePermissionDto> prpDtos =
          CommonUtilities.isEmpty(prpEntities)
              ? Collections.emptyList()
              : prpEntities.stream()
                  .map(
                      prpEntity -> {
                        final RoleDto roleDto =
                            convertEntityToDtoRole(
                                prpEntity.getRole(), null, Boolean.FALSE, Boolean.FALSE);
                        final PermissionDto permissionDto =
                            convertEntityToDtoPermission(
                                prpEntity.getPermission(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformRolePermissionDto.builder()
                            .role(roleDto)
                            .permission(permissionDto)
                            .assignedDate(prpEntity.getAssignedDate())
                            .unassignedDate(prpEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      final List<PlatformProfileRoleDto> pprDtos =
          CommonUtilities.isEmpty(pprEntities)
              ? Collections.emptyList()
              : pprEntities.stream()
                  .map(
                      pprEntity -> {
                        final RoleDto roleDto =
                            convertEntityToDtoRole(
                                pprEntity.getRole(), null, Boolean.FALSE, Boolean.FALSE);
                        final ProfileDto profileDto =
                            convertEntityToDtoProfile(
                                pprEntity.getProfile(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformProfileRoleDto.builder()
                            .role(roleDto)
                            .profile(profileDto)
                            .assignedDate(pprEntity.getAssignedDate())
                            .unassignedDate(pprEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      dto.setPlatformRolePermissions(prpDtos);
      dto.setPlatformProfileRoles(pprDtos);
    } else {
      dto.setPlatformRolePermissions(Collections.emptyList());
      dto.setPlatformProfileRoles(Collections.emptyList());
    }

    return dto;
  }

  public ResponseEntity<PlatformResponse> getResponseSinglePlatform(
      final PlatformEntity entity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final List<AuditPlatformEntity> auditEntities) {
    final List<PlatformDto> dtos =
        (entity == null || entity.getId() == null)
            ? Collections.emptyList()
            : List.of(
                convertEntityToDtoPlatform(entity, auditEntities, Boolean.TRUE, Boolean.TRUE));
    return new ResponseEntity<>(
        PlatformResponse.builder()
            .platforms(dtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(entity),
                    getResponseCrudInfoForResponse(responseCrudInfo),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForSingleResponse(entity));
  }

  public ResponseEntity<PlatformResponse> getResponseMultiplePlatforms(
      final List<PlatformEntity> entities) {
    List<PlatformDto> dtos;

    if (CommonUtilities.isEmpty(entities)) {
      dtos = Collections.emptyList();
    } else {
      dtos =
          entities.stream()
              .map(entity -> convertEntityToDtoPlatform(entity, null, Boolean.FALSE, Boolean.FALSE))
              .toList();
    }

    return ResponseEntity.ok(
        PlatformResponse.builder()
            .platforms(dtos)
            .responseMetadata(ResponseMetadata.emptyResponseMetadata())
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

  // PROFILE

  private ProfileDto convertEntityToDtoProfile(
      final ProfileEntity entity,
      final List<AuditProfileEntity> auditEntities,
      final boolean isIncludeExtras,
      final boolean isIncludeDeleted) {

    if (entity == null) {
      return null;
    }

    final ProfileDto dto = new ProfileDto();
    BeanUtils.copyProperties(entity, dto, "profileAddress");

    if (entity.getProfileAddress() != null) {
      final ProfileAddressDto profileAddressDto = new ProfileAddressDto();
      BeanUtils.copyProperties(entity.getProfileAddress(), profileAddressDto);
      dto.setProfileAddress(profileAddressDto);
    }

    if (CommonUtilities.isEmpty(auditEntities)) {
      dto.setHistory(Collections.emptyList());
    } else {
      final List<AuditProfileDto> auditDtos =
          auditEntities.stream().map(this::convertEntityToDtoProfileAudit).toList();
      dto.setHistory(auditDtos);
    }

    if (isIncludeExtras) {
      final List<PlatformProfileRoleEntity> pprEntities =
          pprService.readPlatformProfileRolesByPlatformIds(
              Collections.singletonList(entity.getId()), isIncludeDeleted);
      final List<PlatformProfileRoleDto> pprDtos =
          CommonUtilities.isEmpty(pprEntities)
              ? Collections.emptyList()
              : pprEntities.stream()
                  .map(
                      pprEntity -> {
                        final PlatformDto platformDto =
                            convertEntityToDtoPlatform(
                                pprEntity.getPlatform(), null, Boolean.FALSE, Boolean.FALSE);
                        final RoleDto roleDto =
                            convertEntityToDtoRole(
                                pprEntity.getRole(), null, Boolean.FALSE, Boolean.FALSE);
                        return PlatformProfileRoleDto.builder()
                            .role(roleDto)
                            .platform(platformDto)
                            .assignedDate(pprEntity.getAssignedDate())
                            .unassignedDate(pprEntity.getUnassignedDate())
                            .build();
                      })
                  .toList();
      dto.setPlatformProfileRoles(pprDtos);
    } else {
      dto.setPlatformProfileRoles(Collections.emptyList());
    }

    return dto;
  }

  public ResponseEntity<ProfileResponse> getResponseSingleProfile(
      final ProfileEntity entity,
      final ResponseMetadata.ResponseCrudInfo responseCrudInfo,
      final List<AuditProfileEntity> auditEntities) {
    final List<ProfileDto> dtos =
        (entity == null || entity.getId() == null)
            ? Collections.emptyList()
            : List.of(convertEntityToDtoProfile(entity, auditEntities, Boolean.TRUE, Boolean.TRUE));
    return new ResponseEntity<>(
        ProfileResponse.builder()
            .profiles(dtos)
            .responseMetadata(
                new ResponseMetadata(
                    getResponseStatusInfoForSingleResponse(entity),
                    getResponseCrudInfoForResponse(responseCrudInfo),
                    ResponseMetadata.emptyResponsePageInfo()))
            .build(),
        getHttpStatusForSingleResponse(entity));
  }

  public ResponseEntity<ProfileResponse> getResponseMultipleProfiles(
      final List<ProfileEntity> entities) {
    List<ProfileDto> dtos;
    List<Long> platformIds;
    List<Long> roleIds;

    if (CommonUtilities.isEmpty(entities)) {
      dtos = Collections.emptyList();
      platformIds = Collections.emptyList();
      roleIds = Collections.emptyList();
    } else {
      dtos =
          entities.stream()
              .map(entity -> convertEntityToDtoProfile(entity, null, Boolean.FALSE, Boolean.FALSE))
              .toList();

      final List<PlatformProfileRoleEntity> pprEntities =
          pprService.readPlatformProfileRolesByProfileIds(
              entities.stream().map(ProfileEntity::getId).toList(), Boolean.TRUE);
      platformIds = pprEntities.stream().map(pprEntity -> pprEntity.getPlatform().getId()).toList();
      roleIds = pprEntities.stream().map(pprEntity -> pprEntity.getRole().getId()).toList();
    }

    return ResponseEntity.ok(
        ProfileResponse.builder()
            .profiles(dtos)
            .platformIds(platformIds)
            .roleIds(roleIds)
            .responseMetadata(ResponseMetadata.emptyResponseMetadata())
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
}
