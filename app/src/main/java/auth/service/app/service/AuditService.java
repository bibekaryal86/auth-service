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
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.AuditPermissionRepository;
import auth.service.app.repository.AuditPlatformRepository;
import auth.service.app.repository.AuditProfileRepository;
import auth.service.app.repository.AuditRoleRepository;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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
  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final PlatformRepository platformRepository;
  private final ProfileRepository profileRepository;

  private String getIpAddress(final HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  private String getUserAgent(final HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }

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

  public void auditProfile(
      final HttpServletRequest request,
      final ProfileEntity profileEntity,
      final AuditEnums.AuditProfile eventType,
      final String eventDesc) {
    try {
      AuditProfileEntity auditProfileEntity = new AuditProfileEntity();
      auditProfileEntity.setEventType(eventType.name());
      auditProfileEntity.setEventDesc(eventDesc);
      auditProfileEntity.setEventData(profileEntity);
      auditProfileEntity.setProfile(profileEntity);

      auditProfileEntity.setCreatedBy(getCreatedByProfileEntity());
      auditProfileEntity.setCreatedAt(LocalDateTime.now());
      auditProfileEntity.setIpAddress(getIpAddress(request));
      auditProfileEntity.setUserAgent(getUserAgent(request));

      auditProfileRepository.save(auditProfileEntity);
    } catch (Exception ex) {
      log.error(
          "AuditProfileException: [{}], [{}], [{}]",
          profileEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }
}
