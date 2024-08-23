package user.management.system.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.entity.AuditAppPermissionEntity;
import user.management.system.app.model.entity.AuditAppRoleEntity;
import user.management.system.app.model.entity.AuditAppUserEntity;
import user.management.system.app.model.entity.AuditAppsEntity;
import user.management.system.app.model.enums.AuditEnums;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.repository.AppPermissionRepository;
import user.management.system.app.repository.AppRoleRepository;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.repository.AppsRepository;
import user.management.system.app.repository.AuditAppPermissionRepository;
import user.management.system.app.repository.AuditAppRoleRepository;
import user.management.system.app.repository.AuditAppUserRepository;
import user.management.system.app.repository.AuditAppsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final ObjectMapper objectMapper;
  private final AuditAppPermissionRepository auditAppPermissionRepository;
  private final AuditAppRoleRepository auditAppRoleRepository;
  private final AuditAppsRepository auditAppsRepository;
  private final AuditAppUserRepository auditAppUserRepository;
  private final AppPermissionRepository appPermissionRepository;
  private final AppRoleRepository appRoleRepository;
  private final AppsRepository appsRepository;
  private final AppUserRepository appUserRepository;

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

  private AppUserEntity getAppUserEntityById(int appUserId) {
    if (appUserId == 0) {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      final AuthToken authToken = (AuthToken) authentication.getCredentials();
      appUserId = authToken.getUser().getId();
    }
    return appUserRepository.findById(appUserId).orElse(null);
  }

  private AppUserEntity getAppUserEntityByEmail(final String email) {
    return appUserRepository.findByEmail(email).orElse(new AppUserEntity());
  }

  private AppPermissionEntity getAppPermissionEntity(final int appPermissionId) {
    return appPermissionRepository.findById(appPermissionId).orElse(null);
  }

  private AppRoleEntity getAppRoleEntity(final int appRoleId) {
    return appRoleRepository.findById(appRoleId).orElse(null);
  }

  private AppsEntity getAppsEntity(final String appId) {
    return appsRepository.findById(appId).orElse(null);
  }

  private String serializeToJson(Object object) {
    if (object == null) {
      return null;
    }

    try {
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
      auditAppPermissionEntity.setCreatedBy(getAppUserEntityById(0));
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
      auditAppsEntity.setCreatedBy(getAppUserEntityById(0));
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
      auditAppRoleEntity.setCreatedBy(getAppUserEntityById(0));
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
      appUserEntity = getAppUserEntityById(appUserId);
    }

    try {
      AuditAppUserEntity auditAppUserEntity = new AuditAppUserEntity();
      auditAppUserEntity.setAppUser(appUserEntity);
      auditAppUserEntity.setEventData(serializeToJson(appUserEntity));
      auditAppUserEntity.setEventType(eventType.name());
      auditAppUserEntity.setEventDesc(eventDesc);
      auditAppUserEntity.setCreatedBy(getAppUserEntityById(0));
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

  public void auditAppUserCreate(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Create User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.CREATE_USER, eventDesc);
  }

  public void auditAppUserUpdate(
      final HttpServletRequest request, final AppUserEntity appUserEntity) {
    final String eventDesc = String.format("Update User [%s]", appUserEntity.getId());
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.UPDATE_USER, eventDesc);
  }

  public void auditAppUserUpdatePassword(
      final HttpServletRequest request, final AppUserEntity appUserEntity) {
    final String eventDesc = String.format("Update User [%s] password", appUserEntity.getId());
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.UPDATE_USER_PASSWORD, eventDesc);
  }

  public void auditAppUserDeleteSoft(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Soft Delete User [%s]", id);
    auditAppUser(request, null, id, AuditEnums.AuditUsers.SOFT_DELETE_USER, eventDesc);
  }

  public void auditAppUserDeleteHard(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Hard Delete User [%s]", id);
    auditAppUser(request, null, id, AuditEnums.AuditUsers.HARD_DELETE_USER, eventDesc);
  }

  public void auditAppUserRestore(final HttpServletRequest request, final int id) {
    final String eventDesc = String.format("Restore User [%s]", id);
    auditAppUser(request, null, id, AuditEnums.AuditUsers.RESTORE_USER, eventDesc);
  }

  public void auditAppUserLoginSuccess(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Login Success User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_LOGIN, eventDesc);
  }

  public void auditAppUserLoginFailure(
      final HttpServletRequest request,
      final String appId,
      final String email,
      final Exception ex) {
    final AppUserEntity appUserEntity = getAppUserEntityByEmail(email);
    final String eventDesc =
        String.format(
            "Login Failed User [%s]-[%s] for app [%s] because [%s]",
            appUserEntity.getId(), email, appId, ex.getMessage());
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_LOGIN_ERROR, eventDesc);
  }

  public void auditAppUserTokenRefreshSuccess(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Token Refresh Success User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.TOKEN_REFRESH, eventDesc);
  }

  public void auditAppUserTokenRefreshFailure(
      final HttpServletRequest request,
      final String appId,
      final int appUserId,
      final Exception ex) {
    final String eventDesc =
        String.format(
            "Token Refresh Failed User [%s] for app [%s] because [%s]",
            appUserId, appId, ex.getMessage());
    auditAppUser(request, null, appUserId, AuditEnums.AuditUsers.TOKEN_REFRESH_ERROR, eventDesc);
  }

  public void auditAppUserLogoutSuccess(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Logout Success User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_LOGOUT, eventDesc);
  }

  public void auditAppUserLogoutFailure(
      final HttpServletRequest request,
      final String appId,
      final int appUserId,
      final Exception ex) {
    final String eventDesc =
        String.format(
            "Logout Failed User [%s] for app [%s] because [%s]", appUserId, appId, ex.getMessage());
    auditAppUser(request, null, appUserId, AuditEnums.AuditUsers.USER_LOGOUT_ERROR, eventDesc);
  }

  public void auditAppUserResetInit(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Reset Init User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_RESET_INIT, eventDesc);
  }

  public void auditAppUserResetExit(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Reset Init User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_RESET_EXIT, eventDesc);
  }

  public void auditAppUserResetSuccess(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Reset Success User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_RESET, eventDesc);
  }

  public void auditAppUserResetFailure(
      final HttpServletRequest request,
      final String appId,
      final String email,
      final Exception ex) {
    final AppUserEntity appUserEntity = getAppUserEntityByEmail(email);
    final String eventDesc =
        String.format(
            "Reset Failed User [%s]-[%s] for app [%s] for [%s]",
            appUserEntity.getId(), email, appId, ex.getMessage());
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_RESET_ERROR, eventDesc);
  }

  public void auditAppUserValidateInit(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Validate Init User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_VALIDATE_INIT, eventDesc);
  }

  public void auditAppUserValidateExit(
      final HttpServletRequest request, final String appId, final AppUserEntity appUserEntity) {
    final String eventDesc =
        String.format("Validate Exit User [%s] for app [%s]", appUserEntity.getId(), appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_VALIDATE_EXIT, eventDesc);
  }

  public void auditAppUserValidateFailure(
      final HttpServletRequest request,
      final String appId,
      final String email,
      final Exception ex) {
    final AppUserEntity appUserEntity = getAppUserEntityByEmail(email);
    final String eventDesc =
        String.format(
            "Validation Failed User [%s]-[%s] for app [%s] for [%s]",
            appUserEntity.getId(), email, appId, ex.getMessage());
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.USER_VALIDATE_ERROR, eventDesc);
  }

  public void auditAppUserAssignRole(
      final HttpServletRequest request, final AppUserRoleEntity appUserRoleEntity) {
    final String eventDesc =
        String.format(
            "Assign Role [%s] to User [%s]",
            appUserRoleEntity.getAppRole().getId(), appUserRoleEntity.getAppUser().getId());
    auditAppUser(
        request, appUserRoleEntity.getAppUser(), 0, AuditEnums.AuditUsers.ASSIGN_ROLE, eventDesc);
  }

  public void auditAppUserUnassignRole(
      final HttpServletRequest request, final int appUserId, final int appRoleId) {
    final String eventDesc =
        String.format("Unassign Role [%s] from User [%s]", appRoleId, appUserId);
    auditAppUser(request, null, appUserId, AuditEnums.AuditUsers.UNASSIGN_ROLE, eventDesc);
  }

  public void auditAppUserAssignApp(
      final HttpServletRequest request, final AppsAppUserEntity appsAppUserEntity) {
    final String eventDesc =
        String.format(
            "Assign User [%s] to App [%s]",
            appsAppUserEntity.getAppUser().getId(), appsAppUserEntity.getApp().getId());
    auditAppUser(
        request, appsAppUserEntity.getAppUser(), 0, AuditEnums.AuditUsers.ASSIGN_APP, eventDesc);
  }

  public void auditAppUserUnassignApp(
      final HttpServletRequest request, final String email, final String appId) {
    final AppUserEntity appUserEntity = getAppUserEntityByEmail(email);
    final String eventDesc =
        String.format("Unassign User [%s]-[%s] from app [%s]", appUserEntity.getId(), email, appId);
    auditAppUser(request, appUserEntity, 0, AuditEnums.AuditUsers.UNASSIGN_APP, eventDesc);
  }
}
