package integration.auth.service.app.controller;

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

import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRolePermissionRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("PermissionController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PermissionControllerTest extends BaseTest {

  @Autowired private PermissionRepository permissionRepository;
  @Autowired private PlatformRolePermissionRepository prpRepository;

  @MockitoBean private AuditService auditService;

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Nested
  @DisplayName("Create Permission Tests")
  class CreatePermissionTests {

    @Test
    @DisplayName("Create Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

      PermissionResponse response =
          webTestClient
              .post()
              .uri("/api/v1/permissions/permission")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals("NEW_PERMISSION_NAME", response.getPermissions().getFirst().getPermissionName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPermission(
              any(HttpServletRequest.class),
              argThat(
                  permissionEntityParam ->
                      permissionEntityParam
                          .getPermissionName()
                          .equals(request.getPermissionName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_CREATE)),
              any(String.class));

      // cleanup
      permissionRepository.deleteById(response.getPermissions().getFirst().getId());
    }

    @Test
    @DisplayName("Create Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/permissions/permission")
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
    @DisplayName("Create Permission Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/permissions/permission")
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
    @DisplayName("Create Permission Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request = new PermissionRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/permissions/permission")
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
    @DisplayName("Create Permission Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionEntity existingPermission = TestData.getPermissionEntities().getFirst();
      PermissionRequest request =
          new PermissionRequest(existingPermission.getPermissionName(), "NEW_PERMISSION_DESC");

      PermissionResponse response =
          webTestClient
              .post()
              .uri("/api/v1/permissions/permission")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isBadRequest()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Action Failed! Permission Name Already Exists!! Please Try Again!!!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Read Permissions Tests")
  class ReadPermissionsTests {

    @Test
    @DisplayName("Read Permissions Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(6, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Superuser Include Deleted")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(9, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Success with PlatformId")
    void test_Success_PlatformId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?isIncludeDeleted=true&platformId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(2, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Success with RoleId")
    void test_Success_RoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?isIncludeDeleted=true&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(2, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Success with PlatformId RoleId")
    void test_Success_PlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?platformId=1&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(2, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Superuser PlatformId RoleId ")
    void test_Success_SuperUser_PlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?platformId=1&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(2, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Superuser Include Deleted PlatformId RoleId ")
    void test_Success_SuperUser_IncludeDeletedPlatformIdRoleId() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri("/api/v1/permissions?isIncludeDeleted=true&platformId=1&roleId=1")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(3, response.getPermissions().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Permissions Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/permissions")
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
    @DisplayName("Read Permissions Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/permissions")
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
    @DisplayName("Read Permissions Failure With Exception")
    void test_Failure_Exception() {
      // not possible without mocking
    }
  }

  @Nested
  @DisplayName("Read Permission Tests")
  class ReadPermissionTests {

    @Test
    @DisplayName("Read Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals(ID, response.getPermissions().getFirst().getId());
      assertNull(response.getPermissions().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Permission Superuser Include Deleted And History")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/permissions/permission/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals(ID_DELETED, response.getPermissions().getFirst().getId());
      assertNotNull(response.getPermissions().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verify(auditService).auditPermissions(9L);
    }

    @Test
    @DisplayName("Read Permissions Failure Include Deleted Not SuperUser")
    void test_Failure_IncludeDeleteNotSuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/permissions/permission/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Permission Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Permission Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Read Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Read Permission Failure With Exception")
    void test_Failure_Exception() {
      // see test_Failure_IncludeDeleteNotSuperUser
    }
  }

  @Nested
  @DisplayName("Update Permission Tests")
  class UpdatePermissionTests {

    @Test
    @DisplayName("Update Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionEntity existingPermission = permissionRepository.findById(ID).orElseThrow();
      String existingName = existingPermission.getPermissionName();
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME", existingPermission.getPermissionDesc());

      PermissionResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals(ID, response.getPermissions().getFirst().getId());
      assertEquals("NEW_PERMISSION_NAME", response.getPermissions().getFirst().getPermissionName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPermission(
              any(HttpServletRequest.class),
              argThat(
                  permissionEntityParam ->
                      permissionEntityParam
                          .getPermissionName()
                          .equals(request.getPermissionName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_UPDATE)),
              any(String.class));

      // reset
      existingPermission.setPermissionName(existingName);
      permissionRepository.save(existingPermission);
    }

    @Test
    @DisplayName("Update Permission Failure Deleted")
    void testFailure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME_DELETED", "NEW_PERMISSION_DESC");

      PermissionResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Permission Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Update Permission Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Update Permission Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request = new PermissionRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Update Permission Failure With Exception Deleted Permission")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionRequest request =
          new PermissionRequest("NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

      PermissionResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/permissions/permission/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Permission Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Soft Delete Permission Tests")
  class SoftDeletePermissionTests {

    @Test
    @DisplayName("Soft Delete Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_SOFTDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionEntity existingPermission = permissionRepository.findById(ID).orElseThrow();

      PermissionResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals(ID, response.getPermissions().getFirst().getId());
      assertNotNull(response.getPermissions().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPermission(
              any(HttpServletRequest.class),
              argThat(
                  permissionEntityParam ->
                      permissionEntityParam
                          .getPermissionName()
                          .equals(existingPermission.getPermissionName())),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_DELETE_SOFT)),
              any(String.class));

      // reset
      existingPermission.setDeletedDate(null);
      permissionRepository.save(existingPermission);
    }

    @Test
    @DisplayName("Soft Delete Permission Failure Deleted")
    void test_Failure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Permission Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Soft Delete Permission Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s", ID))
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
    @DisplayName("Soft Delete Permission Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Hard Delete Permission Tests")
  class HardDeletePermissionTests {

    @Test
    @DisplayName("Hard Delete Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // setup
      PermissionEntity permissionEntity =
          permissionRepository.save(TestData.getNewPermissionEntity());

      PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
      RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
      PlatformRolePermissionEntity prpEntity =
          TestData.getPlatformRolePermissionEntity(
              platformEntity, roleEntity, permissionEntity, null);
      prpRepository.save(prpEntity);
      assertNotNull(prpRepository.findById(prpEntity.getId()).orElse(null));

      PermissionResponse response =
          webTestClient
              .delete()
              .uri(
                  String.format("/api/v1/permissions/permission/%s/hard", permissionEntity.getId()))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPermission(
              any(HttpServletRequest.class),
              argThat(
                  permissionEntityParam ->
                      permissionEntityParam
                          .getPermissionName()
                          .equals(permissionEntity.getPermissionName())),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_DELETE_HARD)),
              any(String.class));

      // PRP is also deleted
      assertNull(prpRepository.findById(prpEntity.getId()).orElse(null));
    }

    @Test
    @DisplayName("Hard Delete Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s/hard", ID))
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
    @DisplayName("Hard Delete Permission Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s/hard", ID))
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
    @DisplayName("Hard Delete Permission Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_HARDDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/permissions/permission/%s/hard", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Restore Soft Deleted Permission Tests")
  class RestorePermissionTests {

    @Test
    @DisplayName("Restore Permission Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(
              List.of("AUTHSVC_PERMISSION_RESTORE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PermissionEntity existingPermission = permissionRepository.findById(ID_DELETED).orElseThrow();

      PermissionResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/permissions/permission/%s/restore", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertEquals(1, response.getPermissions().size());
      assertEquals(ID_DELETED, response.getPermissions().getFirst().getId());
      assertNull(response.getPermissions().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditPermission(
              any(HttpServletRequest.class),
              argThat(
                  permissionEntityParam ->
                      permissionEntityParam
                          .getPermissionName()
                          .equals(existingPermission.getPermissionName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_RESTORE)),
              any(String.class));

      // reset
      existingPermission.setDeletedDate(LocalDateTime.now());
      permissionRepository.save(existingPermission);
    }

    @Test
    @DisplayName("Restore Permission Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/permissions/permission/%s/restore", ID))
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
    @DisplayName("Restore Permission Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/permissions/permission/%s/restore", ID))
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
    @DisplayName("Restore Permission Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PERMISSION_RESTORE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PermissionResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/permissions/permission/%s/restore", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(PermissionResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getPermissions());
      assertTrue(response.getPermissions().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Permission Not Found for [99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }
}
