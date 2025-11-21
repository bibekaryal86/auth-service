package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StringUtils;

public class SsoApiControllerTest extends BaseTest {

  @MockitoBean private SecurityContext securityContext;

  private static String bearerAuthCredentials;
  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
    bearerAuthCredentials =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
  }

  @AfterEach
  void tearDown() {
    reset(securityContext);
  }

  @Test
  void testValidateToken_Success() {
    AuthToken authTokenInput = TestData.getAuthToken();

    when(securityContext.getAuthentication())
        .thenReturn(
            new TestingAuthenticationToken(
                authTokenInput.getProfile().getEmail(), authTokenInput, Collections.emptyList()));

    AuthToken authTokenOutput =
        webTestClient
            .get()
            .uri(String.format("/api/v1/sso/%s/validate/token", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentials)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AuthToken.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(authTokenOutput);
    assertEquals(authTokenInput.getPlatform().getId(), authTokenOutput.getPlatform().getId());
    assertEquals(authTokenInput.getProfile().getId(), authTokenOutput.getProfile().getId());
  }

  @Test
  void testValidateToken_FailureDifferentPlatform() {
    AuthToken authTokenInput = TestData.getAuthToken();

    when(securityContext.getAuthentication())
        .thenReturn(
            new TestingAuthenticationToken(
                authTokenInput.getProfile().getEmail(), authTokenInput, Collections.emptyList()));

    webTestClient
        .get()
        .uri("/api/v1/sso/9999/validate/token")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentials)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testValidateToken_FailureNoToken() {
    webTestClient
        .get()
        .uri("/api/v1/sso/9999/validate/token")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testCheckPermissions_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("PERMISSION-1"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    List<String> permissionsToCheck = List.of("PERMISSION-1");

    Map<String, Boolean> response =
        webTestClient
            .post()
            .uri(String.format("/api/v1/sso/%s/check/permissions", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .bodyValue(permissionsToCheck)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<Map<String, Boolean>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(response);
    assertEquals(1, response.size());
  }

  @Test
  void testCheckPermissions_FailureDifferentPlatform() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("PERMISSION-1"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    List<String> permissionsToCheck = List.of("PERMISSION-1");

    webTestClient
        .post()
        .uri("/api/v1/sso/9999/check/permissions")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .bodyValue(permissionsToCheck)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testCheckPermissions_FailureNoToken() {
    webTestClient
        .post()
        .uri("/api/v1/sso/9999/check/permissions")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testListSsoProfiles_Success() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/sso/%s/ba_profiles/list", 4L))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());
    assertTrue(profileResponse.getProfiles().getFirst().getPlatformRoles().isEmpty());
    assertNull(profileResponse.getProfiles().getFirst().getProfileAddress());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(profileResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(profileResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () -> assertTrue(profileResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(profileResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

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
  void testListSsoProfiles_Success_RequestMetadata() {
    ProfileResponse profileResponse =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/sso/%s/ba_profiles/list?pageNumber=1&perPage=10&sortColumn=firstName&sortDirection=DESC",
                    4L))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthCredentialsForTest)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ProfileResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(profileResponse);
    assertNotNull(profileResponse.getProfiles());
    assertEquals(1, profileResponse.getProfiles().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(profileResponse.getResponseMetadata()),
        () -> assertNotNull(profileResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    profileResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(profileResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(profileResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(profileResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(profileResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () -> assertTrue(profileResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(profileResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(profileResponse.getRequestMetadata()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(profileResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, profileResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, profileResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("firstName", profileResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, profileResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testListSsoProfiles_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/sso/%s/ba_profiles/list", 4L))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
