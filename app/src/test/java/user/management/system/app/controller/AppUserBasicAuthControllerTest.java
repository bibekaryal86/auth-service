package user.management.system.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppTokenRequest;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsAppUserId;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.repository.AppTokenRepository;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.repository.AppUserRoleRepository;
import user.management.system.app.repository.AppsAppUserRepository;
import user.management.system.app.repository.AppsRepository;
import user.management.system.app.service.AuditService;
import user.management.system.app.service.EmailService;
import user.management.system.app.util.JwtUtils;
import user.management.system.app.util.PasswordUtils;

public class AppUserBasicAuthControllerTest extends BaseTest {

  private static final String NEW_USER_NEW_EMAIL = "new-user@new-email.com";
  private static final String NEW_USER_NEW_PASSWORD = "new-user-new-password";
  private static AppUserEntity appUserEntity;
  private static AppsEntity appsEntity;
  private static AppTokenEntity appTokenEntity;

  @MockBean private AuditService auditService;
  @MockBean private EmailService emailService;
  @MockBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private AppUserRepository appUserRepository;
  @Autowired private AppsAppUserRepository appsAppUserRepository;
  @Autowired private AppUserRoleRepository appUserRoleRepository;
  @Autowired private AppTokenRepository appTokenRepository;

  @BeforeAll
  static void setUp(
      @Autowired PasswordUtils passwordUtils,
      @Autowired AppUserRepository appUserRepository,
      @Autowired AppsRepository appsRepository,
      @Autowired AppsAppUserRepository appsAppUserRepository,
      @Autowired AppTokenRepository appTokenRepository) {
    String newUserNewPassword = passwordUtils.hashPassword(NEW_USER_NEW_PASSWORD);
    AppUserEntity appUserEntitySetup = TestData.getNewAppUserEntity();
    appUserEntitySetup.setEmail(NEW_USER_NEW_EMAIL);
    appUserEntitySetup.setPassword(newUserNewPassword);
    appUserEntity = appUserRepository.save(appUserEntitySetup);

    AppsEntity appsEntitySetup = TestData.getNewAppsEntity();
    appsEntity = appsRepository.save(appsEntitySetup);

    AppsAppUserEntity appsAppUserEntitySetup = new AppsAppUserEntity();
    appsAppUserEntitySetup.setAssignedDate(LocalDateTime.now());
    appsAppUserEntitySetup.setApp(appsEntity);
    appsAppUserEntitySetup.setAppUser(appUserEntity);
    appsAppUserEntitySetup.setId(new AppsAppUserId(appsEntity.getId(), appUserEntity.getId()));
    appsAppUserRepository.save(appsAppUserEntitySetup);

    AppUserDto appUserDtoSetup = TestData.getAppUserDto();
    appUserDtoSetup.setEmail(NEW_USER_NEW_EMAIL);
    String accessToken =
        JwtUtils.encodeAuthCredentials(appsEntitySetup.getId(), appUserDtoSetup, 1000 * 60 * 15);
    String refreshToken =
        JwtUtils.encodeAuthCredentials(
            appsEntitySetup.getId(), appUserDtoSetup, 1000 * 60 * 60 * 24);
    AppTokenEntity appTokenEntitySetup = new AppTokenEntity();
    appTokenEntitySetup.setUser(appUserEntity);
    appTokenEntitySetup.setAccessToken(accessToken);
    appTokenEntitySetup.setRefreshToken(refreshToken);
    appTokenEntity = appTokenRepository.save(appTokenEntitySetup);
  }

  @AfterAll
  static void tearDownAfterAll(
      @Autowired AppUserRepository appUserRepository,
      @Autowired AppsRepository appsRepository,
      @Autowired AppsAppUserRepository appsAppUserRepository,
      @Autowired AppTokenRepository appTokenRepository) {
    appTokenRepository.deleteAll();
    appsAppUserRepository.deleteById(new AppsAppUserId(appsEntity.getId(), appUserEntity.getId()));
    appsRepository.deleteById(appsEntity.getId());
    appUserRepository.deleteById(appUserEntity.getId());
  }

  @AfterEach
  void tearDownAfterEach() {
    reset(auditService, emailService, applicationEventPublisher);
  }

  @Test
  void testCreateAppUser_Success() {
    AppUserRequest appUserRequest = TestData.getAppUserRequest(null);

    AppUserResponse appUserResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/create", APP_ID))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appUserRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<AppUserEntity> appUserEntityCaptor =
        ArgumentCaptor.forClass(AppUserEntity.class);
    ArgumentCaptor<Boolean> guestUserCaptor = ArgumentCaptor.forClass(Boolean.class);

    verify(auditService, after(100).times(1))
        .auditAppUserCreate(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            appUserEntityCaptor.capture(),
            guestUserCaptor.capture());

    // cleanup
    int appUserId = appUserResponse.getUsers().getFirst().getId();
    appsAppUserRepository.deleteById(new AppsAppUserId(APP_ID, appUserId));
    appUserRoleRepository.deleteById(new AppUserRoleId(appUserId, 7));
    appUserRepository.deleteById(appUserResponse.getUsers().getFirst().getId());
  }

