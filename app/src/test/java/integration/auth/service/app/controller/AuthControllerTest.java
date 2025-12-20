package integration.auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileAddressRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.JwtUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("AuthController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthControllerTest extends BaseTest {

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformRepository platformRepository;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PlatformProfileRoleRepository pprRepository;
  @Autowired private ProfileAddressRepository profileAddressRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private PasswordUtils passwordUtils;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EmailService emailService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;
  private static RoleEntity roleEntity;
  private static PlatformProfileRoleEntity pprEntity;

  private static final String REDIRECT_URL = "https://some-app-redirect-url.com/home/";
  private static final String PASSWORD = "password-1";

  private static String encodedEmail;

  @BeforeAll
  static void setUpBeforeAll() {
    encodedEmail = JwtUtils.encodeEmailAddress(EMAIL);
  }

  // better to use BeforeAll and AfterAll
  // but did not work in Nested test classes
  // should revisit in future to put them in each Nested class
  @BeforeEach
  void setUp() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileEntity = TestData.getProfileEntities().get(6);
    roleEntity = TestData.getRoleEntities().get(6);

    doNothing().when(emailService).sendProfilePasswordEmail(any(), any());
    doNothing().when(emailService).sendProfileValidationEmail(any(), any(), any());
    doNothing().when(emailService).sendProfileResetEmail(any(), any(), any());
    when(envServiceConnector.getBaseUrlForLinkInEmail())
        .thenReturn("https://base-url-for-link-in-email.com");

    when(envServiceConnector.getRedirectUrls())
        .thenReturn(Map.of(platformEntity.getPlatformName(), REDIRECT_URL));

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
  @DisplayName("Create Profile Tests")
  class CreateProfileTests {

    @Test
    @DisplayName("Create Profile Success")
    void test_Success() {
      String roleName = roleEntity.getRoleName();
      roleEntity.setRoleName(ConstantUtils.ROLE_NAME_GUEST);
      roleRepository.save(roleEntity);

      ProfileRequest request =
          TestData.getProfileRequest(
              "FirstName", "LastName", "new@email.com", "NewPassword123", null);

      ProfileResponse response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", platformEntity.getId()))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(request.getEmail(), response.getProfiles().getFirst().getEmail());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      ProfileEntity newProfileEntity =
          profileRepository.findById(response.getProfiles().getFirst().getId()).orElseThrow();
      assertEquals(request.getEmail(), newProfileEntity.getEmail());
      assertNotEquals(request.getPassword(), newProfileEntity.getPassword());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_CREATE)),
              any(String.class));

      verify(publisher, times(1))
          .publishEvent(
              argThat(
                  event -> {
                    ProfileEvent profileEvent = (ProfileEvent) event;
                    assertEquals(TypeEnums.EventType.CREATE, profileEvent.getEventType());
                    assertEquals(
                        "https://base-url-for-link-in-email.com", profileEvent.getBaseUrl());
                    return true;
                  }));

      // reset
      roleEntity.setRoleName(roleName);
      roleRepository.save(roleEntity);

      pprRepository.deleteById(
          new PlatformProfileRoleId(
              platformEntity.getId(), newProfileEntity.getId(), roleEntity.getId()));
      profileRepository.deleteById(newProfileEntity.getId());
    }

    @Test
    @DisplayName("Create Profile Success Standard User With Address")
    void test_Success_StandardUserWithAddress() {
      when(envServiceConnector.getBaseUrlForLinkInEmail()).thenReturn(null);
      String roleName = roleEntity.getRoleName();
      roleEntity.setRoleName(ConstantUtils.ROLE_NAME_STANDARD);
      roleRepository.save(roleEntity);

      ProfileRequest request =
          TestData.getProfileRequest(
              "FirstName", "LastName", "new@email.com", "NewPassword123", null);
      request.setGuestUser(false);
      request.setAddressRequest(TestData.getProfileAddressRequest(null, null, "123 New St", false));

      ProfileResponse response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", platformEntity.getId()))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(request.getEmail(), response.getProfiles().getFirst().getEmail());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertEquals("123 New St", response.getProfiles().getFirst().getProfileAddress().getStreet());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      ProfileEntity newProfileEntity =
          profileRepository.findById(response.getProfiles().getFirst().getId()).orElseThrow();
      assertEquals(request.getEmail(), newProfileEntity.getEmail());
      assertNotEquals(request.getPassword(), newProfileEntity.getPassword());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_CREATE)),
              any(String.class));

      verify(publisher, times(1))
          .publishEvent(
              argThat(
                  event -> {
                    ProfileEvent profileEvent = (ProfileEvent) event;
                    assertEquals(TypeEnums.EventType.CREATE, profileEvent.getEventType());
                    assertTrue(
                        Pattern.matches(
                            "^http://localhost:\\d+/authsvc$", profileEvent.getBaseUrl()));
                    return true;
                  }));

      // reset
      roleEntity.setRoleName(roleName);
      roleRepository.save(roleEntity);

      profileAddressRepository.deleteById(newProfileEntity.getProfileAddress().getId());
      pprRepository.deleteById(
          new PlatformProfileRoleId(
              platformEntity.getId(), newProfileEntity.getId(), roleEntity.getId()));
      profileRepository.deleteById(newProfileEntity.getId());
    }

    @Test
    @DisplayName("Create Profile Failure Bad Request")
    void test_Failure_BadRequest() {
      ProfileRequest request = TestData.getProfileRequest("", null, "", "NewPassword123", null);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", platformEntity.getId()))
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
                  .contains("First Name is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("Last Name is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("Email is required"));

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Create Profile Failure Validation Error")
    void test_Failure_ValidationError() {
      ProfileRequest request =
          TestData.getProfileRequest("FirstName", "LastName", "new@email.com", "", null);

      ProfileResponse response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", platformEntity.getId()))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isBadRequest()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertTrue(response != null && response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "[password] is Missing in [Profile] request",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Create Profile Failure Platform Deleted")
    void test_Failure_PlatformDeleted() {
      ProfileRequest request =
          TestData.getProfileRequest("FirstName", "LastName", "new@email.com", "", null);

      ProfileResponse response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", ID_DELETED))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertTrue(response != null && response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Create Profile Failure Platform Not Found")
    void test_Failure_PlatformNotFound() {
      ProfileRequest request =
          TestData.getProfileRequest("FirstName", "LastName", "new@email.com", "", null);

      ProfileResponse response =
          webTestClient
              .post()
              .uri(String.format("/api/v1/auth/platform/%s/create", ID_NOT_FOUND))
              .bodyValue(request)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertTrue(response != null && response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
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
                  "/api/v1/auth/platform/%s/validate_init?email=%s",
                  platformEntity.getId(), profileEntity.getEmail()))
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
                      "/api/v1/auth/platform/%s/validate_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
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
                      "/api/v1/auth/platform/%s/validate_init?email=%s",
                      platformEntity.getId(), EMAIL_NOT_FOUND))
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
              .contains("Platform Profile Role Not Found for [1,email@notfound.com]"));

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
  }

  @Nested
  @DisplayName("ValidateProfileExit Tests")
  class ValidateProfileExitTests {

    @Test
    @DisplayName("ValidateProfileExit Success")
    public void test_Success() {
      webTestClient
          .get()
          .uri(
              String.format(
                  "/api/v1/auth/platform/%s/validate_exit?toValidate=%s", ID, encodedEmail))
          .exchange()
          .expectStatus()
          .is3xxRedirection()
          .expectHeader()
          .location(REDIRECT_URL + "?is_validated=true");

      verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
      verify(auditService, after(200).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_EXIT)),
              any(String.class));
    }

    @Test
    @DisplayName("ValidateProfileExit Failure")
    public void test_Failure() {
      webTestClient
          .get()
          .uri(String.format("/api/v1/auth/platform/%s/validate_exit?toValidate=%s", ID, EMAIL))
          .exchange()
          .expectStatus()
          .is3xxRedirection()
          .expectHeader()
          .location(REDIRECT_URL + "?is_validated=false");

      verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
      verify(auditService, after(200).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
              any(String.class));
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
                  "/api/v1/auth/platform/%s/reset_init?email=%s",
                  platformEntity.getId(), profileEntity.getEmail()))
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
                      "/api/v1/auth/platform/%s/reset_init?email=%s",
                      platformEntity.getId(), profileEntity.getEmail()))
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
                      "/api/v1/auth/platform/%s/reset_init?email=%s",
                      platformEntity.getId(), EMAIL_NOT_FOUND))
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
              .contains("Platform Profile Role Not Found for [1,email@notfound.com]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
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
          .uri(String.format("/api/v1/auth/platform/%s/reset", platformEntity.getId()))
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
              .uri(String.format("/api/v1/auth/platform/%s/reset", platformEntity.getId()))
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
              .uri(String.format("/api/v1/auth/platform/%s/reset", platformEntity.getId()))
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
              .contains("Platform Profile Role Not Found for [1,email@notfound.com]"));

      // verify audit service called for reset failure
      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              isNull(),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
    }
  }

  @Nested
  @DisplayName("ResetProfileExit Tests")
  class ResetProfileExitTests {

    @Test
    @DisplayName("ResetProfileExit Success")
    public void test_Success() {
      webTestClient
          .get()
          .uri(String.format("/api/v1/auth/platform/%s/reset_exit?toReset=%s", ID, encodedEmail))
          .exchange()
          .expectStatus()
          .is3xxRedirection()
          .expectHeader()
          .location(REDIRECT_URL + "?is_reset=true&to_reset=" + EMAIL);

      verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
      verify(auditService, after(200).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_EXIT)),
              any(String.class));
    }

    @Test
    @DisplayName("ResetProfileExit Failure")
    public void test_Failure() {
      webTestClient
          .get()
          .uri(String.format("/api/v1/auth/platform/%s/reset_exit?toReset=%s", ID, EMAIL))
          .exchange()
          .expectStatus()
          .is3xxRedirection()
          .expectHeader()
          .location(REDIRECT_URL + "?is_reset=false");

      verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
      verify(auditService, after(200).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
              any(String.class));
    }
  }
}
