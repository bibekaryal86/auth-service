package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import helper.TestData;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class CircularDependencyServiceTest extends BaseTest {

  private PermissionRepository permissionRepository;
  private PlatformRepository platformRepository;
  private RoleRepository roleRepository;
  private ProfileRepository profileRepository;

  @MockitoBean private SecurityContext securityContext;

  @Autowired private CircularDependencyService circularDependencyService;

  @BeforeEach
  void setUpBeforeEach() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testReadPermission_NotDeleted() {
    PermissionEntity result = circularDependencyService.readPermission(ID, !INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID, result.getId());
  }

  @Test
  void testReadPermission_DeleteWithIncludeDeletedAndSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    PermissionEntity result = circularDependencyService.readPermission(ID_DELETED, INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID_DELETED, result.getId());
  }

  @Test
  void testReadPermission_DeletedWithIncludeDeletedButNoSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readPermission(ID_DELETED, INCLUDE_DELETED));
    assertEquals(
        String.format("Active Permission Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadPermission_DeletedNotIncludeDeleted() {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readPermission(ID_DELETED, !INCLUDE_DELETED));
    assertEquals(
        String.format("Active Permission Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadPermission_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readPermission(ID_NOT_FOUND, INCLUDE_DELETED));
    assertEquals("Permission Not Found for [99]", exception.getMessage());
  }

  @Test
  void testReadPlatform_NotDeleted() {
    PlatformEntity result = circularDependencyService.readPlatform(ID, !INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID, result.getId());
  }

  @Test
  void testReadPlatform_DeleteWithIncludeDeletedAndSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    PlatformEntity result = circularDependencyService.readPlatform(ID_DELETED, INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID_DELETED, result.getId());
  }

  @Test
  void testReadPlatform_DeletedWithIncludeDeletedButNoSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readPlatform(ID_DELETED, INCLUDE_DELETED));
    assertEquals(
        String.format("Active Platform Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadPlatform_DeletedNotIncludeDeleted() {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readPlatform(ID_DELETED, !INCLUDE_DELETED));
    assertEquals(
        String.format("Active Platform Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadPlatform_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readPlatform(ID_NOT_FOUND, INCLUDE_DELETED));
    assertEquals("Platform Not Found for [99]", exception.getMessage());
  }

  @Test
  void testReadRole_NotDeleted() {
    RoleEntity result = circularDependencyService.readRole(ID, !INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID, result.getId());
  }

  @Test
  void testReadRole_DeleteWithIncludeDeletedAndSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RoleEntity result = circularDependencyService.readRole(ID_DELETED, INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID_DELETED, result.getId());
  }

  @Test
  void testReadRole_DeletedWithIncludeDeletedButNoSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readRole(ID_DELETED, INCLUDE_DELETED));
    assertEquals(
        String.format("Active Role Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadRole_DeletedNotIncludeDeleted() {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readRole(ID_DELETED, !INCLUDE_DELETED));
    assertEquals(
        String.format("Active Role Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadRole_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readRole(ID_NOT_FOUND, INCLUDE_DELETED));
    assertEquals("Role Not Found for [99]", exception.getMessage());
  }

  @Test
  void testReadRoleByName_NotDeleted() {
    RoleEntity result = circularDependencyService.readRoleByName("ROLE-01", !INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID, result.getId());
  }

  @Test
  void testReadRoleByName_DeleteWithIncludeDeletedAndSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RoleEntity result = circularDependencyService.readRoleByName("ROLE-03", INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID_DELETED, result.getId());
  }

  @Test
  void testReadRoleByName_DeletedWithIncludeDeletedButNoSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readRoleByName("ROLE-03", INCLUDE_DELETED));
    assertEquals("Active Role Not Found for [ROLE-03]", exception.getMessage());
  }

  @Test
  void testReadRoleByName_DeletedNotIncludeDeleted() {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readRoleByName("ROLE-03", !INCLUDE_DELETED));
    assertEquals("Active Role Not Found for [ROLE-03]", exception.getMessage());
  }

  @Test
  void testReadRoleByName_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readRoleByName("ROLE-99", INCLUDE_DELETED));
    assertEquals("Role Not Found for [ROLE-99]", exception.getMessage());
  }

  @Test
  void testReadProfile_notDeleted() {
    ProfileEntity result = circularDependencyService.readProfile(ID, !INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID, result.getId());
  }

  @Test
  void testReadProfile_DeleteWithIncludeDeletedAndSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ProfileEntity result = circularDependencyService.readProfile(ID_DELETED, INCLUDE_DELETED);
    assertNotNull(result);
    assertEquals(ID_DELETED, result.getId());
  }

  @Test
  void testReadProfile_DeletedWithIncludeDeletedButNoSuperuser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readProfile(ID_DELETED, INCLUDE_DELETED));
    assertEquals(
        String.format("Active Profile Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadProfile_DeletedNotIncludeDeleted() {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> circularDependencyService.readProfile(ID_DELETED, !INCLUDE_DELETED));
    assertEquals(
        String.format("Active Profile Not Found for [%s]", ID_DELETED), exception.getMessage());
  }

  @Test
  void testReadProfile_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readProfile(ID_NOT_FOUND, INCLUDE_DELETED));
    assertEquals("Profile Not Found for [99]", exception.getMessage());
  }
}
