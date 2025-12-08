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

import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformRolePermissionRepository;
import auth.service.app.service.AuditService;
import auth.service.app.util.CommonUtils;
import helper.TestData;
import integration.BaseTest;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("PRP Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PlatformRolePermissionControllerTest extends BaseTest {

  @Autowired PlatformRolePermissionRepository prpRepository;

  @MockitoBean private AuditService auditService;

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Nested
  @DisplayName("Assign PRP Tests")
  class AssignPlatformRolePermissionTests {
    final long SUCCESS_ID = 2L;

    @Test
    @DisplayName("Assign PRP Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseCrudInfo() != null);
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
          response.getResponseMetadata().responseCrudInfo());

      // verify audit service called for assign platform role
      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              any(RoleEntity.class),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditRole.ASSIGN_PLATFORM_PERMISSION)),
              any(String.class));

      // reset
      prpRepository.deleteById(new PlatformRolePermissionId(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID));
    }

    @Test
    @DisplayName("Assign PRP Failure Deleted Platform")
    void test_Failure_DeletedPlatform() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(ID_DELETED, SUCCESS_ID, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
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
          "Active Platform Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Assign PRP Failure Deleted Role")
    void test_Failure_DeletedRole() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(SUCCESS_ID, ID_DELETED, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
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
          "Active Role Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Assign PRP Failure Deleted Permission")
    void test_Failure_DeletedPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(SUCCESS_ID, SUCCESS_ID, ID_DELETED);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
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
          "Active Permission Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Assign PRP Failure No Auth")
    void test_Failure_NoAuth() {
      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
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
    @DisplayName("Assign PRP Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformRolePermissionRequest request =
          new PlatformRolePermissionRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
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
    @DisplayName("Assign PRP Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionRequest request = new PlatformRolePermissionRequest(null, 0L, -1L);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/prp")
              .bodyValue(request)
              .header("Authorization", "Bearer " + bearerAuth)
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
                  .contains("PlatformID is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("RoleID is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("PermissionID is required"));
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("UnAssign PRP Tests")
  class UnAssignPlatformRolePermissionTests {
    @Test
    @DisplayName("UnAssign PRP Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_UNASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
              .header("Authorization", "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseCrudInfo() != null);
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      // verify audit service called for assign platform role
      verify(auditService, after(100).times(1))
          .auditRole(
              any(HttpServletRequest.class),
              any(RoleEntity.class),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditRole.UNASSIGN_PLATFORM_PERMISSION)),
              any(String.class));

      // reset
      prpRepository.save(TestData.getPlatformRolePermissionEntities().getFirst());
    }

    @Test
    @DisplayName("Assign PRP Failure Not Found")
    void test_Failure_NotFound() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_UNASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format(
                      "/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID_NOT_FOUND))
              .header("Authorization", "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Role Permission Not Found for [1,1,99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("UnAssign PRP Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
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
    @DisplayName("UnAssign PRP Failure No Permission")
    void testUnAssign_FailureNoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
              .header("Authorization", "Bearer " + bearerAuth)
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
  }

  @Nested
  @DisplayName("Delete PRP Tests")
  class DeletePlatformRolePermissionTests {
    @Test
    @DisplayName("Delete PRP Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformRolePermissionEntity prpEntity =
          TestData.getPlatformRolePermissionEntity(
              TestData.getPlatformEntities().get(7),
              TestData.getRoleEntities().get(7),
              TestData.getPermissionEntities().get(7),
              null);
      prpRepository.save(prpEntity);

      assertNotNull(prpRepository.findById(prpEntity.getId()).orElse(null));

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format(
                      "/api/v1/prp/platform/%s/role/%s/permission/%s/hard",
                      prpEntity.getPlatform().getId(),
                      prpEntity.getRole().getId(),
                      prpEntity.getPermission().getId()))
              .header("Authorization", "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseCrudInfo() != null);
      assertEquals(
          CommonUtils.defaultResponseCrudInfo(0, 0, 1, 0),
          response.getResponseMetadata().responseCrudInfo());

      assertNull(prpRepository.findById(prpEntity.getId()).orElse(null));

      // verify no interactions for PRP hard delete
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Delete PRP Failure Not Found")
    void test_Failure_NotFound() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format(
                      "/api/v1/prp/platform/%s/role/%s/permission/%s/hard", ID, ID, ID_NOT_FOUND))
              .header("Authorization", "Bearer " + bearerAuth)
              .exchange()
              .expectStatus()
              .isNotFound()
              .expectBody(ResponseWithMetadata.class)
              .returnResult()
              .getResponseBody();

      assertTrue(
          response != null
              && response.getResponseMetadata() != null
              && response.getResponseMetadata().responseStatusInfo() != null
              && response.getResponseMetadata().responseStatusInfo().errMsg() != null);
      assertEquals(
          "Platform Role Permission Not Found for [1,1,99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Delete PRP Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s/hard", ID, ID, ID))
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
    @DisplayName("Delete PRP Failure No Permission")
    void testUnAssign_FailureNoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PRP_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s/hard", ID, ID, ID))
              .header("Authorization", "Bearer " + bearerAuth)
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
  }
}
