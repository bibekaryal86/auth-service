package integration.auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.ProfileAddressRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.service.AuditService;
import auth.service.app.util.CommonUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("ProfileController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProfileControllerTest extends BaseTest {

  @Autowired private ProfileRepository profileRepository;
  @Autowired private ProfileAddressRepository profileAddressRepository;
  @Autowired private PlatformProfileRoleRepository pprRepository;
  @Autowired private TokenRepository tokenRepository;

  @Autowired private ApplicationEventPublisher publisher;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  @BeforeEach
  void setUp() {
    when(envServiceConnector.getBaseUrlForLinkInEmail())
        .thenReturn("https://base-url-for-link-in-email.com");
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
    reset(publisher);
  }

  @Nested
  @DisplayName("Read Profiles Tests")
  class ReadProfilesTests {

    @Test
    @DisplayName("Read Profiles Success - Returns Own Profiles Only")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Superuser Include Deleted")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(9, response.getProfiles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Success with PlatformId")
    void test_Success_PlatformId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?isIncludeDeleted=true&platformId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(3, response.getProfiles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Success with RoleId")
    void test_Success_RoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?isIncludeDeleted=true&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Success with PlatformId RoleId")
    void test_Success_PlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?platformId=1&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Superuser PlatformId RoleId ")
    void test_Success_SuperUser_PlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?platformId=1&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Superuser Include Deleted PlatformId RoleId ")
    void test_Success_SuperUser_IncludeDeletedPlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri("/api/v1/profiles?isIncludeDeleted=true&platformId=9&roleId=9")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Profiles Failure No Permissions")
    void test_Failure_NoPermissions() {
      // does not apply, user can view their own profile regardless of permission
    }

    @Test
    @DisplayName("Read Profiles Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/profiles")
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Read Profiles Failure With Exception")
    void test_Failure_Exception() {
      // not possible without mocking
    }
  }

  @Nested
  @DisplayName("Read Profile Tests")
  class ReadProfileTests {

    @Test
    @DisplayName("Read Profile Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID, response.getProfiles().getFirst().getId());
      assertNull(response.getProfiles().getFirst().getDeletedDate());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Profile Superuser Include Deleted And History")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/profiles/profile/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID_DELETED, response.getProfiles().getFirst().getId());
      assertNotNull(response.getProfiles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verify(auditService).auditProfiles(9L);
    }

    @Test
    @DisplayName("Read Profile Failure No Access Others Profile")
    void test_Failure_NoAccessOthersProfile() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/profiles/profile/%s?isIncludeDeleted=true&isIncludeHistory=true", 2))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions to profile entity...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Profile Failure No Permissions")
    void test_Failure_NoPermissions() {
      // not applicable, can view own profile and can't view others regardless of permission
    }

    @Test
    @DisplayName("Read Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Read Profile Failure With Exception")
    void test_Failure_Exception() {
      // see test_Failure_IncludeDeleteNotSuperUser
    }
  }

  @Nested
  @DisplayName("Update Profile Tests")
  class UpdateProfileTests {

    @Test
    @DisplayName("Update Profile Success - Own Profile")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(ID).orElseThrow();
      String existingFirstName = existingProfile.getFirstName();
      String existingLastName = existingProfile.getLastName();
      ProfileRequest request =
          TestData.getProfileRequest(
              "NEW_FIRST_NAME",
              "NEW_LAST_NAME",
              "new@email.com",
              existingProfile.getPassword(),
              null);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID, response.getProfiles().getFirst().getId());
      assertEquals("NEW_FIRST_NAME", response.getProfiles().getFirst().getFirstName());
      assertEquals("NEW_LAST_NAME", response.getProfiles().getFirst().getLastName());
      // email is not changed
      assertNotEquals("new@email.com", response.getProfiles().getFirst().getEmail());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
              any(String.class));

      // reset
      existingProfile.setFirstName(existingFirstName);
      existingProfile.setLastName(existingLastName);
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Update Profile Success - Others Profile")
    void test_Success_Superuser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(5L).orElseThrow();
      ProfileAddressEntity existingProfileAddressEntity =
          profileAddressRepository.findById(2L).orElseThrow();
      String existingFirstName = existingProfile.getFirstName();
      String existingLastName = existingProfile.getLastName();

      assertNotEquals("123 New St", existingProfileAddressEntity.getStreet());

      ProfileRequest request =
          TestData.getProfileRequest(
              "NEW_FIRST_NAME",
              "NEW_LAST_NAME",
              "new@email.com",
              null,
              TestData.getProfileAddressRequest(
                  existingProfileAddressEntity.getId(),
                  existingProfile.getId(),
                  "123 New St",
                  false));

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", 5))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(5L, response.getProfiles().getFirst().getId());
      assertEquals("NEW_FIRST_NAME", response.getProfiles().getFirst().getFirstName());
      assertEquals("NEW_LAST_NAME", response.getProfiles().getFirst().getLastName());
      // email is not changed
      assertNotEquals("new@email.com", response.getProfiles().getFirst().getEmail());
      // address is changed
      assertEquals("123 New St", response.getProfiles().getFirst().getProfileAddress().getStreet());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
              any(String.class));

      // reset
      existingProfile.setFirstName(existingFirstName);
      existingProfile.setLastName(existingLastName);
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Update Profile Failure - Others Profile")
    void test_Failure_OthersProfile() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(2L).orElseThrow();
      ProfileRequest request =
          TestData.getProfileRequest(
              "NEW_FIRST_NAME",
              "NEW_LAST_NAME",
              "new@email.com",
              existingProfile.getPassword(),
              null);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", 2))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions to profile entity...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Profile Failure Deleted")
    void testFailure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileRequest request =
          TestData.getProfileRequest("NEW FIRST NAME", "NEW LAST NAME", "NEW_EMAIL", null, null);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Profile Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Profile Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileRequest request =
          TestData.getProfileRequest("", "", "NEW_EMAIL", "NEW_PASSWORD", null);

      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
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
                  .contains("Last Name is required"));
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Profile Failure With Exception Deleted Profile")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileRequest request =
          TestData.getProfileRequest("NEW FIRST NAME", "NEW LAST NAME", "NEW_EMAIL", null, null);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Profile Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Update Profile Email Tests")
  class UpdateProfileEmailTests {

    @Test
    @DisplayName("Update Profile Email Success - Own Profile")
    void test_Success() {
      AuthToken authToken = TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(ID).orElseThrow();
      String existingEmail = existingProfile.getEmail();
      String newEmail = "new@email.com";
      ProfileEmailRequest request = new ProfileEmailRequest(existingEmail, newEmail);

        TokenEntity tokenEntity = TestData.getTokenEntity(1, TestData.getPlatformEntities().getFirst(), existingProfile);
        tokenEntity = tokenRepository.save(tokenEntity);

        assertNull(tokenEntity.getDeletedDate());

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID, response.getProfiles().getFirst().getId());
      assertEquals("new@email.com", response.getProfiles().getFirst().getEmail());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_EMAIL_UPDATE)),
              any(String.class));

      verify(publisher)
          .publishEvent(
              ArgumentMatchers.argThat(
                  event ->
                      event instanceof ProfileEvent
                          && ((ProfileEvent) event).getEventType()
                              == TypeEnums.EventType.UPDATE_EMAIL));

      tokenEntity = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
      assertNotNull(tokenEntity.getDeletedDate());

      // reset
      existingProfile.setEmail(existingEmail);
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Update Profile Email Success - Others Profile")
    void test_Success_Superuser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(5L).orElseThrow();
      String existingEmail = existingProfile.getEmail();
      String newEmail = "new@email.com";
      ProfileEmailRequest request = new ProfileEmailRequest(existingEmail, newEmail);

        TokenEntity tokenEntity = TestData.getTokenEntity(2, TestData.getPlatformEntities().getFirst(), existingProfile);
        tokenEntity = tokenRepository.save(tokenEntity);

        assertNull(tokenEntity.getDeletedDate());

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", 5, 5))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(5L, response.getProfiles().getFirst().getId());
      assertEquals("new@email.com", response.getProfiles().getFirst().getEmail());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

        tokenEntity = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
        assertNotNull(tokenEntity.getDeletedDate());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_EMAIL_UPDATE)),
              any(String.class));

      verify(publisher)
          .publishEvent(
              ArgumentMatchers.argThat(
                  event ->
                      event instanceof ProfileEvent
                          && ((ProfileEvent) event).getEventType()
                              == TypeEnums.EventType.UPDATE_EMAIL));

      // reset
      existingProfile.setEmail(existingEmail);
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Update Profile Email Failure - Others Profile")
    void test_Failure_OthersProfile() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(2L).orElseThrow();
      String existingEmail = existingProfile.getEmail();
      String newEmail = "new@email.com";
      ProfileEmailRequest request = new ProfileEmailRequest(existingEmail, newEmail);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", 2, 2))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions to profile entity...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Update Profile Email Failure Deleted")
    void testFailure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(7L).orElseThrow();
      String existingEmail = existingProfile.getEmail();
      String newEmail = "new@email.com";
      ProfileEmailRequest request = new ProfileEmailRequest(existingEmail, newEmail);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID_DELETED, 7))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Profile Role Not Found for [9,profile@seven.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Update Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Update Profile Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEmailRequest request = new ProfileEmailRequest("", "");

      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/platform/%s/profile/%s/email", ID, ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
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
                  .contains("Old Email is Required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("New Email is Required"));

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Update Profile Email Failure With Exception Deleted Platform")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(8L).orElseThrow();
      String existingEmail = existingProfile.getEmail();
      String newEmail = "new@email.com";
      ProfileEmailRequest request = new ProfileEmailRequest(existingEmail, newEmail);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(
                  String.format(
                      "/api/v1/profiles/platform/%s/profile/%s/email", ID_DELETED, ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Profile Role Not Found for [9,profile@eight.com]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
      verifyNoInteractions(publisher);
    }
  }

  @Nested
  @DisplayName("Soft Delete Profile Tests")
  class SoftDeleteProfileTests {

    @Test
    @DisplayName("Soft Delete Profile Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PROFILE_SOFTDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(ID).orElseThrow();

      ProfileResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID, response.getProfiles().getFirst().getId());
      assertNotNull(response.getProfiles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_DELETE_SOFT)),
              any(String.class));

      // reset
      existingProfile.setDeletedDate(null);
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Soft Delete Profile Failure Deleted")
    void test_Failure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Profile Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Profile Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();
      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Profile Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Hard Delete Profile Tests")
  class HardDeleteProfileTests {

    @Test
    @DisplayName("Hard Delete Profile Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PROFILE_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // setup
      ProfileEntity profileEntity = profileRepository.save(TestData.getNewProfileEntity());

      ProfileAddressEntity profileAddressEntity = TestData.getNewProfileAddressEntity();
      profileAddressEntity.setProfile(profileEntity);
      profileAddressEntity = profileAddressRepository.save(profileAddressEntity);

      profileEntity.setProfileAddress(profileAddressEntity);
      profileRepository.save(profileEntity);

      PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
      RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
      PlatformProfileRoleEntity pprEntity =
          TestData.getPlatformProfileRoleEntity(platformEntity, profileEntity, roleEntity, null);
      pprRepository.save(pprEntity);

      assertNotNull(profileAddressRepository.findById(profileAddressEntity.getId()).orElse(null));
      assertNotNull(pprRepository.findById(pprEntity.getId()).orElse(null));

      ProfileResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s/hard", profileEntity.getId()))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_DELETE_HARD)),
              any(String.class));

      // PPR is also deleted
      assertNull(pprRepository.findById(pprEntity.getId()).orElse(null));
      // address is also deleted
      assertNull(profileAddressRepository.findById(profileAddressEntity.getId()).orElse(null));
    }

    @Test
    @DisplayName("Hard Delete Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s/hard", ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Hard Delete Profile Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s/hard", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();
      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Hard Delete Profile Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_HARDDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/profiles/profile/%s/hard", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Restore Soft Deleted Profile Tests")
  class RestoreProfileTests {

    @Test
    @DisplayName("Restore Profile Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_RESTORE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ProfileEntity existingProfile = profileRepository.findById(ID_DELETED).orElseThrow();

      ProfileResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/profiles/profile/%s/restore", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(ID_DELETED, response.getProfiles().getFirst().getId());
      assertNull(response.getProfiles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESTORE)),
              any(String.class));

      // reset
      existingProfile.setDeletedDate(LocalDateTime.now());
      profileRepository.save(existingProfile);
    }

    @Test
    @DisplayName("Restore Profile Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/profiles/profile/%s/restore", ID))
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
          "Profile not authenticated to access this resource...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Restore Profile Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/profiles/profile/%s/restore", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();
      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Denied: Profile does not have required permissions...",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Restore Profile Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_RESTORE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/profiles/profile/%s/restore", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertTrue(response.getProfiles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Profile Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("ProfileAddress Tests")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class ProfileAddressTests {
    final Long PROFILE_ID = 4L;

    @Test
    @Order(1)
    @DisplayName("Add Profile Address")
    void test_Success_AddProfileAddress() {
      ProfileEntity existingProfile = profileRepository.findById(PROFILE_ID).orElseThrow();
      ProfileRequest request =
          TestData.getProfileRequest(
              existingProfile.getFirstName(),
              existingProfile.getLastName(),
              existingProfile.getEmail(),
              existingProfile.getPassword(),
              TestData.getProfileAddressRequest(
                  null, existingProfile.getId(), "123 New Street", false));

      assertNull(existingProfile.getProfileAddress());

      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      authToken =
          new AuthToken(
              authToken.getPlatform(),
              new AuthToken.AuthTokenProfile(existingProfile.getId(), existingProfile.getEmail()),
              authToken.getRoles(),
              authToken.getPermissions(),
              Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // Test 1
      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", PROFILE_ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(PROFILE_ID, response.getProfiles().getFirst().getId());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertEquals(
          "123 New Street", response.getProfiles().getFirst().getProfileAddress().getStreet());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
              any(String.class));
    }

    @Test
    @Order(2)
    @DisplayName("Update Profile Address")
    void test_Success_UpdateProfileAddress() {
      ProfileEntity existingProfile = profileRepository.findById(PROFILE_ID).orElseThrow();
      ProfileRequest request =
          TestData.getProfileRequest(
              existingProfile.getFirstName(),
              existingProfile.getLastName(),
              existingProfile.getEmail(),
              existingProfile.getPassword(),
              TestData.getProfileAddressRequest(
                  PROFILE_ID, existingProfile.getId(), "456 New Street", false));

      // Depends on tests executed in order
      assertNotNull(existingProfile.getProfileAddress());

      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      authToken =
          new AuthToken(
              authToken.getPlatform(),
              new AuthToken.AuthTokenProfile(existingProfile.getId(), existingProfile.getEmail()),
              authToken.getRoles(),
              authToken.getPermissions(),
              Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", PROFILE_ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(PROFILE_ID, response.getProfiles().getFirst().getId());
      assertNotNull(response.getProfiles().getFirst().getProfileAddress());
      assertEquals(
          "456 New Street", response.getProfiles().getFirst().getProfileAddress().getStreet());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
              any(String.class));
    }

    @Test
    @Order(3)
    @DisplayName("Delete Profile Address")
    void test_Success_DeleteProfileAddress() {
      ProfileEntity existingProfile = profileRepository.findById(PROFILE_ID).orElseThrow();
      ProfileRequest request =
          TestData.getProfileRequest(
              existingProfile.getFirstName(),
              existingProfile.getLastName(),
              existingProfile.getEmail(),
              existingProfile.getPassword(),
              TestData.getProfileAddressRequest(PROFILE_ID, existingProfile.getId(), null, true));

      // Depends on tests executed in order
      assertNotNull(existingProfile.getProfileAddress());

      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_UPDATE"), Boolean.FALSE);
      authToken =
          new AuthToken(
              authToken.getPlatform(),
              new AuthToken.AuthTokenProfile(existingProfile.getId(), existingProfile.getEmail()),
              authToken.getRoles(),
              authToken.getPermissions(),
              Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // Test 1
      ProfileResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/profiles/profile/%s", PROFILE_ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getProfiles());
      assertEquals(1, response.getProfiles().size());
      assertEquals(4L, response.getProfiles().getFirst().getId());
      assertNull(response.getProfiles().getFirst().getProfileAddress());

      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_UPDATE)),
              any(String.class));
    }
  }
}