  @Test
  void testCreateAppUser_FailureWithNoBasicAuth() {
    AppUserEntity appUserEntity = TestData.getNewAppUserEntity();
    AppUserRequest appUserRequest = new AppUserRequest();
    BeanUtils.copyProperties(appUserEntity, appUserRequest, "id", "isValidated", "addresses");
    appUserRequest.setGuestUser(true);

    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/create", appsEntity.getId()))
        .bodyValue(appUserRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppUser_Failure_BadRequest() {
    AppUserRequest appUserRequest = new AppUserRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/create", appsEntity.getId()))
            .bodyValue(appUserRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(
        responseStatusInfo.getErrMsg().contains("First Name is required")
            && responseStatusInfo.getErrMsg().contains("Last Name is required")
            && responseStatusInfo.getErrMsg().contains("Email is required")
            && responseStatusInfo.getErrMsg().contains("Status is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testLoginAppUser_Success() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/login", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(userLoginRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getRToken());
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getUser());
    assertEquals(userLoginRequest.getEmail(), userLoginResponse.getUser().getEmail());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppUserLoginSuccess(
            requestCaptor.capture(), appIdCaptor.capture(), idCaptor.capture());
  }

  @Test
  void testLoginAppUser_Failure() {
    UserLoginRequest userLoginRequest = new UserLoginRequest(NEW_USER_NEW_EMAIL, "some-password");

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/login", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(userLoginRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getUser());

    verify(auditService, after(100).times(1)).auditAppUserLoginFailure(any(), any(), any(), any());
  }

  @Test
  void testLoginAppUser_FailureWithNoBasicAuth() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/login", appsEntity.getId()))
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testLoginAppUser_Failure_BadRequest() {
    UserLoginRequest userLoginRequest = new UserLoginRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/login", appsEntity.getId()))
            .bodyValue(userLoginRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(responseStatusInfo.getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testRefreshToken_Success() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getRToken());
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getUser());
    assertEquals(appUserEntity.getId(), userLoginResponse.getUser().getId());

    verify(auditService, after(100).times(1)).auditAppUserTokenRefreshSuccess(any(), any(), any());
  }

  @Test
  void testRefreshToken_Failure_NoRefreshToken() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(appUserEntity.getId(), appTokenEntity.getAccessToken(), "");

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getUser());
    assertTrue(userLoginResponse.getResponseStatusInfo().getErrMsg().contains("is Missing in"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserTokenRefreshFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testRefreshToken_Failure_InvalidRefreshToken() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(), appTokenEntity.getAccessToken(), "an.invalid.refresh.token");

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getUser());
    assertTrue(
        userLoginResponse.getResponseStatusInfo().getErrMsg().contains("Invalid Auth Credentials"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserTokenRefreshFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testRefreshToken_Failure_RefreshTokenNotFound() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getAccessToken());

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getUser());
    assertTrue(userLoginResponse.getResponseStatusInfo().getErrMsg().contains("Token Not Found"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserTokenRefreshFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testRefreshToken_Failure_DeletedToken() {
    // setup
    appTokenEntity.setDeletedDate(LocalDateTime.now());
    appTokenRepository.save(appTokenEntity);

    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());

    UserLoginResponse userLoginResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(UserLoginResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getUser());
    assertTrue(userLoginResponse.getResponseStatusInfo().getErrMsg().contains("Deleted Token"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserTokenRefreshFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());

    // reset
    appTokenEntity.setDeletedDate(null);
    appTokenRepository.save(appTokenEntity);
  }

  @Test
  void testRefreshToken_FailureWithNoBasicAuth() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
        .bodyValue(appTokenRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRefreshToken_Failure_BadRequest() {
    AppTokenRequest appTokenRequest = new AppTokenRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .bodyValue(appTokenRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(responseStatusInfo.getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testLogout_Success() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(appTokenRequest)
        .exchange()
        .expectStatus()
        .isNoContent();
    verify(auditService, after(100).times(1)).auditAppUserLogoutSuccess(any(), any(), any());
  }

  @Test
  void testLogout_Failure_NoAccessToken() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(appUserEntity.getId(), "", appTokenEntity.getRefreshToken());

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("is Missing in"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserLogoutFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testLogout_Failure_InvalidAccessToken() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(), appTokenEntity.getAccessToken(), "an.invalid.access.token");

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("Invalid Auth Credentials"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserLogoutFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testLogout_Failure_AccessTokenNotFound() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getRefreshToken(),
            appTokenEntity.getAccessToken());

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("Token Not Found"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserLogoutFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());
  }

  @Test
  void testLogout_Failure_DeletedToken() {
    // setup
    appTokenEntity.setDeletedDate(LocalDateTime.now());
    appTokenRepository.save(appTokenEntity);

    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(appTokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("Deleted Token"));

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> appIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
    verify(auditService, after(100).times(1))
        .auditAppUserLogoutFailure(
            requestCaptor.capture(),
            appIdCaptor.capture(),
            userIdCaptor.capture(),
            exceptionCaptor.capture());

    // reset
    appTokenEntity.setDeletedDate(null);
    appTokenRepository.save(appTokenEntity);
  }

  @Test
  void testLogout_FailureWithNoBasicAuth() {
    AppTokenRequest appTokenRequest =
        new AppTokenRequest(
            appUserEntity.getId(),
            appTokenEntity.getAccessToken(),
            appTokenEntity.getRefreshToken());
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/logout", appsEntity.getId()))
        .bodyValue(appTokenRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testLogout_Failure_BadRequest() {
    AppTokenRequest appTokenRequest = new AppTokenRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/refresh", appsEntity.getId()))
            .bodyValue(appTokenRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(responseStatusInfo.getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testResetAppUser_Success() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest(NEW_USER_NEW_EMAIL, "new-user-newer-password");
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/reset", appsEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(auditService, after(100).times(1)).auditAppUserResetSuccess(any(), any(), any());

    // reset
    userLoginRequest = new UserLoginRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/reset", appsEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void testResetAppUser_Failure() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest("some-old@email.com", "new-user-newer-password");

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/reset", appsEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(userLoginRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("User Not Found"));

    verify(auditService).auditAppUserResetFailure(any(), any(), any(), any());
  }

  @Test
  void testResetAppUser_FailureWithNoBasicAuth() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    webTestClient
        .post()
        .uri(String.format("/api/v1/basic_app_users/user/%s/reset", appsEntity.getId()))
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testResetAppUser_Failure_BadRequest() {
    UserLoginRequest userLoginRequest = new UserLoginRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri(String.format("/api/v1/basic_app_users/user/%s/reset", appsEntity.getId()))
            .bodyValue(userLoginRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(responseStatusInfo.getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testValidateInit_Success() {
    doNothing().when(emailService).sendUserValidationEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/basic_app_users/user/%s/validate_init?email=%s",
                appsEntity.getId(), NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(auditService, after(100).times(1)).auditAppUserValidateInit(any(), any(), any());
    verify(emailService, after(100).times(1))
        .sendUserValidationEmail(
            any(AppsEntity.class), any(AppUserEntity.class), any(String.class));
  }

  @Test
  void testValidateInit_Failure() {
    doThrow(new RuntimeException("something happened"))
        .when(emailService)
        .sendUserValidationEmail(any(), any(), any());

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/basic_app_users/user/%s/validate_init?email=%s",
                    appsEntity.getId(), NEW_USER_NEW_EMAIL))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("something happened"));

    verify(auditService, after(100).times(1))
        .auditAppUserValidateFailure(any(), any(), any(), any());
    verify(emailService, after(100).times(1))
        .sendUserValidationEmail(
            any(AppsEntity.class), any(AppUserEntity.class), any(String.class));
  }

  @Test
  void testValidateInit_FailureWithNoBasicAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/basic_app_users/user/%s/validate_init?email=%s",
                appsEntity.getId(), NEW_USER_NEW_EMAIL))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService, emailService);
  }

  @Test
  void testResetInit_Success() {
    doNothing().when(emailService).sendUserResetEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/basic_app_users/user/%s/reset_init?email=%s",
                appsEntity.getId(), NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(auditService, after(100).times(1)).auditAppUserResetInit(any(), any(), any());
    verify(emailService, after(100).times(1))
        .sendUserResetEmail(any(AppsEntity.class), any(AppUserEntity.class), any(String.class));
  }

  @Test
  void testResetInit_Failure() {
    doThrow(new RuntimeException("something happened"))
        .when(emailService)
        .sendUserResetEmail(any(), any(), any());

    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/basic_app_users/user/%s/reset_init?email=%s",
                    appsEntity.getId(), NEW_USER_NEW_EMAIL))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertTrue(responseStatusInfo.getErrMsg().contains("something happened"));

    verify(auditService, after(100).times(1)).auditAppUserResetFailure(any(), any(), any(), any());
    verify(emailService, after(100).times(1))
        .sendUserResetEmail(any(AppsEntity.class), any(AppUserEntity.class), any(String.class));
  }

  @Test
  void testResetInit_FailureWithNoBasicAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/basic_app_users/user/%s/reset_init?email=%s",
                appsEntity.getId(), NEW_USER_NEW_EMAIL))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService, emailService);
  }
}
