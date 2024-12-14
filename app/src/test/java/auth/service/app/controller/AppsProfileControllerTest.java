package auth.service.app.controller;

import auth.service.BaseTest;

public class AppsProfileControllerTest extends BaseTest {

  //  private static final int APP_USER_ID = 1;
  //
  //  private static AppUserDto appUserDtoNoPermission;
  //  private static AppUserDto appUserDtoWithPermission;
  //
  //  @MockitoBean private AuditService auditService;
  //
  //  @Autowired private AppsAppUserRepository appsAppUserRepository;
  //  @Autowired private AppsRepository appsRepository;
  //  @Autowired private AppUserRepository appUserRepository;
  //
  //  @BeforeAll
  //  static void setUpBeforeAll() {
  //    appUserDtoNoPermission = TestData.getAppUserDto();
  //  }
  //
  //  @AfterEach
  //  void tearDown() {
  //    reset(auditService);
  //  }
  //
  //  @Test
  //  void testCreateAppsAppUser_Success() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    int appUserId = 6;
  //
  //    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest(PLATFORM_ID, appUserId);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .post()
  //            .uri("/api/v1/apps_app_user/apps_user")
  //            .bodyValue(appsAppUserRequest)
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getAppsUsers());
  //    assertEquals(1, appsAppUserResponse.getAppsUsers().size());
  //    assertEquals(PLATFORM_ID, appsAppUserResponse.getAppsUsers().getFirst().getApp().getId());
  //    assertEquals(appUserId, appsAppUserResponse.getAppsUsers().getFirst().getUser().getId());
  //    verify(auditService, after(100).times(1)).auditAppUserAssignApp(any(), any());
  //
  //    // cleanup
  //    appsAppUserRepository.deleteById(new AppsAppUserId(PLATFORM_ID, appUserId));
  //  }
  //
  //  @Test
  //  void testCreateAppsAppUser_FailureWithNoBearerAuth() {
  //    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest(PLATFORM_ID, APP_USER_ID);
  //    webTestClient
  //        .post()
  //        .uri("/api/v1/apps_app_user/apps_user")
  //        .bodyValue(appsAppUserRequest)
  //        .exchange()
  //        .expectStatus()
  //        .isUnauthorized();
  //    verifyNoInteractions(auditService);
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_CREATE", "USER_CREATE"})
  //  void testCreateAppsAppUser_FailureWithAuthButNoSuperUser(String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest(PLATFORM_ID, APP_USER_ID);
  //    webTestClient
  //        .post()
  //        .uri("/api/v1/apps_app_user/apps_user")
  //        .bodyValue(appsAppUserRequest)
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //    verifyNoInteractions(auditService);
  //  }
  //
  //  @Test
  //  void testCreateAppsAppUser_Failure_BadRequest() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest();
  //    ResponseStatusInfo responseStatusInfo =
  //        webTestClient
  //            .post()
  //            .uri("/api/v1/apps_app_user/apps_user")
  //            .bodyValue(appsAppUserRequest)
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isBadRequest()
  //            .expectBody(ResponseStatusInfo.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(responseStatusInfo);
  //    assertNotNull(responseStatusInfo.getErrMsg());
  //    assertTrue(
  //        responseStatusInfo.getErrMsg().contains("AppID is required")
  //            && responseStatusInfo.getErrMsg().contains("UserID is required"));
  //    verifyNoInteractions(auditService);
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsers_Success() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .get()
  //            .uri("/api/v1/apps_app_user")
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getAppsUsers());
  //    assertEquals(6, appsAppUserResponse.getAppsUsers().size());
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsers_FailureWithNoBearerAuth() {
  //    webTestClient.get().uri("/api/v1/apps_app_user").exchange().expectStatus().isUnauthorized();
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_READ", "USER_READ"})
  //  void testReadAppsAppUsers_FailureWithAuthButNoSuperUser(String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    webTestClient
  //        .get()
  //        .uri("/api/v1/apps_app_user")
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByAppId_Success() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .get()
  //            .uri(String.format("/api/v1/apps_app_user/app/%s", "app-99"))
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getAppsUsers());
  //    assertEquals(3, appsAppUserResponse.getAppsUsers().size());
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByAppId_FailureWithNoBearerAuth() {
  //    webTestClient
  //        .get()
  //        .uri(String.format("/api/v1/apps_app_user/app/%s", "app-99"))
  //        .exchange()
  //        .expectStatus()
  //        .isUnauthorized();
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_READ", "USER_READ"})
  //  void testReadAppsAppUsersByAppId_FailureWithAuthButNoSuperUser(String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    webTestClient
  //        .get()
  //        .uri(String.format("/api/v1/apps_app_user/app/%s", "app-99"))
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByUserId_Success() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .get()
  //            .uri(String.format("/api/v1/apps_app_user/user/%s", APP_USER_ID))
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getAppsUsers());
  //    assertEquals(1, appsAppUserResponse.getAppsUsers().size());
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByUserId_FailureWithNoBearerAuth() {
  //    webTestClient
  //        .get()
  //        .uri(String.format("/api/v1/apps_app_user/user/%s", APP_USER_ID))
  //        .exchange()
  //        .expectStatus()
  //        .isUnauthorized();
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_READ", "USER_READ"})
  //  void testReadAppsAppUsersByUserId_FailureWithAuthButNoSuperUser(String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    webTestClient
  //        .get()
  //        .uri(String.format("/api/v1/apps_app_user/user/%s", APP_USER_ID))
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByAppIdAndUserEmail_Success() {
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .get()
  //            .uri(
  //                String.format(
  //                    "/api/v1/apps_app_user/app/%s/user/%s", "app-99",
  // "firstlast@ninetynine3.com"))
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getAppsUsers());
  //    assertEquals(1, appsAppUserResponse.getAppsUsers().size());
  //  }
  //
  //  @Test
  //  void testReadAppsAppUsersByAppIdAndUserEmail_FailureWithNoBearerAuth() {
  //    webTestClient
  //        .get()
  //        .uri(
  //            String.format(
  //                "/api/v1/apps_app_user/app/%s/user/%s", "app-99", "firstlast@ninetynine3.com"))
  //        .exchange()
  //        .expectStatus()
  //        .isUnauthorized();
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_READ", "USER_READ"})
  //  void testReadAppsAppUsersByAppIdAndUserEmail_FailureWithAuthButNoSuperUser(
  //      String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    webTestClient
  //        .get()
  //        .uri(
  //            String.format(
  //                "/api/v1/apps_app_user/app/%s/user/%s", "app-99", "firstlast@ninetynine3.com"))
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //  }
  //
  //  @Test
  //  void testDeleteAppsAppUser_Success() {
  //    // setup
  //    AppsEntity appsEntity = appsRepository.save(TestData.getNewAppsEntity());
  //    AppUserEntity appUserEntity = appUserRepository.save(TestData.getNewAppUserEntity());
  //    AppsAppUserEntity appsAppUserEntity =
  //        appsAppUserRepository.save(TestData.getNewAppsAppUserEntity(appsEntity, appUserEntity));
  //
  //    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    AppsAppUserResponse appsAppUserResponse =
  //        webTestClient
  //            .delete()
  //            .uri(
  //                String.format(
  //                    "/api/v1/apps_app_user/apps_user/%s/%s",
  //                    appsEntity.getId(), appUserEntity.getEmail()))
  //            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //            .exchange()
  //            .expectStatus()
  //            .isOk()
  //            .expectBody(AppsAppUserResponse.class)
  //            .returnResult()
  //            .getResponseBody();
  //
  //    assertNotNull(appsAppUserResponse);
  //    assertNotNull(appsAppUserResponse.getResponseCrudInfo());
  //    assertEquals(1, appsAppUserResponse.getResponseCrudInfo().getDeletedRowsCount());
  //
  //    verify(auditService, after(100).times(1)).auditAppUserUnassignApp(any(), any(), any());
  //
  //    // cleanup
  //    appsAppUserRepository.deleteById(appsAppUserEntity.getId());
  //    appUserRepository.deleteById(appUserEntity.getId());
  //    appsRepository.deleteById(appsEntity.getId());
  //  }
  //
  //  @Test
  //  void testDeleteAppsAppUser_FailureWithNoBearerAuth() {
  //    webTestClient
  //        .delete()
  //        .uri(String.format("/api/v1/apps_app_user/apps_user/%s/%s", PLATFORM_ID, APP_USER_ID))
  //        .exchange()
  //        .expectStatus()
  //        .isUnauthorized();
  //    verifyNoInteractions(auditService);
  //  }
  //
  //  @ParameterizedTest
  //  @ValueSource(strings = {"APP_DELETE", "USER_DELETE"})
  //  void testDeleteAppsAppUser_FailureWithAuthButNoSuperUser(String permissionName) {
  //    appUserDtoWithPermission =
  //        TestData.getAppUserDtoWithPermission(PLATFORM_ID, permissionName,
  // appUserDtoNoPermission);
  //    String bearerAuthCredentialsWithPermission =
  //        TestData.getBearerAuthCredentialsForTest(PLATFORM_ID, appUserDtoWithPermission);
  //
  //    webTestClient
  //        .delete()
  //        .uri(String.format("/api/v1/apps_app_user/apps_user/%s/%s", PLATFORM_ID, APP_USER_ID))
  //        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
  //        .exchange()
  //        .expectStatus()
  //        .isForbidden();
  //
  //    verifyNoInteractions(auditService);
  //  }
}
