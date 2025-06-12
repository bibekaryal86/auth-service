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
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.PermissionRepository;
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

public class PermissionControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  private static PermissionRequest permissionRequest;

  @Autowired private PermissionRepository permissionRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreatePermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_CREATE"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

    PermissionResponse permissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/permissions/permission")
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertEquals(
        "NEW_PERMISSION_NAME", permissionResponse.getPermissions().getFirst().getPermissionName());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            argThat(
                permissionEntityParam ->
                    permissionEntityParam
                        .getPermissionName()
                        .equals(permissionRequest.getPermissionName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_CREATE)),
            any(String.class));

    // cleanup
    permissionRepository.deleteById(permissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testCreatePermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

    PermissionResponse permissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/permissions/permission")
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertEquals(
        "NEW_PERMISSION_NAME", permissionResponse.getPermissions().getFirst().getPermissionName());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            argThat(
                permissionEntityParam ->
                    permissionEntityParam
                        .getPermissionName()
                        .equals(permissionRequest.getPermissionName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_CREATE)),
            any(String.class));

    // cleanup
    permissionRepository.deleteById(permissionResponse.getPermissions().getFirst().getId());
  }

  @Test
  void testCreatePermission_FailureNoAuth() {
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
    webTestClient
        .post()
        .uri("/api/v1/permissions/permission")
        .bodyValue(permissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePermission_FailureNoPermission() {
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
    webTestClient
        .post()
        .uri("/api/v1/permissions/permission")
        .bodyValue(permissionRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreatePermission_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(-1L, "", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/permissions/permission")
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("RoleID is required")
            && responseWithMetadata
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
  void testCreatePermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    PermissionEntity permissionEntity = TestData.getPermissionEntities().getFirst();
    permissionRequest =
        new PermissionRequest(
            ID, permissionEntity.getPermissionName(), permissionEntity.getPermissionDesc());

    PermissionResponse permissionResponse =
        webTestClient
            .post()
            .uri("/api/v1/permissions/permission")
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertNotNull(permissionResponse.getResponseMetadata());
    assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(permissionResponse.getPermissions().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPermissions_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_READ"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/permissions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(9, permissionResponse.getPermissions().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(permissionResponse.getResponseMetadata()),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    permissionResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(permissionResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(permissionResponse.getRequestMetadata()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, permissionResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(100, permissionResponse.getRequestMetadata().getPerPage()),
        () ->
            assertEquals("permissionName", permissionResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.ASC, permissionResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadPermissions_Success_RequestMetadata() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_READ"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/permissions?pageNumber=1&perPage=10&sortColumn=permissionDesc&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(9, permissionResponse.getPermissions().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(permissionResponse.getResponseMetadata()),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    permissionResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(permissionResponse.getResponseMetadata().responsePageInfo()),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(permissionResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () ->
            assertTrue(
                permissionResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(permissionResponse.getRequestMetadata()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(permissionResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, permissionResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, permissionResponse.getRequestMetadata().getPerPage()),
        () ->
            assertEquals("permissionDesc", permissionResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, permissionResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadPermissions_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/permissions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(9, permissionResponse.getPermissions().size());
  }

  @Test
  void testReadPermissions_SuccessSuperUser_IncludeDeleted() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri("/api/v1/permissions?isIncludeDeleted=true")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(13, permissionResponse.getPermissions().size());
  }

  @Test
  void testReadPermissions_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/permissions").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadPermissions_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/permissions")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_READ"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertNotNull(permissionResponse.getPermissions().getFirst().getRole());
    assertEquals(1L, permissionResponse.getPermissions().getFirst().getRole().getId());
    assertTrue(permissionResponse.getPermissions().getFirst().getRole().getPermissions().isEmpty());
    assertTrue(
        permissionResponse.getPermissions().getFirst().getRole().getPlatformProfiles().isEmpty());

    verifyNoInteractions(auditService);
  }

  @Test
  void testReadPermission_Success_WithAudit() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_READ"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/permissions/permission/%s?isIncludeHistory=true", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertNotNull(permissionResponse.getPermissions().getFirst().getRole());
    assertEquals(1L, permissionResponse.getPermissions().getFirst().getRole().getId());
    assertTrue(permissionResponse.getPermissions().getFirst().getRole().getPermissions().isEmpty());
    assertTrue(
        permissionResponse.getPermissions().getFirst().getRole().getPlatformProfiles().isEmpty());
    assertNotNull(permissionResponse.getRequestMetadata());
    assertEquals(1, permissionResponse.getRequestMetadata().getHistoryPage());
    assertEquals(100, permissionResponse.getRequestMetadata().getHistorySize());

    verify(auditService).auditPermissions(any(RequestMetadata.class), eq(ID));
  }

  @Test
  void testReadPermission_Success_IncludeDeletedFalse() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_READ"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/permissions/permission/%s?isIncludeDeleted=true", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(0, permissionResponse.getPermissions().size());
  }

  @Test
  void testReadPermission_Success_IncludeDeletedTrue() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(
                String.format(
                    "/api/v1/permissions/permission/%s?isIncludeDeleted=true", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
  }

  @Test
  void testReadPermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
  }

  @Test
  void testReadPermission_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadPermission_FailureNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadPermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/permissions/permission/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdatePermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_UPDATE"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

    PermissionEntity permissionEntityOriginal =
        permissionRepository.findById(ID).orElse(TestData.getPermissionEntities().getFirst());

    PermissionResponse permissionResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertEquals(
        "NEW_PERMISSION_NAME", permissionResponse.getPermissions().getFirst().getPermissionName());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            argThat(
                permissionEntityParam ->
                    permissionEntityParam
                        .getPermissionName()
                        .equals(permissionRequest.getPermissionName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_UPDATE)),
            any(String.class));

    // reset
    permissionRepository.save(permissionEntityOriginal);
  }

  @Test
  void testUpdatePermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");

    PermissionEntity permissionEntityOriginal =
        permissionRepository.findById(ID).orElse(TestData.getPermissionEntities().getFirst());

    PermissionResponse permissionResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());
    assertEquals(
        "NEW_PERMISSION_NAME", permissionResponse.getPermissions().getFirst().getPermissionName());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            argThat(
                permissionEntityParam ->
                    permissionEntityParam
                        .getPermissionName()
                        .equals(permissionRequest.getPermissionName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_UPDATE)),
            any(String.class));

    // reset
    permissionRepository.save(permissionEntityOriginal);
  }

  @Test
  void testUpdatePermission_FailureNoAuth() {
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .bodyValue(permissionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePermission_FailureNoPermission() {
    permissionRequest = new PermissionRequest(ID, "NEW_PERMISSION_NAME", "NEW_PERMISSION_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .bodyValue(permissionRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdatePermission_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(-1L, "", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ResponseWithMetadata.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseWithMetadata);
    assertNotNull(responseWithMetadata.getResponseMetadata());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo());
    assertNotNull(responseWithMetadata.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(
        responseWithMetadata
                .getResponseMetadata()
                .responseStatusInfo()
                .errMsg()
                .contains("RoleID is required")
            && responseWithMetadata
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
  void testUpdatePermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    permissionRequest = new PermissionRequest(ID, "UPDATED_NAME", "UPDATED_DESC");

    PermissionResponse permissionResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/permissions/permission/%s", ID_NOT_FOUND))
            .bodyValue(permissionRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertNotNull(permissionResponse.getResponseMetadata());
    assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(permissionResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(permissionResponse.getPermissions().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePermission_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_DELETE"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionEntity permissionEntityOriginal =
        permissionRepository.findById(ID).orElse(TestData.getPermissionEntities().getFirst());

    PermissionResponse permissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getResponseMetadata());
    assertNotNull(permissionResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, permissionResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            any(PermissionEntity.class),
            argThat(
                eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_DELETE_SOFT)),
            any(String.class));

    // reset
    permissionRepository.save(permissionEntityOriginal);
  }

  @Test
  void testSoftDeletePermission_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionEntity permissionEntityOriginal =
        permissionRepository.findById(ID).orElse(TestData.getPermissionEntities().getFirst());

    PermissionResponse permissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/permissions/permission/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getResponseMetadata());
    assertNotNull(permissionResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, permissionResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            any(PermissionEntity.class),
            argThat(
                eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_DELETE_SOFT)),
            any(String.class));

    // reset
    permissionRepository.save(permissionEntityOriginal);
  }

  @Test
  void testSoftDeletePermission_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePermission_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeletePermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePermission_Success() {
    // setup
    PermissionEntity permissionEntity =
        permissionRepository.save(TestData.getNewPermissionEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/permissions/permission/%s/hard", permissionEntity.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getResponseMetadata());
    assertNotNull(permissionResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, permissionResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            any(PermissionEntity.class),
            argThat(
                eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_DELETE_HARD)),
            any(String.class));
  }

  @Test
  void testHardDeletePermission_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePermission_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_DELETE"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s/hard", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeletePermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/permissions/permission/%s/hard", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePermission_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    PermissionResponse permissionResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/permissions/permission/%s/restore", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PermissionResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(permissionResponse);
    assertNotNull(permissionResponse.getPermissions());
    assertEquals(1, permissionResponse.getPermissions().size());

    verify(auditService, after(100).times(1))
        .auditPermission(
            any(HttpServletRequest.class),
            argThat(
                permissionEntityParam ->
                    permissionEntityParam
                        .getPermissionName()
                        .equals(
                            permissionResponse.getPermissions().getFirst().getPermissionName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditPermission.PERMISSION_RESTORE)),
            any(String.class));
  }

  @Test
  void testRestorePermission_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/permissions/permission/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePermission_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of("AUTHSVC_PERMISSION_RESTORE"), profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/permissions/permission/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestorePermission_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/permissions/permission/%s/restore", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }
}
