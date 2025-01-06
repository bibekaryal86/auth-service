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
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
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

public class PlatformProfileRoleControllerTest extends BaseTest {

  private static final long PLATFORM_ID = ID;
  private static final long PROFILE_ID = 3;
  private static final long ROLE_ID = 6;

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
  void testCreatePlatformProfileRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_PROFILE_ROLE_ASSIGN", profileDtoNoPermission);
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
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(1, platformProfileRoleResponse.getPlatformProfileRoles().size());
    assertEquals(
        PLATFORM_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getPlatform().getId());
    assertEquals(
        PROFILE_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getProfile().getId());
    assertEquals(
        ROLE_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getRole().getId());

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
  void testCreatePlatformProfileRole_SuccessSuperUser() {
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
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(1, platformProfileRoleResponse.getPlatformProfileRoles().size());
    assertEquals(
        PLATFORM_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getPlatform().getId());
    assertEquals(
        PROFILE_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getProfile().getId());
    assertEquals(
        ROLE_ID,
        platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getRole().getId());

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
  void testCreatePlatformProfileRole_FailureNoAuth() {
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
  void testCreatePlatformProfileRole_FailureNoPermission() {
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
  void testCreatePlatformProfileRole_FailureBadRequest() {
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
  void testCreatePlatformProfileRole_FailureNotFound() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(9L, 9L, 9L);

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
  void testReadPlatformProfileRoles_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_PROFILE_ROLE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/ppr")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(6, platformProfileRoleResponse.getPlatformProfileRoles().size());
  }

  @Test
  void testReadAppRolePermissions_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/ppr")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(6, platformProfileRoleResponse.getPlatformProfileRoles().size());
  }

  @Test
  void testReadAppRolePermissions_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/ppr").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppRolePermissions_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/ppr")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatformProfileRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_PROFILE_ROLE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(1, platformProfileRoleResponse.getPlatformProfileRoles().size());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getPlatform().getId());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getProfile().getId());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getRole().getId());
  }

  @Test
  void testReadPlatformProfileRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformProfileRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformProfileRoleResponse);
    assertNotNull(platformProfileRoleResponse.getPlatformProfileRoles());
    assertEquals(1, platformProfileRoleResponse.getPlatformProfileRoles().size());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getPlatform().getId());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getProfile().getId());
    assertEquals(
        ID, platformProfileRoleResponse.getPlatformProfileRoles().getFirst().getRole().getId());
  }

  @Test
  void testReadPlatformProfileRole_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadPlatformProfileRole_FailureNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", ID, ID, ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatformProfileRole_FailureNotFound() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", 9L, 9L, 9L))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testDeletePlatformProfileRole_Success() {
    // setup
    ProfileEntity profileEntity = profileRepository.findById(PROFILE_ID).orElse(null);
    RoleEntity roleEntity = roleRepository.findById(ROLE_ID).orElse(null);
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    platformProfileRoleEntity.setId(new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    platformProfileRoleRepository.save(platformProfileRoleEntity);

    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission(
            "PLATFORM_PROFILE_ROLE_UNASSIGN", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/ppr/platform/%s/profile/%s/role/%s", PLATFORM_ID, PROFILE_ID, ROLE_ID))
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
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
  }

  @Test
  void testDeletePlatformProfileRole_SuccessSuperUser() {
    // setup
    ProfileEntity profileEntity = profileRepository.findById(PROFILE_ID).orElse(null);
    RoleEntity roleEntity = roleRepository.findById(ROLE_ID).orElse(null);
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    platformProfileRoleEntity.setId(new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    platformProfileRoleRepository.save(platformProfileRoleEntity);

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformProfileRoleResponse platformProfileRoleResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/ppr/platform/%s/profile/%s/role/%s", PLATFORM_ID, PROFILE_ID, ROLE_ID))
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
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(PLATFORM_ID, PROFILE_ID, ROLE_ID));
  }

  @Test
  void testDeletePlatformProfileRole_FailureNoAuth() {
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
  void testDeletePlatformProfileRole_FailureNoPermission() {
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
  void testDeletePlatformProfileRole_FailureNotFound() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/ppr/platform/%s/profile/%s/role/%s", 9L, 9L, 9L))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
