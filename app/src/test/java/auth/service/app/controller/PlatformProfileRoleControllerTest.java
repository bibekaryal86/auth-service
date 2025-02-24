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
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
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
import jakarta.servlet.http.HttpServletRequest;
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
        TestData.getProfileDtoWithPermission(
            "AUTHSVC_PLATFORM_PROFILE_ROLE_ASSIGN", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(PLATFORM_ID, PROFILE_ID, ROLE_ID);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getResponseMetadata());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponsePageInfo());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getInsertedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getUpdatedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getRestoredRowsCount());

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

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getResponseMetadata());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponsePageInfo());
    assertNotNull(platformProfileRoleResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getInsertedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getUpdatedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());
    assertEquals(
        0,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getRestoredRowsCount());

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
    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/ppr")
            .bodyValue(platformProfileRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("PlatformID is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("ProfileID is required")
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("RoleID is required"));
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
        TestData.getProfileDtoWithPermission(
            "AUTHSVC_PLATFORM_PROFILE_ROLE_UNASSIGN", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/ppr//platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getResponseMetadata());
    assertEquals(
        1,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());

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

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getResponseMetadata());
    assertEquals(
        1,
        platformProfileRoleResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());

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
