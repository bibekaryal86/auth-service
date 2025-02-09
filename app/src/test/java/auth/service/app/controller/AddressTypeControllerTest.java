package auth.service.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.dto.AddressTypeRequest;
import auth.service.app.model.dto.AddressTypeResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.repository.AddressTypeRepository;
import helper.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

public class AddressTypeControllerTest extends BaseTest {

  private static PlatformEntity platformEntity;
  private static ProfileDto profileDtoNoPermission;
  private static ProfileDto profileDtoWithPermission;
  private static String bearerAuthCredentialsNoPermission;

  private static AddressTypeRequest addressTypeRequest;

  @Autowired private AddressTypeRepository addressTypeRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileDtoNoPermission = TestData.getProfileDto();
    bearerAuthCredentialsNoPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoNoPermission);
  }

  @Test
  void testCreateAddressType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_CREATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/address_types/address_type")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
    assertEquals("NEW_TYPE_NAME", addressTypeResponse.getAddressTypes().getFirst().getTypeName());

    // cleanup
    addressTypeRepository.deleteById(addressTypeResponse.getAddressTypes().getFirst().getId());
  }

  @Test
  void testCreateAddressType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/address_types/address_type")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
    assertEquals("NEW_TYPE_NAME", addressTypeResponse.getAddressTypes().getFirst().getTypeName());

    // cleanup
    addressTypeRepository.deleteById(addressTypeResponse.getAddressTypes().getFirst().getId());
  }

  @Test
  void testCreateAddressType_FailureNoAuth() {
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/address_types/address_type")
        .bodyValue(addressTypeRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testCreateAddressType_FailureNoPermission() {
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");
    webTestClient
        .post()
        .uri("/api/v1/address_types/address_type")
        .bodyValue(addressTypeRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testCreateAddressType_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .post()
            .uri("/api/v1/address_types/address_type")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testCreateAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    AddressTypeEntity addressTypeEntity = TestData.getAddressTypeEntities().getFirst();
    addressTypeRequest =
        new AddressTypeRequest(addressTypeEntity.getTypeName(), addressTypeEntity.getTypeDesc());

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .post()
            .uri("/api/v1/address_types/address_type")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .is5xxServerError()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertNotNull(addressTypeResponse.getResponseMetadata());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(addressTypeResponse.getAddressTypes().isEmpty());
  }

  @Test
  void testReadAddressTypes_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/address_types")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(6, addressTypeResponse.getAddressTypes().size());
  }

  @Test
  void testReadAddressTypes_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/address_types")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(6, addressTypeResponse.getAddressTypes().size());
  }

  @Test
  void testReadAddressTypes_FailureNoAuth() {
    webTestClient.get().uri("/api/v1/address_types").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void testReadAddressTypes_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/address_types")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAddressType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_READ", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/address_types/address_type/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
  }

  @Test
  void testReadAddressType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .get()
            .uri("/api/v1/address_types/address_type/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
  }

  @Test
  void testReadAddressType_FailureNoAuth() {
    webTestClient
        .get()
        .uri("/api/v1/address_types/address_type/1")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testReadAddressType_FailureNoPermission() {
    webTestClient
        .get()
        .uri("/api/v1/address_types/address_type/1")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testReadAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    webTestClient
        .get()
        .uri("/api/v1/address_types/address_type/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testUpdateAddressType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_UPDATE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");

    AddressTypeEntity addressTypeEntityOriginal =
        addressTypeRepository.findById(1L).orElse(TestData.getAddressTypeEntities().getFirst());

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/address_types/address_type/1")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
    assertEquals("NEW_TYPE_NAME", addressTypeResponse.getAddressTypes().getFirst().getTypeName());

    // reset
    addressTypeRepository.save(addressTypeEntityOriginal);
  }

  @Test
  void testUpdateAddressType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");

    AddressTypeEntity addressTypeEntityOriginal =
        addressTypeRepository.findById(1L).orElse(TestData.getAddressTypeEntities().getFirst());

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/address_types/address_type/1")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
    assertEquals("NEW_TYPE_NAME", addressTypeResponse.getAddressTypes().getFirst().getTypeName());

    // reset
    addressTypeRepository.save(addressTypeEntityOriginal);
  }

  @Test
  void testUpdateAddressType_FailureNoAuth() {
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");
    webTestClient
        .put()
        .uri("/api/v1/address_types/address_type/1")
        .bodyValue(addressTypeRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testUpdateAddressType_FailureNoPermission() {
    addressTypeRequest = new AddressTypeRequest("NEW_TYPE_NAME", "NEW_TYPE_DESC");
    webTestClient
        .put()
        .uri("/api/v1/address_types/address_type/1")
        .bodyValue(addressTypeRequest)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateAddressType_FailureBadRequest() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("", null);

    ResponseMetadata responseMetadata =
        webTestClient
            .put()
            .uri("/api/v1/address_types/address_type/1")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
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
  void testUpdateAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    addressTypeRequest = new AddressTypeRequest("UPDATED_NAME", "UPDATED_DESC");

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .put()
            .uri("/api/v1/address_types/address_type/9999")
            .bodyValue(addressTypeRequest)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertNotNull(addressTypeResponse.getResponseMetadata());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseStatusInfo());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(addressTypeResponse.getAddressTypes().isEmpty());
  }

  @Test
  void testSoftDeleteAddressType_Success() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/address_types/address_type/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getResponseMetadata());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, addressTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testSoftDeleteAddressType_SuccessSuperUser() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .delete()
            .uri(String.format("/api/v1/address_types/address_type/%s", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getResponseMetadata());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, addressTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testSoftDeleteAddressType_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/address_types/address_type/%s", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testSoftDeleteAddressType_FailureNoPermission() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/address_types/address_type/%s", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsNoPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testSoftDeleteAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    webTestClient
        .delete()
        .uri("/api/v1/address_types/address_type/9999")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testHardDeleteAddressType_Success() {
    // setup
    AddressTypeEntity addressTypeEntity =
        addressTypeRepository.save(TestData.getNewAddressTypeEntity());

    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .delete()
            .uri(
                String.format(
                    "/api/v1/address_types/address_type/%s/hard", addressTypeEntity.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getResponseMetadata());
    assertNotNull(addressTypeResponse.getResponseMetadata().getResponseCrudInfo());
    assertEquals(
        1, addressTypeResponse.getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testHardDeleteAddressType_FailureNoAuth() {
    webTestClient
        .delete()
        .uri(String.format("/api/v1/address_types/address_type/%s/hard", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testHardDeleteAddressType_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_DELETE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .delete()
        .uri(String.format("/api/v1/address_types/address_type/%s/hard", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testHardDeleteAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    webTestClient
        .delete()
        .uri("/api/v1/address_types/address_type/9999/hard")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testRestoreAddressType_Success() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    AddressTypeResponse addressTypeResponse =
        webTestClient
            .patch()
            .uri(String.format("/api/v1/address_types/address_type/%s/restore", ID))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AddressTypeResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(addressTypeResponse);
    assertNotNull(addressTypeResponse.getAddressTypes());
    assertEquals(1, addressTypeResponse.getAddressTypes().size());
  }

  @Test
  void testRestoreAddressType_FailureNoAuth() {
    webTestClient
        .patch()
        .uri(String.format("/api/v1/address_types/address_type/%s/restore", ID))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void testRestoreAddressType_FailureNoPermission() {
    profileDtoWithPermission =
        TestData.getProfileDtoWithPermission("ADDRESS_TYPE_RESTORE", profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);

    webTestClient
        .patch()
        .uri(String.format("/api/v1/address_types/address_type/%s/restore", ID))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testRestoreAddressType_FailureException() {
    profileDtoWithPermission = TestData.getProfileDtoWithSuperUserRole(profileDtoNoPermission);
    String bearerAuthCredentialsWithPermission =
        TestData.getBearerAuthCredentialsForTest(platformEntity, profileDtoWithPermission);
    webTestClient
        .patch()
        .uri("/api/v1/address_types/address_type/9999/restore")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthCredentialsWithPermission)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
