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
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.dto.PlatformRolePermissionResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.PlatformRolePermissionRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class PlatformRolePermissionControllerTest extends BaseTest {

  private static final long PLATFORM_ID = ID;
  private static final long ROLE_ID = 6;
  private static final long PERMISSION_ID = 3;

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @Autowired private PlatformRolePermissionRepository platformRolePermissionRepository;
  @Autowired private PlatformRepository platformRepository;
  @Autowired private PermissionRepository permissionRepository;
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
  void testCreatePlatformRolePermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_ROLE_PERMISSION_ASSIGN", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(PLATFORM_ID, ROLE_ID, PERMISSION_ID);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/prp")
            .bodyValue(platformRolePermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(1, platformRolePermissionResponse.getPlatformRolePermissions().size());
    assertEquals(
        PLATFORM_ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPlatform()
            .getId());
    assertEquals(
        ROLE_ID,
        platformRolePermissionResponse.getPlatformRolePermissions().getFirst().getRole().getId());
    assertEquals(
        PERMISSION_ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPermission()
            .getId());

    // verify audit service called for assign platform permission
    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ASSIGN_PLATFORM_PERMISSION)),
            any(String.class));

    // cleanup
    platformRolePermissionRepository.deleteById(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
  }

  @Test
  void testCreatePlatformRolePermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(PLATFORM_ID, ROLE_ID, PERMISSION_ID);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/prp")
            .bodyValue(platformRolePermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(1, platformRolePermissionResponse.getPlatformRolePermissions().size());
    assertEquals(
        PLATFORM_ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPlatform()
            .getId());
    assertEquals(
        ROLE_ID,
        platformRolePermissionResponse.getPlatformRolePermissions().getFirst().getRole().getId());
    assertEquals(
        PERMISSION_ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPermission()
            .getId());

    // verify audit service called for assign platform permission
    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ASSIGN_PLATFORM_PERMISSION)),
            any(String.class));

    // cleanup
    platformRolePermissionRepository.deleteById(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
  }

  @Test
  void testCreatePlatformRolePermission_FailureNoAuth() {
    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(PLATFORM_ID, ROLE_ID, PERMISSION_ID);
    webTestClient
        .post()
        .uri("/api/v1/prp")
        .bodyValue(platformRolePermissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatformRolePermission_FailureNoPermission() {
    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(PLATFORM_ID, ROLE_ID, PERMISSION_ID);
    webTestClient
        .post()
        .uri("/api/v1/prp")
        .bodyValue(platformRolePermissionRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatformRolePermission_FailureBadRequest() {
    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(null, 0L, -1L);
    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/prp")
            .bodyValue(platformRolePermissionRequest)
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
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("RoleID is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("PermissionID is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPlatformRolePermissions_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_ROLE_PERMISSION_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/prp")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(6, platformRolePermissionResponse.getPlatformRolePermissions().size());
  }

  @Test
  void testReadAppRolePermissions_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/prp")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(6, platformRolePermissionResponse.getPlatformRolePermissions().size());
  }

  @Test
  void testReadAppRolePermissions_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/prp").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppRolePermissions_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/prp")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatformRolePermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_ROLE_PERMISSION_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(1, platformRolePermissionResponse.getPlatformRolePermissions().size());
    assertEquals(
        ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPlatform()
            .getId());
    assertEquals(
        ID,
        platformRolePermissionResponse.getPlatformRolePermissions().getFirst().getRole().getId());
    assertEquals(
        ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPermission()
            .getId());
  }

  @Test
  void testReadPlatformRolePermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getPlatformRolePermissions());
    assertEquals(1, platformRolePermissionResponse.getPlatformRolePermissions().size());
    assertEquals(
        ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPlatform()
            .getId());
    assertEquals(
        ID,
        platformRolePermissionResponse.getPlatformRolePermissions().getFirst().getRole().getId());
    assertEquals(
        ID,
        platformRolePermissionResponse
            .getPlatformRolePermissions()
            .getFirst()
            .getPermission()
            .getId());
  }

  @Test
  void testReadPlatformRolePermission_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadPlatformRolePermission_FailureNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/prp/platform/%s/role/%s/permission/%s", ID, ID, ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testDeletePlatformRolePermission_Success() {
    // setup
    RoleEntity roleEntity = roleRepository.findById(ROLE_ID).orElse(null);
    PermissionEntity permissionEntity = permissionRepository.findById(PERMISSION_ID).orElse(null);
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    platformRolePermissionEntity.setId(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    platformRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    platformRolePermissionRepository.save(platformRolePermissionEntity);

    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_ROLE_PERMISSION_UNASSIGN", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/prp/platform/%s/role/%s/permission/%s",
                    PLATFORM_ID, ROLE_ID, PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getResponseMetadata());
    assertEquals(
        1,
        platformRolePermissionResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());

    // verify audit service called for unassign platform permission success
    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(
                eventType -> eventType.equals(AuditEnums.AuditRole.UNASSIGN_PLATFORM_PERMISSION)),
            any(String.class));

    // cleanup
    platformRolePermissionRepository.deleteById(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
  }

  @Test
  void testDeletePlatformRolePermission_SuccessSuperUser() {
    // setup
    RoleEntity roleEntity = roleRepository.findById(ROLE_ID).orElse(null);
    PermissionEntity permissionEntity = permissionRepository.findById(PERMISSION_ID).orElse(null);
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    platformRolePermissionEntity.setId(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    platformRolePermissionRepository.save(platformRolePermissionEntity);

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformRolePermissionResponse platformRolePermissionResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/prp/platform/%s/role/%s/permission/%s",
                    PLATFORM_ID, ROLE_ID, PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformRolePermissionResponse);
    assertNotNull(platformRolePermissionResponse.getResponseMetadata());
    assertEquals(
        1,
        platformRolePermissionResponse
            .getResponseMetadata()
            .getResponseCrudInfo()
            .getDeletedRowsCount());

    // verify audit service called for unassign platform permission success
    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(
                eventType -> eventType.equals(AuditEnums.AuditRole.UNASSIGN_PLATFORM_PERMISSION)),
            any(String.class));

    // cleanup
    platformRolePermissionRepository.deleteById(
        new PlatformRolePermissionId(PLATFORM_ID, ROLE_ID, PERMISSION_ID));
  }

  @Test
  void testDeletePlatformRolePermission_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/prp/platform/%s/role/%s/permission/%s",
                PLATFORM_ID, ROLE_ID, PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testDeletePlatformRolePermission_FailureNoPermission() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/prp/platform/%s/role/%s/permission/%s",
                PLATFORM_ID, ROLE_ID, PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}
