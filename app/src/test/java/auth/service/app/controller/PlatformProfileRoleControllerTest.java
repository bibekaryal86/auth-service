package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import auth.service.BaseTest;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class PlatformProfileRoleControllerTest extends BaseTest {

  private static final long PLATFORM_ID = ID;
  private static final long PROFILE_ID = 5L;
  private static final long ROLE_ID = 7L;

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;
  @Autowired private PlatformRepository platformRepository;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private RoleRepository roleRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testAssignPlatformProfileRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_PROFILE_ROLE_ASSIGN"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(PLATFORM_ID, PROFILE_ID, ROLE_ID);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responsePageInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseCrudInfo());
    assertEquals(
        1, responseWithMetadata.getResponseMetadata().responseCrudInfo().insertedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().updatedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().deletedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().restoredRowsCount());

    // verify audit service called for assign platform role
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.ASSIGN_PLATFORM_ROLE)),
            any(String.class));

    // cleanup
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
  }

  @Test
  void testAssignPlatformProfileRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(PLATFORM_ID, PROFILE_ID, ROLE_ID);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responsePageInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseCrudInfo());
    assertEquals(
        1, responseWithMetadata.getResponseMetadata().responseCrudInfo().insertedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().updatedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().deletedRowsCount());
    assertEquals(
        0, responseWithMetadata.getResponseMetadata().responseCrudInfo().restoredRowsCount());

    // verify audit service called for assign platform role
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.ASSIGN_PLATFORM_ROLE)),
            any(String.class));

    // cleanup
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
  }

  @Test
  void testAssignPlatformProfileRole_FailureNoAuth() {
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(PLATFORM_ID, PROFILE_ID, ROLE_ID);
    webTestClient
        .post()
        .uri("/api/v1/ppr")
        .bodyValue(platformProfileRoleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testAssignPlatformProfileRole_FailureNoPermission() {
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(PLATFORM_ID, PROFILE_ID, ROLE_ID);
    webTestClient
        .post()
        .uri("/api/v1/ppr")
        .bodyValue(platformProfileRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testAssignPlatformProfileRole_FailureBadRequest() {
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(null, 0L, -1L);
    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("PlatformID is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("ProfileID is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("RoleID is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testAssignPlatformProfileRole_FailureNotFound() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(ID_NOT_FOUND, ID_NOT_FOUND, ID_NOT_FOUND);

    webTestClient
        .post()
        .uri("/api/v1/ppr")
        .bodyValue(platformProfileRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();

    verifyNoInteractions(auditService);
  }

  @Test
  void testAssignPlatformProfileRole_FailureNotActive() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(ID_DELETED, ID_DELETED, ID_DELETED);

    webTestClient
        .post()
        .uri("/api/v1/ppr")
        .bodyValue(platformProfileRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUnassignPlatformProfileRole_Success() {
    // setup
    PlatformProfileRoleEntity pprOriginal =
        platformProfileRoleRepository.findById(new PlatformProfileRoleId(ID, ID, ID)).orElse(null);
    assertNotNull(pprOriginal);

    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_PROFILE_ROLE_UNASSIGN"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/ppr//platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertEquals(
        1, responseWithMetadata.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    // verify audit service called for unassign platform role success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.UNASSIGN_PLATFORM_ROLE)),
            any(String.class));

    // cleanup
    platformProfileRoleRepository.save(pprOriginal);
  }

  @Test
  void testUnassignPlatformProfileRole_SuccessSuperUser() {
    // setup
    PlatformProfileRoleEntity pprOriginal =
        platformProfileRoleRepository.findById(new PlatformProfileRoleId(ID, ID, ID)).orElse(null);
    assertNotNull(pprOriginal);

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertEquals(
        1, responseWithMetadata.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    // verify audit service called for unassign platform role success
    verify(auditService, after(100).times(1))
        .auditProfile(
            any(HttpServletRequest.class),
            any(ProfileEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.UNASSIGN_PLATFORM_ROLE)),
            any(String.class));

    // cleanup
    platformProfileRoleRepository.save(pprOriginal);
  }

  @Test
  void testUnassignPlatformProfileRole_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/ppr/platform/%s/profile/%s/role/%s", PLATFORM_ID, PROFILE_ID, ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testUnassignPlatformProfileRole_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/ppr/platform/%s/profile/%s/role/%s", PLATFORM_ID, PROFILE_ID, ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUnassignPlatformProfileRole_FailureNotFound() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/ppr/platform/%s/profile/%s/role/%s",
                ID_NOT_FOUND, ID_NOT_FOUND, ID_NOT_FOUND))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
