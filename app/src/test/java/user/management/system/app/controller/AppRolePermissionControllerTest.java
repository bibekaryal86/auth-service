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
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppRolePermissionRequest;
import user.management.system.app.model.dto.AppRolePermissionResponse;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppRolePermissionId;
import user.management.system.app.repository.AppPermissionRepository;
import user.management.system.app.repository.AppRolePermissionRepository;
import user.management.system.app.repository.AppRoleRepository;
import user.management.system.app.service.AuditService;

public class AppRolePermissionControllerTest extends BaseTest {

  private static final int APP_ROLE_ID = 1;
  private static final int APP_PERMISSION_ID = 1;

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @MockBean private AuditService auditService;

  @Autowired private AppRolePermissionRepository appRolePermissionRepository;
  @Autowired private AppRoleRepository appRoleRepository;
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
  void testCreateAppRolePermission_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(
            APP_ID, "ROLE_PERMISSION_ASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
    verify(auditService, times(1)).auditAppRoleAssignPermission(any(), any());

    // cleanup
    appRolePermissionRepository.deleteById(new AppRolePermissionId(appRoleId, appPermissionId));
  }

  @Test
  void testCreateAppRolePermission_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
    verify(auditService, times(1)).auditAppRoleAssignPermission(any(), any());

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

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_READ", "PERMISSION_READ"})
  void testReadAppRolePermissions_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    appRoleEntity.setName("Role Permission Delete");
    appRoleEntity.setDescription("Role Permission Delete Test");
    appRoleEntity = appRoleRepository.save(appRoleEntity);

    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setAppId("app-99");
    appPermissionEntity.setName("Permission Role Delete");
    appPermissionEntity.setDescription("Permission Role Delete Test");
    appPermissionEntity = appPermissionRepository.save(appPermissionEntity);

    AppRolePermissionId appRolePermissionId =
        new AppRolePermissionId(appRoleEntity.getId(), appPermissionEntity.getId());
    AppRolePermissionEntity appRolePermissionEntity = new AppRolePermissionEntity();
    appRolePermissionEntity.setAppRole(appRoleEntity);
    appRolePermissionEntity.setAppPermission(appPermissionEntity);
    appRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    appRolePermissionEntity.setId(appRolePermissionId);
    appRolePermissionRepository.save(appRolePermissionEntity);

    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(
            APP_ID, "ROLE_PERMISSION_UNASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
    verify(auditService, times(1))
        .auditAppRoleUnassignPermission(
            requestCaptor.capture(), roleIdCaptor.capture(), permissionIdCaptor.capture());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());
    assertEquals(appPermissionEntity.getId(), permissionIdCaptor.getValue());

    // cleanup
    appRolePermissionRepository.deleteById(appRolePermissionId);
    appRoleRepository.deleteById(appRoleEntity.getId());
    appPermissionRepository.deleteById(appPermissionEntity.getId());
  }

  @Test
  void testDeleteAppRolePermission_Success_NoPermission_ButSuperUser() {
    // setup
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    appRoleEntity.setName("Role Permission Delete");
    appRoleEntity.setDescription("Role Permission Delete Test");
    appRoleEntity = appRoleRepository.save(appRoleEntity);

    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setAppId("app-99");
    appPermissionEntity.setName("Permission Role Delete");
    appPermissionEntity.setDescription("Permission Role Delete Test");
    appPermissionEntity = appPermissionRepository.save(appPermissionEntity);

    AppRolePermissionId appRolePermissionId =
        new AppRolePermissionId(appRoleEntity.getId(), appPermissionEntity.getId());
    AppRolePermissionEntity appRolePermissionEntity = new AppRolePermissionEntity();
    appRolePermissionEntity.setAppRole(appRoleEntity);
    appRolePermissionEntity.setAppPermission(appPermissionEntity);
    appRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    appRolePermissionEntity.setId(appRolePermissionId);
    appRolePermissionRepository.save(appRolePermissionEntity);

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

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
    verify(auditService, times(1))
        .auditAppRoleUnassignPermission(
            requestCaptor.capture(), roleIdCaptor.capture(), permissionIdCaptor.capture());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());
    assertEquals(appPermissionEntity.getId(), permissionIdCaptor.getValue());

    // cleanup
    appRolePermissionRepository.deleteById(appRolePermissionId);
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
