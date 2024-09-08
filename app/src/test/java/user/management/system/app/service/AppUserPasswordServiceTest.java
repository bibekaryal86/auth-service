package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import helper.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotActiveException;
import user.management.system.app.exception.UserNotActiveException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.exception.UserNotValidatedException;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsAppUserId;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.repository.AppTokenRepository;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.repository.AppsAppUserRepository;
import user.management.system.app.repository.AppsRepository;
import user.management.system.app.util.JwtUtils;
import user.management.system.app.util.PasswordUtils;

public class AppUserPasswordServiceTest extends BaseTest {

  private static int appUserId;
  private static String appId;
  private static UserLoginRequest userLoginRequest;

  private static final String USER_EMAIL = "new@email.com";
  private static final String USER_EMAIL_ENCODED = "very-encoded-email-address";
  private static final String OLD_PASSWORD = "old_password";
  private static final String NEW_PASSWORD = "new_password";

  @Autowired private AppUserPasswordService appUserPasswordService;
  @Autowired private PasswordUtils passwordUtils;
  @Autowired private AppUserRepository appUserRepository;
  @Autowired private AppsRepository appsRepository;

  @BeforeAll
  static void setUp(
      @Autowired AppUserRepository appUserRepository,
      @Autowired AppsAppUserRepository appsAppUserRepository,
      @Autowired PasswordUtils passwordUtils) {
    AppsEntity appsEntity = TestData.getAppsEntities().getLast();

    AppUserEntity appUserEntity = TestData.getAppUserEntities().getLast();
    appUserEntity.setEmail(USER_EMAIL);
    appUserEntity.setPassword(passwordUtils.hashPassword(OLD_PASSWORD));
    appUserEntity.setId(null);
    appUserEntity.setIsValidated(true);
    appUserEntity = appUserRepository.save(appUserEntity);

    appUserId = appUserEntity.getId();
    appId = appsEntity.getId();
    userLoginRequest = new UserLoginRequest(USER_EMAIL, OLD_PASSWORD);

    AppsAppUserEntity appsAppUserEntity = new AppsAppUserEntity();
    appsAppUserEntity.setApp(appsEntity);
    appsAppUserEntity.setAppUser(appUserEntity);
    appsAppUserEntity.setId(new AppsAppUserId(appId, appUserId));
    appsAppUserEntity.setAssignedDate(LocalDateTime.now());
    appsAppUserRepository.save(appsAppUserEntity);
  }

  @AfterAll
  static void tearDown(
      @Autowired AppUserRepository appUserRepository,
      @Autowired AppsAppUserRepository appsAppUserRepository,
      @Autowired AppTokenRepository appTokenRepository) {
    appTokenRepository.deleteAll();
    appsAppUserRepository.deleteById(new AppsAppUserId(appId, appUserId));
    appUserRepository.deleteById(appUserId);
  }

  @Test
  void testResetUser() {
    AppUserEntity appUserEntity =
        appUserPasswordService.resetUser(appId, new UserLoginRequest(USER_EMAIL, NEW_PASSWORD));
    assertNotNull(appUserEntity);
    assertFalse(passwordUtils.verifyPassword(OLD_PASSWORD, appUserEntity.getPassword()));
    assertTrue(passwordUtils.verifyPassword(NEW_PASSWORD, appUserEntity.getPassword()));

    // reset so that other test cases pass
    appUserEntity =
        appUserPasswordService.resetUser(appId, new UserLoginRequest(USER_EMAIL, OLD_PASSWORD));
    assertNotNull(appUserEntity);
    assertTrue(passwordUtils.verifyPassword(OLD_PASSWORD, appUserEntity.getPassword()));
    assertFalse(passwordUtils.verifyPassword(NEW_PASSWORD, appUserEntity.getPassword()));
  }

