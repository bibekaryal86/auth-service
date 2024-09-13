package user.management.system.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import helper.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.dto.AppsResponse;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.repository.AppsRepository;
import user.management.system.app.service.AuditService;

public class AppsControllerTest extends BaseTest {

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;

  @MockBean private AuditService auditService;

  @Autowired private AppsRepository appsRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    appUserDtoNoPermission = TestData.getAppUserDto();
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreateApp_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsRequest appsRequest = new AppsRequest("App Test", "App Test Create");

    AppsResponse appsResponse =
        webTestClient
            .post()
            .uri("/api/v1/apps/app")
            .bodyValue(appsRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getApps());
    assertEquals(1, appsResponse.getApps().size());
    verify(auditService, times(1)).auditAppsCreate(any(), any());

    // cleanup
    appsRepository.deleteById(appsResponse.getApps().getFirst().getId());
  }

  @Test
  void testCreateApp_FailureWithNoBearerAuth() {
    AppsRequest appsRequest = new AppsRequest("App Test", "App Test Create");
    webTestClient
        .post()
        .uri("/api/v1/apps/app")
        .bodyValue(appsRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "APP_CREATE", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsRequest appsRequest = new AppsRequest("App Test", "App Test Create");
    webTestClient
        .post()
        .uri("/api/v1/apps/app")
        .bodyValue(appsRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadApps_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsResponse appsResponse =
        webTestClient
            .get()
            .uri("/api/v1/apps")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getApps());
    assertEquals(4, appsResponse.getApps().size());
  }

  @Test
  void testReadApps_FailureWithNoBearerAuth() {
    webTestClient.get().uri("/api/v1/apps").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadApps_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "APP_READ", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/apps")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadApp_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsResponse appsResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/apps/app/%s", APP_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getApps());
    assertEquals(1, appsResponse.getApps().size());
  }

  @Test
  void testReadApp_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "READ_APP", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .get()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateApp_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsRequest appsRequest = new AppsRequest("App One", "App Description One Update Test");

    AppsResponse appsResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/apps/app/%s", APP_ID))
            .bodyValue(appsRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getApps());
    assertEquals(1, appsResponse.getApps().size());
    verify(auditService, times(1)).auditAppsUpdate(any(), any());
  }

  @Test
  void testUpdateApp_FailureWithNoBearerAuth() {
    AppsRequest appsRequest = new AppsRequest("App One", "App Description One Update Test");
    webTestClient
        .put()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .bodyValue(appsRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "READ_APP", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsRequest appsRequest = new AppsRequest("App One", "App Description One Update Test");
    webTestClient
        .put()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .bodyValue(appsRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteApp_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsResponse appsResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/apps/app/%s", APP_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getResponseCrudInfo());
    assertEquals(1, appsResponse.getResponseCrudInfo().getDeletedRowsCount());
    verify(auditService, times(1)).auditAppsDeleteSoft(any(), any());
  }

  @Test
  void testSoftDeleteApp_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "DELETE_APP", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/apps/app/%s", APP_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteApp_Success() {
    // setup
    AppsEntity appsEntity = new AppsEntity();
    appsEntity.setId("app-1001");
    appsEntity.setName("App Test");
    appsEntity.setDescription("App Test Delete");
    appsEntity = appsRepository.save(appsEntity);

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsResponse appsResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/apps/app/%s/hard", appsEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getResponseCrudInfo());
    assertEquals(1, appsResponse.getResponseCrudInfo().getDeletedRowsCount());
    verify(auditService, times(1)).auditAppsDeleteHard(any(), any());
  }

  @Test
  void testHardDeleteApp_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/apps/app/%s/hard", APP_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "DELETE_APP", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/apps/app/%s/hard", APP_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreApp_Success() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppsResponse appsResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/apps/app/%s/restore", APP_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppsResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appsResponse);
    assertNotNull(appsResponse.getApps());
    assertEquals(1, appsResponse.getApps().size());
    verify(auditService, times(1)).auditAppsRestore(any(), any());
  }

  @Test
  void testRestoreApp_FailureWithNoBearerAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/apps/app/%s/restore", APP_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreApp_FailureWithAuthButNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "RESTORE_APP", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/apps/app/%s/restore", APP_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
