package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import user.management.system.BaseTest;
import user.management.system.app.exception.CheckPermissionException;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.model.token.AuthTokenRole;

public class PermissionCheckTest extends BaseTest {

  private static final String TEST_EMAIL = "firstlast@one.com";

  private static Authentication authentication;
  private static AuthToken authToken;
  private static List<AppUserEntity> appUserEntities;

  @Mock private SecurityContext securityContext;

  @Autowired private PermissionCheck permissionCheck;

  @BeforeAll
  public static void setupAll() {
    authToken = TestData.getAuthToken();
    appUserEntities = TestData.getAppUserEntities();
  }

  @BeforeEach
  public void setUpEach() {
    SecurityContextHolder.setContext(securityContext);
    authentication = new TestingAuthenticationToken(TEST_EMAIL, authToken, Collections.emptyList());
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
        List.of(AuthTokenRole.builder().name(ConstantUtils.APP_ROLE_NAME_SUPERUSER).build()));
    authentication = new TestingAuthenticationToken(TEST_EMAIL, authToken, Collections.emptyList());

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
  void testCanUserAccessAppUser_Email() {
    assertDoesNotThrow(() -> permissionCheck.canUserAccessAppUser(TEST_EMAIL, 0));
  }

  @Test
  void testCanUserAccessAppUser_Id() {
    assertDoesNotThrow(() -> permissionCheck.canUserAccessAppUser(null, 1));
  }

  @Test
  void testCanUserAccessAppUser_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().name(ConstantUtils.APP_ROLE_NAME_SUPERUSER).build()));
    authentication = new TestingAuthenticationToken(TEST_EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    assertDoesNotThrow(() -> permissionCheck.canUserAccessAppUser("some@email.com", 99));
    verify(securityContext).getAuthentication();
  }

  @Test
  void testCanUserAccessAppUser_Failure() {
    CheckPermissionException exception =
        assertThrows(
            CheckPermissionException.class,
            () -> permissionCheck.canUserAccessAppUser("some@email.com", 99));

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
        List.of(AuthTokenRole.builder().name(ConstantUtils.APP_ROLE_NAME_SUPERUSER).build()));
    authentication = new TestingAuthenticationToken(TEST_EMAIL, authToken, Collections.emptyList());
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
