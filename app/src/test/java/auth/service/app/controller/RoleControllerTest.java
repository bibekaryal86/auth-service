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
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.repository.RoleRepository;
import auth.service.app.service.AuditService;
import auth.service.app.util.ConstantUtils;
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

public class RoleControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoRole;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  private static RoleRequest roleRequest;

  @Autowired private RoleRepository roleRepository;

  @MockitoBean private AuditService auditService;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoRole = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoRole);
  }

  @AfterEach
  void tearDown() {
    reset(auditService);
  }

  @Test
  void testCreateRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_CREATE"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_CREATE)),
            any(String.class));

    // cleanup
    roleRepository.deleteById(roleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_CREATE)),
            any(String.class));

    // cleanup
    roleRepository.deleteById(roleResponse.getRoles().getFirst().getId());
  }

  @Test
  void testCreateRole_FailureNoAuth() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/roles/role")
        .bodyValue(roleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateRole_FailureNoPermission() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/roles/role")
        .bodyValue(roleRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testCreateRole_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testCreateRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
    roleRequest = new RoleRequest(roleEntity.getRoleName(), roleEntity.getRoleDesc());

    RoleResponse roleResponse =
        webTestClient
            .post()
            .uri("/api/v1/roles/role")
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(roleResponse.getRoles().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testReadRoles_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_READ"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(9, roleResponse.getRoles().size());
    assertEquals(0, roleResponse.getRoles().getFirst().getPermissions().size());
    assertEquals(0, roleResponse.getRoles().getFirst().getPlatformProfiles().size());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(roleResponse.getResponseMetadata()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    roleResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responsePageInfo()),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(roleResponse.getRequestMetadata()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, roleResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(100, roleResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("roleName", roleResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(Sort.Direction.ASC, roleResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadRoles_Success_RequestMetadata() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(
            List.of(
                ConstantUtils.PERMISSION_READ_PERMISSION,
                ConstantUtils.PERMISSION_READ_ROLE,
                ConstantUtils.PERMISSION_READ_PLATFORM,
                ConstantUtils.PERMISSION_READ_PROFILE),
            profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/roles?isIncludePermissions=true&isIncludePlatforms=true&isIncludeProfiles=true&pageNumber=1&perPage=10&sortColumn=roleDesc&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(9, roleResponse.getRoles().size());
    assertFalse(roleResponse.getRoles().getFirst().getPermissions().isEmpty());
    assertFalse(roleResponse.getRoles().getFirst().getPlatformProfiles().isEmpty());
    assertFalse(roleResponse.getRoles().getFirst().getProfilePlatforms().isEmpty());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(roleResponse.getResponseMetadata()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    roleResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responsePageInfo()),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(roleResponse.getRequestMetadata()),
        () -> assertTrue(roleResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertTrue(roleResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, roleResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, roleResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("roleDesc", roleResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, roleResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadRoles_Success_RequestMetadata_NoReadPermissions() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_READ"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(
                "/api/v1/roles?isIncludePermissions=true&isIncludePlatforms=true&isIncludeProfiles=true&pageNumber=1&perPage=10&sortColumn=roleDesc&sortDirection=DESC")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(9, roleResponse.getRoles().size());
    assertTrue(roleResponse.getRoles().getFirst().getPermissions().isEmpty());
    assertTrue(roleResponse.getRoles().getFirst().getPlatformProfiles().isEmpty());
    assertTrue(roleResponse.getRoles().getLast().getProfilePlatforms().isEmpty());

    assertAll(
        "Response Metadata",
        () -> assertNotNull(roleResponse.getResponseMetadata()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo()),
        () ->
            assertFalse(
                StringUtils.hasText(
                    roleResponse.getResponseMetadata().responseStatusInfo().errMsg())),
        () -> assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo()),
        () -> assertNotNull(roleResponse.getResponseMetadata().responsePageInfo()),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().pageNumber() >= 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().perPage() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalItems() > 0),
        () -> assertTrue(roleResponse.getResponseMetadata().responsePageInfo().totalPages() > 0));

    assertAll(
        "Request Metadata",
        () -> assertNotNull(roleResponse.getRequestMetadata()),
        () -> assertTrue(roleResponse.getRequestMetadata().isIncludePermissions()),
        () -> assertTrue(roleResponse.getRequestMetadata().isIncludePlatforms()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeProfiles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeRoles()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeDeleted()),
        () -> assertFalse(roleResponse.getRequestMetadata().isIncludeHistory()),
        () -> assertEquals(1, roleResponse.getRequestMetadata().getPageNumber()),
        () -> assertEquals(10, roleResponse.getRequestMetadata().getPerPage()),
        () -> assertEquals("roleDesc", roleResponse.getRequestMetadata().getSortColumn()),
        () ->
            assertEquals(
                Sort.Direction.DESC, roleResponse.getRequestMetadata().getSortDirection()));
  }

  @Test
  void testReadRoles_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(9, roleResponse.getRoles().size());
  }

  @Test
  void testReadRoles_SuccessSuperUser_IncludeDeleted() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri("/api/v1/roles?isIncludeDeleted=true")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(13, roleResponse.getRoles().size());
  }

  @Test
  void testReadRoles_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/roles").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadRoles_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/roles")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_READ"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());

    verifyNoInteractions(auditService);
  }

  @Test
  void testReadRole_Success_WithAudit() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_READ"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/roles/role/%s?isIncludeHistory=true", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());

    verify(auditService).auditRoles(any(RequestMetadata.class), eq(ID));
  }

  @Test
  void testReadRole_Success_IncludeDeletedFalse() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_READ"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/roles/role/%s?isIncludeDeleted=true", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(0, roleResponse.getRoles().size());
  }

  @Test
  void testReadRole_Success_IncludeDeletedTrue() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/roles/role/%s?isIncludeDeleted=true", ID_DELETED))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
  }

  @Test
  void testReadRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .get()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
  }

  @Test
  void testReadRole_FailureNoAuth() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadRole_FailureNoPermission() {
    webTestClient
        .get()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri(String.format("/api/v1/roles/role/%s", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdateRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_UPDATE"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleEntity roleEntityOriginal =
        roleRepository.findById(ID).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_UPDATE)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testUpdateRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");

    RoleEntity roleEntityOriginal =
        roleRepository.findById(ID).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());
    assertEquals("NEW_ROLE_NAME", roleResponse.getRoles().getFirst().getRoleName());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam -> roleEntityParam.getRoleName().equals(roleRequest.getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_UPDATE)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testUpdateRole_FailureNoAuth() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .bodyValue(roleRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateRole_FailureNoPermission() {
    roleRequest = new RoleRequest("NEW_ROLE_NAME", "NEW_ROLE_DESC");
    webTestClient
        .put()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .bodyValue(roleRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testUpdateRole_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("", null);

    ResponseWithMetadata responseWithMetadata =
        webTestClient
            .put()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testUpdateRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    roleRequest = new RoleRequest("UPDATED_NAME", "UPDATED_DESC");

    RoleResponse roleResponse =
        webTestClient
            .put()
            .uri(String.format("/api/v1/roles/role/%s", ID_NOT_FOUND))
            .bodyValue(roleRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo());
    assertNotNull(roleResponse.getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(roleResponse.getRoles().isEmpty());
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_DELETE"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleEntity roleEntityOriginal =
        roleRepository.findById(ID).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_SOFT)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testSoftDeleteRole_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleEntity roleEntityOriginal =
        roleRepository.findById(ID).orElse(TestData.getRoleEntities().getFirst());

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_SOFT)),
            any(String.class));

    // reset
    roleRepository.save(roleEntityOriginal);
  }

  @Test
  void testSoftDeleteRole_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testSoftDeleteRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_Success() {
    // setup
    RoleEntity roleEntity = roleRepository.save(TestData.getNewRoleEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/roles/role/%s/hard", roleEntity.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getResponseMetadata());
    assertNotNull(roleResponse.getResponseMetadata().responseCrudInfo());
    assertEquals(1, roleResponse.getResponseMetadata().responseCrudInfo().deletedRowsCount());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            any(RoleEntity.class),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_DELETE_HARD)),
            any(String.class));
  }

  @Test
  void testHardDeleteRole_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_DELETE"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s/hard", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testHardDeleteRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/roles/role/%s/hard", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    RoleResponse roleResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/roles/role/%s/restore", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RoleResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(roleResponse);
    assertNotNull(roleResponse.getRoles());
    assertEquals(1, roleResponse.getRoles().size());

    verify(auditService, after(100).times(1))
        .auditRole(
            any(HttpServletRequest.class),
            argThat(
                roleEntityParam ->
                    roleEntityParam
                        .getRoleName()
                        .equals(roleResponse.getRoles().getFirst().getRoleName())),
            argThat(eventType -> eventType.equals(AuditEnums.AuditRole.ROLE_RESTORE)),
            any(String.class));
  }

  @Test
  void testRestoreRole_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/roles/role/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermissions(List.of("AUTHSVC_ROLE_RESTORE"), profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/roles/role/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
    verifyNoInteractions(auditService);
  }

  @Test
  void testRestoreRole_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoRole);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/roles/role/%s/restore", ID_NOT_FOUND))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
    verifyNoInteractions(auditService);
  }
}
