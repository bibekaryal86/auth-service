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
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class PlatformControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;

  private static PlatformRequest platformRequest;

  @Autowired private PlatformRepository platformRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreatePlatform_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    PlatformResponse platformResponse =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());
    assertEquals("NEW_PLATFORM_NAME", platformResponse.getPlatforms().getFirst().getPlatformName());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            argThat(
                platformEntityParam ->
                    platformEntityParam
                        .getPlatformName()
                        .equals(platformRequest.getPlatformName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_CREATE)),
            any(String.class));

    // cleanup
    platformRepository.deleteById(platformResponse.getPlatforms().getFirst().getId());
  }

  @Test
  void testCreatePlatform_FailureNoAuth() {
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .post()
        .uri("/api/v1/platforms/platform")
        .bodyValue(platformRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatform_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_CREATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .post()
        .uri("/api/v1/platforms/platform")
        .bodyValue(platformRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatform_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    platformRequest = new PlatformRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testCreatePlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    platformRequest =
        new PlatformRequest(platformEntity.getPlatformName(), platformEntity.getPlatformDesc());

    PlatformResponse platformResponse =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(platformResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(platformResponse.getPlatforms().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPlatforms_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri("/api/v1/platforms")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(6, platformResponse.getPlatforms().size());
  }

  @Test
  void testReadPlatforms_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/platforms").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadPlatforms_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/platforms")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatform_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri("/api/v1/platforms/platform/1")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());
  }

  @Test
  void testReadPlatform_FailureNoAuth() {
    webTestClient
        .get()
        .uri("/api/v1/platforms/platform/1")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadPlatform_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/platforms/platform/1")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/platforms/platform/9999")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdatePlatform_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    PlatformEntity platformEntityOriginal =
        platformRepository.findById(1L).orElse(TestData.getPlatformEntities().getFirst());

    PlatformResponse platformResponse =
        webTestClient
            .put()
            .uri("/api/v1/platforms/platform/1")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());
    assertEquals("NEW_PLATFORM_NAME", platformResponse.getPlatforms().getFirst().getPlatformName());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            argThat(
                platformEntityParam ->
                    platformEntityParam
                        .getPlatformName()
                        .equals(platformRequest.getPlatformName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_UPDATE)),
            any(String.class));

    // reset
    platformRepository.save(platformEntityOriginal);
  }

  @Test
  void testUpdatePlatform_FailureNoAuth() {
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .put()
        .uri("/api/v1/platforms/platform/1")
        .bodyValue(platformRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_NoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_UPDATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .put()
        .uri("/api/v1/platforms/platform/1")
        .bodyValue(platformRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    platformRequest = new PlatformRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri("/api/v1/platforms/platform/1")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testUpdatePlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    platformRequest = new PlatformRequest("UPDATED_NAME", "UPDATED_DESC");

    PlatformResponse platformResponse =
        webTestClient
            .put()
            .uri("/api/v1/platforms/platform/9999")
            .bodyValue(platformRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(platformResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(platformResponse.getPlatforms().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePlatform_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformResponse platformResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/platforms/platform/%s", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, platformResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            any(PlatformEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT)),
            any(String.class));
  }

  @Test
  void testSoftDeletePlatform_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePlatform_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/platforms/platform/9999")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePlatform_Success() {
    // setup
    PlatformEntity platformEntity = platformRepository.save(TestData.getNewPlatformEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformResponse platformResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/platforms/platform/%s/hard", platformEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, platformResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            any(PlatformEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_DELETE_HARD)),
            any(String.class));
  }

  @Test
  void testHardDeletePlatform_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePlatform_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s/hard", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/platforms/platform/9999/hard")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePlatform_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PlatformResponse platformResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            argThat(
                platformEntityParam ->
                    platformEntityParam
                        .getPlatformName()
                        .equals(platformResponse.getPlatforms().getFirst().getPlatformName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_RESTORE)),
            any(String.class));
  }

  @Test
  void testRestorePlatform_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePlatform_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("PLATFORM_RESTORE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePlatform_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri("/api/v1/platforms/platform/9999/restore")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }
}
