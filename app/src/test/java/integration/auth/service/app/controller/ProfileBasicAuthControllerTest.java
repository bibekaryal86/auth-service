package integration.auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.after;
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
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
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

public class ProfileBasicAuthControllerTest extends BaseTest {

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private ProfileRepository profileRepository;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EmailService emailService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;
  private static RoleEntity roleEntity;
  private static PlatformProfileRoleEntity pprEntity;

  private static final Long ID_TO_USE = 7L;
  private static final String PASSWORD_BEFORE_RESET = "password-1";
  private static final String PASSWORD_AFTER_RESET = "password-9";

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
    profileEntity.setPassword(passwordUtils.hashPassword(PASSWORD_BEFORE_RESET));
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
    when(envServiceConnector.getBaseUrlForLinkInEmail()).thenReturn(null);
  }

  @AfterEach
  void tearDown() {
    reset(auditService, emailService, envServiceConnector, publisher);
  }

  @Nested
  @DisplayName("Reset Profile Tests")
  class ResetProfileTests {

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
      ProfileEntity existingProfileEntity =
          profileRepository.findById(profileEntity.getId()).orElseThrow();
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(PASSWORD_BEFORE_RESET, PASSWORD_AFTER_RESET);

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
              .contains("Platform Profile Role Not Found for [7,password-1]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
    }

    @Test
    void testResetProfile_FailureNoAuth() {
      ProfileEntity existingProfileEntity =
          profileRepository.findById(profileEntity.getId()).orElseThrow();
      ProfilePasswordRequest request =
          new ProfilePasswordRequest(existingProfileEntity.getEmail(), "");
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

      verifyNoInteractions(auditService);
    }
  }
}
