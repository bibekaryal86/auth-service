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

import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRolePermissionRepository;
import auth.service.app.repository.RoleRepository;
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
@DisplayName("RoleController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RoleControllerTest extends BaseTest {

  @Autowired private RoleRepository roleRepository;
  @Autowired private PlatformProfileRoleRepository pprRepository;
  @Autowired private PlatformRolePermissionRepository prpRepository;

  @MockitoBean private AuditService auditService;

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Nested
  @DisplayName("Create Role Tests")
  class CreateRoleTests {

    @Test
    @DisplayName("Create Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

      RoleResponse response =
          webTestClient
              .post()
              .uri("/api/v1/roles/role")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals("NEW_ROLE_NAME", response.getRoles().getFirst().getRoleName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              argThat(
                  roleEntityParam -> roleEntityParam.getRoleName().equals(request.getRoleName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_CREATE)),
              any(String.class));

      // cleanup
      roleRepository.deleteById(response.getRoles().getFirst().getId());
    }

    @Test
    @DisplayName("Create Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/roles/role")
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
    @DisplayName("Create Role Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/roles/role")
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
    @DisplayName("Create Role Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/roles/role")
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
    @DisplayName("Create Role Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_CREATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleEntity existingRole = TestData.getRoleEntities().getFirst();
      RoleRequest request = new RoleRequest(existingRole.getRoleName(), "NEW_ROLE_DESC");

      RoleResponse response =
          webTestClient
              .post()
              .uri("/api/v1/roles/role")
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isBadRequest()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Action Failed! Role Name Already Exists!! Please Try Again!!!",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Read Roles Tests")
  class ReadRolesTests {

    @Test
    @DisplayName("Read Roles Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri("/api/v1/roles?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(6, response.getRoles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Roles Superuser Include Deleted")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri("/api/v1/roles?isIncludeDeleted=true")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(9, response.getRoles().size());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());
    }

    @Test
    @DisplayName("Read Roles Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/roles")
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
    @DisplayName("Read Roles Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/roles")
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
    @DisplayName("Read Roles Failure With Exception")
    void test_Failure_Exception() {
      // not possible without mocking
    }
  }

  @Nested
  @DisplayName("Read Role Tests")
  class ReadRoleTests {

    @Test
    @DisplayName("Read Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/roles/role/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals(ID, response.getRoles().getFirst().getId());
      assertNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Role Superuser Include Deleted And History")
    void test_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/roles/role/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals(ID_DELETED, response.getRoles().getFirst().getId());
      assertNotNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verify(auditService).auditRoles(9L);
    }

    @Test
    @DisplayName("Read Roles Failure Include Deleted Not SuperUser")
    void test_Failure_IncludeDeleteNotSuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri(
                  String.format(
                      "/api/v1/roles/role/%s?isIncludeDeleted=true&isIncludeHistory=true",
                      ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Role Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Role Failure No Permissions")
    void test_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Read Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Read Role Failure With Exception")
    void test_Failure_Exception() {
      // see test_Failure_IncludeDeleteNotSuperUser
    }
  }

  @Nested
  @DisplayName("Update Role Tests")
  class UpdateRoleTests {

    @Test
    @DisplayName("Update Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleEntity existingRole = roleRepository.findById(ID).orElseThrow();
      String existingName = existingRole.getRoleName();
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME", existingRole.getRoleDesc());

      RoleResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals(ID, response.getRoles().getFirst().getId());
      assertEquals("NEW_ROLE_NAME", response.getRoles().getFirst().getRoleName());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 1, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              argThat(
                  roleEntityParam -> roleEntityParam.getRoleName().equals(request.getRoleName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_UPDATE)),
              any(String.class));

      // reset
      existingRole.setRoleName(existingName);
      roleRepository.save(existingRole);
    }

    @Test
    @DisplayName("Update Role Failure Deleted")
    void testFailure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME_DELETED", "NEW_ROLE_DESC");

      RoleResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Role Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Update Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Update Role Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Update Role Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_UPDATE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("", null);

      ResponseWithMetadata response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Update Role Failure With Exception Deleted Role")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_UPDATE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleRequest request = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

      RoleResponse response =
          webTestClient
              .put()
              .uri(String.format("/api/v1/roles/role/%s", ID_DELETED))
              .bodyValue(request)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Role Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Soft Delete Role Tests")
  class SoftDeleteRoleTests {

    @Test
    @DisplayName("Soft Delete Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_SOFTDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleEntity existingRole = roleRepository.findById(ID).orElseThrow();

      RoleResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s", ID))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals(ID, response.getRoles().getFirst().getId());
      assertNotNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              argThat(
                  roleEntityParam ->
                      roleEntityParam.getRoleName().equals(existingRole.getRoleName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_SOFT)),
              any(String.class));

      // reset
      existingRole.setDeletedDate(null);
      roleRepository.save(existingRole);
    }

    @Test
    @DisplayName("Soft Delete Role Failure Deleted")
    void test_Failure_IsDeleted() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isForbidden()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Active Role Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Soft Delete Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Soft Delete Role Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s", ID))
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
    @DisplayName("Soft Delete Role Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_SOFTDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Role Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Hard Delete Role Tests")
  class HardDeleteRoleTests {

    @Test
    @DisplayName("Hard Delete Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      // setup
      RoleEntity roleEntity = roleRepository.save(TestData.getNewRoleEntity());

      PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
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

      RoleResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s/hard", roleEntity.getId()))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              argThat(
                  roleEntityParam ->
                      roleEntityParam.getRoleName().equals(roleEntity.getRoleName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_HARD)),
              any(String.class));

      // PPR and PRP are also deleted
      assertNull(pprRepository.findById(pprEntity.getId()).orElse(null));
      assertNull(prpRepository.findById(prpEntity.getId()).orElse(null));
    }

    @Test
    @DisplayName("Hard Delete Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s/hard", ID))
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
    @DisplayName("Hard Delete Role Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s/hard", ID))
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
    @DisplayName("Hard Delete Role Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_HARDDELETE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/roles/role/%s/hard", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Role Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("Restore Soft Deleted Role Tests")
  class RestoreRoleTests {

    @Test
    @DisplayName("Restore Role Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_RESTORE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      RoleEntity existingRole = roleRepository.findById(ID_DELETED).orElseThrow();

      RoleResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/roles/role/%s/restore", ID_DELETED))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertEquals(1, response.getRoles().size());
      assertEquals(ID_DELETED, response.getRoles().getFirst().getId());
      assertNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 0, 1),
          response.getResponseMetadata().responseCrudInfo());

      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              argThat(
                  roleEntityParam ->
                      roleEntityParam.getRoleName().equals(existingRole.getRoleName())),
              argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_RESTORE)),
              any(String.class));

      // reset
      existingRole.setDeletedDate(LocalDateTime.now());
      roleRepository.save(existingRole);
    }

    @Test
    @DisplayName("Restore Role Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/roles/role/%s/restore", ID))
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
    @DisplayName("Restore Role Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      ResponseWithMetadata response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/roles/role/%s/restore", ID))
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
    @DisplayName("Restore Role Failure With Exception")
    void test_Failure_Exception() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_RESTORE"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .patch()
              .uri(String.format("/api/v1/roles/role/%s/restore", ID_NOT_FOUND))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(RoleResponse.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertNotNull(response.getRoles());
      assertTrue(response.getRoles().isEmpty());
      assertTrue(
          response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Role Not Found for [99]", response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }
  }
}
