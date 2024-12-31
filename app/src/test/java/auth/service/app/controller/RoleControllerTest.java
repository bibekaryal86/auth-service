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
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.RoleRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class RoleControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoRole;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  private static RoleRequest roleRequest;

  @Autowired private RoleRepository roleRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoRole = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoRole);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreateRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ROLE_CREATE", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_CREATE)),
            any(String.class));

    // cleanup
    roleRepository.deleteById(roleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_CREATE)),
            any(String.class));

    // cleanup
    roleRepository.deleteById(roleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateRole_FailureNoAuth() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/roles/role")
        .bodyValue(roleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateRole_FailureNoPermission() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/roles/role")
        .bodyValue(roleRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateRole_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo());
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Name is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("Description is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
    roleRequest = new RoleRequest(roleEntity.getRoleName(), roleEntity.getRoleDesc());

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(roleResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(roleResponse.getRoles().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadRoles_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithPermission("ROLE_READ", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(6, roleResponse.getRoles().size());
  }

  @Test
  void testReadRoles_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(6, roleResponse.getRoles().size());
  }

  @Test
  void testReadRoles_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/roles").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadRoles_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/roles")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadRole_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithPermission("ROLE_READ", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles/role/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
  }

  @Test
  void testReadRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles/role/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
  }

  @Test
  void testReadRole_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/roles/role/1").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadRole_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/roles/role/1")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/roles/role/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdateRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ROLE_UPDATE", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleEntity roleEntityOriginal =
        roleRepository.findById(1L).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri("/api/v1/roles/role/1")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_UPDATE)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testUpdateRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleEntity roleEntityOriginal =
        roleRepository.findById(1L).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri("/api/v1/roles/role/1")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_UPDATE)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testUpdateRole_FailureNoAuth() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .put()
        .uri("/api/v1/roles/role/1")
        .bodyValue(roleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateRole_FailureNoPermission() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .put()
        .uri("/api/v1/roles/role/1")
        .bodyValue(roleRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateRole_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri("/api/v1/roles/role/1")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseMetadata);
    assertNotNull(responseMetadata.getResponseStatusInfo());
    assertNotNull(responseMetadata.getResponseStatusInfo().getErrMsg());
    assertTrue(
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Name is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("Description is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("UPDATED_NAME", "UPDATED_DESC");

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri("/api/v1/roles/role/9999")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(roleResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(roleResponse.getRoles().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ROLE_DELETE", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_SOFT)),
            any(String.class));
  }

  @Test
  void testSoftDeleteRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_SOFT)),
            any(String.class));
  }

  @Test
  void testSoftDeleteRole_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/roles/role/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_Success() {
    // setup
    RoleEntity roleEntity = roleRepository.save(TestData.getNewRoleEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s/hard", roleEntity.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_HARD)),
            any(String.class));
  }

  @Test
  void testHardDeleteRole_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ROLE_DELETE", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s/hard", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/roles/role/9999/hard")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/roles/role/%s/restore", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam ->
                    roleEntityParam
                        .getRoleName()
                        .equals(roleResponse.getRoles().getFirst().getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_RESTORE)),
            any(String.class));
  }

  @Test
  void testRestoreRole_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/roles/role/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ROLE_RESTORE", profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/roles/role/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri("/api/v1/roles/role/9999/restore")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }
}
