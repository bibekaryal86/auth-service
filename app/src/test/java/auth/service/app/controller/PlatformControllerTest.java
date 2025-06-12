package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import auth.service.BaseTest;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.service.AuditService;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StringUtils;

public class PlatformControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPlatform;
  private static ProfileDto profileDtoWithPlatform;
  private static String bearerAuthCredentialsNoPlatform;

  private static PlatformRequest platformRequest;

  @Autowired private PlatformRepository platformRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPlatform = TestData.getProfileDto();
    bearerAuthCredentialsNoPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPlatform);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreatePlatform_SuccessSuperUser() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    PlatformResponse platformResponse =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
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
  void testCreatePlatform_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_CREATE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    webTestClient
        .post()
        .uri("/api/v1/platforms/platform")
        .bodyValue(platformRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
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
  void testCreatePlatform_FailureNoPlatform() {
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .post()
        .uri("/api/v1/platforms/platform")
        .bodyValue(platformRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatform_FailureBadRequest() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Name is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Description is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    platformRequest =
        new PlatformRequest(platformEntity.getPlatformName(), platformEntity.getPlatformDesc());

    PlatformResponse platformResponse =
        webTestClient
            .post()
            .uri("/api/v1/platforms/platform")
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(platformResponse.getPlatforms().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPlatforms_SuccessSuperUser() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri("/api/v1/platforms")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(9, platformResponse.getPlatforms().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(platformResponse.getResponseMetadata()),
        () -> assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    platformResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(platformResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(platformResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(platformResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(platformResponse.getRequestMetadata()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, platformResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(100, platformResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("platformName", platformResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.ASC, platformResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadPlatforms_SuccessSuperUser_RequestMetadata() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/platforms?isIncludeProfiles=true&pageNumber=1&perPage=10&sortColumn=platformDesc&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(9, platformResponse.getPlatforms().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(platformResponse.getResponseMetadata()),
        () -> assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    platformResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(platformResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(platformResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(platformResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(platformResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(platformResponse.getRequestMetadata()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertTrue(platformResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(platformResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, platformResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, platformResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("platformDesc", platformResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, platformResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadPlatforms_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_READ"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .get()
        .uri("/api/v1/platforms")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatforms_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/platforms").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadPlatforms_FailureNoPlatform() {
    webTestClient
        .get()
        .uri("/api/v1/platforms")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatform_SuccessSuperUser() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/platforms/platform/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());

    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPlatform_SuccessSuperUser_WithAudit() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/platforms/platform/%s?isIncludeHistory=true", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(1, platformResponse.getPlatforms().size());

    verify(auditService).auditPlatforms(any(RequestMetadata.class), eq(ID));
  }

  @Test
  void testReadPlatform_SuccessSuperUser_IncludeDeletedFalse() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/platforms/platform/%s?isIncludeDeleted=false", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertEquals(0, platformResponse.getPlatforms().size());
  }

  @Test
  void testReadPlatform_SuccessSuperUser_IncludeDeletedTrue() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/platforms/platform/%s?isIncludeDeleted=true", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
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
  void testReadPlatform_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_READ"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .get()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatform_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadPlatform_FailureNoPlatform() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .get()
        .uri(String.format("/api/v1/platforms/platform/%s", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdatePlatform_SuccessSuperUser() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    PlatformEntity platformEntityOriginal =
        platformRepository.findById(ID).orElse(TestData.getPlatformEntities().getFirst());

    PlatformResponse platformResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/platforms/platform/%s", ID))
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
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
  void testUpdatePlatform_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_UPDATE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");

    webTestClient
        .put()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .bodyValue(platformRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();

    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_FailureNoAuth() {
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .bodyValue(platformRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_FailureNoPlatform() {
    platformRequest = new PlatformRequest("NEW_PLATFORM_NAME", "NEW_PLATFORM_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .bodyValue(platformRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_FailureBadRequest() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/platforms/platform/%s", ID))
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Name is required")
            && responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("Description is required"));
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);
    platformRequest = new PlatformRequest("UPDATED_NAME", "UPDATED_DESC");

    PlatformResponse platformResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/platforms/platform/%s", ID_NOT_FOUND))
            .bodyValue(platformRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getPlatforms());
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(platformResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(platformResponse.getPlatforms().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePlatform_SuccessSuperUser() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformEntity platformEntityOriginal =
        platformRepository.findById(ID).orElse(TestData.getPlatformEntities().getFirst());

    PlatformResponse platformResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/platforms/platform/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, platformResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPlatform(
            any(HttpServletRequest.class),
            any(PlatformEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPlatform.PLATFORM_DELETE_SOFT)),
            any(String.class));

    // reset
    platformRepository.save(platformEntityOriginal);
  }

  @Test
  void testSoftDeletePlatform_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_DELETE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
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
  void testSoftDeletePlatform_FailureNoPlatform() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePlatform_Success() {
    // setup
    PlatformEntity platformEntity = platformRepository.save(TestData.getNewPlatformEntity());

    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/platforms/platform/%s/hard", platformEntity.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PlatformResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(platformResponse);
    assertNotNull(platformResponse.getResponseMetadata());
    assertNotNull(platformResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, platformResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

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
  void testHardDeletePlatform_FailureNoPlatform() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_DELETE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s/hard", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/platforms/platform/%s/hard", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePlatform_Success() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    PlatformResponse platformResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
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
  void testRestorePlatform_FailureNoSuperUser() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_RESTORE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
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
  void testRestorePlatform_FailureNoPlatform() {
    profileDtoWithPlatform =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PLATFORM_RESTORE"), profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/platforms/platform/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePlatform_FailureException() {
    profileDtoWithPlatform = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPlatform);
    String bearerAuthCredentialsWithPlatform =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPlatform);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/platforms/platform/%s/restore", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPlatform)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }
}
