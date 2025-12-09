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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

public class ProfileBasicAuthControllerTest extends BaseTest {

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private TokenRepository tokenRepository;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EmailService emailService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;
  private static RoleEntity roleEntity;
  private static PlatformProfileRoleEntity pprEntity;

  private static final Long ID_TO_USE = 7L;

  @BeforeAll
  static void setUpBeforeAll(
      @Autowired PasswordUtils passwordUtils,
      @Autowired PlatformRepository platformRepository,
      @Autowired ProfileRepository profileRepository,
      @Autowired RoleRepository roleRepository,
      @Autowired PlatformProfileRoleRepository pprRepository,
      @Autowired TokenRepository tokenRepository) {
    platformEntity = TestData.getPlatformEntities().get(6);
    profileEntity = TestData.getProfileEntities().get(6);
    roleEntity = TestData.getRoleEntities().get(6);
    pprEntity =
        TestData.getPlatformProfileRoleEntity(platformEntity, profileEntity, roleEntity, null);
    pprRepository.save(pprEntity);

    // hash password
    profileEntity.setPassword(passwordUtils.hashPassword("password-1"));
    profileRepository.save(profileEntity);
  }

  @AfterAll
  static void tearDownAfterAll(
      @Autowired PlatformRepository platformRepository,
      @Autowired ProfileRepository profileRepository,
      @Autowired RoleRepository roleRepository,
      @Autowired PlatformProfileRoleRepository pprRepository,
      @Autowired TokenRepository tokenRepository) {
    pprRepository.deleteById(pprEntity.getId());

    profileEntity.setIsValidated(true);
    profileEntity.setPassword("password7");
    profileRepository.save(profileEntity);
  }

  @BeforeEach
  void setUpBeforeEach() {
    doNothing().when(emailService).sendProfilePasswordEmail(any(), any());
    doNothing().when(emailService).sendProfileValidationEmail(any(), any(), any());
    doNothing().when(emailService).sendProfileResetEmail(any(), any(), any());
    when(envServiceConnector.getBaseUrlForLinkInEmail())
        .thenReturn("https://base-url-for-link-in-email.com");
  }

  @AfterEach
  void tearDown() {
    reset(auditService, emailService, envServiceConnector, publisher);
  }

  @Nested
  @DisplayName("Logout Tests")
  class LogoutTests {
    @Test
    @DisplayName("Logout Success Refresh Token Cookie")
    void test_Success_RefreshToken() {
      TokenEntity tokenEntity = TestData.getTokenEntity(1, platformEntity, profileEntity);
      tokenEntity = tokenRepository.save(tokenEntity);
      assertNull(tokenEntity.getDeletedDate());

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout", ID_TO_USE, ID_TO_USE))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, tokenEntity.getRefreshToken())
              .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, tokenEntity.getCsrfToken())
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

      tokenEntity = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
      assertNotNull(tokenEntity.getDeletedDate());

      // verify audit service called for logout success
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
              any(String.class));

      // reset
      tokenRepository.deleteById(tokenEntity.getId());
    }

    @Test
    @DisplayName("Logout Success No Refresh Token Cookie")
    void test_Success_NoRefreshToken() {
      TokenEntity tokenEntity1 = TestData.getTokenEntity(1, platformEntity, profileEntity);
      TokenEntity tokenEntity2 = TestData.getTokenEntity(2, platformEntity, profileEntity);
      tokenEntity1 = tokenRepository.save(tokenEntity1);
      tokenEntity2 = tokenRepository.save(tokenEntity2);
      assertNull(tokenEntity1.getDeletedDate());
      assertNull(tokenEntity2.getDeletedDate());

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout", ID_TO_USE, ID_TO_USE))
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
    @DisplayName("Logout Success Refresh Token Not Found")
    void test_Success_BadRefreshToken() {
      TokenEntity tokenEntity1 = TestData.getTokenEntity(1, platformEntity, profileEntity);
      TokenEntity tokenEntity2 = TestData.getTokenEntity(2, platformEntity, profileEntity);
      tokenEntity1 = tokenRepository.save(tokenEntity1);
      tokenEntity2 = tokenRepository.save(tokenEntity2);
      assertNull(tokenEntity1.getDeletedDate());
      assertNull(tokenEntity2.getDeletedDate());

      WebTestClient.ResponseSpec responseSpec =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout", ID_TO_USE, ID_TO_USE))
              .header("Authorization", "Basic " + basicAuthCredentialsForTest)
              .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "TOKEN-DOES-NOT-EXIST")
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
      TokenEntity tokenEntity = TestData.getTokenEntity(1, platformEntity, profileEntity);
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
                      "/api/v1/ba_profiles/platform/%s/profile/%s/logout", ID_TO_USE, ID_TO_USE))
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
                  ID_TO_USE, profileEntity.getEmail()))
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
                      ID_TO_USE, profileEntity.getEmail()))
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
                      ID_TO_USE, EMAIL_NOT_FOUND))
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
                      ID_TO_USE, profileEntity.getEmail()))
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
                  ID_TO_USE, profileEntity.getEmail()))
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
                      ID_TO_USE, profileEntity.getEmail()))
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
                      ID_TO_USE, EMAIL_NOT_FOUND))
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
                      ID_TO_USE, profileEntity.getEmail()))
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
    private static final String PASSWORD_BEFORE_RESET = "password-1";
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
          .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", ID_TO_USE))
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
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", ID_TO_USE))
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
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", ID_TO_USE))
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
              .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", ID_TO_USE))
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
