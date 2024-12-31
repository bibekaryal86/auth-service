package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.StatusTypeRequest;
import auth.service.app.model.dto.StatusTypeResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.repository.StatusTypeRepository;
import helper.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StatusTypeControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  private static StatusTypeRequest statusTypeRequest;

  @Autowired private StatusTypeRepository statusTypeRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
  }

  @Test
  void testCreateStatusType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_CREATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/status_types/status_type")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
    assertEquals("NEW_STATUS_NAME", statusTypeResponse.getStatusTypes().getFirst().getStatusName());

    // cleanup
    statusTypeRepository.deleteById(statusTypeResponse.getStatusTypes().getFirst().getId());
  }

  @Test
  void testCreateStatusType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/status_types/status_type")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
    assertEquals("NEW_STATUS_NAME", statusTypeResponse.getStatusTypes().getFirst().getStatusName());

    // cleanup
    statusTypeRepository.deleteById(statusTypeResponse.getStatusTypes().getFirst().getId());
  }

  @Test
  void testCreateStatusType_FailureNoAuth() {
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");
    webTestClient
        .post()
        .uri("/api/v1/status_types/status_type")
        .bodyValue(statusTypeRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testCreateStatusType_FailureNoPermission() {
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");
    webTestClient
        .post()
        .uri("/api/v1/status_types/status_type")
        .bodyValue(statusTypeRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testCreateStatusType_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest = new StatusTypeRequest("", "", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/status_types/status_type")
            .bodyValue(statusTypeRequest)
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
        responseMetadata.getResponseStatusInfo().getErrMsg().contains("Component is required")
            && responseMetadata.getResponseStatusInfo().getErrMsg().contains("Name is required")
            && responseMetadata
                .getResponseStatusInfo()
                .getErrMsg()
                .contains("Description is required"));
  }

  @Test
  void testCreateStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    StatusTypeEntity statusTypeEntity = TestData.getStatusTypeEntities().getFirst();
    statusTypeRequest =
        new StatusTypeRequest(
            statusTypeEntity.getComponentName(),
            statusTypeEntity.getStatusName(),
            statusTypeEntity.getStatusDesc());

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/status_types/status_type")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertNotNull(statusTypeResponse.getResponseMetadata());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(statusTypeResponse.getStatusTypes().isEmpty());
  }

  @Test
  void testReadStatusTypes_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/status_types")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(6, statusTypeResponse.getStatusTypes().size());
  }

  @Test
  void testReadStatusTypes_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/status_types")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(6, statusTypeResponse.getStatusTypes().size());
  }

  @Test
  void testReadStatusTypes_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/status_types").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadStatusTypes_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/status_types")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadStatusType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/status_types/status_type/1")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
  }

  @Test
  void testReadStatusType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/status_types/status_type/1")
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
  }

  @Test
  void testReadStatusType_FailureNoAuth() {
    webTestClient
        .get()
        .uri("/api/v1/status_types/status_type/1")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadStatusType_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/status_types/status_type/1")
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .get()
        .uri("/api/v1/status_types/status_type/9999")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdateStatusType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_UPDATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");

    StatusTypeEntity statusTypeEntityOriginal =
        statusTypeRepository.findById(1L).orElse(TestData.getStatusTypeEntities().getFirst());

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/status_types/status_type/1")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
    assertEquals("NEW_STATUS_NAME", statusTypeResponse.getStatusTypes().getFirst().getStatusName());

    // reset
    statusTypeRepository.save(statusTypeEntityOriginal);
  }

  @Test
  void testUpdateStatusType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");

    StatusTypeEntity statusTypeEntityOriginal =
        statusTypeRepository.findById(1L).orElse(TestData.getStatusTypeEntities().getFirst());

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/status_types/status_type/1")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
    assertEquals("NEW_STATUS_NAME", statusTypeResponse.getStatusTypes().getFirst().getStatusName());

    // reset
    statusTypeRepository.save(statusTypeEntityOriginal);
  }

  @Test
  void testUpdateStatusType_FailureNoAuth() {
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");
    webTestClient
        .put()
        .uri("/api/v1/status_types/status_type/1")
        .bodyValue(statusTypeRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testUpdateStatusType_FailureNoPermission() {
    statusTypeRequest =
        new StatusTypeRequest("NEW_COMPONENT_NAME", "NEW_STATUS_NAME", "NEW_STATUS_DESC");
    webTestClient
        .put()
        .uri("/api/v1/status_types/status_type/1")
        .bodyValue(statusTypeRequest)
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateStatusType_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest = new StatusTypeRequest("", "", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri("/api/v1/status_types/status_type/1")
            .bodyValue(statusTypeRequest)
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
  }

  @Test
  void testUpdateStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    statusTypeRequest = new StatusTypeRequest("UPDATED_COMPONENT", "UPDATED_NAME", "UPDATED_DESC");

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/status_types/status_type/9999")
            .bodyValue(statusTypeRequest)
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertNotNull(statusTypeResponse.getResponseMetadata());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(statusTypeResponse.getStatusTypes().isEmpty());
  }

  @Test
  void testSoftDeleteStatusType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/status_types/status_type/%s", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getResponseMetadata());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, statusTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testSoftDeleteStatusType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/status_types/status_type/%s", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getResponseMetadata());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, statusTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testSoftDeleteStatusType_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/status_types/status_type/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testSoftDeleteStatusType_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/status_types/status_type/%s", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testSoftDeleteStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/status_types/status_type/9999")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testHardDeleteStatusType_Success() {
    // setup
    StatusTypeEntity statusTypeEntity =
        statusTypeRepository.save(TestData.getNewStatusTypeEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .delete()
            .uri(
                String.format("/api/v1/status_types/status_type/%s/hard", statusTypeEntity.getId()))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getResponseMetadata());
    assertNotNull(statusTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, statusTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testHardDeleteStatusType_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/status_types/status_type/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testHardDeleteStatusType_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/status_types/status_type/%s/hard", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testHardDeleteStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri("/api/v1/status_types/status_type/9999/hard")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testRestoreStatusType_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    StatusTypeResponse statusTypeResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/status_types/status_type/%s/restore", ID))
            .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StatusTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(statusTypeResponse);
    assertNotNull(statusTypeResponse.getStatusTypes());
    assertEquals(1, statusTypeResponse.getStatusTypes().size());
  }

  @Test
  void testRestoreStatusType_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/status_types/status_type/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testRestoreStatusType_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("STATUS_TYPE_RESTORE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/status_types/status_type/%s/restore", ID))
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testRestoreStatusType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri("/api/v1/status_types/status_type/9999/restore")
        .header("Authorization", "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
