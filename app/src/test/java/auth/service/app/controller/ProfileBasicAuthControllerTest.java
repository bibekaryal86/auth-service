package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.TokenRequest;
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
import auth.service.app.util.JwtUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
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

public class ProfileBasicAuthControllerTest extends BaseTest {

  private static final String NEW_USER_NEW_EMAIL = "new-user@new-email.com";
  private static final String NEW_USER_NEW_PASSWORD = "new-user-new-password";
  private static final Long GUEST_ROLE_ID = 3L;
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
    String accessToken =
        JwtUtils.encodeAuthCredentials(platformEntity, profileDtoSetup, 1000 * 60 * 15);
    String refreshToken =
        JwtUtils.encodeAuthCredentials(platformEntity, profileDtoSetup, 1000 * 60 * 60 * 24);

    TokenEntity tokenEntitySetup = new TokenEntity();
    tokenEntitySetup.setPlatform(platformEntity);
    tokenEntitySetup.setProfile(profileEntity);
    tokenEntitySetup.setIpAddress("some-ip-address");
    tokenEntitySetup.setAccessToken(accessToken);
    tokenEntitySetup.setRefreshToken(refreshToken);
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
    ProfileRequest profileRequest =
        TestData.getProfileRequest(
            "Request", "Success", "success" + NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);

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
    Long profileId = profileResponse.getProfiles().getFirst().getId();
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(ID, profileId, GUEST_ROLE_ID));
    profileRepository.deleteById(profileId);
  }

  @Test
  void testCreateProfile_FailureNoAuth() {
    ProfileRequest profileRequest =
        TestData.getProfileRequest(
            "Request", "Failure", "failure" + NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);

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
        new ProfileRequest(
            "", null, "", "some-phone", "some-password", true, Collections.emptyList());
    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/create", ID))
            .bodyValue(profileRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo());
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("First Name is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("Last Name is required")
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("Email is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateProfile_FailureValidation() {
    ProfileRequest profileRequest =
        TestData.getProfileRequest("Request", "Failure", "failure" + NEW_USER_NEW_EMAIL, "");

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
    assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(
        profileResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("[password] is Missing in [Profile] request"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateProfile_FailureException() {
    ProfileRequest profileRequest =
        TestData.getProfileRequest("Request", "Failure", NEW_USER_NEW_EMAIL, NEW_USER_NEW_PASSWORD);

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
    assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    verifyNoInteractions(auditService);
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
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/login", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getAToken());
    assertNotNull(profilePasswordTokenResponse.getRToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertEquals(
        profilePasswordRequest.getEmail(), profilePasswordTokenResponse.getProfile().getEmail());

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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo());
    assertEquals(
        String.format("Active Platform Not Found for [%s]", platformEntity.getId()),
        profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());

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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo());
    assertEquals(
        String.format("Active Profile Not Found for [%s]", NEW_USER_NEW_EMAIL),
        profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());

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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo());
    assertEquals(
        "Profile not validated, please check your email for instructions to validate account!",
        profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());

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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo());
    assertEquals(
        "Profile is not active, please revalidate or reset your account!",
        profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());

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
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata());
    assertNotNull(profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo());
    assertEquals(
        "Profile is locked, please reset your account!",
        profilePasswordTokenResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());

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
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getRToken());
    assertNotNull(profilePasswordTokenResponse.getAToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertNotEquals(tokenRequest.getAccessToken(), profilePasswordTokenResponse.getAToken());
    assertNotEquals(tokenRequest.getRefreshToken(), profilePasswordTokenResponse.getRToken());
    assertEquals(profileEntity.getId(), profilePasswordTokenResponse.getProfile().getId());

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
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getRefreshToken(), tokenEntity.getAccessToken());

    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
        .bodyValue(tokenRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRefreshToken_FailureBadRequest() {
    TokenRequest tokenRequest =
        new TokenRequest(null, tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());
    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .bodyValue(tokenRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo());
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testRefreshToken_FailureMissingRefreshToken() {
    TokenRequest tokenRequest =
        new TokenRequest(profileEntity.getId(), tokenEntity.getAccessToken(), "");

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertTrue(
        profilePasswordTokenResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("is Missing in"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureInvalidRefreshToken() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getAccessToken(), "an.invalid.refresh.token");

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertTrue(
        profilePasswordTokenResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("Invalid Auth Credentials"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureExpiredRefreshToken() {
    // setup
    ProfileDto profileDtoSetup = TestData.getProfileDto();
    profileDtoSetup.setEmail(NEW_USER_NEW_EMAIL);
    String refreshTokenExpiry =
        JwtUtils.encodeAuthCredentials(platformEntity, profileDtoSetup, 100);
    TokenRequest tokenRequest =
        new TokenRequest(profileEntity.getId(), tokenEntity.getAccessToken(), refreshTokenExpiry);

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertTrue(
        profilePasswordTokenResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("Expired Auth Credentials"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureRefreshTokenNotFound() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getRefreshToken(), tokenEntity.getAccessToken());

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertTrue(
        profilePasswordTokenResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("Token Not Found"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));
  }

  @Test
  void testRefreshToken_FailureDeletedToken() {
    // setup
    tokenEntity.setDeletedDate(LocalDateTime.now());
    tokenRepository.save(tokenEntity);

    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());

    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        webTestClient
            .post()
            .uri(
                String.format(
                    "/api/v1/ba_profiles/platform/%s/token/refresh", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ProfilePasswordTokenResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRToken());
    assertNull(profilePasswordTokenResponse.getAToken());
    assertNull(profilePasswordTokenResponse.getProfile());
    assertTrue(
        profilePasswordTokenResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("Deleted Token"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR)),
            any(String.class));

    // reset
    tokenEntity.setDeletedDate(null);
    tokenRepository.save(tokenEntity);
  }

  @Test
  void testLogout_Success() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());

    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .bodyValue(tokenRequest)
        .exchange()
        .expectStatus()
        .isNoContent();

    // verify audit service called for logout success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT)),
            any(String.class));
  }

  @Test
  void testLogout_FailureNoAuth() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getRefreshToken(), tokenEntity.getAccessToken());

    webTestClient
        .post()
        .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
        .bodyValue(tokenRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testLogout_FailureBadRequest() {
    TokenRequest tokenRequest =
        new TokenRequest(null, tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());
    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .bodyValue(tokenRequest)
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo());
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("REQUIRED"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testLogout_FailureMissingAccessToken() {
    TokenRequest tokenRequest =
        new TokenRequest(profileEntity.getId(), "", tokenEntity.getRefreshToken());

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("is Missing in"));

    // verify audit service called for logout failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));
  }

  @Test
  void testLogout_FailureInvalidAccessToken() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), "an.invalid.access.token", tokenEntity.getRefreshToken());

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Invalid Auth Credentials"));

    // verify audit service called for logout failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));
  }

  @Test
  void testLogout_FailureExpiredAccessToken() {
    // setup
    ProfileDto profileDtoSetup = TestData.getProfileDto();
    profileDtoSetup.setEmail(NEW_USER_NEW_EMAIL);
    String accessTokenExpiry = JwtUtils.encodeAuthCredentials(platformEntity, profileDtoSetup, 100);
    TokenRequest tokenRequest =
        new TokenRequest(profileEntity.getId(), accessTokenExpiry, tokenEntity.getRefreshToken());

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Expired Auth Credentials"));

    // verify audit service called for token refresh failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));
  }

  @Test
  void testLogout_FailureAccessTokenNotFound() {
    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getRefreshToken(), tokenEntity.getAccessToken());

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("Token Not Found"));

    // verify audit service called for logout failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            isNull(),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));
  }

  @Test
  void testLogout_FailureDeletedToken() {
    // setup
    tokenEntity.setDeletedDate(LocalDateTime.now());
    tokenRepository.save(tokenEntity);

    TokenRequest tokenRequest =
        new TokenRequest(
            profileEntity.getId(), tokenEntity.getAccessToken(), tokenEntity.getRefreshToken());

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/logout", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(tokenRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("Deleted Token"));

    // verify audit service called for logout failure
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR)),
            any(String.class));

    // reset
    tokenEntity.setDeletedDate(null);
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

    ResponseMetadata responseMetadata =
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
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("something happened"));

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

    ResponseMetadata responseMetadata =
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
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(responseMetadata.getResponseStatusInfo().getErrMsg().contains("something happened"));

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

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri(String.format("/api/v1/ba_profiles/platform/%s/reset", platformEntity.getId()))
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .bodyValue(profilePasswordRequest)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(
        responseMetadata
            .getResponseStatusInfo()
            .getErrMsg()
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
