package user.management.system.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import helper.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import user.management.system.BaseTest;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserUpdateEmailRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.events.AppUserUpdatedEvent;
import user.management.system.app.repository.AppUserRepository;
import user.management.system.app.service.AuditService;

public class AppUserControllerTest extends BaseTest {

  private static int APP_USER_ID;
  private static AppUserDto appUserDtoNoPermission;
  private static AppUserDto appUserDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;
  private static String bearerAuthCredentialsWithPermission;

  @MockBean private AuditService auditService;
  @MockBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private AppUserRepository appUserRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    appUserDtoNoPermission = TestData.getAppUserDto();
    appUserDtoWithPermission = TestData.getAppUserDtoWithSuperUserRole(appUserDtoNoPermission);

    APP_USER_ID = appUserDtoNoPermission.getId();

    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoNoPermission);
    bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(APP_ID, appUserDtoWithPermission);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testReadAppUsers_Success() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_users")
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUsers_Success_SuperUser() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri("/api/v1/app_users")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(6, appUserResponse.getUsers().size());

    for (AppUserDto appUserDto : appUserResponse.getUsers()) {
      // make sure password is not returned with DTO
      assertNull(appUserDto.getPassword());
    }
  }

  @Test
  void testReadAppUsers_FailureWithNoBearerAuth() {
    webTestClient.get().uri("/api/v1/app_users").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAppUsersByAppId_Success() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/app/%s", APP_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUsersByAppId_Success_SuperUser() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/app/%s", "app-99"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(3, appUserResponse.getUsers().size());

    for (AppUserDto appUserDto : appUserResponse.getUsers()) {
      // make sure password is not returned with DTO
      assertNull(appUserDto.getPassword());
    }
  }

  @Test
  void testReadAppUsersByAppId_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users/app/%s", "app-99"))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUser_Success() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUser_Success_SuperUser() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUser_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUser_FailureWithDifferentUserId() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users/user/%s", 2))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAppUserByEmail_Success() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/user/email/%s", "firstlast@one.com"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUserByEmail_Success_SuperUser() {
    AppUserResponse appUserResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/app_users/user/email/%s", "firstlast@one.com"))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());
  }

  @Test
  void testReadAppUserByEmail_FailureWithNoBearerAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users/user/%s", "firstlast@one.com"))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAppUserByEmail_FailureWithDifferentUserId() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/app_users/user/email/%s", "firstlast@two.com"))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateAppUser_Success() {
    AppUserRequest appUserRequest = new AppUserRequest();
    BeanUtils.copyProperties(appUserDtoNoPermission, appUserRequest, "guestUser", "addresses");
    appUserRequest.setFirstName("New F Name One");
    appUserRequest.setLastName("New L Name One");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
            .bodyValue(appUserRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    assertEquals("New F Name One", appUserResponse.getUsers().getFirst().getFirstName());
    assertEquals("New L Name One", appUserResponse.getUsers().getFirst().getLastName());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdate(any(), any());
  }

  @Test
  void testUpdateAppUser_Success_SuperUser() {
    AppUserRequest appUserRequest = new AppUserRequest();
    BeanUtils.copyProperties(
        appUserDtoNoPermission, appUserRequest, "guestUser", "addresses", "password");
    appUserRequest.setFirstName("F Name One New");
    appUserRequest.setLastName("L Name One New");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
            .bodyValue(appUserRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    assertEquals("F Name One New", appUserResponse.getUsers().getFirst().getFirstName());
    assertEquals("L Name One New", appUserResponse.getUsers().getFirst().getLastName());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdate(any(), any());
  }

  @Test
  void testUpdateAppUser_FailureWithNoBearerAuth() {
    AppUserRequest appUserRequest = new AppUserRequest();
    BeanUtils.copyProperties(
        appUserDtoNoPermission, appUserRequest, "guestUser", "addresses", "password");
    appUserRequest.setFirstName("F Name Failed One");
    appUserRequest.setLastName("L Name Failed One");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/user/%s", APP_USER_ID))
        .bodyValue(appUserRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppUser_FailureWithDifferentUserId() {
    AppUserRequest appUserRequest = new AppUserRequest();
    BeanUtils.copyProperties(
        appUserDtoNoPermission, appUserRequest, "guestUser", "addresses", "password");
    appUserRequest.setFirstName("F Name One Failed");
    appUserRequest.setLastName("L Name One Failed");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/user/%s", 2))
        .bodyValue(appUserRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppUserEmail_Success() {
    UserUpdateEmailRequest userUpdateEmailRequest =
        new UserUpdateEmailRequest("firstlast@one.com", "lastfirst@one.com");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/app/%s/user/%s/email", APP_ID, APP_USER_ID))
            .bodyValue(userUpdateEmailRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    assertEquals("lastfirst@one.com", appUserResponse.getUsers().getFirst().getEmail());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdateEmail(any(), any(), any());
    verify(applicationEventPublisher, times(1)).publishEvent(any(AppUserUpdatedEvent.class));

    // reset
    AppUserEntity appUserEntity = appUserRepository.findById(APP_USER_ID).orElse(null);
    assertNotNull(appUserEntity);
    appUserEntity.setEmail("firstlast@one.com");
    appUserEntity = appUserRepository.save(appUserEntity);
    assertNotNull(appUserEntity);
    assertEquals("firstlast@one.com", appUserEntity.getEmail());
  }

  @Test
  void testUpdateAppUserEmail_Success_SuperUser() {
    UserUpdateEmailRequest userUpdateEmailRequest =
        new UserUpdateEmailRequest("firstlast@one.com", "lastfirst@one.com");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/app/%s/user/%s/email", APP_ID, APP_USER_ID))
            .bodyValue(userUpdateEmailRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(1, appUserResponse.getUsers().getFirst().getId());
    assertEquals("lastfirst@one.com", appUserResponse.getUsers().getFirst().getEmail());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdateEmail(any(), any(), any());
    verify(applicationEventPublisher, times(1)).publishEvent(any(AppUserUpdatedEvent.class));

    // reset
    AppUserEntity appUserEntity = appUserRepository.findById(APP_USER_ID).orElse(null);
    assertNotNull(appUserEntity);
    appUserEntity.setEmail("firstlast@one.com");
    appUserEntity = appUserRepository.save(appUserEntity);
    assertNotNull(appUserEntity);
    assertEquals("firstlast@one.com", appUserEntity.getEmail());
  }

  @Test
  void testUpdateAppUserEmail_FailureWithNoBearerAuth() {
    UserUpdateEmailRequest userUpdateEmailRequest =
        new UserUpdateEmailRequest("firstlast@three.com", "lastfirst@three.com");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/app/%s/user/%s/email", APP_ID, 2))
        .bodyValue(userUpdateEmailRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppUserEmail_FailureWithDifferentUserId() {
    UserUpdateEmailRequest userUpdateEmailRequest =
        new UserUpdateEmailRequest("firstlast@three.com", "lastfirst@three.com");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/app/%s/user/%s/email", APP_ID, 2))
        .bodyValue(userUpdateEmailRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppUserPassword_Success() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest("firstlast@one.com", "password-one-new");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/user/%s/password", APP_USER_ID))
            .bodyValue(userLoginRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdatePassword(any(), any());
  }

  @Test
  void testUpdateAppUserPassword_Success_SuperUser() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest("firstlast@one.com", "password-one-new");

    AppUserResponse appUserResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/app_users/user/%s/password", APP_USER_ID))
            .bodyValue(userLoginRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AppUserResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(appUserResponse);
    assertNotNull(appUserResponse.getUsers());
    assertEquals(1, appUserResponse.getUsers().size());
    assertEquals(APP_USER_ID, appUserResponse.getUsers().getFirst().getId());
    // make sure password is not returned with DTO
    assertNull(appUserResponse.getUsers().getFirst().getPassword());

    verify(auditService, times(1)).auditAppUserUpdatePassword(any(), any());
  }

  @Test
  void testUpdateAppUserPassword_FailureWithNoBearerAuth() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest("firstlast@one.com", "password-one-fail");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/user/%s/password", APP_USER_ID))
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateAppUserPassword_FailureWithDifferentUserId() {
    UserLoginRequest userLoginRequest =
        new UserLoginRequest("firstlast@one.com", "password-one-fail");

    webTestClient
        .put()
        .uri(String.format("/api/v1/app_users/user/%s/password", 2))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .bodyValue(userLoginRequest)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }
}
