package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.entity.ProfileEntity;
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
  private static List<ProfileEntity> profileEntities;

  @Mock private SecurityContext securityContext;

  @Autowired private PermissionCheck permissionCheck;

  @BeforeAll
  public static void setUpBeforeAll() {
    authToken = TestData.getAuthToken();
    profileEntities = TestData.getProfileEntities();
  }

  @BeforeEach
  public void setUpBeforeEach() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication = new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  @Test
  void testCheckPermission() {
    CheckPermission checkPermission = mock(CheckPermission.class);
    when(checkPermission.value())
        .thenReturn(new String[] {"PERMISSION-1", "SOME_OTHER_PERMISSION"});

    assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermission));
    verify(securityContext).getAuthentication();
  }

  @Test
  void testCheckPermission_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().roleName(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authToken.setSuperUser(true);
    authentication = new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());

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
        "Permission Denied: Profile does not have required permissions...", exception.getMessage());
  }

  @Test
  void testCheckProfileAccess_Email() {
    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(EMAIL, 0));
  }

  @Test
  void testCheckProfileAccess_Id() {
    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(null, 1));
  }

  @Test
  void testCheckProfile_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().roleName(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authToken.setSuperUser(true);
    authentication = new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    assertDoesNotThrow(() -> permissionCheck.checkProfileAccess("some@email.com", ID_NOT_FOUND));
    verify(securityContext).getAuthentication();
  }

  @Test
  void testCheckProfileAccess_Failure() {
    CheckPermissionException exception =
        assertThrows(
            CheckPermissionException.class,
            () -> permissionCheck.checkProfileAccess("some@email.com", ID_NOT_FOUND));

    assertEquals(
        "Permission Denied: Profile does not have required permissions to profile entity...",
        exception.getMessage());
  }

  @Test
  void testFilterProfileListByAccess() {
    List<ProfileEntity> profileEntitiesFiltered =
        permissionCheck.filterProfileListByAccess(profileEntities);
    assertEquals(1, profileEntitiesFiltered.size());
    assertEquals(authToken.getProfile().getId(), profileEntitiesFiltered.getFirst().getId());
  }

  @Test
  void testFilterProfileListByAccess_SuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setRoles(
        List.of(AuthTokenRole.builder().roleName(ConstantUtils.ROLE_NAME_SUPERUSER).build()));
    authToken.setSuperUser(true);
    authentication = new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    List<ProfileEntity> profileEntitiesFiltered =
        permissionCheck.filterProfileListByAccess(profileEntities);
    assertEquals(profileEntitiesFiltered.size(), profileEntities.size());
  }

  @Test
  void testFilterProfileListByAccess_Failure() {
    when(securityContext.getAuthentication()).thenReturn(null);

    assertThrows(
        CheckPermissionException.class,
        () -> permissionCheck.filterProfileListByAccess(profileEntities));
  }
}
