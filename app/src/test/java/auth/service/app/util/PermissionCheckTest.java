package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.model.token.AuthTokenRole;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PermissionCheckTest extends BaseTest {

  private static Authentication authentication;
  private static AuthToken authToken;
  private static List<AppUserEntity> appUserEntities;

  @Mock private SecurityContext securityContext;

  @Autowired private PermissionCheck permissionCheck;

  @BeforeAll
  public static void setUpBeforeAll() {
    authToken = TestData.getAuthToken();
    appUserEntities = TestData.getAppUserEntities();
  }

  @BeforeEach
  public void setUpBeforeEach() {
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(APP_USER_EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  @Test
  void testCheckPermission() {
    CheckPermission checkPermission = mock(CheckPermission.class);
    when(checkPermission.value()).thenReturn(new String[] {"Permission One", "Permission Two"});

    assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermission));
    verify(securityContext).getAuthentication();
  }

  @Test
  void testCheckPermission_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().name(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authentication =
        new TestingAuthenticationToken(APP_USER_EMAIL, authToken, Collections.emptyList());

    CheckPermission checkPermission = mock(CheckPermission.class);
    when(checkPermission.value())
        .thenReturn(new String[] {"Permission OneOne", "Permission TwoTwo"});
    when(securityContext.getAuthentication()).thenReturn(authentication);

    assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermission));
  }

  @Test
  void testCheckPermission_Failure() {
    CheckPermission checkPermission = mock(CheckPermission.class);
    when(checkPermission.value()).thenReturn(new String[] {"Permission Two"});

    CheckPermissionException exception =
        assertThrows(
            CheckPermissionException.class, () -> permissionCheck.checkPermission(checkPermission));

    assertEquals(
        "Permission Denied: User does not have required permissions...", exception.getMessage());
  }

  @Test
  void testCheckProfileAccess_Email() {
    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(APP_USER_EMAIL, 0));
  }

  @Test
  void testCheckProfileAccess_Id() {
    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(null, 1));
  }

  @Test
  void testCheckProfile_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().name(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authentication =
        new TestingAuthenticationToken(APP_USER_EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess("some@email.com", 99));
    verify(securityContext).getAuthentication();
  }

  @Test
  void testCheckProfileAccess_Failure() {
    CheckPermissionException exception =
        assertThrows(
            CheckPermissionException.class,
            () -> permissionCheck.checkProfileAccess("some@email.com", 99));

    assertEquals(
        "Permission Denied: User does not have required permissions to user entity...",
        exception.getMessage());
  }

  @Test
  void testFilterAppUserListByAccess() {
    List<AppUserEntity> appUserEntitiesFiltered =
        permissionCheck.filterAppUserListByAccess(appUserEntities);
    assertEquals(1, appUserEntitiesFiltered.size());
    assertEquals(authToken.getUser().getId(), appUserEntitiesFiltered.getFirst().getId());
  }

  @Test
  void testFilterAppUserListByAccess_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().name(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authentication =
        new TestingAuthenticationToken(APP_USER_EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    List<AppUserEntity> appUserEntitiesFiltered =
        permissionCheck.filterAppUserListByAccess(appUserEntities);
    assertEquals(appUserEntities.size(), appUserEntitiesFiltered.size());
  }

  @Test
  void testFilterAppUserListByAccess_Failure() {
    when(securityContext.getAuthentication()).thenReturn(null);

    assertThrows(
        CheckPermissionException.class,
        () -> permissionCheck.filterAppUserListByAccess(appUserEntities));
  }
}
