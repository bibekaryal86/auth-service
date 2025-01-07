package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import auth.service.BaseTest;
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
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

public class ProfileControllerTest extends BaseTest {

  private static ProfileEntity profileEntity;
  private static String bearerAuthCredentialsNoPermission;
  private static String bearerAuthCredentialsWithPermission;

  @Autowired private ProfileRepository profileRepository;
  @Autowired private PasswordUtils passwordUtils;

  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;
  @MockitoBean private AuditService auditService;

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
  void testReadProfiles_SuccessSuperUser() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri("/api/v1/profiles")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(6, profileResponse.getProfiles().size());

    assertAll(
        "Profiles Without Roles Controller",
        () -> assertEquals(0, profileResponse.getProfiles().get(0).getPlatformRoles().size()),
        () -> assertEquals(0, profileResponse.getProfiles().get(1).getPlatformRoles().size()),
        () -> assertEquals(0, profileResponse.getProfiles().get(2).getPlatformRoles().size()));
  }

  @Test
  void testReadProfiles_SuccessSuperUser_IncludeRoles() {
    String uri =
        UriComponentsBuilder.fromPath("/api/v1/profiles")
            .queryParam("isIncludeRoles", true)
            .toUriString();

    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(uri)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(6, profileResponse.getProfiles().size());

    assertAll(
        "Profiles With Roles Controller",
        () -> assertEquals(1, profileResponse.getProfiles().get(0).getPlatformRoles().size()),
        () -> assertEquals(1, profileResponse.getProfiles().get(1).getPlatformRoles().size()),
        () -> assertEquals(1, profileResponse.getProfiles().get(2).getPlatformRoles().size()));
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
            .uri(String.format("/api/v1/profiles/platform/%s", ID))
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
  void testReadProfilesByPlatformId_SuccessButEmpty() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/platform/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(0, profileResponse.getProfiles().size());
  }

  @Test
  void testReadProfilesByPlatformId_SuccessSuperUser() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/platform/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(3, profileResponse.getProfiles().size());
  }

  @Test
  void testReadProfilesByPlatform_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/profiles/platform/%s", ID))
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
  void testReadProfile_SuccessButEmpty() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", 4))
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
  void testReadProfile_SuccessSuperUser() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/%s", 4))
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
            .uri(String.format("/api/v1/profiles/profile/%s", 99))
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
  void testReadProfileByEmail_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/email/%s", EMAIL))
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
  void testReadProfileByEmail_SuccessButEmpty() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/email/%s", "firstlast-1@one.com"))
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
  void testReadProfileByEmail_SuccessSuperUser() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/email/%s", "firstlast-1@one.com"))
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
  void testReadProfileByEmail_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/profiles/profile/email/%s", EMAIL))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadProfileByEmail_FailureNotFound() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/profiles/profile/email/%s", "firstlast-99@one.com"))
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
        "Profile Not Found for [firstlast-99@one.com]",
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
            profileEntity.getStatusType().getId(),
            false,
            Collections.emptyList());

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
            profileEntity.getStatusType().getId(),
            false,
            Collections.emptyList());

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
            profileEntity.getStatusType().getId(),
            false,
            Collections.emptyList());
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
            profileEntity.getStatusType().getId(),
            false,
            Collections.emptyList());
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
    ProfileRequest profileRequest = new ProfileRequest("", null, "", null, null, 0L, false, null);
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
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("Email is required")
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("Status is required"));
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
        new ProfileEmailRequest("firstlast-1@one.com", "lastfirst-1@one.com");

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
    assertEquals("lastfirst-1@one.com", profileResponse.getProfiles().getFirst().getEmail());

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
    profileEntity.setEmail("firstlast-1@one.com");
    profileEntity = profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
    assertEquals("firstlast-1@one.com", profileEntity.getEmail());
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
        new ProfilePasswordRequest("firstlast-1@one.com", "password-new-one");

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
  void testDeleteProfileAddress_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/profiles/profile/%s/address/%s", ID, 2))
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
    assertEquals(1, profileResponse.getProfiles().getFirst().getAddresses().size());
    assertEquals(ID, profileResponse.getProfiles().getFirst().getId());

    // verify audit service called for profile address delete success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_ADDRESS_DELETE)),
            any(String.class));
  }

  @Test
  void testDeleteProfileAddress_Success_SuperUser() {
    ProfileResponse profileResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/profiles/profile/%s/address/%s", 2, 3))
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
    assertEquals(1, profileResponse.getProfiles().getFirst().getAddresses().size());
    assertEquals(2, profileResponse.getProfiles().getFirst().getId());

    // verify audit service called for profile address delete success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_ADDRESS_DELETE)),
            any(String.class));
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
  void testDeleteProfileAddress_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/profiles/profile/%s/address/%s", 3, 5))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteProfile_Success() {
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
  void testHardDeleteAppUser_Success() {
    // setup
    ProfileEntity profileEntityNew = TestData.getNewProfileEntity();
    profileEntityNew.setStatusType(TestData.getStatusTypeEntities().getFirst());
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
  void testRestoreAppUser_Success() {
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
