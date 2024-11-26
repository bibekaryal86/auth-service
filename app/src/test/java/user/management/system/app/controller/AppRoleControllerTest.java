package user.management.system.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.repository.AppRoleRepository;
import user.management.system.app.service.AuditService;

public class AppRoleControllerTest extends BaseTest {

  private static final int APP_ROLE_ID = 1;

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @MockitoBean private AuditService auditService;

  @Autowired private AppRoleRepository appRoleRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    appUserDtoNoPermission = TestData.getAppUserDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoNoPermission);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreateAppRole_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_CREATE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);
    AppRoleRequest appRoleRequest = new AppRoleRequest("Role Create", "Role Create Test");

    AppRoleResponse appRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_roles/role")
            .bodyValue(appRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    verify(auditService, after(100).times(1)).auditAppRoleCreate(any(), any());

    // cleanup
    appRoleRepository.deleteById(appRoleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateAppRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleRequest appRoleRequest = new AppRoleRequest("Role Create", "Role Create Test");

    AppRoleResponse appRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_roles/role")
            .bodyValue(appRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    verify(auditService, after(100).times(1)).auditAppRoleCreate(any(), any());

    // cleanup
    appRoleRepository.deleteById(appRoleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateAppRole_FailureWithNoBearerAuth() {
    AppRoleRequest appRoleRequest = new AppRoleRequest("Role Create", "Role Create Test");

    webTestClient
        .post()
        .uri("/api/v1/app_roles/role")
        .bodyValue(appRoleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppRole_FailureWithAuthButNoPermission() {
    AppRoleRequest appRoleRequest = new AppRoleRequest("Role Create", "Role Create Test");
    webTestClient
        .post()
        .uri("/api/v1/app_roles/role")
        .bodyValue(appRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppRole_Failure_BadRequest() {
    AppRoleRequest appRoleRequest = new AppRoleRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri("/api/v1/app_roles/role")
            .bodyValue(appRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseStatusInfo.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseStatusInfo);
    assertNotNull(responseStatusInfo.getErrMsg());
    assertTrue(
        responseStatusInfo.getErrMsg().contains("Name is required")
            && responseStatusInfo.getErrMsg().contains("Description is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadAppRoles_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_READ", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_roles")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(7, appRoleResponse.getRoles().size());
  }

  @Test
  void testReadAppRoles_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_roles")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(7, appRoleResponse.getRoles().size());
  }

  @Test
  void testReadAppRoles_FailureWithNoBearerAuth() {
    webTestClient.get().uri("/api/v1/app_roles").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppRoles_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/app_roles")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAppRole_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_READ", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    assertEquals(APP_ROLE_ID, appRoleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testReadAppRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    assertEquals(APP_ROLE_ID, appRoleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testReadAppRole_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppRole_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateAppRole_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_UPDATE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleRequest appRoleRequest = new AppRoleRequest("Role One", "Role One Updated 1");

    AppRoleResponse appRoleResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .bodyValue(appRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    verify(auditService, after(100).times(1)).auditAppRoleUpdate(any(), any());
  }

  @Test
  void testUpdateAppRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleRequest appRoleRequest = new AppRoleRequest("Role One", "Role One Updated 2");

    AppRoleResponse appRoleResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .bodyValue(appRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());
    verify(auditService, after(100).times(1)).auditAppRoleUpdate(any(), any());
  }

  @Test
  void testUpdateAppRole_FailureWithNoBearerAuth() {
    AppRoleRequest appRoleRequest = new AppRoleRequest("Role One", "Role One Updated");
    webTestClient
        .put()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .bodyValue(appRoleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppRole_FailureWithAuthButNoPermission() {
    AppRoleRequest appRoleRequest = new AppRoleRequest("Role One", "Role One Updated");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .bodyValue(appRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteAppRole_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_DELETE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getResponseCrudInfo());
    assertEquals(1, appRoleResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleDeleteSoft(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_ROLE_ID, idCaptor.getValue());
  }

  @Test
  void testSoftDeleteAppRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getResponseCrudInfo());
    assertEquals(1, appRoleResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleDeleteSoft(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_ROLE_ID, idCaptor.getValue());
  }

  @Test
  void testSoftDeleteAppRole_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteAppRole_FailureWithAuthButNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_roles/role/%s", APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteAppRole_Success_WithSuperUser() {
    // setup
    AppRoleEntity appRoleEntity = appRoleRepository.save(TestData.getNewAppRoleEntity());

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_roles/role/%s/hard", appRoleEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getResponseCrudInfo());
    assertEquals(1, appRoleResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleDeleteHard(requestCaptor.capture(), idCaptor.capture());
    assertEquals(appRoleEntity.getId(), idCaptor.getValue());
  }

  @Test
  void testHardDeleteAppRole_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_roles/role/%s/hard", APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteAppRole_FailureWithAuthNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_DELETE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_roles/role/%s/hard", APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreAppRole_Success_WithSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppRoleResponse appRoleResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/app_roles/role/%s/restore", APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRoleResponse);
    assertNotNull(appRoleResponse.getRoles());
    assertEquals(1, appRoleResponse.getRoles().size());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleRestore(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_ROLE_ID, idCaptor.getValue());
  }

  @Test
  void testRestoreAppRole_FailureWithNoBearerAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/app_roles/role/%s/restore", APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreAppRole_FailureWithAuthNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "ROLE_DELETE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/app_roles/role/%s/restore", APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
