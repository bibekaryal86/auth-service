package user.management.system.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.entity.AuditAppPermissionEntity;
import user.management.system.app.model.entity.AuditAppRoleEntity;
import user.management.system.app.model.entity.AuditAppUserEntity;
import user.management.system.app.model.entity.AuditAppsEntity;
import user.management.system.app.model.enums.AuditEnums;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.repository.AuditAppPermissionRepository;
import user.management.system.app.repository.AuditAppRoleRepository;
import user.management.system.app.repository.AuditAppUserRepository;
import user.management.system.app.repository.AuditAppsRepository;

@Slf4j
@Service
public class AuditService {

  private final ObjectMapper objectMapper;
  private final AuditAppPermissionRepository auditAppPermissionRepository;
  private final AuditAppRoleRepository auditAppRoleRepository;
  private final AuditAppsRepository auditAppsRepository;
  private final AuditAppUserRepository auditAppUserRepository;

  public AuditService(
      final AuditAppPermissionRepository auditAppPermissionRepository,
      final AuditAppRoleRepository auditAppRoleRepository,
      final AuditAppsRepository auditAppsRepository,
      final AuditAppUserRepository auditAppUserRepository) {
    this.auditAppPermissionRepository = auditAppPermissionRepository;
    this.auditAppRoleRepository = auditAppRoleRepository;
    this.auditAppsRepository = auditAppsRepository;
    this.auditAppUserRepository = auditAppUserRepository;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

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

  private AppUserEntity getAppUserEntity(final int appUserId) {
    AppUserEntity appUserEntity = new AppUserEntity();

    if (appUserId == 0) {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      final AuthToken authToken = (AuthToken) authentication.getCredentials();
      appUserEntity.setId(authToken.getUser().getId());
    } else {
      appUserEntity.setId(appUserId);
    }

    return appUserEntity;
  }

  private AppPermissionEntity getAppPermissionEntity(final int appPermissionId) {
    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setId(appPermissionId);
    return appPermissionEntity;
  }

  private AppRoleEntity getAppRoleEntity(final int appRoleId) {
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    appRoleEntity.setId(appRoleId);
    return appRoleEntity;
  }

  private AppsEntity getAppsEntity(final String appId) {
    AppsEntity appsEntity = new AppsEntity();
    appsEntity.setId(appId);
    return appsEntity;
  }

  private String serializeToJson(Object object) {
    if (object == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      log.error("Serialize To JSON: [{}]", object, ex);
      return object.toString();
    }
  }

  private void auditAppPermission(
      final HttpServletRequest request,
      AppPermissionEntity appPermissionEntity,
      final int appPermissionId,
      final AuditEnums.AuditPermissions eventType,
      final String eventDesc) {
    if (appPermissionEntity == null) {
      appPermissionEntity = getAppPermissionEntity(appPermissionId);
    }

    try {
      AuditAppPermissionEntity auditAppPermissionEntity = new AuditAppPermissionEntity();
      auditAppPermissionEntity.setAppPermission(appPermissionEntity);
      auditAppPermissionEntity.setEventData(serializeToJson(appPermissionEntity));
      auditAppPermissionEntity.setEventType(eventType.name());
      auditAppPermissionEntity.setEventDesc(eventDesc);
      auditAppPermissionEntity.setCreatedBy(getAppUserEntity(0));
      auditAppPermissionEntity.setCreatedAt(LocalDateTime.now());
      auditAppPermissionEntity.setIpAddress(getIpAddress(request));
      auditAppPermissionEntity.setUserAgent(getUserAgent(request));

      auditAppPermissionRepository.save(auditAppPermissionEntity);
    } catch (Exception ex) {
      log.error(
          "AuditAppPermissionException: [{}], [{}], [{}]",
          appPermissionEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  private void auditApps(
      final HttpServletRequest request,
      AppsEntity appsEntity,
      final String appId,
      final AuditEnums.AuditApps eventType,
      final String eventDesc) {
    if (appsEntity == null) {
      appsEntity = getAppsEntity(appId);
    }

    try {
      AuditAppsEntity auditAppsEntity = new AuditAppsEntity();
      auditAppsEntity.setApp(appsEntity);
      auditAppsEntity.setEventData(serializeToJson(appsEntity));
      auditAppsEntity.setEventType(eventType.name());
      auditAppsEntity.setEventDesc(eventDesc);
      auditAppsEntity.setCreatedBy(getAppUserEntity(0));
      auditAppsEntity.setCreatedAt(LocalDateTime.now());
      auditAppsEntity.setIpAddress(getIpAddress(request));
      auditAppsEntity.setUserAgent(getUserAgent(request));

      auditAppsRepository.save(auditAppsEntity);
    } catch (Exception ex) {
      log.error(
          "AuditAppsException: [{}], [{}], [{}]", appsEntity.getId(), eventType, eventDesc, ex);
    }
  }

  private void auditAppRole(
      final HttpServletRequest request,
      AppRoleEntity appRoleEntity,
      final int appRoleId,
      final AuditEnums.AuditRoles eventType,
      final String eventDesc) {
    if (appRoleEntity == null) {
      appRoleEntity = getAppRoleEntity(appRoleId);
    }

    try {
      AuditAppRoleEntity auditAppRoleEntity = new AuditAppRoleEntity();
      auditAppRoleEntity.setAppRole(appRoleEntity);
      auditAppRoleEntity.setEventData(serializeToJson(appRoleEntity));
      auditAppRoleEntity.setEventType(eventType.name());
      auditAppRoleEntity.setEventDesc(eventDesc);
      auditAppRoleEntity.setCreatedBy(getAppUserEntity(0));
      auditAppRoleEntity.setCreatedAt(LocalDateTime.now());
      auditAppRoleEntity.setIpAddress(getIpAddress(request));
      auditAppRoleEntity.setUserAgent(getUserAgent(request));

      auditAppRoleRepository.save(auditAppRoleEntity);
    } catch (Exception ex) {
      log.error(
          "AuditAppRoleException: [{}], [{}], [{}]",
          appRoleEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  private void auditAppUser(
      final HttpServletRequest request,
      AppUserEntity appUserEntity,
      final int appUserId,
      final AuditEnums.AuditUsers eventType,
      final String eventDesc) {
    if (appUserEntity == null) {
      appUserEntity = getAppUserEntity(appUserId);
    }

    try {

      AuditAppUserEntity auditAppUserEntity = new AuditAppUserEntity();
      auditAppUserEntity.setAppUser(appUserEntity);
      auditAppUserEntity.setEventData(serializeToJson(appUserEntity));
      auditAppUserEntity.setEventType(eventType.name());
      auditAppUserEntity.setEventDesc(eventDesc);
      auditAppUserEntity.setCreatedBy(getAppUserEntity(0));
      auditAppUserEntity.setCreatedAt(LocalDateTime.now());
      auditAppUserEntity.setIpAddress(getIpAddress(request));
      auditAppUserEntity.setUserAgent(getUserAgent(request));

      auditAppUserRepository.save(auditAppUserEntity);
    } catch (Exception ex) {
      log.error(
          "AuditAppUserException: [{}], [{}], [{}]",
          appUserEntity.getId(),
          eventType,
          eventDesc,
          ex);
    }
  }

  public void auditAppPermissionCreate(
      final HttpServletRequest request,
      final String appId,
      final AppPermissionEntity appPermissionEntity) {
    final String eventDesc =
        String.format("Create Permission [%s] for app [%s]", appPermissionEntity.getId(), appId);
    auditAppPermission(
        request, appPermissionEntity, 0, AuditEnums.AuditPermissions.CREATE_PERMISSION, eventDesc);
  }

  public void auditAppPermissionUpdate(
      final HttpServletRequest request, final AppPermissionEntity appPermissionEntity) {
    final String eventDesc = String.format("Update Permission [%s]", appPermissionEntity.getId());
    auditAppPermission(
        request, appPermissionEntity, 0, AuditEnums.AuditPermissions.UPDATE_PERMISSION, eventDesc);
  }

  public void auditAppPermissionDeleteSoft(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Soft Delete Permission [%s]", id);
    auditAppPermission(
        request, null, id, AuditEnums.AuditPermissions.SOFT_DELETE_PERMISSION, eventDesc);
  }

  public void auditAppPermissionDeleteHard(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Hard Delete Permission [%s]", id);
    auditAppPermission(
        request, null, id, AuditEnums.AuditPermissions.HARD_DELETE_PERMISSION, eventDesc);
  }

  public void auditAppPermissionRestore(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Restore Permission [%s]", id);
    auditAppPermission(
        request, null, id, AuditEnums.AuditPermissions.RESTORE_PERMISSION, eventDesc);
  }

  public void auditAppRoleCreate(
      final HttpServletRequest request, final AppRoleEntity appRoleEntity) {
    final String eventDesc = String.format("Create App Role [%s]", appRoleEntity.getId());
    auditAppRole(request, appRoleEntity, 0, AuditEnums.AuditRoles.CREATE_ROLE, eventDesc);
  }

  public void auditAppRoleUpdate(
      final HttpServletRequest request, final AppRoleEntity appRoleEntity) {
    final String eventDesc = String.format("Update App Role [%s]", appRoleEntity.getId());
    auditAppRole(request, appRoleEntity, 0, AuditEnums.AuditRoles.UPDATE_ROLE, eventDesc);
  }

  public void auditAppRoleDeleteSoft(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Soft Delete App Role [%s]", id);
    auditAppRole(request, null, id, AuditEnums.AuditRoles.SOFT_DELETE_ROLE, eventDesc);
  }

  public void auditAppRoleDeleteHard(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Hard Delete App Role [%s]", id);
    auditAppRole(request, null, id, AuditEnums.AuditRoles.HARD_DELETE_ROLE, eventDesc);
  }

  public void auditAppRoleRestore(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Restore App Role [%s]", id);
    auditAppRole(request, null, id, AuditEnums.AuditRoles.RESTORE_ROLE, eventDesc);
  }

  public void auditAppRoleAssignPermission(
      final HttpServletRequest request, final AppRolePermissionEntity appRolePermissionEntity) {
    final String eventDesc =
        String.format(
            "Assign Permission [%s] to Role [%s]",
            appRolePermissionEntity.getAppPermission().getId(),
            appRolePermissionEntity.getAppRole().getId());
    auditAppRole(
        request,
        appRolePermissionEntity.getAppRole(),
        0,
        AuditEnums.AuditRoles.ASSIGN_PERMISSION,
        eventDesc);
  }

  public void auditAppRoleUnassignPermission(
      final HttpServletRequest request, final int appRoleId, final int appPermissionId) {
    final String eventDesc =
        String.format("Unassign Permission [%s] from Role [%s]", appPermissionId, appRoleId);
    auditAppRole(request, null, appRoleId, AuditEnums.AuditRoles.UNASSIGN_PERMISSION, eventDesc);
  }

  public void auditAppsCreate(final HttpServletRequest request, final AppsEntity appsEntity) {
    final String eventDesc = String.format("Create App [%s]", appsEntity.getId());
    auditApps(request, appsEntity, "", AuditEnums.AuditApps.CREATE_APP, eventDesc);
  }

  public void auditAppsUpdate(final HttpServletRequest request, final AppsEntity appsEntity) {
    final String eventDesc = String.format("Update App [%s]", appsEntity.getId());
    auditApps(request, appsEntity, "", AuditEnums.AuditApps.UPDATE_APP, eventDesc);
  }

  public void auditAppsDeleteSoft(final HttpServletRequest request, final String id) {
    final String eventDesc = String.format("Soft Delete App [%s]", id);
    auditApps(request, null, id, AuditEnums.AuditApps.SOFT_DELETE_APP, eventDesc);
  }

  public void auditAppsDeleteHard(final HttpServletRequest request, final String id) {
    final String eventDesc = String.format("Hard Delete App [%s]", id);
    auditApps(request, null, id, AuditEnums.AuditApps.HARD_DELETE_APP, eventDesc);
  }

  public void auditAppsRestore(final HttpServletRequest request, final String id) {
    final String eventDesc = String.format("Restore App [%s]", id);
    auditApps(request, null, id, AuditEnums.AuditApps.RESTORE_APP, eventDesc);
  }
}
