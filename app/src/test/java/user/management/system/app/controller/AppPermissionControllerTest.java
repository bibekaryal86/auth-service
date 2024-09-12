package user.management.system.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppPermissionDto;
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.repository.AppPermissionRepository;
import user.management.system.app.service.AuditService;

public class AppPermissionControllerTest extends BaseTest {

  private static final int APP_PERMISSION_ID = 1;

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @MockBean private AuditService auditService;

  @Autowired private AppPermissionRepository appPermissionRepository;

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
  void testCreateAppPermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_CREATE");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission Create", "Permission Create Test");

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/app_permissions/%s/permission", APP_ID))
            .bodyValue(appPermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    verify(auditService, times(1)).auditAppPermissionCreate(any(), any(), any());

    // cleanup
    appPermissionRepository.deleteById(appPermissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testCreateAppPermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission Create", "Permission Create Test");

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .post()
            .uri(String.format("/api/v1/app_permissions/%s/permission", APP_ID))
            .bodyValue(appPermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    verify(auditService, times(1)).auditAppPermissionCreate(any(), any(), any());

    // cleanup
    appPermissionRepository.deleteById(appPermissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testCreateAppPermission_FailureWithNoBearerAuth() {
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission Create", "Permission Create Test");

    webTestClient
        .post()
        .uri(String.format("/api/v1/app_permissions/%s/permission", APP_ID))
        .bodyValue(appPermissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppPermission_FailureWithAuthButNoPermission() {
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission Create", "Permission Create Test");
    webTestClient
        .post()
        .uri(String.format("/api/v1/app_permissions/%s/permission", APP_ID))
        .bodyValue(appPermissionRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadAppPermissions_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_READ");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_permissions")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(6, appPermissionResponse.getPermissions().size());
  }

  @Test
  void testReadAppPermissions_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_permissions")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(6, appPermissionResponse.getPermissions().size());
  }

  @Test
  void testReadAppPermissions_FailureWithNoBearerAuth() {
    webTestClient.get().uri("/api/v1/app_permissions").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppPermissions_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/app_permissions")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAppPermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_READ");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    assertEquals(APP_PERMISSION_ID, appPermissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testReadAppPermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    assertEquals(APP_PERMISSION_ID, appPermissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testReadAppPermission_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppPermission_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAppPermissionsByAppId_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_READ");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    String appId = "app-99";
    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_permissions/app/" + appId)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(3, appPermissionResponse.getPermissions().size());

    for (AppPermissionDto appPermissionDto : appPermissionResponse.getPermissions()) {
      assertEquals(appId, appPermissionDto.getAppId());
    }
  }

  @Test
  void testReadAppPermissionsByAppId_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    String appId = "app-99";
    AppPermissionResponse appPermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_permissions/app/" + appId)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(3, appPermissionResponse.getPermissions().size());

    for (AppPermissionDto appPermissionDto : appPermissionResponse.getPermissions()) {
      assertEquals(appId, appPermissionDto.getAppId());
    }
  }

  @Test
  void testReadAppPermissionsByAppId_FailureWithNoBearerAuth() {
    String appId = "app-99";
    webTestClient
        .get()
        .uri("/api/v1/app_permissions/app/" + appId)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppPermissionsByAppId_FailureWithAuthButNoPermission() {
    String appId = "app-99";
    webTestClient
        .get()
        .uri("/api/v1/app_permissions/app/" + appId)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateAppPermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_CREATE");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission One", "Permission One Updated 1");

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .bodyValue(appPermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    verify(auditService, times(1)).auditAppPermissionUpdate(any(), any());
  }

  @Test
  void testUpdateAppPermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission One", "Permission One Updated 2");

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .bodyValue(appPermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());
    verify(auditService, times(1)).auditAppPermissionUpdate(any(), any());
  }

  @Test
  void testUpdateAppPermission_FailureWithNoBearerAuth() {
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission One", "Permission One Updated");
    webTestClient
        .put()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .bodyValue(appPermissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppPermission_FailureWithAuthButNoPermission() {
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("Permission One", "Permission One Updated");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .bodyValue(appPermissionRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteAppPermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_DELETE");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getResponseCrudInfo());
    assertEquals(1, appPermissionResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1)).auditAppPermissionDeleteSoft(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_PERMISSION_ID, idCaptor.getValue());
  }

  @Test
  void testSoftDeleteAppPermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getResponseCrudInfo());
    assertEquals(1, appPermissionResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1)).auditAppPermissionDeleteSoft(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_PERMISSION_ID, idCaptor.getValue());
  }

  @Test
  void testSoftDeleteAppPermission_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteAppPermission_FailureWithAuthButNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_permissions/permission/%s", APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteAppPermission_Success_WithSuperUser() {
    // setup
    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setAppId("app-99");
    appPermissionEntity.setName("Permission Delete");
    appPermissionEntity.setDescription("Permission Delete Test");
    int appPermissionId = appPermissionRepository.save(appPermissionEntity).getId();

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/app_permissions/permission/%s/hard", appPermissionId))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getResponseCrudInfo());
    assertEquals(1, appPermissionResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1)).auditAppPermissionDeleteHard(requestCaptor.capture(), idCaptor.capture());
    assertEquals(appPermissionId, idCaptor.getValue());
  }

  @Test
  void testHardDeleteAppPermission_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_permissions/permission/%s/hard", APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteAppPermission_FailureWithAuthNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_DELETE");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_permissions/permission/%s/hard", APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreAppPermission_Success_WithSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppPermissionResponse appPermissionResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/app_permissions/permission/%s/restore", APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppPermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appPermissionResponse);
    assertNotNull(appPermissionResponse.getPermissions());
    assertEquals(1, appPermissionResponse.getPermissions().size());

    ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1)).auditAppPermissionRestore(requestCaptor.capture(), idCaptor.capture());
    assertEquals(APP_PERMISSION_ID, idCaptor.getValue());
  }

  @Test
  void testRestoreAppPermission_FailureWithNoBearerAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/app_permissions/permission/%s/restore", APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreAppPermission_FailureWithAuthNoSuperUser() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, appUserDtoNoPermission, "PERMISSION_DELETE");
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/app_permissions/permission/%s/restore", APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
