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
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.RoleRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
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
    void testCreateRole_Success() {
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
    void testCreateRole_Failure_NoAuth() {
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
    void testCreateRole_Failure_NoPermission() {
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
    void testCreateRole_Failure_BadRequest() {
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
    void testCreateRole_Failure_Exception() {
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
    void testReadRoles_Success() {
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
    void testReadRoles_Success_SuperUser() {
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
    void testReadRoles_Failure_NoPermissions() {
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
    void testReadRoles_Failure_NoAuth() {
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
  }

  @Nested
  @DisplayName("Read Role Tests")
  class ReadRoleTests {

    @Test
    @DisplayName("Read Role Success")
    void testReadRole_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri("/api/v1/roles/role/1")
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
      assertEquals(1L, response.getRoles().getFirst().getId());
      assertNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Role Superuser Include Deleted")
    void testReadRole_Success_SuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.TRUE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri("/api/v1/roles/role/9?isIncludeDeleted=true&isIncludeHistory=true")
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
      assertEquals(9L, response.getRoles().getFirst().getId());
      assertNotNull(response.getRoles().getFirst().getDeletedDate());
      assertNotNull(response.getResponseMetadata());
      assertEquals(ResponseMetadata.emptyResponseMetadata(), response.getResponseMetadata());

      verify(auditService).auditRoles(9L);
    }

    @Test
    @DisplayName("Read Roles Failure Include Deleted Not SuperUser")
    void testReadRole_Failure_IncludeDeleteNotSuperUser() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      RoleResponse response =
          webTestClient
              .get()
              .uri("/api/v1/roles/role/9?isIncludeDeleted=true")
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
          "Active Role Not Found for [9]", response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Read Role Failure No Permissions")
    void testReadRole_Failure_NoPermissions() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/roles/role/1")
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
    void testReadRoles_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .get()
              .uri("/api/v1/roles/role/1")
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
  }
}
