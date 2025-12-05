package auth.service.app.integration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.dto.ProfileResponse;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StringUtils;

@DisplayName("SsoApiControllerTest Tests")
public class SsoApiControllerTest extends BaseTest {

    @MockitoBean private SecurityContext securityContext;

    private static String bearerAuthCredentials;

    @BeforeAll
    static void setUpBeforeAll() {
        bearerAuthCredentials = TestData.getBearerAuthCredentialsForTest();
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
        @DisplayName("ValidateToken failure for different platform")
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
        @DisplayName("ValidateToken failure for no auth")
        void testValidateToken_FailureNoAuth() {
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
            String bearerAuthCredentialsWithPermission = TestData.getBearerAuthCredentialsForTest();
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
        void testCheckPermissions_FailureDifferentPlatform() {
            String bearerAuthCredentialsWithPermission =
                    TestData.getBearerAuthCredentialsForTest();
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
        void testCheckPermissions_FailureNoToken() {
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
        }

        @Test
        @DisplayName("ListSsoProfiles failure for no auth")
        void testListSsoProfiles_FailureNoAuth() {
            webTestClient
                    .get()
                    .uri(String.format("/api/v1/sso/%s/ba_profiles/list", 4L))
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }
}