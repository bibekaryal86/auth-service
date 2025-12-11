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

import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
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
@DisplayName("PPR Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PlatformProfileRoleControllerTest extends BaseTest {

  @Autowired PlatformProfileRoleRepository pprRepository;

  @MockitoBean private AuditService auditService;

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Nested
  @DisplayName("Assign PPR Tests")
  class AssignPlatformProfileRoleTests {
    final long SUCCESS_ID = 2L;

    @Test
    @DisplayName("Assign PPR Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.ASSIGN_PLATFORM_ROLE)),
              any(String.class));

      // reset
      pprRepository.deleteById(new PlatformProfileRoleId(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID));
    }

    @Test
    @DisplayName("Assign PPR Failure Deleted Platform")
    void test_Failure_DeletedPlatform() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(ID_DELETED, SUCCESS_ID, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
    @DisplayName("Assign PPR Failure Deleted Profile")
    void test_Failure_DeletedProfile() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(SUCCESS_ID, ID_DELETED, SUCCESS_ID);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
          "Active Profile Not Found for [9]",
          response.getResponseMetadata().responseStatusInfo().errMsg());
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Assign PPR Failure Deleted Role")
    void test_Failure_DeletedRole() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(SUCCESS_ID, SUCCESS_ID, ID_DELETED);

      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
    @DisplayName("Assign PPR Failure No Auth")
    void test_Failure_NoAuth() {
      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
    @DisplayName("Assign PPR Failure No Permission")
    void test_Failure_NoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);
      PlatformProfileRoleRequest request =
          new PlatformProfileRoleRequest(SUCCESS_ID, SUCCESS_ID, SUCCESS_ID);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
    @DisplayName("Assign PPR Failure Bad Request")
    void test_Failure_BadRequest() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_ASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleRequest request = new PlatformProfileRoleRequest(null, 0L, -1L);
      ResponseWithMetadata response =
          webTestClient
              .post()
              .uri("/api/v1/ppr")
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
                  .contains("ProfileID is required")
              && response
                  .getResponseMetadata()
                  .responseStatusInfo()
                  .errMsg()
                  .contains("RoleID is required"));
      verifyNoInteractions(auditService);
    }
  }

  @Nested
  @DisplayName("UnAssign PPR Tests")
  class UnAssignPlatformProfileRoleTests {
    @Test
    @DisplayName("UnAssign PPR Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_UNASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
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
          .auditProfile(
              any(HttpServletRequest.class),
              any(ProfileEntity.class),
              argThat(
                  eventType -> eventType.equals(AuditEnums.AuditProfile.UNASSIGN_PLATFORM_ROLE)),
              any(String.class));

      // reset
      pprRepository.save(TestData.getPlatformProfileRoleEntities().getFirst());
    }

    @Test
    @DisplayName("Assign PPR Failure Not Found")
    void test_Failure_NotFound() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_UNASSIGN"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID_NOT_FOUND))
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
          "Platform Profile Role Not Found for [1,1,99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("UnAssign PPR Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
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
    @DisplayName("UnAssign PPR Failure No Permission")
    void testUnAssign_FailureNoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
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
  @DisplayName("Delete PPR Tests")
  class DeletePlatformProfileRoleTests {
    @Test
    @DisplayName("Delete PPR Success")
    void test_Success() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      PlatformProfileRoleEntity pprEntity =
          TestData.getPlatformProfileRoleEntity(
              TestData.getPlatformEntities().get(7),
              TestData.getProfileEntities().get(7),
              TestData.getRoleEntities().get(7),
              null);
      pprRepository.save(pprEntity);

      assertNotNull(pprRepository.findById(pprEntity.getId()).orElse(null));

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format(
                      "/api/v1/ppr/platform/%s/profile/%s/role/%s/hard",
                      pprEntity.getPlatform().getId(),
                      pprEntity.getProfile().getId(),
                      pprEntity.getRole().getId()))
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

      assertNull(pprRepository.findById(pprEntity.getId()).orElse(null));

      // verify no interactions for PPR hard delete
      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Delete PPR Failure Not Found")
    void test_Failure_NotFound() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_HARDDELETE"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(
                  String.format(
                      "/api/v1/ppr/platform/%s/profile/%s/role/%s/hard", ID, ID, ID_NOT_FOUND))
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
          "Platform Profile Role Not Found for [1,1,99]",
          response.getResponseMetadata().responseStatusInfo().errMsg());

      verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Delete PPR Failure No Auth")
    void test_Failure_NoAuth() {
      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s/hard", ID, ID, ID))
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
    @DisplayName("Delete PPR Failure No Permission")
    void testUnAssign_FailureNoPermission() {
      AuthToken authToken =
          TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PPR_READ"), Boolean.FALSE);
      String bearerAuth = TestData.getBearerAuthCredentialsForTest(authToken);

      ResponseWithMetadata response =
          webTestClient
              .delete()
              .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s/hard", ID, ID, ID))
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
