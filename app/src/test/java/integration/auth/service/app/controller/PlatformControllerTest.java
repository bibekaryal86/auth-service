package integration.auth.service.app.controller;

import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRolePermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.service.AuditService;
import auth.service.app.util.CommonUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@Tag("integration")
@DisplayName("PlatformController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PlatformControllerTest extends BaseTest {

  @Autowired private PlatformRepository platformRepository;
  @Autowired private PlatformProfileRoleRepository pprRepository;
  @Autowired private PlatformRolePermissionRepository prpRepository;

  @MockitoBean private AuditService auditService;

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Nested
  @DisplayName("Create Platform Tests")
  class CreatePlatformTests {

    @Test
    @DisplayName("Create Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

      PlatformResponse response =
          webTestClient
              .post()
              .uri("/api/v1/platforms/platform")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals("NEW_PLATFORM_NAME", response.getPlatforms().getFirst().getPlatformName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPlatform(
              any(HttpServletRequest.class),
              argThat(
                  platformEntityParam -> platformEntityParam.getPlatformName().equals(request.getPlatformName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_CREATE)),
              any(String.class));

      // cleanup
      platformRepository.deleteById(response.getPlatforms().getFirst().getId());
    }

    @Test
    @DisplayName("Create Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/platforms/platform")
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
    @DisplayName("Create Platform Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/platforms/platform")
              .bodyValue(request)
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
    @DisplayName("Create Platform Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/platforms/platform")
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
          response.getResponseMetadata().responseStatusInfo().errMsg().contains("Name is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("Description is required"));
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Create Platform Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformEntity existingPlatform = TestData.getPlatformEntities().getFirst();
      PlatformRequest request = new PlatformRequest(existingPlatform.getPlatformName(), "NEW_PLATFORM_DESC");

      PlatformResponse response =
          webTestClient
              .post()
              .uri("/api/v1/platforms/platform")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isBadRequest()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Action Failed! Platform Name Already Exists!! Please Try Again!!!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Read Platforms Tests")
  class ReadPlatformsTests {

    @Test
    @DisplayName("Read Platforms Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .get()
              .uri("/api/v1/platforms?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(6, response.getPlatforms().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Platforms Superuser Include Deleted")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .get()
              .uri("/api/v1/platforms?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(9, response.getPlatforms().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Platforms Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/platforms")
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
    }

    @Test
    @DisplayName("Read Platforms Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/platforms")
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
    @DisplayName("Read Platforms Failure With Exception")
    void test_Failure_Exception() {
      // not possible without mocking
    }
  }

  @Nested
  @DisplayName("Read Platform Tests")
  class ReadPlatformTests {

    @Test
    @DisplayName("Read Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals(ID, response.getPlatforms().getFirst().getId());
      assertNull(response.getPlatforms().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Platform Superuser Include Deleted And History")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/platforms/platform/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals(ID_DELETED, response.getPlatforms().getFirst().getId());
      assertNotNull(response.getPlatforms().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verify(auditService).auditPlatforms(9L);
    }

    @Test
    @DisplayName("Read Platforms Failure Include Deleted Not SuperUser")
    void test_Failure_IncludeDeleteNotSuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/platforms/platform/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Platform Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
    }

    @Test
    @DisplayName("Read Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
    @DisplayName("Read Platform Failure With Exception")
    void test_Failure_Exception() {
      // see test_Failure_IncludeDeleteNotSuperUser
    }
  }

  @Nested
  @DisplayName("Update Platform Tests")
  class UpdatePlatformTests {

    @Test
    @DisplayName("Update Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformEntity existingPlatform = platformRepository.findById(ID).orElseThrow();
      String existingName = existingPlatform.getPlatformName();
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME", existingPlatform.getPlatformDesc());

      PlatformResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals(ID, response.getPlatforms().getFirst().getId());
      assertEquals("NEW_PLATFORM_NAME", response.getPlatforms().getFirst().getPlatformName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPlatform(
              any(HttpServletRequest.class),
              argThat(
                  platformEntityParam -> platformEntityParam.getPlatformName().equals(request.getPlatformName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_UPDATE)),
              any(String.class));

      // reset
      existingPlatform.setPlatformName(existingName);
      platformRepository.save(existingPlatform);
    }

    @Test
    @DisplayName("Update Platform Failure Deleted")
    void testFailure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME_DELETED", "NEW_PLATFORM_DESC");

      PlatformResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
    @DisplayName("Update Platform Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
              .bodyValue(request)
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
    @DisplayName("Update Platform Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
          response.getResponseMetadata().responseStatusInfo().errMsg().contains("Name is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("Description is required"));
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Platform Failure With Exception Deleted Platform")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRequest request = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

      PlatformResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/platforms/platform/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Soft Delete Platform Tests")
  class SoftDeletePlatformTests {

    @Test
    @DisplayName("Soft Delete Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_SOFTDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformEntity existingPlatform = platformRepository.findById(ID).orElseThrow();

      PlatformResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals(ID, response.getPlatforms().getFirst().getId());
      assertNotNull(response.getPlatforms().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPlatform(
              any(HttpServletRequest.class),
              argThat(
                  platformEntityParam ->
                      platformEntityParam.getPlatformName().equals(existingPlatform.getPlatformName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT)),
              any(String.class));

      // reset
      existingPlatform.setDeletedDate(null);
      platformRepository.save(existingPlatform);
    }

    @Test
    @DisplayName("Soft Delete Platform Failure Deleted")
    void test_Failure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
    @DisplayName("Soft Delete Platform Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s", ID))
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
    @DisplayName("Soft Delete Platform Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Hard Delete Platform Tests")
  class HardDeletePlatformTests {

    @Test
    @DisplayName("Hard Delete Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // setup
      PlatformEntity platformEntity = platformRepository.save(TestData.getNewPlatformEntity());

      RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
      ProfileEntity profileEntity = TestData.getProfileEntities().getFirst();
      PermissionEntity permissionEntity = TestData.getPermissionEntities().getFirst();
      PlatformProfileRoleEntity pprEntity =
          TestData.getPlatformProfileRoleEntity(platformEntity, profileEntity, roleEntity, null);
      PlatformRolePermissionEntity prpEntity =
          TestData.getPlatformRolePermissionEntity(
              platformEntity, roleEntity, permissionEntity, null);
      pprRepository.save(pprEntity);
      prpRepository.save(prpEntity);
      assertNotNull(pprRepository.findById(pprEntity.getId()).orElse(null));
      assertNotNull(prpRepository.findById(prpEntity.getId()).orElse(null));

      PlatformResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s/hard", platformEntity.getId()))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPlatform(
              any(HttpServletRequest.class),
              argThat(
                  platformEntityParam ->
                      platformEntityParam.getPlatformName().equals(platformEntity.getPlatformName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_DELETE_HARD)),
              any(String.class));

      // PPR and PRP are also deleted
      assertNull(pprRepository.findById(pprEntity.getId()).orElse(null));
      assertNull(prpRepository.findById(prpEntity.getId()).orElse(null));
    }

    @Test
    @DisplayName("Hard Delete Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s/hard", ID))
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
    @DisplayName("Hard Delete Platform Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s/hard", ID))
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
    @DisplayName("Hard Delete Platform Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_HARDDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/platforms/platform/%s/hard", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Restore Soft Deleted Platform Tests")
  class RestorePlatformTests {

    @Test
    @DisplayName("Restore Platform Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_RESTORE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformEntity existingPlatform = platformRepository.findById(ID_DELETED).orElseThrow();

      PlatformResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/platforms/platform/%s/restore", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertEquals(1, response.getPlatforms().size());
      assertEquals(ID_DELETED, response.getPlatforms().getFirst().getId());
      assertNull(response.getPlatforms().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPlatform(
              any(HttpServletRequest.class),
              argThat(
                  platformEntityParam ->
                      platformEntityParam.getPlatformName().equals(existingPlatform.getPlatformName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_RESTORE)),
              any(String.class));

      // reset
      existingPlatform.setDeletedDate(LocalDateTime.now());
      platformRepository.save(existingPlatform);
    }

    @Test
    @DisplayName("Restore Platform Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
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
    @DisplayName("Restore Platform Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
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
    @DisplayName("Restore Platform Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_RESTORE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/platforms/platform/%s/restore", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PlatformResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPlatforms());
      assertTrue(response.getPlatforms().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }
}
