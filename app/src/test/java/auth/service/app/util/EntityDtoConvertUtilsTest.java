package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.AddressTypeDto;
import auth.service.app.model.dto.AddressTypeResponse;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleDto;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.PlatformRolePermissionDto;
import auth.service.app.model.dto.PlatformRolePermissionResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.dto.StatusTypeDto;
import auth.service.app.model.dto.StatusTypeResponse;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import helper.EntityDtoComparator;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EntityDtoConvertUtilsTest extends BaseTest {

  @Autowired private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<AddressTypeEntity> addressTypeEntities;
  private static List<StatusTypeEntity> statusTypeEntities;
  private static List<PermissionEntity> permissionEntities;
  private static List<RoleEntity> roleEntities;
  private static List<PlatformEntity> platformEntities;
  private static List<ProfileEntity> profileEntities;
  private static List<PlatformProfileRoleEntity> platformProfileRoleEntities;
  private static List<PlatformRolePermissionEntity> platformRolePermissionEntities;

  @BeforeAll
  static void setUp() {
    addressTypeEntities = TestData.getAddressTypeEntities();
    statusTypeEntities = TestData.getStatusTypeEntities();
    permissionEntities = TestData.getPermissionEntities();
    roleEntities = TestData.getRoleEntities();
    platformEntities = TestData.getPlatformEntities();
    profileEntities = TestData.getProfileEntities();
    platformProfileRoleEntities = TestData.getPlatformProfileRoleEntities();
    platformRolePermissionEntities = TestData.getPlatformRolePermissionEntities();
  }

  @Test
  void testGetResponseSingleAddressType_NullEntity() {
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseSingleAddressType(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAddressTypes());
    assertTrue(response.getBody().getAddressTypes().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAddressType_NonNullEntity() {
    AddressTypeEntity entity = addressTypeEntities.getFirst();
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseSingleAddressType(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAddressTypes());
    assertEquals(1, response.getBody().getAddressTypes().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    AddressTypeDto dto = response.getBody().getAddressTypes().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultipleAddressTypes_EmptyList() {
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseMultipleAddressTypes(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAddressTypes());
    assertTrue(response.getBody().getAddressTypes().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAddressTypes_NonEmptyList() {
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseMultipleAddressTypes(addressTypeEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAddressTypes());
    assertEquals(3, response.getBody().getAddressTypes().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<AddressTypeDto> addressTypeDtos = response.getBody().getAddressTypes();

    Map<AddressTypeEntity, AddressTypeDto> entityDtoMap =
        addressTypeEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        addressTypeDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(new AddressTypeDto(entity.getId(), null, null, null, "", ""))));

    for (Map.Entry<AddressTypeEntity, AddressTypeDto> entry : entityDtoMap.entrySet()) {
      AddressTypeEntity entity = entry.getKey();
      AddressTypeDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeleteAddressType() {
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseDeleteAddressType();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getAddressTypes().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorAddressType() {
    ResponseEntity<AddressTypeResponse> response =
        entityDtoConvertUtils.getResponseErrorAddressType(
            new ElementNotFoundException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getAddressTypes().isEmpty());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleStatusType_NullEntity() {
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseSingleStatusType(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getStatusTypes());
    assertTrue(response.getBody().getStatusTypes().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleStatusType_NonNullEntity() {
    StatusTypeEntity entity = statusTypeEntities.getFirst();
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseSingleStatusType(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getStatusTypes());
    assertEquals(1, response.getBody().getStatusTypes().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    StatusTypeDto dto = response.getBody().getStatusTypes().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultipleStatusTypes_EmptyList() {
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseMultipleStatusTypes(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getStatusTypes());
    assertTrue(response.getBody().getStatusTypes().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleStatusTypes_NonEmptyList() {
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseMultipleStatusTypes(statusTypeEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getStatusTypes());
    assertEquals(3, response.getBody().getStatusTypes().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<StatusTypeDto> statusTypeDtos = response.getBody().getStatusTypes();

    Map<StatusTypeEntity, StatusTypeDto> entityDtoMap =
        statusTypeEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        statusTypeDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(
                                new StatusTypeDto(entity.getId(), null, null, null, "", "", ""))));

    for (Map.Entry<StatusTypeEntity, StatusTypeDto> entry : entityDtoMap.entrySet()) {
      StatusTypeEntity entity = entry.getKey();
      StatusTypeDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeleteStatusType() {
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseDeleteStatusType();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getStatusTypes().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorStatusType() {
    ResponseEntity<StatusTypeResponse> response =
        entityDtoConvertUtils.getResponseErrorStatusType(
            new ElementMissingException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getStatusTypes().isEmpty());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePermission_NullEntity() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePermission(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePermission_NonNullEntity() {
    PermissionEntity entity = permissionEntities.getFirst();
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePermission(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(1, response.getBody().getPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PermissionDto dto = response.getBody().getPermissions().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultiplePermissions_EmptyList() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePermissions(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePermissions_NonEmptyList() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePermissions(permissionEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(3, response.getBody().getPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PermissionDto> permissionDtos = response.getBody().getPermissions();

    Map<PermissionEntity, PermissionDto> entityDtoMap =
        permissionEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        permissionDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(new PermissionDto(entity.getId(), null, null, null, "", ""))));

    for (Map.Entry<PermissionEntity, PermissionDto> entry : entityDtoMap.entrySet()) {
      PermissionEntity entity = entry.getKey();
      PermissionDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeletePermission() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseDeletePermission();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorPermission() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseErrorPermission(
            new ElementNotActiveException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NullEntity() {
    ResponseEntity<RoleResponse> response = entityDtoConvertUtils.getResponseSingleRole(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NonNullEntity() {
    RoleEntity entity = roleEntities.getFirst();
    ResponseEntity<RoleResponse> response = entityDtoConvertUtils.getResponseSingleRole(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertEquals(1, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    RoleDto dto = response.getBody().getRoles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultipleRoles_EmptyList() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(Collections.emptyList(), Boolean.TRUE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleRoles_NonEmptyList() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(roleEntities, Boolean.TRUE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertEquals(3, response.getBody().getRoles().size());

    assertAll(
        "Roles With Permissions",
        () -> assertEquals(1, response.getBody().getRoles().get(0).getPlatformPermissions().size()),
        () -> assertEquals(1, response.getBody().getRoles().get(1).getPlatformPermissions().size()),
        () ->
            assertEquals(1, response.getBody().getRoles().get(2).getPlatformPermissions().size()));

    assertEquals(1, response.getBody().getRoles().getFirst().getPlatformPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();

    Map<RoleEntity, RoleDto> entityDtoMap =
        roleEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        roleDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(
                                new RoleDto(
                                    entity.getId(),
                                    null,
                                    null,
                                    null,
                                    "",
                                    "",
                                    Collections.emptyList()))));

    for (Map.Entry<RoleEntity, RoleDto> entry : entityDtoMap.entrySet()) {
      RoleEntity entity = entry.getKey();
      RoleDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseMultipleRoles_NonEmptyListNoPermissions() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(roleEntities, Boolean.FALSE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertEquals(3, response.getBody().getRoles().size());

    assertAll(
        "Roles Without Permissions",
        () -> assertEquals(0, response.getBody().getRoles().get(0).getPlatformPermissions().size()),
        () -> assertEquals(0, response.getBody().getRoles().get(1).getPlatformPermissions().size()),
        () ->
            assertEquals(0, response.getBody().getRoles().get(2).getPlatformPermissions().size()));

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();

    Map<RoleEntity, RoleDto> entityDtoMap =
        roleEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        roleDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(
                                new RoleDto(
                                    entity.getId(),
                                    null,
                                    null,
                                    null,
                                    "",
                                    "",
                                    Collections.emptyList()))));

    for (Map.Entry<RoleEntity, RoleDto> entry : entityDtoMap.entrySet()) {
      RoleEntity entity = entry.getKey();
      RoleDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeleteRole() {
    ResponseEntity<RoleResponse> response = entityDtoConvertUtils.getResponseDeleteRole();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorRole() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseErrorRole(
            new CheckPermissionException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NullEntity() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NonNullEntity() {
    PlatformEntity entity = platformEntities.getFirst();
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertEquals(1, response.getBody().getPlatforms().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformDto dto = response.getBody().getPlatforms().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultiplePlatforms_EmptyList() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatforms_NonEmptyList() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(platformEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertEquals(3, response.getBody().getPlatforms().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();

    Map<PlatformEntity, PlatformDto> entityDtoMap =
        platformEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        platformDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(new PlatformDto(entity.getId(), null, null, null, "", ""))));

    for (Map.Entry<PlatformEntity, PlatformDto> entry : entityDtoMap.entrySet()) {
      PlatformEntity entity = entry.getKey();
      PlatformDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeletePlatform() {
    ResponseEntity<PlatformResponse> response = entityDtoConvertUtils.getResponseDeletePlatform();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorPlatform() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseErrorPlatform(
            new JwtInvalidException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NullEntity() {
    ResponseEntity<ProfileResponse> response = entityDtoConvertUtils.getResponseSingleProfile(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NonNullEntity() {
    ProfileEntity entity = profileEntities.getFirst();
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertEquals(1, response.getBody().getProfiles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ProfileDto dto = response.getBody().getProfiles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultipleProfiles_EmptyList() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(Collections.emptyList(), Boolean.TRUE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleProfiles_NonEmptyList() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(profileEntities, Boolean.TRUE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertEquals(3, response.getBody().getProfiles().size());

    assertAll(
        "Profiles With Roles",
        () -> assertEquals(1, response.getBody().getProfiles().get(0).getPlatformRoles().size()),
        () -> assertEquals(1, response.getBody().getProfiles().get(1).getPlatformRoles().size()),
        () -> assertEquals(1, response.getBody().getProfiles().get(2).getPlatformRoles().size()));

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();

    Map<ProfileEntity, ProfileDto> entityDtoMap =
        profileEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        profileDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(
                                new ProfileDto(
                                    entity.getId(),
                                    null,
                                    null,
                                    null,
                                    "",
                                    "",
                                    "",
                                    "",
                                    false,
                                    0,
                                    null,
                                    Collections.emptyList(),
                                    null,
                                    Collections.emptyList()))));

    for (Map.Entry<ProfileEntity, ProfileDto> entry : entityDtoMap.entrySet()) {
      ProfileEntity entity = entry.getKey();
      ProfileDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseMultipleProfiles_NonEmptyListNoRoles() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(profileEntities, Boolean.FALSE);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertEquals(3, response.getBody().getProfiles().size());

    assertAll(
        "Profiles Without Roles",
        () -> assertEquals(0, response.getBody().getProfiles().get(0).getPlatformRoles().size()),
        () -> assertEquals(0, response.getBody().getProfiles().get(1).getPlatformRoles().size()),
        () -> assertEquals(0, response.getBody().getProfiles().get(2).getPlatformRoles().size()));

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();

    Map<ProfileEntity, ProfileDto> entityDtoMap =
        profileEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        profileDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(
                                new ProfileDto(
                                    entity.getId(),
                                    null,
                                    null,
                                    null,
                                    "",
                                    "",
                                    "",
                                    "",
                                    false,
                                    0,
                                    null,
                                    Collections.emptyList(),
                                    null,
                                    Collections.emptyList()))));

    for (Map.Entry<ProfileEntity, ProfileDto> entry : entityDtoMap.entrySet()) {
      ProfileEntity entity = entry.getKey();
      ProfileDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseDeleteProfile() {
    ResponseEntity<ProfileResponse> response = entityDtoConvertUtils.getResponseDeleteProfile();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorProfile() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseErrorProfile(new ProfileNotValidatedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatformProfileRole_NullEntity() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatformProfileRole(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformProfileRoles());
    assertTrue(response.getBody().getPlatformProfileRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatformProfileRole_NonNullEntity() {
    PlatformProfileRoleEntity entity = platformProfileRoleEntities.getFirst();
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatformProfileRole(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformProfileRoles());
    assertEquals(1, response.getBody().getPlatformProfileRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformProfileRoleDto dto = response.getBody().getPlatformProfileRoles().getFirst();
    assertAll(
        "Platform Profile Role",
        () -> assertEquals(entity.getId().getPlatformId(), dto.getPlatform().getId()),
        () -> assertEquals(entity.getId().getProfileId(), dto.getProfile().getId()),
        () -> assertEquals(entity.getId().getRoleId(), dto.getRole().getId()));

    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultiplePlatformProfileRoles_EmptyList() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatformProfileRoles(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformProfileRoles());
    assertTrue(response.getBody().getPlatformProfileRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatformProfileRoles_NonEmptyList() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatformProfileRoles(platformProfileRoleEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformProfileRoles());
    assertEquals(3, response.getBody().getPlatformProfileRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseDeletePlatformProfileRole() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseDeletePlatformProfileRole();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getPlatformProfileRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorPlatformProfileRole() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseErrorPlatformProfileRole(
            new RuntimeException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getPlatformProfileRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatformRolePermission_NullEntity() {
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatformRolePermission(null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformRolePermissions());
    assertTrue(response.getBody().getPlatformRolePermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatformRolePermission_NonNullEntity() {
    PlatformRolePermissionEntity entity = platformRolePermissionEntities.getFirst();
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatformRolePermission(entity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformRolePermissions());
    assertEquals(1, response.getBody().getPlatformRolePermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformRolePermissionDto dto = response.getBody().getPlatformRolePermissions().getFirst();
    assertAll(
        "Platform Role Permission",
        () -> assertEquals(entity.getId().getPlatformId(), dto.getPlatform().getId()),
        () -> assertEquals(entity.getId().getRoleId(), dto.getRole().getId()),
        () -> assertEquals(entity.getId().getPermissionId(), dto.getPermission().getId()));

    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultiplePlatformRolePermissions_EmptyList() {
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatformRolePermissions(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformRolePermissions());
    assertTrue(response.getBody().getPlatformRolePermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatformRolePermissions_NonEmptyList() {
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatformRolePermissions(
            platformRolePermissionEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatformRolePermissions());
    assertEquals(3, response.getBody().getPlatformRolePermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseDeletePlatformRolePermission() {
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseDeletePlatformRolePermission();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseCrudInfo());
    assertTrue(response.getBody().getPlatformRolePermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        1, response.getBody().getResponseMetadata().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseErrorPlatformRolePermission() {
    ResponseEntity<PlatformRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseErrorPlatformRolePermission(
            new NullPointerException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getPlatformRolePermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorProfilePassword() {
    ResponseEntity<ProfilePasswordTokenResponse> response =
        entityDtoConvertUtils.getResponseErrorProfilePassword(new ProfileNotActiveException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertNull(response.getBody().getProfile());
    assertTrue(response.getBody().getAToken() == null && response.getBody().getRToken() == null);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorResponseMetadata() {
    ResponseEntity<ResponseMetadata> response =
        entityDtoConvertUtils.getResponseErrorResponseMetadata(new ProfileLockedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo().getErrMsg());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseValidateProfile_Validated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = true;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=true", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateProfile_NotValidated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = false;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateProfile_EmptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseValidateProfile("", true));
  }

  @Test
  void testGetResponseResetProfile_Reset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = true;
    String email = "user@example.com";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetProfile(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_reset=true&to_reset=" + email,
        response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetProfile_NotReset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = false;
    String email = "";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetProfile(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(redirectUrl + "?is_reset=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetProfile_EmptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseResetProfile("", true, "some@email.com"));
  }
}
