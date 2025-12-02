package auth.service.app.service;

import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.AuditPermissionRepository;
import auth.service.app.repository.AuditPlatformRepository;
import auth.service.app.repository.AuditProfileRepository;
import auth.service.app.repository.AuditRoleRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
      auditPermissionEntity.setIpAddress(CommonUtils.getIpAddress(request));
      auditPermissionEntity.setUserAgent(CommonUtils.getUserAgent(request));

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

  public List<AuditPermissionEntity> auditPermissions(final Long id) {
    return auditPermissionRepository.findByPermissionId(id);
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
      auditRoleEntity.setIpAddress(CommonUtils.getIpAddress(request));
      auditRoleEntity.setUserAgent(CommonUtils.getUserAgent(request));

      auditRoleRepository.save(auditRoleEntity);
    } catch (Exception ex) {
      log.error(
          "AuditRoleException: [{}], [{}], [{}]", roleEntity.getId(), eventType, eventDesc, ex);
    }
  }

  public List<AuditRoleEntity> auditRoles(final Long id) {
    return auditRoleRepository.findByRoleId(id);
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
      auditPlatformEntity.setIpAddress(CommonUtils.getIpAddress(request));
      auditPlatformEntity.setUserAgent(CommonUtils.getUserAgent(request));

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

  public List<AuditPlatformEntity> auditPlatforms(final Long id) {
    return auditPlatformRepository.findByPlatformId(id);
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
      auditProfileEntity.setIpAddress(CommonUtils.getIpAddress(request));
      auditProfileEntity.setUserAgent(CommonUtils.getUserAgent(request));

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

  public List<AuditProfileEntity> auditProfiles(final Long id) {
    return auditProfileRepository.findByProfileId(id);
  }
}
