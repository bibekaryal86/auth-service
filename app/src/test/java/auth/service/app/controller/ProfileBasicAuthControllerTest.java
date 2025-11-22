package auth.service.app.controller;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_GUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.service.ProfileService;
import auth.service.app.service.RoleService;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

public class ProfileBasicAuthControllerTest extends BaseTest {

  private static final String NEW_USER_NEW_EMAIL = "new-user@new-email.com";
  private static final String NEW_USER_NEW_PASSWORD = "new-user-new-password";
  private static final String REFRESH_TOKEN = "some-refresh-token";
  private static final String CSRF_TOKEN = "some-csrf-token";
  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;
  private static RoleEntity roleEntity;
  private static TokenEntity tokenEntity;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EmailService emailService;
  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  @Autowired private ProfileRepository profileRepository;
  @Autowired private PlatformRepository platformRepository;
  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private RoleService roleService;
  @Autowired private ProfileService profileService;

  @BeforeAll
  static void setUp(
      @Autowired PasswordUtils passwordUtils,
      @Autowired PlatformRepository platformRepository,
      @Autowired ProfileRepository profileRepository,
      @Autowired RoleRepository roleRepository,
      @Autowired PlatformProfileRoleRepository platformProfileRoleRepository,
      @Autowired TokenRepository tokenRepository) {
    String newUserNewPassword = passwordUtils.hashPassword(NEW_USER_NEW_PASSWORD);
    ProfileEntity profileEntitySetup = TestData.getNewProfileEntity();
    profileEntitySetup.setEmail(NEW_USER_NEW_EMAIL);
    profileEntitySetup.setPassword(newUserNewPassword);
    profileEntity = profileRepository.save(profileEntitySetup);

    PlatformEntity platformEntitySetup = TestData.getNewPlatformEntity();
    platformEntity = platformRepository.save(platformEntitySetup);

    RoleEntity roleEntitySetup = TestData.getNewRoleEntity();
    roleEntity = roleRepository.save(roleEntitySetup);

    PlatformProfileRoleEntity platformProfileRoleEntitySetup = new PlatformProfileRoleEntity();
    platformProfileRoleEntitySetup.setAssignedDate(LocalDateTime.now());
    platformProfileRoleEntitySetup.setPlatform(platformEntity);
    platformProfileRoleEntitySetup.setProfile(profileEntity);
    platformProfileRoleEntitySetup.setRole(roleEntity);
    platformProfileRoleEntitySetup.setId(
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId()));
    platformProfileRoleRepository.save(platformProfileRoleEntitySetup);

    ProfileDto profileDtoSetup = TestData.getProfileDto();
    profileDtoSetup.setEmail(NEW_USER_NEW_EMAIL);

