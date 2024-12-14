package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import auth.service.BaseTest;
import auth.service.app.model.dto.AppRolePermissionRequest;
import auth.service.app.model.dto.AppRolePermissionResponse;
import auth.service.app.model.dto.AppUserDto;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppRolePermissionId;
import auth.service.app.repository.AppPermissionRepository;
import auth.service.app.repository.AppRolePermissionRepository;
import auth.service.app.repository.AppRoleRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class PlatformRolePermissionControllerTest extends BaseTest {

  private static final int APP_ROLE_ID = 1;
  private static final int APP_PERMISSION_ID = 1;

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @MockitoBean private AuditService auditService;

  @Autowired private AppRolePermissionRepository appRolePermissionRepository;
  @Autowired private AppRoleRepository appRoleRepository;
  @Autowired private AppPermissionRepository appPermissionRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    appUserDtoNoPermission = TestData.getAppUserDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoNoPermission);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreateAppRolePermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(
                PLATFORM_ID, "ROLE_PERMISSION_ASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    int appRoleId = 6;
    int appPermissionId = 6;

    AppRolePermissionRequest appRolePermissionRequest =
        new AppRolePermissionRequest(appRoleId, appPermissionId);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_roles_permissions/role_permission")
            .bodyValue(appRolePermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(1, appRolePermissionResponse.getRolesPermissions().size());
    assertEquals(
        appRoleId, appRolePermissionResponse.getRolesPermissions().getFirst().getRole().getId());
    assertEquals(
        appPermissionId,
        appRolePermissionResponse.getRolesPermissions().getFirst().getPermission().getId());
    verify(auditService, after(100).times(1)).auditAppRoleAssignPermission(any(), any());

    // cleanup
    appRolePermissionRepository.deleteById(new AppRolePermissionId(appRoleId, appPermissionId));
  }

  @Test
  void testCreateAppRolePermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    int appRoleId = 6;
    int appPermissionId = 6;

    AppRolePermissionRequest appRolePermissionRequest =
        new AppRolePermissionRequest(appRoleId, appPermissionId);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_roles_permissions/role_permission")
            .bodyValue(appRolePermissionRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(1, appRolePermissionResponse.getRolesPermissions().size());
    assertEquals(
        appRoleId, appRolePermissionResponse.getRolesPermissions().getFirst().getRole().getId());
    assertEquals(
        appPermissionId,
        appRolePermissionResponse.getRolesPermissions().getFirst().getPermission().getId());
    verify(auditService, after(100).times(1)).auditAppRoleAssignPermission(any(), any());

    // cleanup
    appRolePermissionRepository.deleteById(new AppRolePermissionId(appRoleId, appPermissionId));
  }

  @Test
  void testCreateAppRolePermission_FailureWithNoBearerAuth() {
    AppRolePermissionRequest appRolePermissionRequest =
        new AppRolePermissionRequest(APP_ROLE_ID, APP_PERMISSION_ID);
    webTestClient
        .post()
        .uri("/api/v1/app_roles_permissions/role_permission")
        .bodyValue(appRolePermissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppRolePermission_FailureWithAuthButNoPermission() {
    AppRolePermissionRequest appRolePermissionRequest =
        new AppRolePermissionRequest(APP_ROLE_ID, APP_PERMISSION_ID);
    webTestClient
        .post()
        .uri("/api/v1/app_roles_permissions/role_permission")
        .bodyValue(appRolePermissionRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppRolePermission_Failure_BadRequest() {
    AppRolePermissionRequest appRolePermissionRequest = new AppRolePermissionRequest();
    ResponseStatusInfo responseStatusInfo =
        webTestClient
            .post()
            .uri("/api/v1/app_roles_permissions/role_permission")
            .bodyValue(appRolePermissionRequest)
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
        responseStatusInfo.getErrMsg().contains("RoleID is required")
            && responseStatusInfo.getErrMsg().contains("PermissionID is required"));
    verifyNoInteractions(auditService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_READ", "PERMISSION_READ"})
  void testReadAppRolePermissions_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_roles_permissions")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(6, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissions_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_roles_permissions")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(6, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissions_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri("/api/v1/app_roles_permissions")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppRolePermissions_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/app_roles_permissions")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_READ", "PERMISSION_READ"})
  void testReadAppRolePermissionsByRoleId_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles_permissions/role/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(3, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissionsByRoleId_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles_permissions/role/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(3, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissionsByRoleId_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles_permissions/role/%s", 4))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppRolePermissionsByRoleId_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles_permissions/role/%s", 4))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_READ", "PERMISSION_READ"})
  void testReadAppRolePermissionsByAppIdAndRoleIds_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles_permissions/app/%s/roles/%s", "app-99", "1,4"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(3, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissionsByAppIdAndRoleIds_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_roles_permissions/app/%s/roles/%s", "app-99", "1,4"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(3, appRolePermissionResponse.getRolesPermissions().size());
  }

  @Test
  void testReadAppRolePermissionsByAppIdAndRoleIds_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles_permissions/app/%s/roles/%s", "app-99", "1,4"))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppRolePermissionsByAppIdAndRoleIds_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_roles_permissions/app/%s/roles/%s", "app-99", "1,4"))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_READ", "PERMISSION_READ"})
  void readAppRolePermission_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/app_roles_permissions/role_permission/%s/%s",
                    APP_ROLE_ID, APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(1, appRolePermissionResponse.getRolesPermissions().size());
    assertEquals(
        APP_ROLE_ID, appRolePermissionResponse.getRolesPermissions().getFirst().getRole().getId());
    assertEquals(
        APP_PERMISSION_ID,
        appRolePermissionResponse.getRolesPermissions().getFirst().getPermission().getId());
  }

  @Test
  void readAppRolePermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/app_roles_permissions/role_permission/%s/%s",
                    APP_ROLE_ID, APP_PERMISSION_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getRolesPermissions());
    assertEquals(1, appRolePermissionResponse.getRolesPermissions().size());
    assertEquals(
        APP_ROLE_ID, appRolePermissionResponse.getRolesPermissions().getFirst().getRole().getId());
    assertEquals(
        APP_PERMISSION_ID,
        appRolePermissionResponse.getRolesPermissions().getFirst().getPermission().getId());
  }

  @Test
  void testReadAppRolePermission_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/app_roles_permissions/role_permission/%s/%s",
                APP_ROLE_ID, APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppRolePermission_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/app_roles_permissions/role_permission/%s/%s",
                APP_ROLE_ID, APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testDeleteAppRolePermission_Success() {
    // setup
    AppRoleEntity appRoleEntity = appRoleRepository.save(TestData.getNewAppRoleEntity());
    AppPermissionEntity appPermissionEntity =
        appPermissionRepository.save(TestData.getNewAppPermissionEntity());
    AppRolePermissionEntity appRolePermissionEntity =
        appRolePermissionRepository.save(
            TestData.getNewAppRolePermissionEntity(appRoleEntity, appPermissionEntity));

    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(
                PLATFORM_ID, "ROLE_PERMISSION_UNASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/app_roles_permissions/role_permission/%s/%s",
                    appRoleEntity.getId(), appPermissionEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getResponseCrudInfo());
    assertEquals(1, appRolePermissionResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> roleIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> permissionIdCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleUnassignPermission(
            requestCaptor.capture(), roleIdCaptor.capture(), permissionIdCaptor.capture());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());
    assertEquals(appPermissionEntity.getId(), permissionIdCaptor.getValue());

    // cleanup
    appRolePermissionRepository.deleteById(appRolePermissionEntity.getId());
    appRoleRepository.deleteById(appRoleEntity.getId());
    appPermissionRepository.deleteById(appPermissionEntity.getId());
  }

  @Test
  void testDeleteAppRolePermission_Success_NoPermission_ButSuperUser() {
    // setup
    AppRoleEntity appRoleEntity = appRoleRepository.save(TestData.getNewAppRoleEntity());
    AppPermissionEntity appPermissionEntity =
        appPermissionRepository.save(TestData.getNewAppPermissionEntity());
    AppRolePermissionEntity appRolePermissionEntity =
        appRolePermissionRepository.save(
            TestData.getNewAppRolePermissionEntity(appRoleEntity, appPermissionEntity));

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);

    AppRolePermissionResponse appRolePermissionResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/app_roles_permissions/role_permission/%s/%s",
                    appRoleEntity.getId(), appPermissionEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppRolePermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appRolePermissionResponse);
    assertNotNull(appRolePermissionResponse.getResponseCrudInfo());
    assertEquals(1, appRolePermissionResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> roleIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> permissionIdCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, after(100).times(1))
        .auditAppRoleUnassignPermission(
            requestCaptor.capture(), roleIdCaptor.capture(), permissionIdCaptor.capture());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());
    assertEquals(appPermissionEntity.getId(), permissionIdCaptor.getValue());

    // cleanup
    appRolePermissionRepository.deleteById(appRolePermissionEntity.getId());
    appRoleRepository.deleteById(appRoleEntity.getId());
    appPermissionRepository.deleteById(appPermissionEntity.getId());
  }

  @Test
  void testDeleteAppRolePermission_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/app_roles_permissions/role_permission/%s/%s",
                APP_ROLE_ID, APP_PERMISSION_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testDeleteAppRolePermission_FailureWithAuthButNoPermission() {
    webTestClient
        .delete()
        .uri(
            String.format(
                "/api/v1/app_roles_permissions/role_permission/%s/%s",
                APP_ROLE_ID, APP_PERMISSION_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
