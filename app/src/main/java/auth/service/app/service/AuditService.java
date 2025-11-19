package auth.service.app.service;

import static auth.service.app.util.CommonUtils.getIpAddress;
import static auth.service.app.util.CommonUtils.getUserAgent;

import auth.service.app.model.dto.AuditPermissionDto;
import auth.service.app.model.dto.AuditPlatformDto;
import auth.service.app.model.dto.AuditProfileDto;
import auth.service.app.model.dto.AuditResponse;
import auth.service.app.model.dto.AuditRoleDto;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.AuditPermissionRepository;
import auth.service.app.repository.AuditPlatformRepository;
import auth.service.app.repository.AuditProfileRepository;
import auth.service.app.repository.AuditRoleRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.JpaDataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditPermissionRepository auditPermissionRepository;
  private final AuditRoleRepository auditRoleRepository;
  private final AuditPlatformRepository auditPlatformRepository;
  private final AuditProfileRepository auditProfileRepository;
  private final ProfileRepository profileRepository;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  private ProfileEntity getCreatedByProfileEntity() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getCredentials() != null
        && authentication.getCredentials() instanceof AuthToken authToken) {
      return profileRepository.findById(authToken.getProfile().getId()).orElse(null);
    }
    return null;
  }

  public void auditPermission(
      final HttpServletRequest request,
      final PermissionEntity permissionEntity,
      final AuditEnums.AuditPermission eventType,
      final String eventDesc) {
    try {
      AuditPermissionEntity auditPermissionEntity = new AuditPermissionEntity();
      auditPermissionEntity.setEventType(eventType.name());
      auditPermissionEntity.setEventDesc(eventDesc);
      auditPermissionEntity.setEventData(permissionEntity);
      auditPermissionEntity.setPermission(permissionEntity);

      auditPermissionEntity.setCreatedBy(getCreatedByProfileEntity());
      auditPermissionEntity.setCreatedAt(LocalDateTime.now());
      auditPermissionEntity.setIpAddress(getIpAddress(request));
      auditPermissionEntity.setUserAgent(getUserAgent(request));

      auditPermissionRepository.save(auditPermissionEntity);
    } catch (Exception ex) {
      log.error(
          "AuditPermissionException: [{}], [{}], [{}]",
          permissionEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  public AuditResponse auditPermissions(
      final RequestMetadata requestMetadata, final Long permissionId) {
    final Page<AuditPermissionEntity> auditEntityPage =
        auditPermissionRepository.findByPermissionId(
            permissionId, JpaDataUtils.getQueryPageableAudit(requestMetadata));
    final ResponseMetadata.ResponsePageInfo auditPageInfo =
        CommonUtils.defaultResponsePageInfo(auditEntityPage);
    final List<AuditPermissionEntity> auditEntities = auditEntityPage.getContent();
    final List<AuditPermissionDto> auditDtos =
        entityDtoConvertUtils.convertEntityToDtoPermissionsAudit(auditEntities);
    return new AuditResponse(auditPageInfo, auditDtos, null, null, null);
  }

  public void auditRole(
      final HttpServletRequest request,
      final RoleEntity roleEntity,
      final AuditEnums.AuditRole eventType,
      final String eventDesc) {
    try {
      AuditRoleEntity auditRoleEntity = new AuditRoleEntity();
      auditRoleEntity.setEventType(eventType.name());
      auditRoleEntity.setEventDesc(eventDesc);
      auditRoleEntity.setEventData(roleEntity);
      auditRoleEntity.setRole(roleEntity);

      auditRoleEntity.setCreatedBy(getCreatedByProfileEntity());
      auditRoleEntity.setCreatedAt(LocalDateTime.now());
      auditRoleEntity.setIpAddress(getIpAddress(request));
      auditRoleEntity.setUserAgent(getUserAgent(request));

      auditRoleRepository.save(auditRoleEntity);
    } catch (Exception ex) {
      log.error(
          "AuditRoleException: [{}], [{}], [{}]", roleEntity.getId(), eventType, eventDesc, ex);
    }
  }

  public AuditResponse auditRoles(final RequestMetadata requestMetadata, final Long roleId) {
    final Page<AuditRoleEntity> auditEntityPage =
        auditRoleRepository.findByRoleId(
            roleId, JpaDataUtils.getQueryPageableAudit(requestMetadata));
    final ResponseMetadata.ResponsePageInfo auditPageInfo =
        CommonUtils.defaultResponsePageInfo(auditEntityPage);
    final List<AuditRoleEntity> auditEntities = auditEntityPage.getContent();
    final List<AuditRoleDto> auditDtos =
        entityDtoConvertUtils.convertEntityToDtoRolesAudit(auditEntities);
    return new AuditResponse(auditPageInfo, null, auditDtos, null, null);
  }

  public void auditPlatform(
      final HttpServletRequest request,
      final PlatformEntity platformEntity,
      final AuditEnums.AuditPlatform eventType,
      final String eventDesc) {
    try {
      AuditPlatformEntity auditPlatformEntity = new AuditPlatformEntity();
      auditPlatformEntity.setEventType(eventType.name());
      auditPlatformEntity.setEventDesc(eventDesc);
      auditPlatformEntity.setEventData(platformEntity);
      auditPlatformEntity.setPlatform(platformEntity);

      auditPlatformEntity.setCreatedBy(getCreatedByProfileEntity());
      auditPlatformEntity.setCreatedAt(LocalDateTime.now());
      auditPlatformEntity.setIpAddress(getIpAddress(request));
      auditPlatformEntity.setUserAgent(getUserAgent(request));

      auditPlatformRepository.save(auditPlatformEntity);
    } catch (Exception ex) {
      log.error(
          "AuditPlatformException: [{}], [{}], [{}]",
          platformEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  public AuditResponse auditPlatforms(
      final RequestMetadata requestMetadata, final Long platformId) {
    final Page<AuditPlatformEntity> auditEntityPage =
        auditPlatformRepository.findByPlatformId(
            platformId, JpaDataUtils.getQueryPageableAudit(requestMetadata));
    final ResponseMetadata.ResponsePageInfo auditPageInfo =
        CommonUtils.defaultResponsePageInfo(auditEntityPage);
    final List<AuditPlatformEntity> auditEntities = auditEntityPage.getContent();
    final List<AuditPlatformDto> auditDtos =
        entityDtoConvertUtils.convertEntityToDtoPlatformsAudit(auditEntities);
    return new AuditResponse(auditPageInfo, null, null, auditDtos, null);
  }

  public void auditProfile(
      final HttpServletRequest request,
      final ProfileEntity profileEntity,
      final AuditEnums.AuditProfile eventType,
      final String eventDesc) {
    try {
      AuditProfileEntity auditProfileEntity = new AuditProfileEntity();
      auditProfileEntity.setEventType(eventType.name());
      auditProfileEntity.setEventDesc(eventDesc);
      if (profileEntity != null) {
        auditProfileEntity.setEventData(profileEntity);
        auditProfileEntity.setProfile(profileEntity);
      }
      auditProfileEntity.setCreatedBy(getCreatedByProfileEntity());
      auditProfileEntity.setCreatedAt(LocalDateTime.now());
      auditProfileEntity.setIpAddress(getIpAddress(request));
      auditProfileEntity.setUserAgent(getUserAgent(request));

      auditProfileRepository.save(auditProfileEntity);
    } catch (Exception ex) {
      log.error(
          "AuditProfileException: [{}], [{}], [{}]",
          profileEntity == null ? ConstantUtils.ELEMENT_ID_NOT_FOUND : profileEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  public AuditResponse auditProfiles(final RequestMetadata requestMetadata, final Long profileId) {
    final Page<AuditProfileEntity> auditEntityPage =
        auditProfileRepository.findByProfileId(
            profileId, JpaDataUtils.getQueryPageableAudit(requestMetadata));
    final ResponseMetadata.ResponsePageInfo auditPageInfo =
        CommonUtils.defaultResponsePageInfo(auditEntityPage);
    final List<AuditProfileEntity> auditEntities = auditEntityPage.getContent();
    final List<AuditProfileDto> auditDtos =
        entityDtoConvertUtils.convertEntityToDtoProfilesAudit(auditEntities);
    return new AuditResponse(auditPageInfo, null, null, null, auditDtos);
  }
}
