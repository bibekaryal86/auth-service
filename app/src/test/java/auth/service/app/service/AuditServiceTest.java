package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
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
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
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
  private static PermissionEntity permissionEntity;
  private static RoleEntity roleEntity;
  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;

  @Mock private HttpServletRequest request;
  @Mock private SecurityContext securityContext;
  @MockitoBean private AuditPermissionRepository auditPermissionRepository;
  @MockitoBean private AuditRoleRepository auditRoleRepository;
  @MockitoBean private AuditPlatformRepository auditPlatformRepository;
  @MockitoBean private AuditProfileRepository auditProfileRepository;

  @Autowired private AuditService auditService;

  @BeforeAll
  public static void setUpBeforeAll() {
    authToken = TestData.getAuthToken();
    permissionEntity = TestData.getPermissionEntities().getFirst();
    roleEntity = TestData.getRoleEntities().getFirst();
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileEntity = TestData.getProfileEntities().getFirst();
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
                profileEntity.getEmail(), authToken, Collections.emptyList()));
  }

  @AfterEach
  void tearDown() {
    reset(request);
    reset(securityContext);
    reset(auditPermissionRepository);
    reset(auditRoleRepository);
    reset(auditPlatformRepository);
    reset(auditProfileRepository);
  }

  @Test
  void testAuditPermission() {
    String eventDesc =
        String.format(
            "Permission Create [Id: %s] - [Name: %s]",
            permissionEntity.getId(), permissionEntity.getPermissionName());
    assertDoesNotThrow(
        () ->
            auditService.auditPermission(
                request,
                permissionEntity,
                AuditEnums.AuditPermission.PERMISSION_CREATE,
                eventDesc));

    ArgumentCaptor<AuditPermissionEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditPermissionEntity.class);
    verify(auditPermissionRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditPermissionEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditPermission.PERMISSION_CREATE.name(), actualEntity.getEventType());
    assertEquals(eventDesc, actualEntity.getEventDesc());
  }

  @Test
  void testAuditRole() {
    String eventDesc =
        String.format(
            "Role Update [Id: %s] - [Name: %s]", roleEntity.getId(), roleEntity.getRoleName());
    assertDoesNotThrow(
        () ->
            auditService.auditRole(
                request, roleEntity, AuditEnums.AuditRole.ROLE_UPDATE, eventDesc));

    ArgumentCaptor<AuditRoleEntity> entityCaptor = ArgumentCaptor.forClass(AuditRoleEntity.class);
    verify(auditRoleRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditRoleEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditRole.ROLE_UPDATE.name(), actualEntity.getEventType());
    assertEquals(eventDesc, actualEntity.getEventDesc());
  }

  @Test
  void testAuditPlatform() {
    String eventDesc =
        String.format(
            "Platform Delete Soft [Id: %s] - [Name: %s]",
            platformEntity.getId(), platformEntity.getPlatformName());
    assertDoesNotThrow(
        () ->
            auditService.auditPlatform(
                request, platformEntity, AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT, eventDesc));

    ArgumentCaptor<AuditPlatformEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditPlatformEntity.class);
    verify(auditPlatformRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditPlatformEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT.name(), actualEntity.getEventType());
    assertEquals(eventDesc, actualEntity.getEventDesc());
  }

  @Test
  void testAuditProfile() {
    String eventDesc =
        String.format(
            "Profile Restore [Id: %s] - [Email: %s]",
            profileEntity.getId(), profileEntity.getEmail());
    assertDoesNotThrow(
        () ->
            auditService.auditProfile(
                request, profileEntity, AuditEnums.AuditProfile.PROFILE_RESTORE, eventDesc));

    ArgumentCaptor<AuditProfileEntity> entityCaptor =
        ArgumentCaptor.forClass(AuditProfileEntity.class);
    verify(auditProfileRepository, times(1)).save(entityCaptor.capture());
    verify(securityContext, times(1)).getAuthentication();

    AuditProfileEntity actualEntity = entityCaptor.getValue();
    assertEquals(AuditEnums.AuditProfile.PROFILE_RESTORE.name(), actualEntity.getEventType());
    assertEquals(eventDesc, actualEntity.getEventDesc());
  }
}
