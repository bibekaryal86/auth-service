package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.entity.ProfileEntity;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.exception.CheckPermissionException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PermissionCheck Unit Tests")
class PermissionCheckTest {

  @InjectMocks private PermissionCheck permissionCheck;

  private MockedStatic<CommonUtils> commonUtilsMock;
  private AuthToken authToken;
  private CheckPermission checkPermissionAnnotation;

  private static final String TEST_EMAIL = "profile@one.com";
  private static final String TEST_PERMISSION = "Permission 1";
  private static final Long TEST_ID = 1L;
  private static final String PERMISSION_READ = "READ";
  private static final String PERMISSION_WRITE = "WRITE";
  private static final String PERMISSION_DELETE = "DELETE";

  @BeforeEach
  void setUp() {
    commonUtilsMock = mockStatic(CommonUtils.class);
    authToken = TestData.getAuthToken();
    checkPermissionAnnotation = mock(CheckPermission.class);
  }

  @AfterEach
  void tearDown() {
    if (commonUtilsMock != null) {
      commonUtilsMock.close();
    }
  }

  @Nested
  @DisplayName("checkPermission() - Aspect method tests")
  class CheckPermissionAspectTests {

    @Test
    @DisplayName("Should allow access when user is super user")
    void shouldAllowAccessWhenUserIsSuperUser() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {PERMISSION_READ});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(true);

      assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermissionAnnotation));

      commonUtilsMock.verify(CommonUtils::getAuthentication);
    }

    @Test
    @DisplayName("Should allow access when user has required permission")
    void shouldAllowAccessWhenUserHasRequiredPermission() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {TEST_PERMISSION});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermissionAnnotation));
    }

    @Test
    @DisplayName("Should allow access when user has one of multiple required permissions")
    void shouldAllowAccessWhenUserHasOneOfMultipleRequiredPermissions() {
      when(checkPermissionAnnotation.value())
          .thenReturn(new String[] {PERMISSION_READ, PERMISSION_WRITE, PERMISSION_DELETE, TEST_PERMISSION});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      assertDoesNotThrow(() -> permissionCheck.checkPermission(checkPermissionAnnotation));
    }

    @Test
    @DisplayName("Should deny access when user does not have required permission")
    void shouldDenyAccessWhenUserDoesNotHaveRequiredPermission() {
      AuthToken.AuthTokenPermission permission =
          new AuthToken.AuthTokenPermission(TEST_ID, PERMISSION_READ);

      when(checkPermissionAnnotation.value()).thenReturn(new String[] {PERMISSION_WRITE});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermission(checkPermissionAnnotation));

      assertEquals(
          "Permission Denied: Profile does not have required permissions...",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should deny access when user has no permissions")
    void shouldDenyAccessWhenUserHasNoPermissions() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {PERMISSION_READ});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermission(checkPermissionAnnotation));

      assertEquals(
          "Permission Denied: Profile does not have required permissions...",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should throw CheckPermissionException when authentication fails")
    void shouldThrowCheckPermissionExceptionWhenAuthenticationFails() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {PERMISSION_READ});
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new CheckPermissionException("Not authenticated"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermission(checkPermissionAnnotation));

      assertEquals("Permission Denied: Not authenticated", exception.getMessage());
    }

    @Test
    @DisplayName("Should wrap non-CheckPermissionException in CheckPermissionException")
    void shouldWrapNonCheckPermissionExceptionInCheckPermissionException() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {PERMISSION_READ});
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new RuntimeException("Unexpected error"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermission(checkPermissionAnnotation));

      assertEquals("Permission Denied: Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty required permissions array")
    void shouldHandleEmptyRequiredPermissionsArray() {
      when(checkPermissionAnnotation.value()).thenReturn(new String[] {});
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermission(checkPermissionAnnotation));

      assertEquals("Permission Denied: Profile does not have required permissions...", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("checkPermissionDuplicate() tests")
  class CheckPermissionDuplicateTests {

    @Test
    @DisplayName("Should return all permissions as true for super user")
    void shouldReturnAllPermissionsAsTrueForSuperUser() {
      List<String> requiredPermissions = List.of(PERMISSION_READ, PERMISSION_WRITE, TEST_PERMISSION);
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(true);

      Map<String, Boolean> result = permissionCheck.checkPermissionDuplicate(requiredPermissions);

      assertNotNull(result);
      assertEquals(4, result.size());
      assertFalse(result.get(PERMISSION_READ));
      assertFalse(result.get(PERMISSION_WRITE));
      assertTrue(result.get(ConstantUtils.ROLE_NAME_SUPERUSER));
      assertTrue(result.get(TEST_PERMISSION));
    }

    @Test
    @DisplayName("Should return correct permission status for regular user")
    void shouldReturnCorrectPermissionStatusForRegularUser() {
      List<String> requiredPermissions = List.of(PERMISSION_READ, PERMISSION_WRITE, TEST_PERMISSION);
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      Map<String, Boolean> result = permissionCheck.checkPermissionDuplicate(requiredPermissions);

      assertNotNull(result);
      assertEquals(3, result.size());
        assertTrue(result.get(TEST_PERMISSION));
      assertFalse(result.get(PERMISSION_READ));
      assertFalse(result.get(PERMISSION_WRITE));
    }

    @Test
    @DisplayName("Should return all false for user with no permissions")
    void shouldReturnAllFalseForUserWithNoPermissions() {
      List<String> requiredPermissions = List.of(PERMISSION_READ, PERMISSION_WRITE);
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      Map<String, Boolean> result = permissionCheck.checkPermissionDuplicate(requiredPermissions);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertFalse(result.get(PERMISSION_READ));
      assertFalse(result.get(PERMISSION_WRITE));
    }

    @Test
    @DisplayName("Should handle empty required permissions list")
    void shouldHandleEmptyRequiredPermissionsList() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      Map<String, Boolean> result = permissionCheck.checkPermissionDuplicate(List.of());

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple permissions correctly")
    void shouldHandleMultiplePermissionsCorrectly() {
      AuthToken.AuthTokenPermission permission1 =
          new AuthToken.AuthTokenPermission(TEST_ID, PERMISSION_READ);
      AuthToken.AuthTokenPermission permission2 =
          new AuthToken.AuthTokenPermission(TEST_ID, PERMISSION_WRITE);
        AuthToken authTokenNew = new AuthToken(authToken.getPlatform(), authToken.getProfile(), authToken.getRoles(), List.of(permission1, permission2), false);


      List<String> requiredPermissions =
          List.of(PERMISSION_READ, PERMISSION_WRITE, PERMISSION_DELETE);
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authTokenNew);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authTokenNew)).thenReturn(false);

      Map<String, Boolean> result = permissionCheck.checkPermissionDuplicate(requiredPermissions);

      assertNotNull(result);
      assertEquals(3, result.size());
      assertTrue(result.get(PERMISSION_READ));
        assertTrue(result.get(PERMISSION_WRITE));
      assertFalse(result.get(PERMISSION_DELETE));
    }

    @Test
    @DisplayName("Should throw CheckPermissionException when authentication fails")
    void shouldThrowCheckPermissionExceptionWhenAuthenticationFails() {
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new CheckPermissionException("Not authenticated"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermissionDuplicate(List.of(PERMISSION_READ)));

      assertEquals("Permission Denied: Not authenticated", exception.getMessage());
    }

    @Test
    @DisplayName("Should wrap non-CheckPermissionException in CheckPermissionException")
    void shouldWrapNonCheckPermissionExceptionInCheckPermissionException() {
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new RuntimeException("Unexpected error"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkPermissionDuplicate(List.of(PERMISSION_READ)));

      assertEquals("Permission Denied: Unexpected error", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("checkProfileAccess() tests")
  class CheckProfileAccessTests {

    @Test
    @DisplayName("Should allow access for super user")
    void shouldAllowAccessForSuperUser() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(true);

      assertDoesNotThrow(() -> permissionCheck.checkProfileAccess("different@example.com", 999L));
    }

    @Test
    @DisplayName("Should allow access when email matches")
    void shouldAllowAccessWhenEmailMatches() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(TEST_EMAIL, 999L));
    }

    @Test
    @DisplayName("Should allow access when ID matches")
    void shouldAllowAccessWhenIdMatches() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      assertDoesNotThrow(
          () -> permissionCheck.checkProfileAccess("different@example.com", TEST_ID));
    }

    @Test
    @DisplayName("Should allow access when both email and ID match")
    void shouldAllowAccessWhenBothEmailAndIdMatch() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      assertDoesNotThrow(() -> permissionCheck.checkProfileAccess(TEST_EMAIL, TEST_ID));
    }

    @Test
    @DisplayName("Should deny access when neither email nor ID match")
    void shouldDenyAccessWhenNeitherEmailNorIdMatch() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkProfileAccess("different@example.com", 999L));

      assertEquals(
          "Permission Denied: Profile does not have required permissions to profile entity...",
          exception.getMessage());
    }

    @Test
    @DisplayName("Should throw CheckPermissionException when authentication fails")
    void shouldThrowCheckPermissionExceptionWhenAuthenticationFails() {
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new CheckPermissionException("Not authenticated"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkProfileAccess(TEST_EMAIL, TEST_ID));

      assertEquals("Permission Denied: Not authenticated", exception.getMessage());
    }

    @Test
    @DisplayName("Should wrap non-CheckPermissionException in CheckPermissionException")
    void shouldWrapNonCheckPermissionExceptionInCheckPermissionException() {
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new RuntimeException("Unexpected error"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.checkProfileAccess(TEST_EMAIL, TEST_ID));

      assertEquals("Permission Denied: Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void shouldHandleNullEmailGracefully() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class, () -> permissionCheck.checkProfileAccess(null, 999L));

      assertEquals(
          "Permission Denied: Profile does not have required permissions to profile entity...",
          exception.getMessage());
    }
  }

  @Nested
  @DisplayName("filterProfileListByAccess() tests")
  class FilterProfileListByAccessTests {

    @Test
    @DisplayName("Should return all profiles for super user")
    void shouldReturnAllProfilesForSuperUser() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(true);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(profiles);

      assertEquals(9, result.size());
      assertEquals(profiles, result);
    }

    @Test
    @DisplayName("Should filter profiles by email match for regular user")
    void shouldFilterProfilesByEmailMatchForRegularUser() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(profiles);

      assertEquals(1, result.size());
      assertEquals(TEST_EMAIL, result.get(0).getEmail());
    }

    @Test
    @DisplayName("Should filter profiles by ID match for regular user")
    void shouldFilterProfilesByIdMatchForRegularUser() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(profiles);

      assertEquals(1, result.size());
      assertEquals(TEST_ID, result.get(0).getId());
    }

    @Test
    @DisplayName("Should return empty list when no profiles match for regular user")
    void shouldReturnEmptyListWhenNoProfilesMatchForRegularUser() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();
      profiles.removeFirst();
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(profiles);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty profile list")
    void shouldHandleEmptyProfileList() {
      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(List.of());

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw CheckPermissionException when authentication fails")
    void shouldThrowCheckPermissionExceptionWhenAuthenticationFails() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();
      commonUtilsMock
          .when(CommonUtils::getAuthentication)
          .thenThrow(new RuntimeException("Not authenticated"));

      CheckPermissionException exception =
          assertThrows(
              CheckPermissionException.class,
              () -> permissionCheck.filterProfileListByAccess(profiles));

      assertEquals("Permission Denied: Not authenticated", exception.getMessage());
    }

    @Test
    @DisplayName("Should filter matching profiles")
    void shouldFilterMultipleMatchingProfiles() {
      List<ProfileEntity> profiles = TestData.getProfileEntities();

      commonUtilsMock.when(CommonUtils::getAuthentication).thenReturn(authToken);
      commonUtilsMock.when(() -> CommonUtils.isSuperUser(authToken)).thenReturn(false);

      List<ProfileEntity> result = permissionCheck.filterProfileListByAccess(profiles);

      assertEquals(1, result.size());
    }
  }
}
