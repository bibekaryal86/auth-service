package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.entity.AuditAppPermissionEntity;
import auth.service.app.model.entity.AuditAppRoleEntity;
import auth.service.app.model.entity.AuditAppUserEntity;
import auth.service.app.model.entity.AuditAppsEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.AuditAppPermissionRepository;
import auth.service.app.repository.AuditAppRoleRepository;
import auth.service.app.repository.AuditAppUserRepository;
import auth.service.app.repository.AuditAppsRepository;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class AuditServiceTest extends BaseTest {

  private static AuthToken authToken;
  private static AppsEntity appsEntity;
  private static AppUserEntity appUserEntity;
  private static AppRoleEntity appRoleEntity;
  private static AppPermissionEntity appPermissionEntity;
  private static AppRolePermissionEntity appRolePermissionEntity;
  private static AppUserRoleEntity appUserRoleEntity;
  private static AppsAppUserEntity appsAppUserEntity;

  @Mock private HttpServletRequest request;
  @Mock private SecurityContext securityContext;
  @MockitoBean private AuditAppPermissionRepository auditAppPermissionRepository;
  @MockitoBean private AuditAppRoleRepository auditAppRoleRepository;
  @MockitoBean private AuditAppUserRepository auditAppUserRepository;
  @MockitoBean private AuditAppsRepository auditAppsRepository;

  @Autowired private AuditService auditService;

  @BeforeAll
  public static void setUpBeforeAll() {
    authToken = TestData.getAuthToken();
    appsEntity = TestData.getAppsEntities().getFirst();
    appUserEntity = TestData.getAppUserEntities().getFirst();
    appRoleEntity = TestData.getAppRoleEntities().getFirst();
    appPermissionEntity = TestData.getAppPermissionEntities().getFirst();
    appRolePermissionEntity = TestData.getAppRolePermissionEntities().getFirst();
    appUserRoleEntity = TestData.getAppUserRoleEntities().getFirst();
    appsAppUserEntity = TestData.getAppsAppUserEntities().getFirst();
  }

  @BeforeEach
  void setUpBeforeEach() {
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("localhost");
    when(request.getHeader("User-Agent")).thenReturn("agent");

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication())
        .thenReturn(
            new TestingAuthenticationToken(
                appUserEntity.getEmail(), authToken, Collections.emptyList()));
  }

  @AfterEach
  void tearDown() {
    reset(request);
    reset(securityContext);
    reset(auditAppPermissionRepository);
    reset(auditAppRoleRepository);
    reset(auditAppUserRepository);
    reset(auditAppsRepository);
  }

  @Test
  void testAuditAppPermissionCreate() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppPermissionCreate(
                request, appsEntity.getId(), appPermissionEntity));

    ArgumentCaptor<AuditAppPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppPermissionEntity.class);
    verify(auditAppPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditPermissions.CREATE_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppPermissionUpdate() {
    assertDoesNotThrow(() -> auditService.auditAppPermissionUpdate(request, appPermissionEntity));

    ArgumentCaptor<AuditAppPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppPermissionEntity.class);
    verify(auditAppPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditPermissions.UPDATE_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppPermissionDeleteSoft() {
    assertDoesNotThrow(
        () -> auditService.auditAppPermissionDeleteSoft(request, appPermissionEntity.getId()));

    ArgumentCaptor<AuditAppPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppPermissionEntity.class);
    verify(auditAppPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(
        AuditEnums.AuditPermissions.SOFT_DELETE_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppPermissionDeleteHard() {
    assertDoesNotThrow(
        () -> auditService.auditAppPermissionDeleteHard(request, appPermissionEntity.getId()));

    ArgumentCaptor<AuditAppPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppPermissionEntity.class);
    verify(auditAppPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(
        AuditEnums.AuditPermissions.HARD_DELETE_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppPermissionRestore() {
    assertDoesNotThrow(
        () -> auditService.auditAppPermissionRestore(request, appPermissionEntity.getId()));

    ArgumentCaptor<AuditAppPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppPermissionEntity.class);
    verify(auditAppPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(
        AuditEnums.AuditPermissions.RESTORE_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleCreate() {
    assertDoesNotThrow(() -> auditService.auditAppRoleCreate(request, appRoleEntity));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.CREATE_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleUpdate() {
    assertDoesNotThrow(() -> auditService.auditAppRoleUpdate(request, appRoleEntity));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.UPDATE_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleDeleteSoft() {
    assertDoesNotThrow(() -> auditService.auditAppRoleDeleteSoft(request, appRoleEntity.getId()));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.SOFT_DELETE_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleDeleteHard() {
    assertDoesNotThrow(() -> auditService.auditAppRoleDeleteHard(request, appRoleEntity.getId()));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.HARD_DELETE_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleRestore() {
    assertDoesNotThrow(() -> auditService.auditAppRoleRestore(request, appRoleEntity.getId()));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.RESTORE_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleAssignPermission() {
    assertDoesNotThrow(
        () -> auditService.auditAppRoleAssignPermission(request, appRolePermissionEntity));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.ASSIGN_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppRoleUnassignPermission() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppRoleUnassignPermission(
                request, appRoleEntity.getId(), appPermissionEntity.getId()));

    ArgumentCaptor<AuditAppRoleEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppRoleEntity.class);
    verify(auditAppRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRoles.UNASSIGN_PERMISSION.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppsCreate() {
    assertDoesNotThrow(() -> auditService.auditAppsCreate(request, appsEntity));

    ArgumentCaptor<AuditAppsEntity> entityCaptor = ArgumentCaptor.forClass(AuditAppsEntity.class);
    verify(auditAppsRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppsEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditApps.CREATE_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppsUpdate() {
    assertDoesNotThrow(() -> auditService.auditAppsUpdate(request, appsEntity));

    ArgumentCaptor<AuditAppsEntity> entityCaptor = ArgumentCaptor.forClass(AuditAppsEntity.class);
    verify(auditAppsRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppsEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditApps.UPDATE_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppsDeleteSoft() {
    assertDoesNotThrow(() -> auditService.auditAppsDeleteSoft(request, appsEntity.getId()));

    ArgumentCaptor<AuditAppsEntity> entityCaptor = ArgumentCaptor.forClass(AuditAppsEntity.class);
    verify(auditAppsRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppsEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditApps.SOFT_DELETE_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppsDeleteHard() {
    assertDoesNotThrow(() -> auditService.auditAppsDeleteHard(request, appsEntity.getId()));

    ArgumentCaptor<AuditAppsEntity> entityCaptor = ArgumentCaptor.forClass(AuditAppsEntity.class);
    verify(auditAppsRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppsEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditApps.HARD_DELETE_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppsRestore() {
    assertDoesNotThrow(() -> auditService.auditAppsRestore(request, appsEntity.getId()));

    ArgumentCaptor<AuditAppsEntity> entityCaptor = ArgumentCaptor.forClass(AuditAppsEntity.class);
    verify(auditAppsRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppsEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditApps.RESTORE_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserCreate() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserCreate(request, appsEntity.getId(), appUserEntity, true));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(4)).save(entityCaptor.capture());
    verify(securityContext, times(4)).getAuthentication();

    List<AuditAppUserEntity> actualEntities = entityCaptor.getAllValues();
    assertEquals(4, actualEntities.size());
    assertEquals(AuditEnums.AuditUsers.CREATE_USER.name(), actualEntities.get(0).getEventType());
    assertEquals(AuditEnums.AuditUsers.ASSIGN_APP.name(), actualEntities.get(1).getEventType());
    assertEquals(AuditEnums.AuditUsers.ASSIGN_ROLE.name(), actualEntities.get(2).getEventType());
    assertEquals(
        AuditEnums.AuditUsers.USER_VALIDATE_INIT.name(), actualEntities.get(3).getEventType());
  }

  @Test
  void testAuditAppUserUpdate() {
    assertDoesNotThrow(() -> auditService.auditAppUserUpdate(request, appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.UPDATE_USER.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserUpdateEmail() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserUpdateEmail(request, appUserEntity, appsEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(2)).save(entityCaptor.capture());
    verify(securityContext, times(2)).getAuthentication();

    List<AuditAppUserEntity> actualEntities = entityCaptor.getAllValues();
    assertEquals(2, actualEntities.size());
    assertEquals(
        AuditEnums.AuditUsers.UPDATE_USER_EMAIL.name(), actualEntities.get(0).getEventType());
    assertEquals(
        AuditEnums.AuditUsers.USER_VALIDATE_INIT.name(), actualEntities.get(1).getEventType());
  }

  @Test
  void testAuditAppUserUpdatePassword() {
    assertDoesNotThrow(() -> auditService.auditAppUserUpdatePassword(request, appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.UPDATE_USER_PASSWORD.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserDeleteAddress() {
    assertDoesNotThrow(() -> auditService.auditAppUserDeleteAddress(request, appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(
        AuditEnums.AuditUsers.UPDATE_USER_DELETE_ADDRESS.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserDeleteSoft() {
    assertDoesNotThrow(() -> auditService.auditAppUserDeleteSoft(request, appUserEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.SOFT_DELETE_USER.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserDeleteHard() {
    assertDoesNotThrow(() -> auditService.auditAppUserDeleteHard(request, appUserEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.HARD_DELETE_USER.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserRestore() {
    assertDoesNotThrow(() -> auditService.auditAppUserRestore(request, appUserEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.RESTORE_USER.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserLoginSuccess() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserLoginSuccess(
                request, appsEntity.getId(), appUserEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_LOGIN.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserLoginFailure() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserLoginFailure(
                request,
                appsEntity.getId(),
                appUserEntity.getEmail(),
                new Exception("login exception")));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_LOGIN_ERROR.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserTokenRefreshSuccess() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserTokenRefreshSuccess(
                request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.TOKEN_REFRESH.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserTokenRefreshFailure() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserTokenRefreshFailure(
                request,
                appsEntity.getId(),
                appUserEntity.getId(),
                new Exception("token refresh exception")));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.TOKEN_REFRESH_ERROR.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserLogoutSuccess() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserLogoutSuccess(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_LOGOUT.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserLogoutFailure() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserLogoutFailure(
                request,
                appsEntity.getId(),
                appUserEntity.getId(),
                new Exception("logout exception")));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_LOGOUT_ERROR.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserResetInit() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserResetInit(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_RESET_INIT.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserResetExit() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserResetExit(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_RESET_EXIT.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserResetSuccess() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserResetSuccess(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_RESET.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserResetFailure() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserResetFailure(
                request,
                appsEntity.getId(),
                appUserEntity.getEmail(),
                new Exception("reset exception")));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_RESET_ERROR.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserValidateInit() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserValidateInit(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_VALIDATE_INIT.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserValidateExit() {
    assertDoesNotThrow(
        () -> auditService.auditAppUserValidateExit(request, appsEntity.getId(), appUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_VALIDATE_EXIT.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserValidateFailure() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserValidateFailure(
                request,
                appsEntity.getId(),
                appUserEntity.getEmail(),
                new Exception("validate exception")));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.USER_VALIDATE_ERROR.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserAssignRole() {
    assertDoesNotThrow(() -> auditService.auditAppUserAssignRole(request, appUserRoleEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.ASSIGN_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserUnassignRole() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserUnassignRole(
                request, appUserEntity.getId(), appRoleEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.UNASSIGN_ROLE.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserAssignApp() {
    assertDoesNotThrow(() -> auditService.auditAppUserAssignApp(request, appsAppUserEntity));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.ASSIGN_APP.name(), actualEntity.getEventType());
  }

  @Test
  void testAuditAppUserUnassignApp() {
    assertDoesNotThrow(
        () ->
            auditService.auditAppUserUnassignApp(
                request, appUserEntity.getEmail(), appsEntity.getId()));

    ArgumentCaptor<AuditAppUserEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditAppUserEntity.class);
    verify(auditAppUserRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditAppUserEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditUsers.UNASSIGN_APP.name(), actualEntity.getEventType());
  }
}
