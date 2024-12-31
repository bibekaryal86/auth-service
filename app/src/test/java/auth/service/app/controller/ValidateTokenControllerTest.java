package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;

public class ValidateTokenControllerTest extends BaseTest {

  private static String bearerAuthCredentials;
  @Mock private SecurityContext securityContext;

  @BeforeAll
  static void setUpBeforeAll() {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileDto profileDtoNoPermission = TestData.getProfileDto();
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
            .uri(String.format("/api/v1/validate/token/%s", ID))
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
        .uri("/api/v1/validate/token/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentials)
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}
