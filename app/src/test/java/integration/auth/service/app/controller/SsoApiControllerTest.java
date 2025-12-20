package integration.auth.service.app.controller;

import static auth.service.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_SELF_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.app.model.dto.ProfileResponse;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("SsoApiController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SsoApiControllerTest extends BaseTest {

  @MockitoBean private SecurityContext securityContext;

  private static String bearerAuthCredentials;

  private String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", ENV_SELF_USERNAME, ENV_SELF_PASSWORD)
                  .getBytes(StandardCharsets.UTF_8));

  @BeforeAll
  static void setUpBeforeAll() {
    bearerAuthCredentials = TestData.getBearerAuthCredentialsForTest(TestData.getAuthToken());
  }

  @AfterEach
  void tearDownAfterEach() {
    reset(securityContext);
  }

  @Nested
  @DisplayName("ValidateToken tests")
  class ValidateToken {

    @Test
    @DisplayName("ValidateToken success")
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
    @DisplayName("ValidateToken failure different platform")
    void testValidateToken_Failure_DifferentPlatform() {
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
    @DisplayName("ValidateToken failure no auth")
    void testValidateToken_Failure_NoAuth() {
      webTestClient
          .get()
          .uri("/api/v1/sso/9999/validate/token")
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }

  @Nested
  @DisplayName("CheckPermissions tests")
  class CheckPermissions {

    @DisplayName("CheckPermissions success")
    @Test
    void testCheckPermissions_Success() {
      String bearerAuthCredentialsWithPermission =
          TestData.getBearerAuthCredentialsForTest(TestData.getAuthToken());
      List<String> permissionsToCheck = List.of("Permission 1", "Permission 2", "Permission 3");

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
      assertEquals(3, response.size());
    }

    @Test
    @DisplayName("CheckPermissions failure different platform")
    void testCheckPermissions_Failure_DifferentPlatform() {
      String bearerAuthCredentialsWithPermission =
          TestData.getBearerAuthCredentialsForTest(TestData.getAuthToken());
      List<String> permissionsToCheck = List.of("Permission 1");

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
    @DisplayName("CheckPermissions failure no auth")
    void testCheckPermissions_Failure_NoToken() {
      webTestClient
          .post()
          .uri("/api/v1/sso/9999/check/permissions")
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }

  @Nested
  @DisplayName("ListSsoProfiles tests")
  class ListSsoProfiles {

    @Test
    @DisplayName("ListSsoProfiles success")
    void testListSsoProfiles_Success() {
      ProfileResponse profileResponse =
          webTestClient
              .get()
              .uri(String.format("/api/v1/sso/%s/ba_profiles/list", 1L))
              .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthCredentialsForTest)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ProfileResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(profileResponse);
      assertNotNull(profileResponse.getProfiles());
      assertEquals(2, profileResponse.getProfiles().size());
    }

    @Test
    @DisplayName("ListSsoProfiles failure for no auth")
    void testListSsoProfiles_Failure_NoAuth() {
      webTestClient
          .get()
          .uri(String.format("/api/v1/sso/%s/ba_profiles/list", 4L))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }
}
