package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.service.AuditService;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StringUtils;

public class ProfileControllerTest extends BaseTest {

  private static ProfileEntity profileEntity;
  private static String bearerAuthCredentialsNoPermission;
  private static String bearerAuthCredentialsWithPermission;

  @Autowired private ProfileRepository profileRepository;
  @Autowired private PasswordUtils passwordUtils;

  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;
  @MockitoBean private AuditService auditService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  @BeforeAll
  static void setUpBeforeAll() {
    profileEntity = TestData.getProfileEntities().getFirst();
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileDto profileDtoNoPermission = TestData.getProfileDto();
    ProfileDto profileDtoWithPermission =
        TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);

    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
    bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
  }

  @BeforeEach
  void setUpBeforeEach() {
    when(envServiceConnector.getBaseUrlForLinkInEmail()).thenReturn(null);
  }

  @AfterEach
  void tearDown() {
    reset(applicationEventPublisher, auditService);
  }

  @Test
  void testReadProfiles_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri("/api/v1/profiles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(9, profileResponse.getProfiles().size());
    assertTrue(profileResponse.getProfiles().getLast().getPlatformRoles().isEmpty());
    assertNotNull(profileResponse.getProfiles().getLast().getProfileAddress());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponsePageInfo()),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPageNumber() >= 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPerPage() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalItems() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(profileResponse.getRequestMetadata()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, profileResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(100, profileResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("lastName", profileResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.ASC, profileResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadProfiles_Success_RequestMetadata() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/profiles?isIncludeRoles=true&isIncludePlatforms=true&isIncludeDeleted=true&pageNumber=1&perPage=10&sortColumn=firstName&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(10, profileResponse.getProfiles().size());
    assertFalse(profileResponse.getProfiles().getFirst().getPlatformRoles().isEmpty());
    assertNotNull(profileResponse.getProfiles().getFirst().getProfileAddress());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponsePageInfo()),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPageNumber() >= 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPerPage() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalItems() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(profileResponse.getRequestMetadata()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, profileResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, profileResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("firstName", profileResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, profileResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadProfiles_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/profiles").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadProfilesByPlatformId_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri("/api/v1/profiles/platform/4")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(2, profileResponse.getProfiles().size());
    assertTrue(profileResponse.getProfiles().getFirst().getPlatformRoles().isEmpty());
    assertNull(profileResponse.getProfiles().getFirst().getProfileAddress());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponsePageInfo()),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPageNumber() >= 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPerPage() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalItems() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(profileResponse.getRequestMetadata()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, profileResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(100, profileResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("lastName", profileResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.ASC, profileResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadProfilesByPlatformId_Success_RequestMetadata() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/profiles/platform/4?isIncludeRoles=true&isIncludePlatforms=true&isIncludeDeleted=true&pageNumber=1&perPage=10&sortColumn=firstName&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(3, profileResponse.getProfiles().size());
    assertFalse(profileResponse.getProfiles().getFirst().getPlatformRoles().isEmpty());
    assertNull(profileResponse.getProfiles().getFirst().getProfileAddress());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().getResponsePageInfo()),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPageNumber() >= 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getPerPage() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalItems() > 0),
        () ->
            assertTrue(
                profileResponse.getResponseMetadata().getResponsePageInfo().getTotalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(profileResponse.getRequestMetadata()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertTrue(profileResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, profileResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, profileResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("firstName", profileResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, profileResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadProfilesByPlatformId_FailureNoAuth() {
    webTestClient
        .get()
        .uri("/api/v1/profiles/platform/4")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadProfile_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(ID, profileResponse.getProfiles().getFirst().getId());
  }

  @Test
  void testReadProfile_SuccessButNoPermission() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", 4L))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertEquals(0, profileResponse.getProfiles().size());
    assertNotNull(profileResponse.getResponseMetadata());
    assertTrue(
        profileResponse
            .getResponseMetadata()
            .getResponseStatusInfo()
            .getErrMsg()
            .contains("Profile does not have required permissions to profile entity"));
  }

  @Test
  void testReadProfile_SuperUser_IncludeDeletedFalse() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", ID_DELETED))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(0, profileResponse.getProfiles().size());
  }

  @Test
  void testReadProfile_SuperUser_IncludeDeletedTrue() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s?isIncludeDeleted=true", ID_DELETED))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
  }

  @Test
  void testReadProfile_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/profiles/profile/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadProfile_FailureNotFound() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", ID_NOT_FOUND))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertTrue(profileResponse.getProfiles().isEmpty());
    assertNotNull(profileResponse.getResponseMetadata());
    assertEquals(
        "Profile Not Found for [99]",
        profileResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
  }

  @Test
  void testUpdateProfile_Success() {
    ProfileRequest profileRequest =
        new ProfileRequest(
            "New F Name One",
            "New L Name One",
            "some-new@email.com",
            profileEntity.getPhone(),
            profileEntity.getPassword(),
            false,
            null);

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/profile/%s", ID))
            .bodyValue(profileRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(ID, profileResponse.getProfiles().getFirst().getId());
    assertEquals("New F Name One", profileResponse.getProfiles().getFirst().getFirstName());
    assertEquals("New L Name One", profileResponse.getProfiles().getFirst().getLastName());

    // email has not changed
    assertNotEquals("some-new@email.com", profileResponse.getProfiles().getFirst().getEmail());

    // verify audit service called for profile update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
            any(String.class));
  }

  @Test
  void testUpdateProfile_SuccessSuperUser() {
    ProfileRequest profileRequest =
        new ProfileRequest(
            "New F Name One",
            "New L Name One",
            "some-new@email.com",
            profileEntity.getPhone(),
            profileEntity.getPassword(),
            false,
            null);

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/profile/%s", 4))
            .bodyValue(profileRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(4, profileResponse.getProfiles().getFirst().getId());
    assertEquals("New F Name One", profileResponse.getProfiles().getFirst().getFirstName());
    assertEquals("New L Name One", profileResponse.getProfiles().getFirst().getLastName());

    // email has not changed
    assertNotEquals("some-new@email.com", profileResponse.getProfiles().getFirst().getEmail());

    // verify audit service called for profile update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
            any(String.class));
  }

  @Test
  void testUpdateProfile_FailureNoAuth() {
    ProfileRequest profileRequest =
        new ProfileRequest(
            "New F Name One",
            "New L Name One",
            "some-new@email.com",
            profileEntity.getPhone(),
            profileEntity.getPassword(),
            false,
            null);
    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/profile/%s", ID))
        .bodyValue(profileRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfile_FailureNoPermission() {
    ProfileRequest profileRequest =
        new ProfileRequest(
            "New F Name One",
            "New L Name One",
            "some-new@email.com",
            profileEntity.getPhone(),
            profileEntity.getPassword(),
            false,
            null);
    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/profile/%s", 4))
        .bodyValue(profileRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfile_FailureBadRequest() {
    ProfileRequest profileRequest = new ProfileRequest("", null, "", null, null, false, null);
    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/profile/%s", ID))
            .bodyValue(profileRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
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
  void testUpdateProfileEmail_Success() {
    ProfileEmailRequest profileEmailRequest = new ProfileEmailRequest(EMAIL, "lastfirst@one.com");

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
            .bodyValue(profileEmailRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(ID, profileResponse.getProfiles().getFirst().getId());
    assertEquals("lastfirst@one.com", profileResponse.getProfiles().getFirst().getEmail());

    // verify audit service called for profile email update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_EMAIL_UPDATE)),
            any(String.class));
    // verify event published for profile email update success
    verify(applicationEventPublisher, after(100).times(1)).publishEvent(any(ProfileEvent.class));

    // reset
    ProfileEntity profileEntity = profileRepository.findById(ID).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setEmail(EMAIL);
    profileEntity = profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
    assertEquals(EMAIL, profileEntity.getEmail());
  }

  @Test
  void testUpdateProfileEmail_SuccessSuperUser() {
    ProfileEmailRequest profileEmailRequest =
        new ProfileEmailRequest("firstlast@four.com", "lastfirst@four.com");

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", 4, 4))
            .bodyValue(profileEmailRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(4, profileResponse.getProfiles().getFirst().getId());
    assertEquals("lastfirst@four.com", profileResponse.getProfiles().getFirst().getEmail());

    // verify audit service called for profile email update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_EMAIL_UPDATE)),
            any(String.class));
    // verify event published for profile email update
    verify(applicationEventPublisher, after(100).times(1)).publishEvent(any(ProfileEvent.class));

    // reset
    ProfileEntity profileEntity = profileRepository.findById(4L).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setEmail("firstlast@four.com");
    profileEntity = profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
    assertEquals("firstlast@four.com", profileEntity.getEmail());
  }

  @Test
  void testUpdateProfileEmail_FailureWithNoBearerAuth() {
    ProfileEmailRequest profileEmailRequest = new ProfileEmailRequest(EMAIL, "lastfirst@one.com");
    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
        .bodyValue(profileEmailRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfileEmail_FailureNoPermission() {
    ProfileEmailRequest profileEmailRequest =
        new ProfileEmailRequest("firstlast-1@one.com", "lastfirst-1@one.com");

    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", 2, 2))
        .bodyValue(profileEmailRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfileEmail_FailureBadRequest() {
    ProfileEmailRequest profileEmailRequest = new ProfileEmailRequest("", null);
    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
            .bodyValue(profileEmailRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(responseMetadata);
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Old Email is Required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("New Email is Required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfilePassword_Success() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(EMAIL, "password-one-new");

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/password", ID, ID))
            .bodyValue(profilePasswordRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(ID, profileResponse.getProfiles().getFirst().getId());

    ProfileEntity profileEntity = profileRepository.findById(ID).orElse(null);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getPassword());
    assertTrue(passwordUtils.verifyPassword("password-one-new", profileEntity.getPassword()));

    // verify audit service called for profile password update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_PASSWORD_UPDATE)),
            any(String.class));
  }

  @Test
  void testUpdateProfilePassword_SuccessSuperUser() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest("firstlast@four.com", "password-new-one");

    ProfileResponse profileResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/password", 4, 4))
            .bodyValue(profilePasswordRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(4, profileResponse.getProfiles().getFirst().getId());

    ProfileEntity profileEntity = profileRepository.findById(4L).orElse(null);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getPassword());
    assertTrue(passwordUtils.verifyPassword("password-new-one", profileEntity.getPassword()));

    // verify audit service called for profile password update success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_PASSWORD_UPDATE)),
            any(String.class));
  }

  @Test
  void testUpdateProfilePassword_FailureWithNoBearerAuth() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(EMAIL, "password-one-fail");
    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/password", ID, ID))
        .bodyValue(profilePasswordRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfilePassword_FailureNoPermission() {
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest("firstlast-1@one.com", "password-one-fail");
    webTestClient
        .put()
        .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/password", 4, 4))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .bodyValue(profilePasswordRequest)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateProfilePassword_FailureBadRequest() {
    ProfilePasswordRequest profilePasswordRequest = new ProfilePasswordRequest("", null);
    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/password", ID, ID))
            .bodyValue(profilePasswordRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Email is Required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("Password is Required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testDeleteProfileAddress_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s/address/%s", 3, 5))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteProfile_Success() {
    // setup
    ProfileEntity profileEntityOriginal = profileRepository.findById(ID).orElse(null);
    assertNotNull(profileEntityOriginal);

    ProfileResponse profileResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/profiles/profile/%s", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getResponseMetadata());
    assertEquals(
        1, profileResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    // verify audit service called for profile soft delete success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_DELETE_SOFT)),
            any(String.class));

    // cleanup
    profileRepository.save(profileEntityOriginal);
  }

  @Test
  void testSoftDeleteApp_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteProfile_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteProfile_Success() {
    // setup
    ProfileEntity profileEntityNew = TestData.getNewProfileEntity();
    profileEntityNew = profileRepository.save(profileEntityNew);

    ProfileResponse profileResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/profiles/profile/%s/hard", profileEntityNew.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getResponseMetadata());
    assertEquals(
        1, profileResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    // verify audit service called for profile soft delete success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_DELETE_HARD)),
            any(String.class));

    // cleanup
    profileRepository.deleteById(profileEntityNew.getId());
  }

  @Test
  void testHardDeleteApp_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteProfile_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s/hard", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreProfile_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/profiles/profile/%s/restore", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertEquals(1, profileResponse.getProfiles().size());
    assertEquals(
        1, profileResponse.getResponseMetadata().getResponseCrudInfo().getRestoredRowsCount());

    // verify audit service called for profile soft delete success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESTORE)),
            any(String.class));
  }

  @Test
  void testRestoreProfile_FailureNorAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/app_users/user/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreProfile_FailureNoPermission() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/profiles/profile/%s/restore", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
