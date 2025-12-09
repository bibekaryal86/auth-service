package integration.auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("integration")
@DisplayName("ProfileBasicAuthControllerTest Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProfileBasicAuthControllerTest extends BaseTest {

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformRepository platformRepository;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PlatformProfileRoleRepository pprRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private PasswordUtils passwordUtils;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EmailService emailService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;
  private static RoleEntity roleEntity;
  private static PlatformProfileRoleEntity pprEntity;

  private static final String PASSWORD = "password-1";

  // better to use BeforeAll and AfterAll
  // but did not work in Nested test classes
  // should revisit in future to put them in each Nested class
  @BeforeEach
  void setUp() {
    doNothing().when(emailService).sendProfilePasswordEmail(any(), any());
    doNothing().when(emailService).sendProfileValidationEmail(any(), any(), any());
    doNothing().when(emailService).sendProfileResetEmail(any(), any(), any());
    when(envServiceConnector.getBaseUrlForLinkInEmail())
        .thenReturn("https://base-url-for-link-in-email.com");

    platformEntity = TestData.getPlatformEntities().get(6);
    profileEntity = TestData.getProfileEntities().get(6);
    roleEntity = TestData.getRoleEntities().get(6);
    pprEntity =
        TestData.getPlatformProfileRoleEntity(platformEntity, profileEntity, roleEntity, null);
    pprRepository.save(pprEntity);

    // hash password
    profileEntity.setPassword(passwordUtils.hashPassword(PASSWORD));
    profileEntity.setIsValidated(true);
    profileEntity.setLoginAttempts(2);
    profileEntity.setLastLogin(null);
    profileRepository.save(profileEntity);
  }

  @AfterEach
  void tearDown() {
    reset(auditService, emailService, envServiceConnector, publisher);
    pprRepository.deleteById(pprEntity.getId());

    profileEntity.setPassword("password7");
    profileRepository.save(profileEntity);
  }

  @Nested
  @DisplayName("Login Tests")
  class LoginTests {

    @Test
    @DisplayName("Login Success")
    void test_Success() {
      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertNull(profileEntity.getLastLogin());
      assertEquals(2, profileEntity.getLoginAttempts());

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isOk();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertNotNull(response.getAccessToken());
      assertNotNull(response.getAuthToken());
      assertEquals(profileEntity.getEmail(), response.getAuthToken().getProfile().getEmail());
      // refresh and csrf tokens should not be contained in the body
      assertNull(response.getRefreshToken());
      assertNull(response.getCsrfToken());

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_CSRF_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertNotNull(profileEntity.getLastLogin());
      assertEquals(0, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Success Null Login Attempts")
    void test_Success_NullLoginAttempts() {
      profileEntity.setLoginAttempts(null);
      profileEntity.setLastLogin(LocalDateTime.now().minusDays(1L));
      profileRepository.save(profileEntity);

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isOk();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertNotNull(response.getAccessToken());
      assertNotNull(response.getAuthToken());
      assertEquals(profileEntity.getEmail(), response.getAuthToken().getProfile().getEmail());
      // refresh and csrf tokens should not be contained in the body
      assertNull(response.getRefreshToken());
      assertNull(response.getCsrfToken());

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_CSRF_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertNotNull(profileEntity.getLastLogin());
      assertEquals(0, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Invalid Email")
    void test_Failure_InvalidEmail() {
      ProfilePasswordRequest request = new ProfilePasswordRequest(EMAIL_NOT_FOUND, PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isNotFound();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile Not Found for [email@notfound.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Invalid Platform")
    void test_Failure_InvalidPlatform() {
      int loginAttempts = profileEntity.getLoginAttempts();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", ID))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isNotFound();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Profile Role Not Found for [1,profile@seven.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Platform Deleted")
    void test_Failure_PlatformDeleted() {
      int loginAttempts = profileEntity.getLoginAttempts();

      platformEntity.setDeletedDate(LocalDateTime.now());
      platformRepository.save(platformEntity);

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Profile Role Not Found for [7,profile@seven.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));

      // reset
      platformEntity.setDeletedDate(null);
      platformRepository.save(platformEntity);
    }

    @Test
    @DisplayName("Login Failure Profile Deleted")
    void test_Failure_ProfileDeleted() {
      profileEntity.setDeletedDate(LocalDateTime.now());
      profileRepository.save(profileEntity);

      int loginAttempts = profileEntity.getLoginAttempts();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Profile Role Not Found for [7,profile@seven.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));

      // reset
      profileEntity.setDeletedDate(null);
      profileRepository.save(profileEntity);
    }

    @Test
    @DisplayName("Login Failure Role Deleted")
    void test_Failure_RoleDeleted() {
      int loginAttempts = profileEntity.getLoginAttempts();

      roleEntity.setDeletedDate(LocalDateTime.now());
      roleRepository.save(roleEntity);

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Profile Role Not Found for [7,profile@seven.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));

      // reset
      roleEntity.setDeletedDate(null);
      roleRepository.save(roleEntity);
    }

    @Test
    @DisplayName("Login Failure Profile Not Validated")
    void test_Failure_ProfileNotValidated() {
      profileEntity.setIsValidated(false);
      profileRepository.save(profileEntity);

      int loginAttempts = profileEntity.getLoginAttempts();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile not validated, please check your email for instructions to validate account!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Profile Locked")
    void test_Failure_ProfileLocked() {
      profileEntity.setLoginAttempts(5);
      profileRepository.save(profileEntity);

      int loginAttempts = profileEntity.getLoginAttempts();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile is locked, please reset your account!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Profile Inactive")
    void test_Failure_ProfileInactive() {
      int loginAttempts = profileEntity.getLoginAttempts();

      profileEntity.setLastLogin(LocalDateTime.now().minusDays(46));
      profileRepository.save(profileEntity);

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile is not active, please revalidate or reset your account!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Incorrect Password")
    void test_Failure_IncorrectPassword() {
      int loginAttempts = profileEntity.getLoginAttempts();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD + "IS_WRONG");

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Unauthorized Profile, email and/or password not found in the system...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      profileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertEquals(loginAttempts + 1, profileEntity.getLoginAttempts());

      // verify audit service called for token login success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Login Failure Bad Request")
    void test_Failure_BadRequest() {
      ProfilePasswordRequest request = new ProfilePasswordRequest("", null);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange();

      // assert status
      responseSpec.expectStatus().isBadRequest();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertTrue(
          response.getResponseMetadata().responseStatusInfo().errMsg().contains("Email is Required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("Password is Required"));
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Login Failure No Auth")
    void test_Failure_NoAuth() {
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Refresh Token Tests")
  class RefreshTokenTests {

    @Test
    @DisplayName("Refresh Token Success")
    void test_Success() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isOk();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertNotNull(response.getAccessToken());
      assertNotNull(response.getAuthToken());
      assertEquals(profileEntity.getEmail(), response.getAuthToken().getProfile().getEmail());
      // refresh and csrf tokens should not be contained in the body
      assertNull(response.getRefreshToken());
      assertNull(response.getCsrfToken());

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));
      responseSpec
          .expectCookie()
          .maxAge(
              ConstantUtils.COOKIE_CSRF_TOKEN,
              Duration.of(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS));

      TokenEntity finalTokenEntity = tokenEntity;
      // refreshed token should be different than provided token
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertNotEquals(finalTokenEntity.getRefreshToken(), value));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN,
              value -> assertNotEquals(finalTokenEntity.getCsrfToken(), value));

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure No Refresh Token Cookie")
    void test_Failure_NoRefreshTokenCookie() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Token Mismatch/Invalid...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure No Csrf Token Cookie")
    void test_Failure_NoCsrfTokenCookie() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Token Invalid/Mismatch...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure No Csrf Token Header")
    void test_Failure_NoCsrfTokenHeader() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Token Invalid/Mismatch...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Mismatch Csrf Token")
    void test_Failure_MismatchCsrfToken() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getRefreshToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Token Invalid/Mismatch...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Refresh Token Not Found")
    void test_Failure_RefreshTokenNotFound() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "TOKEN-NOT-FOUND")
              .exchange();

      // assert status
      responseSpec.expectStatus().isNotFound();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Token Not Found for [refresh]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Platform Mismatch")
    void test_Failure_PlatformMismatch() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      ID, profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Mismatch...", response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Profile Mismatch")
    void test_Failure_ProfileMismatch() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), ID))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile Mismatch...", response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Deleted Token")
    void test_Failure_DeletedToken() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity.setDeletedDate(LocalDateTime.now());
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Deleted Token...", response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Expired Token")
    void test_Failure_ExpiredToken() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity.setExpiryDate(LocalDateTime.now().minusSeconds(61L));
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isUnauthorized();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Expired Token...", response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Deleted Platform")
    void test_Failure_DeletedPlatform() {
      PlatformEntity deletedPlatformEntity = TestData.getPlatformEntities().getLast();
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), deletedPlatformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      ID_DELETED, profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure Deleted Profile")
    void test_Failure_DeletedProfile() {
      ProfileEntity deletedProfileEntity = TestData.getProfileEntities().getLast();
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, deletedProfileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), ID_DELETED))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .header(ConstantUtils.HEADER_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .exchange();

      // assert status
      responseSpec.expectStatus().isForbidden();

      // assert body
      ProfilePasswordTokenResponse response =
          responseSpec
              .expectBody(ProfilePasswordTokenResponse.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(response);
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Profile Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      assertTrue(
          response.getAccessToken() == null
              && response.getAuthToken() == null
              && response.getRefreshToken() == null
              && response.getCsrfToken() == null);

      // assert cookies
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
      responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      // verify audit service called for token refresh success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Refresh Token Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/refresh",
                      platformEntity.getId(), profileEntity.getId()))
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Logout Tests")
  class LogoutTests {

    @Test
    @DisplayName("Logout Success")
    void test_Success() {
      TokenEntity tokenEntity1 =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      TokenEntity tokenEntity2 =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity1 = tokenRepository.save(tokenEntity1);
      tokenEntity2 = tokenRepository.save(tokenEntity2);
      assertNull(tokenEntity1.getDeletedDate());
      assertNull(tokenEntity2.getDeletedDate());

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                      platformEntity.getId(), profileEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .exchange();

      responseSpec.expectStatus().isNoContent();
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      tokenEntity1 = tokenRepository.findById(tokenEntity1.getId()).orElseThrow();
      tokenEntity2 = tokenRepository.findById(tokenEntity2.getId()).orElseThrow();
      assertNotNull(tokenEntity1.getDeletedDate());
      assertNotNull(tokenEntity2.getDeletedDate());

      // verify audit service called for logout success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity1.getId());
      tokenRepository.deleteById(tokenEntity2.getId());
    }

    @Test
    @DisplayName("Logout Failure Profile Not Found")
    void test_Failure_ProfileNotFound() {
      TokenEntity tokenEntity =
          TestData.getTokenEntity(
              ThreadLocalRandom.current().nextInt(1, 9999), platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);
      assertNull(tokenEntity.getDeletedDate());

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                      ID_NOT_FOUND, ID_NOT_FOUND))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "TOKEN-NOT-FOUND")
              .exchange();

      responseSpec.expectStatus().isNotFound();
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_REFRESH_TOKEN,
              value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec
          .expectCookie()
          .value(
              ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertTrue(CommonUtilities.isEmpty(value)));
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_REFRESH_TOKEN, Duration.ZERO);
      responseSpec.expectCookie().maxAge(ConstantUtils.COOKIE_CSRF_TOKEN, Duration.ZERO);

      tokenEntity = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
      assertNull(tokenEntity.getDeletedDate());

      // verify audit service called for logout success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Logout Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                      platformEntity.getId(), profileEntity.getId()))
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Validate Profile Init Tests")
  class ValidateProfileInitTests {

    @Test
    @DisplayName("Validate Profile Init Success")
    void test_Success() {
      webTestClient
          .get()
          .uri(
              String.format(
                  "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                  platformEntity.getId(), profileEntity.getEmail()))
          .header("Authorization", "Basic " + basicAuthCredentialsForTest)
          .exchange()
          .expectStatus()
          .isNoContent();

      // verify audit service called for reset success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_INIT)),
              any(String.class));

      // verify email sent for reset
      verify(emailService, after(200).times(1))
          .sendProfileValidationEmail(
              any(PlatformEntity.class), any(ProfileEntity.class), any(String.class));
    }

    @Test
    @DisplayName("Validate Profile Failure Runtime Error")
    void test_Failure_RuntimeError() {
      doThrow(new RuntimeException("Something Bad Happened"))
          .when(emailService)
          .sendProfileValidationEmail(any(), any(), any());

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .exchange()
              .expectStatus()
              .is5xxServerError()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Something Bad Happened"));

      // verify audit service called for validate failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Validate Profile Init Failure Profile Not Found")
    void test_Failure_NotFound() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                      platformEntity.getId(), EMAIL_NOT_FOUND))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Platform Profile Role Not Found for [7,email@notfound.com]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
              any(String.class));
      verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Validate Profile Init Failure No Auth")
    void test_FailureNoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(emailService);
    }
  }

  @Nested
  @DisplayName("Reset Profile Init Tests")
  class ResetProfileInitTests {

    @Test
    @DisplayName("Reset Profile Init Success")
    void test_Success() {
      webTestClient
          .get()
          .uri(
              String.format(
                  "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                  platformEntity.getId(), profileEntity.getEmail()))
          .header("Authorization", "Basic " + basicAuthCredentialsForTest)
          .exchange()
          .expectStatus()
          .isNoContent();

      // verify audit service called for reset success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_INIT)),
              any(String.class));

      // verify email sent for reset
      verify(emailService, after(200).times(1))
          .sendProfileResetEmail(
              any(PlatformEntity.class), any(ProfileEntity.class), any(String.class));
    }

    @Test
    @DisplayName("Reset Profile Failure Runtime Error")
    void test_Failure_RuntimeError() {
      doThrow(new RuntimeException("Something Bad Happened"))
          .when(emailService)
          .sendProfileResetEmail(any(), any(), any());

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .exchange()
              .expectStatus()
              .is5xxServerError()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Something Bad Happened"));

      // verify audit service called for reset init failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Reset Profile Init Failure Profile Not Found")
    void test_Failure_NotFound() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                      platformEntity.getId(), EMAIL_NOT_FOUND))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Platform Profile Role Not Found for [7,email@notfound.com]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
      verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Reset Profile Init Failure No Auth")
    void test_FailureNoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(emailService);
    }
  }

  @Nested
  @DisplayName("Reset Profile Tests")
  class ResetProfileTests {
    private static final String PASSWORD_AFTER_RESET = "password-9";

    @Test
    @DisplayName("Reset Profile Success")
    void test_Success() {
      ProfileEntity existingProfileEntity =
          profileRepository.findById(profileEntity.getId()).orElseThrow();
      String existingPassword = existingProfileEntity.getPassword();

      ProfilePasswordRequest request =
          new ProfilePasswordRequest(profileEntity.getEmail(), PASSWORD_AFTER_RESET);
      webTestClient
          .post()
          .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
          .header("Authorization", "Basic " + basicAuthCredentialsForTest)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isNoContent();

      existingProfileEntity = profileRepository.findById(profileEntity.getId()).orElseThrow();
      assertNotEquals(existingPassword, existingProfileEntity.getPassword());

      // verify audit service called for reset success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_SUCCESS)),
              any(String.class));

      // reset
      existingProfileEntity.setPassword(existingPassword);
      profileRepository.save(existingProfileEntity);
    }

    @Test
    @DisplayName("Reset Profile Failure Bad Request")
    void test_Failure_BadRequest() {
      ProfileEntity existingProfileEntity =
          profileRepository.findById(profileEntity.getId()).orElseThrow();
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(existingProfileEntity.getEmail(), "");

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isBadRequest()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Password is Required"));

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Reset Profile Failure Profile Not Found")
    void test_Failure_NotFound() {
      profileRepository.findById(profileEntity.getId()).orElseThrow();
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(EMAIL_NOT_FOUND, PASSWORD_AFTER_RESET);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertTrue(
          response
              .getResponseMetadata()
              .responseStatusInfo()
              .errMsg()
              .contains("Platform Profile Role Not Found for [7,email@notfound.com]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
    }

    @Test
    @DisplayName("Reset Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ProfileEntity existingProfileEntity =
          profileRepository.findById(profileEntity.getId()).orElseThrow();
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(existingProfileEntity.getEmail(), PASSWORD_AFTER_RESET);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isUnauthorized()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);

      assertEquals(
          "Not authorized to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }
  }
}