    TokenEntity tokenEntitySetup = new TokenEntity();
    tokenEntitySetup.setPlatform(platformEntity);
    tokenEntitySetup.setProfile(profileEntity);
    tokenEntitySetup.setIpAddress("some-ip-address");
    tokenEntitySetup.setRefreshToken(REFRESH_TOKEN);
    tokenEntitySetup.setCsrfToken(CSRF_TOKEN);
    tokenEntitySetup.setExpiryDate(LocalDateTime.now().plusDays(1L));
    tokenEntity = tokenRepository.save(tokenEntitySetup);
  }

  @BeforeEach
  void setUpBeforeEach() {
    clearInvocations(applicationEventPublisher);
    doNothing().when(applicationEventPublisher).publishEvent(any(ProfileEvent.class));
    when(envServiceConnector.getBaseUrlForLinkInEmail()).thenReturn(null);
  }

  @AfterAll
  static void tearDownAfterAll(
      @Autowired PlatformRepository platformRepository,
      @Autowired ProfileRepository profileRepository,
      @Autowired RoleRepository roleRepository,
      @Autowired PlatformProfileRoleRepository platformProfileRoleRepository,
      @Autowired TokenRepository tokenRepository) {
    tokenRepository.deleteAll();
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId()));
    roleRepository.deleteById(roleEntity.getId());
    profileRepository.deleteById(profileEntity.getId());
    platformRepository.deleteById(platformEntity.getId());
  }

  @AfterEach
  void tearDownAfterEach() {
    reset(auditService, emailService, applicationEventPublisher);
  }

  @Test
  void testCreateProfile_Success() {
    // setup
    // insert GUEST role as its used in create profile
    RoleEntity roleEntity = roleService.createRole(new RoleRequest(ROLE_NAME_GUEST, "something"));

    ProfileRequest profileRequest =
        TestData.getProfileRequest(
            "Request", "Success", "success_" + NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD, null);

    ProfileResponse profileResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profileRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());

    // verify application event publisher is called
    verify(applicationEventPublisher, times(1))
        .publishEvent(
            argThat(
                event -> {
                  ProfileEvent profileEvent = (ProfileEvent) event;
                  assertEquals(TypeEnums.EventType.CREATE, profileEvent.getEventType());
                  return true;
                }));

    // verify audit service is called
    ArgumentCaptor<HttpServletRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<ProfileEntity> profileEntityArgumentCaptor =
        ArgumentCaptor.forClass(ProfileEntity.class);
    ArgumentCaptor<AuditEnums.AuditProfile> auditProfileArgumentCaptor =
        ArgumentCaptor.forClass(AuditEnums.AuditProfile.class);
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    verify(auditService, after(100).times(1))
        .auditProfile(
            requestArgumentCaptor.capture(),
            profileEntityArgumentCaptor.capture(),
            auditProfileArgumentCaptor.capture(),
            stringArgumentCaptor.capture());

    // cleanup
    profileService.hardDeleteProfile(profileResponse.getProfiles().getFirst().getId());
    // delete GUEST role
    roleService.hardDeleteRole(roleEntity.getId());
  }

  @Test
  void testCreateProfile_FailureNoAuth() {
    ProfileRequest profileRequest =
        TestData.getProfileRequest(
            "Request", "Failure", "failure_" + NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD, null);

    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
        .bodyValue(profileRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateProfile_FailureBadRequest() {
    ProfileRequest profileRequest =
        new ProfileRequest("", null, "", "some-phone", "some-password", true, null);
    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
            .bodyValue(profileRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("First Name is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Last Name is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Email is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateProfile_FailureValidation() {
    ProfileRequest profileRequest =
        TestData.getProfileRequest("Request", "Failure", "failure_" + NEW_USER_NEW_EMAIL, "", null);

    ProfileResponse profileResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profileRequest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getResponseMetadata());
    assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        profileResponse
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[password] is Missing in [Profile] request"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateProfile_FailureException() {
    // setup
    // insert GUEST role as its used in create profile
    RoleEntity roleEntity = roleService.createRole(new RoleRequest(ROLE_NAME_GUEST, "something"));

    ProfileRequest profileRequest =
        TestData.getProfileRequest(
            "Request", "Failure", NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD, null);

    ProfileResponse profileResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profileRequest)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getResponseMetadata());
    assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo().errMsg());
    verifyNoInteractions(auditService);

    // cleanup
    // delete GUEST role
    roleService.hardDeleteRole(roleEntity.getId());
  }

  @Test
  void testLoginProfile_Success() {
    // setup
    profileEntity.setIsValidated(true);
    profileEntity.setLastLogin(null);
    profileEntity.setLoginAttempts(2);
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange();

    // assert status
    responseSpec.expectStatus().isOk();

    // assert body
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getAccessToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertEquals(
        profilePasswordRequest.getEmail(), profilePasswordTokenResponse.getProfile().getEmail());
    // refresh and csrf tokens should not be contained in the body
    assertNull(profilePasswordTokenResponse.getRefreshToken());
    assertNull(profilePasswordTokenResponse.getCsrfToken());

    // assert cookies
    responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
    responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);

    // verify audit service is called for login success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            argThat(profileEntityParam -> profileEntityParam.getEmail().equals(NEW_USER_NEW_EMAIL)),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN)),
            any(String.class));

    // validate that login attempts is reset to 0 and last login is populated
    ProfileEntity peFound = profileRepository.findById(profileEntity.getId()).orElse(null);
    assertNotNull(peFound);
    assertNotNull(peFound.getLastLogin());
    assertEquals(0, peFound.getLoginAttempts());

    // reset
    peFound.setIsValidated(false);
    profileRepository.save(peFound);
  }

  @Test
  void testLoginProfile_Failure() {
    // setup
    profileEntity.setIsValidated(true);
    profileEntity.setLastLogin(null);
    profileEntity.setLoginAttempts(2);
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, "wrong-password");
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());

    // verify audit service is called for login failed
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
            any(String.class));

    // validate that login attempts increased by 1 and last login is not populated
    ProfileEntity peFound = profileRepository.findByEmail(NEW_USER_NEW_EMAIL).orElse(null);
    assertNotNull(peFound);
    assertNull(peFound.getLastLogin());
    assertEquals(3, peFound.getLoginAttempts());

    // reset
    profileEntity.setIsValidated(false);
    profileRepository.save(profileEntity);
  }

  @Test
  void testLoginProfile_FailureDeletedPlatform() {
    // setup
    platformEntity.setDeletedDate(LocalDateTime.now());
    platformRepository.save(platformEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertEquals(
        String.format(
            "Active Platform Profile Role Not Found for [%s,%s]",
            platformEntity.getId(), profileEntity.getEmail()),
        profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg());

    // verify audit service is called for login failed
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
  void testLoginProfile_FailureDeletedProfile() {
    // setup
    profileEntity.setDeletedDate(LocalDateTime.now());
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertEquals(
        String.format(
            "Active Platform Profile Role Not Found for [%s,%s]",
            platformEntity.getId(), NEW_USER_NEW_EMAIL),
        profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg());

    // verify audit service is called for login failed
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
  void testLoginProfile_FailureProfileNotValidated() {
    // setup
    profileEntity.setIsValidated(false);
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertEquals(
        "Profile not validated, please check your email for instructions to validate account!",
        profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg());

    // verify audit service is called for login failed
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
            any(String.class));
  }

  @Test
  void testLoginProfile_FailureProfileNotActive() {
    // setup
    profileEntity.setIsValidated(true);
    profileEntity.setLastLogin(LocalDateTime.now().minusDays(46));
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertEquals(
        "Profile is not active, please revalidate or reset your account!",
        profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg());

    // verify audit service is called for login failed
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
            any(String.class));

    // reset
    profileEntity.setIsValidated(false);
    profileEntity.setLastLogin(null);
    profileRepository.save(profileEntity);
  }

  @Test
  void testLoginProfile_FailureProfileLoginAttemptExceeded() {
    // setup
    profileEntity.setIsValidated(true);
    profileEntity.setLoginAttempts(5);
    profileRepository.save(profileEntity);

    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertEquals(
        "Profile is locked, please reset your account!",
        profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg());

    // verify audit service is called for login failed
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR)),
            any(String.class));

    // reset
    profileEntity.setIsValidated(false);
    profileEntity.setLoginAttempts(0);
    profileRepository.save(profileEntity);
  }

  @Test
  void testRefreshToken_Success() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    // assert status
    responseSpec.expectStatus().isOk();

    // assert body
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getAccessToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertEquals(profileEntity.getEmail(), profilePasswordTokenResponse.getProfile().getEmail());
    // refresh and csrf tokens should not be contained in the body
    assertNull(profilePasswordTokenResponse.getRefreshToken());
    assertNull(profilePasswordTokenResponse.getCsrfToken());

    // assert cookies
    responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_REFRESH_TOKEN, Boolean.TRUE);
    responseSpec.expectCookie().httpOnly(ConstantUtils.COOKIE_CSRF_TOKEN, Boolean.FALSE);

    responseSpec
        .expectCookie()
        .value(ConstantUtils.COOKIE_REFRESH_TOKEN, value -> assertNotEquals(REFRESH_TOKEN, value));
    responseSpec
        .expectCookie()
        .value(ConstantUtils.COOKIE_CSRF_TOKEN, value -> assertNotEquals(CSRF_TOKEN, value));

    // verify audit service called for token refresh success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureNoAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                platformEntity.getId(), profileEntity.getId()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRefreshToken_Failure_NoRefreshToken() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isUnauthorized();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(e -> e.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_Failure_NoCsrfTokenHeader() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isForbidden();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(e -> e.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_Failure_NoCsrfTokenCookie() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .exchange();

    responseSpec.expectStatus().isForbidden();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(e -> e.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_Failure_CsrfMismatch() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, "WRONG_CSRF")
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isForbidden();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(e -> e.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureExpiredRefreshToken() {
    // setup
    final LocalDateTime expiryDate = tokenEntity.getExpiryDate();
    tokenEntity.setExpiryDate(LocalDateTime.now().minusHours(1L));
    tokenRepository.save(tokenEntity);

    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isUnauthorized();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));

    // reset
    tokenEntity.setExpiryDate(expiryDate);
    tokenRepository.save(tokenEntity);
  }

  @Test
  void testRefreshToken_FailureRefreshTokenNotFound() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "WRONG-REFRESH")
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isNotFound();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureDeletedToken() {
    // setup
    final LocalDateTime deletedDate = tokenEntity.getDeletedDate();
    tokenEntity.setDeletedDate(LocalDateTime.now());
    tokenRepository.save(tokenEntity);

    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/token/refresh",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .header(ConstantUtils.HEADER_CSRF_TOKEN, CSRF_TOKEN)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .cookie(ConstantUtils.COOKIE_CSRF_TOKEN, CSRF_TOKEN)
            .exchange();

    responseSpec.expectStatus().isUnauthorized();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getAccessToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));

    // reset
    tokenEntity.setDeletedDate(deletedDate);
    tokenRepository.save(tokenEntity);
  }

  @Test
  void testLogout_SuccessRefreshToken() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
            .exchange();

    responseSpec.expectStatus().isNoContent();
    responseSpec.expectBody().isEmpty();
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

    // verify audit service called for logout success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
            any(String.class));
  }

  @Test
  void testLogout_SuccessNoRefreshToken() {
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
    responseSpec.expectBody().isEmpty();
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

    // verify audit service called for logout success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
            any(String.class));
  }

  @Test
  void testLogout_SuccessExpiredRefreshToken() {
    // setup
    final LocalDateTime expiryDate = tokenEntity.getExpiryDate();
    tokenEntity.setExpiryDate(LocalDateTime.now().minusHours(1L));
    tokenRepository.save(tokenEntity);

    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
            any(String.class));

    // reset
    tokenEntity.setExpiryDate(expiryDate);
    tokenRepository.save(tokenEntity);
  }

  @Test
  void testLogout_FailureNoAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                platformEntity.getId(), profileEntity.getId()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testLogout_FailureRefreshTokenNotFound() {
    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "WRONG-REFRESH")
            .exchange();

    responseSpec.expectStatus().isNotFound();

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        responseSpec
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo());
    assertFalse(
        CommonUtilities.isEmpty(
            profilePasswordTokenResponse.getResponseMetadata().responseStatusInfo().errMsg()));

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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));
  }

  @Test
  void testLogout_FailureDeletedToken() {
    // setup
    final LocalDateTime deletedDate = tokenEntity.getDeletedDate();
    tokenEntity.setDeletedDate(LocalDateTime.now());
    tokenRepository.save(tokenEntity);

    WebTestClient.ResponseSpec responseSpec =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/profile/%s/logout",
                    platformEntity.getId(), profileEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, REFRESH_TOKEN)
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

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
            any(String.class));

    // reset
    tokenEntity.setDeletedDate(deletedDate);
    tokenRepository.save(tokenEntity);
  }

  @Test
  void testValidateProfileInit_Success() {
    doNothing().when(emailService).sendProfileValidationEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                platformEntity.getId(), NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNoContent();

    // verify audit service called for validate init success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_INIT)),
            any(String.class));
    // verify email sent for validation
    verify(emailService, after(200).times(1))
        .sendProfileValidationEmail(
            any(PlatformEntity.class), any(ProfileEntity.class), any(String.class));
  }

  @Test
  void testValidateProfileInit_Failure() {
    doThrow(new RuntimeException("something happened"))
        .when(emailService)
        .sendProfileValidationEmail(any(), any(), any());

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                    platformEntity.getId(), NEW_USER_NEW_EMAIL))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertTrue(
        responseWithMetadata
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("something happened"));

    // verify audit service called for validate init failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
            any(String.class));
    // verify email service called
    verify(emailService, after(200).times(1))
        .sendProfileValidationEmail(
            any(PlatformEntity.class), any(ProfileEntity.class), any(String.class));
  }

  @Test
  void testValidateProfileInit_FailureNoAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/validate_init?email=%s",
                platformEntity.getId(), NEW_USER_NEW_EMAIL))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService, emailService);
  }

  @Test
  void testValidateProfileInit_FailureNotFound() {
    doNothing().when(emailService).sendProfileValidationEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/validate_init?email=%s", ID, NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNotFound();

    // verify audit service called for validate init failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
            any(String.class));
    // verify email not sent for validation
    verifyNoInteractions(emailService);
  }

  @Test
  void testResetProfileInit_Success() {
    doNothing().when(emailService).sendProfileResetEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                platformEntity.getId(), NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNoContent();

    // verify audit service called for reset init success
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
  void testResetProfileInit_Failure() {
    doThrow(new RuntimeException("something happened"))
        .when(emailService)
        .sendProfileResetEmail(any(), any(), any());

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                    platformEntity.getId(), NEW_USER_NEW_EMAIL))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertTrue(
        responseWithMetadata
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("something happened"));

    // verify audit service called for reset init failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
            any(String.class));
    // verify email service called
    verify(emailService, after(200).times(1))
        .sendProfileResetEmail(
            any(PlatformEntity.class), any(ProfileEntity.class), any(String.class));
  }

  @Test
  void testResetProfileInit_FailureNoAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/reset_init?email=%s",
                platformEntity.getId(), NEW_USER_NEW_EMAIL))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService, emailService);
  }

  @Test
  void testResetProfileInit_FailureNotFound() {
    doNothing().when(emailService).sendProfileResetEmail(any(), any(), any());
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/ba_profiles/platform/%s/reset_init?email=%s", ID, NEW_USER_NEW_EMAIL))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isNotFound();

    // verify audit service called for reset init failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
            any(String.class));
    // verify email not sent for reset error
    verifyNoInteractions(emailService);
  }

  @Test
  void testResetProfile_Success() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, "new-user-newer-password");
    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(profilePasswordRequest)
        .exchange()
        .expectStatus()
        .isNoContent();

    // verify audit service called for reset success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_SUCCESS)),
            any(String.class));

    // reset
    profilePasswordRequest = new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);
    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(profilePasswordRequest)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void testResetProfile_Failure() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest("some-old@email.com", "new-user-newer-password");

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertTrue(
        responseWithMetadata
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("Platform Profile Role Not Found"));

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
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_USER_NEW_EMAIL, "new-user-newer-password");
    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
        .bodyValue(profilePasswordRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }
}