  @Test
  void testValidateAndResetUser() {
    AppUserEntity appUserEntity = appUserRepository.findById(appUserId).orElse(null);
    assertNotNull(appUserEntity);

    // set it as false for testing
    appUserEntity.setIsValidated(false);
    appUserRepository.save(appUserEntity);

    try (MockedStatic<JwtUtils> mockedStatic = mockStatic(JwtUtils.class)) {
      mockedStatic
          .when(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED))
          .thenReturn(USER_EMAIL);

      appUserEntity = appUserPasswordService.validateAndResetUser(appId, USER_EMAIL_ENCODED, false);
      assertNotNull(appUserEntity);
      assertFalse(appUserEntity.getIsValidated());

      appUserEntity = appUserPasswordService.validateAndResetUser(appId, USER_EMAIL_ENCODED, true);
      assertNotNull(appUserEntity);
      assertTrue(appUserEntity.getIsValidated());

      mockedStatic.verify(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED), times(2));
    }
  }

  @Test
  void testLoginUser_Success() {
    UserLoginResponse userLoginResponse = appUserPasswordService.loginUser(appId, userLoginRequest);
    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getRToken());
  }

  @Test
  void testLoginUser_Failure() {
    UserNotAuthorizedException exception =
        assertThrows(
            UserNotAuthorizedException.class,
            () ->
                appUserPasswordService.loginUser(
                    appId, new UserLoginRequest(USER_EMAIL, NEW_PASSWORD)));
    assertEquals(
        "User Unauthorized, username and/or password not found in the system...",
        exception.getMessage());
  }

  @Test
  void testLoginUser_DeletedApp() {
    AppsEntity appsEntity = appsRepository.findById(appId).orElse(null);
    assertNotNull(appsEntity);
    appsEntity.setDeletedDate(LocalDateTime.now());
    appsRepository.save(appsEntity);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> appUserPasswordService.loginUser(appId, userLoginRequest));
    assertEquals(String.format("Active App Not Found for [%s]", appId), exception.getMessage());

    // reset
    appsEntity.setDeletedDate(null);
    appsRepository.save(appsEntity);
    assertNotNull(appsEntity);
  }

  @Test
  void testLoginUser_DeletedAppUser() {
    AppUserEntity appUserEntity = appUserRepository.findById(appUserId).orElse(null);
    assertNotNull(appUserEntity);
    appUserEntity.setDeletedDate(LocalDateTime.now());
    appUserRepository.save(appUserEntity);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> appUserPasswordService.loginUser(appId, userLoginRequest));
    assertEquals(
        String.format("Active User Not Found for [%s]", USER_EMAIL), exception.getMessage());

    // reset
    appUserEntity.setDeletedDate(null);
    appUserRepository.save(appUserEntity);
    assertNotNull(appUserEntity);
  }

  @Test
  void testLoginUser_NotValidatedAppUser() {
    AppUserEntity appUserEntity = appUserRepository.findById(appUserId).orElse(null);
    assertNotNull(appUserEntity);
    appUserEntity.setIsValidated(false);
    appUserRepository.save(appUserEntity);

    UserNotValidatedException exception =
        assertThrows(
            UserNotValidatedException.class,
            () -> appUserPasswordService.loginUser(appId, userLoginRequest));
    assertEquals(
        "User not validated, please check your email for instructions to validate account!",
        exception.getMessage());

    // reset
    appUserEntity.setIsValidated(true);
    appUserRepository.save(appUserEntity);
    assertNotNull(appUserEntity);
  }

  @Test
  void testLoginUser_NotActiveAppUser() {
    AppUserEntity appUserEntity = appUserRepository.findById(appUserId).orElse(null);
    assertNotNull(appUserEntity);
    appUserEntity.setStatus("PENDING");
    appUserRepository.save(appUserEntity);

    UserNotActiveException exception =
        assertThrows(
            UserNotActiveException.class,
            () -> appUserPasswordService.loginUser(appId, userLoginRequest));
    assertEquals(
        "User is not active, please revalidate or reset your account!", exception.getMessage());

    // reset
    appUserEntity.setStatus("ACTIVE");
    appUserRepository.save(appUserEntity);
    assertNotNull(appUserEntity);
  }
}
