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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.dto.AppUserRoleResponse;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.repository.AppRoleRepository;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.repository.AppUserRoleRepository;
import user.management.system.app.service.AuditService;

public class AppUserRoleControllerTest extends BaseTest {

  private static final int APP_USER_ID = 1;
  private static final int APP_ROLE_ID = 1;

  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  @MockBean private AuditService auditService;

  @Autowired private AppUserRoleRepository appUserRoleRepository;
  @Autowired private AppUserRepository appUserRepository;
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
  void testCreateAppUserRole_Success() {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "USER_ROLE_ASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    int appUserId = 6;
    int appRoleId = 1;

    AppUserRoleRequest appUserRoleRequest = new AppUserRoleRequest(appUserId, appRoleId);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_users_roles/user_role")
            .bodyValue(appUserRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(1, appUserRoleResponse.getUsersRoles().size());
    assertEquals(appUserId, appUserRoleResponse.getUsersRoles().getFirst().getUser().getId());
    assertEquals(appRoleId, appUserRoleResponse.getUsersRoles().getFirst().getRole().getId());
    verify(auditService, times(1)).auditAppUserAssignRole(any(), any());

    // cleanup
    appUserRoleRepository.deleteById(new AppUserRoleId(appUserId, appRoleId));
  }

  @Test
  void testCreateAppUserRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    int appUserId = 6;
    int appRoleId = 1;

    AppUserRoleRequest appUserRoleRequest = new AppUserRoleRequest(appUserId, appRoleId);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .post()
            .uri("/api/v1/app_users_roles/user_role")
            .bodyValue(appUserRoleRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(1, appUserRoleResponse.getUsersRoles().size());
    assertEquals(appUserId, appUserRoleResponse.getUsersRoles().getFirst().getUser().getId());
    assertEquals(appRoleId, appUserRoleResponse.getUsersRoles().getFirst().getRole().getId());
    verify(auditService, times(1)).auditAppUserAssignRole(any(), any());

    // cleanup
    appUserRoleRepository.deleteById(new AppUserRoleId(appUserId, appRoleId));
  }

  @Test
  void testCreateAppUserRole_FailureWithNoBearerAuth() {
    AppUserRoleRequest appUserRoleRequest = new AppUserRoleRequest(APP_USER_ID, APP_ROLE_ID);
    webTestClient
        .post()
        .uri("/api/v1/app_users_roles/user_role")
        .bodyValue(appUserRoleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateAppUserRole_FailureWithAuthButNoPermission() {
    AppUserRoleRequest appUserRoleRequest = new AppUserRoleRequest(APP_USER_ID, APP_ROLE_ID);
    webTestClient
        .post()
        .uri("/api/v1/app_users_roles/user_role")
        .bodyValue(appUserRoleRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"USER_READ", "ROLE_READ"})
  void testReadAppUserRoles_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_users_roles")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(6, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRoles_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_users_roles")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(6, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRoles_FailureWithNoBearerAuth() {
    webTestClient.get().uri("/api/v1/app_users_roles").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppUserRoles_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/app_users_roles")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"USER_READ", "ROLE_READ"})
  void testReadAppUserRolesByUserId_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/user/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(3, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRolesByUserId_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/user/%s", 4))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(3, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRolesByUserId_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/user/%s", 4))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUserRolesByUserId_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/user/%s", 4))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"USER_READ", "ROLE_READ"})
  void testReadAppUserRolesByUserIds_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/users/%s", "1,4"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(4, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRolesByUserIds_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/users/%s", "1,4"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(4, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRolesByUserIds_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/users/%s", "1,4"))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUserRolesByUserIds_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/users/%s", "1,4"))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {"USER_READ", "ROLE_READ"})
  void testReadAppUserRole_Success(String permissionName) {
    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, permissionName, appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(1, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRole_Success_NoPermission_ButSuperUser() {
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getUsersRoles());
    assertEquals(1, appUserRoleResponse.getUsersRoles().size());
  }

  @Test
  void testReadAppUserRole_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUserRole_FailureWithAuthButNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testDeleteAppUserRole_Success() {
    // setup
    AppUserEntity appUserEntity = appUserRepository.save(TestData.getNewAppUserEntity());
    AppRoleEntity appRoleEntity = appRoleRepository.save(TestData.getNewAppRoleEntity());
    AppUserRoleEntity appUserRoleEntity =
        appUserRoleRepository.save(TestData.getNewAppUserRoleEntity(appUserEntity, appRoleEntity));

    appUserDtoWithPermission =
        TestData.getAppUserDtoWithPermission(APP_ID, "USER_ROLE_UNASSIGN", appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/app_users_roles/user_role/%s/%s",
                    appUserEntity.getId(), appRoleEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getResponseCrudInfo());
    assertEquals(1, appUserRoleResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> roleIdCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1))
        .auditAppUserUnassignRole(
            requestCaptor.capture(), userIdCaptor.capture(), roleIdCaptor.capture());
    assertEquals(appUserEntity.getId(), userIdCaptor.getValue());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());

    // cleanup
    appUserRoleRepository.deleteById(appUserRoleEntity.getId());
    appUserRepository.deleteById(appUserEntity.getId());
    appRoleRepository.deleteById(appRoleEntity.getId());
  }

  @Test
  void testDeleteAppUserRole_Success_NoPermission_ButSuperUser() {
    // setup
    AppUserEntity appUserEntity = appUserRepository.save(TestData.getNewAppUserEntity());
    AppRoleEntity appRoleEntity = appRoleRepository.save(TestData.getNewAppRoleEntity());
    AppUserRoleEntity appUserRoleEntity =
        appUserRoleRepository.save(TestData.getNewAppUserRoleEntity(appUserEntity, appRoleEntity));

    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);

    AppUserRoleResponse appUserRoleResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/app_users_roles/user_role/%s/%s",
                    appUserEntity.getId(), appRoleEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserRoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserRoleResponse);
    assertNotNull(appUserRoleResponse.getResponseCrudInfo());
    assertEquals(1, appUserRoleResponse.getResponseCrudInfo().getDeletedRowsCount());

    ArgumentCaptor<HttpServletRequest> requestCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> roleIdCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(auditService, times(1))
        .auditAppUserUnassignRole(
            requestCaptor.capture(), userIdCaptor.capture(), roleIdCaptor.capture());
    assertEquals(appUserEntity.getId(), userIdCaptor.getValue());
    assertEquals(appRoleEntity.getId(), roleIdCaptor.getValue());

    // cleanup
    appUserRoleRepository.deleteById(appUserRoleEntity.getId());
    appUserRepository.deleteById(appUserEntity.getId());
    appRoleRepository.deleteById(appRoleEntity.getId());
  }

  @Test
  void testDeleteAppUserRole_FailureWithNoBearerAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testDeleteAppRolePermission_FailureWithAuthButNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/app_users_roles/user_role/%s/%s", APP_USER_ID, APP_ROLE_ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }
}
