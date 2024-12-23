package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.UserForbiddenException;
import auth.service.app.exception.UserNotAuthorizedException;
import auth.service.app.model.dto.AppPermissionDto;
import auth.service.app.model.dto.AppPermissionResponse;
import auth.service.app.model.dto.AppRoleDto;
import auth.service.app.model.dto.AppRolePermissionDto;
import auth.service.app.model.dto.AppRolePermissionResponse;
import auth.service.app.model.dto.AppRoleResponse;
import auth.service.app.model.dto.AppUserAddressDto;
import auth.service.app.model.dto.AppUserDto;
import auth.service.app.model.dto.AppUserResponse;
import auth.service.app.model.dto.AppUserRoleDto;
import auth.service.app.model.dto.AppUserRoleResponse;
import auth.service.app.model.dto.AppsAppUserDto;
import auth.service.app.model.dto.AppsAppUserResponse;
import auth.service.app.model.dto.AppsDto;
import auth.service.app.model.dto.AppsResponse;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.dto.UserLoginResponse;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppUserAddressEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

public class EntityDtoConvertUtilsTest extends BaseTest {

  @Autowired private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<AppsEntity> appsEntities;
  private static List<AppPermissionEntity> appPermissionEntities;
  private static List<AppRoleEntity> appRoleEntities;
  private static List<AppUserEntity> appUserEntities;
  private static List<AppUserRoleEntity> appUserRoleEntities;
  private static List<AppRolePermissionEntity> appRolePermissionEntities;
  private static List<AppsAppUserEntity> appsAppUserEntities;

  @BeforeAll
  static void setUp() {
    appsEntities = TestData.getAppsEntities();
    appPermissionEntities = TestData.getAppPermissionEntities();
    appRoleEntities = TestData.getAppRoleEntities();
    appUserEntities = TestData.getAppUserEntities();
    appUserRoleEntities = TestData.getAppUserRoleEntities();
    appRolePermissionEntities = TestData.getAppRolePermissionEntities();
    appsAppUserEntities = TestData.getAppsAppUserEntities();
  }

  @Test
  void testConvertEntityToDtoApps_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoApps(null));
  }

  @Test
  void testConvertEntityToDtoApps_NonNullEntity() {
    AppsEntity appsEntity = appsEntities.getFirst();
    AppsDto appsDto = entityDtoConvertUtils.convertEntityToDtoApps(appsEntity);

    assertNotNull(appsDto);
    assertEquals(appsEntity.getId(), appsDto.getId());
    assertEquals(appsEntity.getName(), appsDto.getName());
    assertEquals(appsEntity.getDescription(), appsDto.getDescription());
  }

  @Test
  void testConvertEntitiesToDtosApps_EmptyList() {
    assertTrue(entityDtoConvertUtils.convertEntitiesToDtosApps(Collections.emptyList()).isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosApps_NonEmptyList() {
    List<AppsDto> appsDtos = entityDtoConvertUtils.convertEntitiesToDtosApps(appsEntities);
    assertNotNull(appsDtos);
    assertEquals(appsDtos.size(), appsEntities.size());

    for (int i = 0; i < appsEntities.size(); i++) {
      final int finalI = i + 1;
      AppsEntity appsEntity =
          appsEntities.stream()
              .filter(x -> x.getId().equals("app-" + finalI))
              .findFirst()
              .orElse(null);
      AppsDto appsDto =
          appsDtos.stream().filter(x -> x.getId().equals("app-" + finalI)).findFirst().orElse(null);
      assertNotNull(appsEntity);
      assertNotNull(appsDto);

      assertEquals(appsEntity.getId(), appsDto.getId());
      assertEquals(appsEntity.getName(), appsDto.getName());
      assertEquals(appsEntity.getDescription(), appsDto.getDescription());
    }
  }

  @Test
  void testGetResponseErrorApps() {
    ResponseEntity<AppsResponse> response =
        entityDtoConvertUtils.getResponseErrorApps(
            new ElementNotFoundException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getApps().isEmpty());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteApps() {
    ResponseEntity<AppsResponse> response = entityDtoConvertUtils.getResponseDeleteApps();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getApps().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleApp_NullEntity() {
    ResponseEntity<AppsResponse> response = entityDtoConvertUtils.getResponseSingleApps(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getApps());
    assertTrue(response.getBody().getApps().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleApps_NonNullEntity() {
    ResponseEntity<AppsResponse> response =
        entityDtoConvertUtils.getResponseSingleApps(appsEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getApps());
    assertEquals(1, response.getBody().getApps().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleApps_EmptyList() {
    ResponseEntity<AppsResponse> response =
        entityDtoConvertUtils.getResponseMultipleApps(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getApps());
    assertTrue(response.getBody().getApps().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleApps_NonEmptyList() {
    ResponseEntity<AppsResponse> response =
        entityDtoConvertUtils.getResponseMultipleApps(appsEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getApps());
    assertEquals(3, response.getBody().getApps().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppPermission_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppPermission(null));
  }

  @Test
  void testConvertEntityToDtoAppPermission_NonNullEntity() {
    AppPermissionEntity appPermissionEntity = appPermissionEntities.getFirst();
    AppPermissionDto appPermissionDto =
        entityDtoConvertUtils.convertEntityToDtoAppPermission(appPermissionEntity);

    assertNotNull(appPermissionDto);
    assertEquals(appPermissionEntity.getId(), appPermissionDto.getId());
    assertEquals(appPermissionEntity.getName(), appPermissionDto.getName());
    assertEquals(appPermissionEntity.getDescription(), appPermissionDto.getDescription());
  }

  @Test
  void testConvertEntitiesToDtosAppPermission_EmptyList() {
    assertTrue(
        entityDtoConvertUtils
            .convertEntitiesToDtosAppPermission(Collections.emptyList())
            .isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppPermission_NonEmptyList() {
    List<AppPermissionDto> appPermissionDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppPermission(appPermissionEntities);
    assertNotNull(appPermissionDtos);
    assertEquals(appPermissionDtos.size(), appPermissionEntities.size());

    for (int i = 0; i < appPermissionEntities.size(); i++) {
      final int finalI = i + 1;
      AppPermissionEntity appPermissionEntity =
          appPermissionEntities.stream().filter(x -> x.getId() == finalI).findFirst().orElse(null);
      AppPermissionDto appPermissionDto =
          appPermissionDtos.stream().filter(x -> x.getId() == finalI).findFirst().orElse(null);
      assertNotNull(appPermissionEntity);
      assertNotNull(appPermissionDto);

      assertEquals(appPermissionEntity.getId(), appPermissionDto.getId());
      assertEquals(appPermissionEntity.getName(), appPermissionDto.getName());
      assertEquals(appPermissionEntity.getDescription(), appPermissionDto.getDescription());
    }
  }

  @Test
  void testGetResponseErrorAppPermission() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseErrorAppPermission(
            new ElementMissingException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppPermission() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseDeleteAppPermission();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppPermission_NullEntity() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseSingleAppPermission(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppPermission_NonNullEntity() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(1, response.getBody().getPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppPermission_EmptyList() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppPermission(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppPermission_NonEmptyList() {
    ResponseEntity<AppPermissionResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(3, response.getBody().getPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppRole_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppRole(null, true));
  }

  @Test
  void testConvertEntityToDtoAppRole_NonNullEntity() {
    AppRoleEntity appRoleEntity = appRoleEntities.getFirst();
    AppRoleDto appRoleDto = entityDtoConvertUtils.convertEntityToDtoAppRole(appRoleEntity, true);

    assertNotNull(appRoleDto);
    assertEquals(appRoleEntity.getId(), appRoleDto.getId());
    assertEquals(appRoleEntity.getName(), appRoleDto.getName());
    assertEquals(appRoleEntity.getDescription(), appRoleDto.getDescription());

    assertEquals(1, appRoleDto.getPermissions().size());
    assertEquals(
        appRolePermissionEntities.getFirst().getAppPermission().getId(),
        appRoleDto.getPermissions().getFirst().getId());
  }

  @Test
  void testConvertEntitiesToDtosAppRole_EmptyList() {
    assertTrue(
        entityDtoConvertUtils
            .convertEntitiesToDtosAppRole(Collections.emptyList(), true)
            .isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppRole_NonEmptyList() {
    List<AppRoleDto> appRoleDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppRole(appRoleEntities, true);
    assertNotNull(appRoleDtos);
    assertEquals(appRoleDtos.size(), appRoleEntities.size());

    for (int i = 0; i < appRoleEntities.size(); i++) {
      final int finalI = i + 1;
      AppRoleEntity appRoleEntity =
          appRoleEntities.stream().filter(x -> x.getId() == finalI).findFirst().orElse(null);
      AppRoleDto appRoleDto =
          appRoleDtos.stream().filter(x -> x.getId() == finalI).findFirst().orElse(null);
      assertNotNull(appRoleEntity);
      assertNotNull(appRoleDto);

      assertEquals(appRoleEntity.getId(), appRoleDto.getId());
      assertEquals(appRoleEntity.getName(), appRoleDto.getName());
      assertEquals(appRoleEntity.getDescription(), appRoleDto.getDescription());

      AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionEntities.stream()
              .filter(y -> Objects.equals(y.getAppRole().getId(), appRoleDtos.getFirst().getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(appRolePermissionEntity);
      assertEquals(
          appRoleDtos.getFirst().getPermissions().getFirst().getId(),
          appRolePermissionEntity.getAppPermission().getId());
    }
  }

  @Test
  void testConvertEntitiesToDtosAppRole_NonEmptyList_NotIncludeRoles() {
    List<AppRoleDto> appRoleDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppRole(appRoleEntities, false);
    assertNotNull(appRoleDtos);
    assertEquals(3, appRoleDtos.size());

    for (AppRoleDto appRoleDto : appRoleDtos) {
      assertNull(appRoleDto.getPermissions());
    }
  }

  @Test
  void testGetResponseErrorAppRole() {
    ResponseEntity<AppRoleResponse> response =
        entityDtoConvertUtils.getResponseErrorAppRole(new Exception("something or anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppRole() {
    ResponseEntity<AppRoleResponse> response = entityDtoConvertUtils.getResponseDeleteAppRole();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppRole_NullEntity() {
    ResponseEntity<AppRoleResponse> response = entityDtoConvertUtils.getResponseSingleAppRole(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppRole_NonNullEntity() {
    ResponseEntity<AppRoleResponse> response =
        entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertEquals(1, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppRole_EmptyList() {
    ResponseEntity<AppRoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppRole(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppRole_NonEmptyList() {
    ResponseEntity<AppRoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppRole(appRoleEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertEquals(3, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppUser_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppUser(null, true));
  }

  @Test
  void testConvertEntityToDtoAppUser_NonNullEntity() {
    AppUserEntity appUserEntity = appUserEntities.getFirst();
    AppUserDto appUserDto = entityDtoConvertUtils.convertEntityToDtoAppUser(appUserEntity, true);
    assertNotNull(appUserDto);

    // check dto against entity
    assertEquals(appUserDto.getId(), appUserEntity.getId());
    assertEquals(appUserDto.getEmail(), appUserEntity.getEmail());
    assertEquals(appUserDto.getCreatedDate(), appUserEntity.getCreatedDate());
    assertNull(appUserDto.getPassword());

    // check addresses
    List<AppUserAddressEntity> appUserAddressEntities = appUserEntity.getAddresses();
    List<AppUserAddressDto> appUserAddressDtos = appUserDto.getAddresses();

    assertEquals(appUserAddressEntities.size(), appUserAddressDtos.size());

    for (int i = 0; i < appUserAddressEntities.size(); i++) {
      final int finalI = i + 1;
      Optional<AppUserAddressEntity> appUserAddressEntity =
          appUserAddressEntities.stream().filter(x -> x.getId() == finalI).findFirst();
      Optional<AppUserAddressDto> appUserAddressDto =
          appUserAddressDtos.stream().filter(x -> x.getId() == finalI).findFirst();
      assertTrue(appUserAddressEntity.isPresent());
      assertTrue(appUserAddressDto.isPresent());
      assertEquals(appUserAddressEntity.get().getId(), appUserAddressDto.get().getId());
      assertEquals(
          appUserAddressEntity.get().getAddressType(), appUserAddressDto.get().getAddressType());
      assertEquals(appUserAddressEntity.get().getStreet(), appUserAddressDto.get().getStreet());
    }

    // check roles and permissions
    List<AppRoleDto> appRoleDtos = appUserDto.getRoles();
    assertEquals(1, appRoleDtos.size());
    assertEquals(
        appUserRoleEntities.getFirst().getAppRole().getId(), appRoleDtos.getFirst().getId());
    assertEquals(1, appRoleDtos.getFirst().getPermissions().size());
    assertEquals(
        appRolePermissionEntities.getFirst().getAppPermission().getId(),
        appRoleDtos.getFirst().getPermissions().getFirst().getId());
  }

  @Test
  void testConvertEntitiesToDtosAppUser_EmptyList() {
    assertTrue(
        entityDtoConvertUtils
            .convertEntitiesToDtosAppUser(Collections.emptyList(), true)
            .isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppUser_NonEmptyList() {
    List<AppUserDto> appUserDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppUser(appUserEntities, true);

    assertNotNull(appUserDtos);
    assertEquals(3, appUserDtos.size());

    for (int i = 0; i < appUserEntities.size(); i++) {
      final int finalI = i + 1;
      Optional<AppUserEntity> appUserEntity =
          appUserEntities.stream().filter(x -> x.getId() == finalI).findFirst();
      Optional<AppUserDto> appUserDto =
          appUserDtos.stream().filter(x -> x.getId() == finalI).findFirst();

      assertTrue(appUserEntity.isPresent());
      assertTrue(appUserDto.isPresent());
      assertEquals(appUserEntity.get().getId(), appUserDto.get().getId());
      assertEquals(appUserEntity.get().getEmail(), appUserDto.get().getEmail());
      assertEquals(appUserEntity.get().getCreatedDate(), appUserDto.get().getCreatedDate());
      assertNull(appUserDto.get().getPassword());

      // check addresses
      List<AppUserAddressEntity> appUserAddressEntities = appUserEntity.get().getAddresses();
      List<AppUserAddressDto> appUserAddressDtos = appUserDto.get().getAddresses();

      if (CollectionUtils.isEmpty(appUserAddressEntities)) {
        assertNull(appUserAddressDtos);
      } else {
        assertEquals(appUserAddressEntities.size(), appUserAddressDtos.size());

        Optional<AppUserAddressEntity> appUserAddressEntity =
            appUserAddressEntities.stream().filter(x -> x.getId() == finalI).findFirst();
        Optional<AppUserAddressDto> appUserAddressDto =
            appUserAddressDtos.stream().filter(y -> y.getId() == finalI).findFirst();

        // adjustment for app user with id = 2, because address has id of 3
        if (finalI == 2 && (appUserAddressEntity.isEmpty() && appUserAddressDto.isEmpty())) {
          appUserAddressEntity =
              appUserAddressEntities.stream().filter(x -> x.getId() == finalI + 1).findFirst();
          appUserAddressDto =
              appUserAddressDtos.stream().filter(y -> y.getId() == finalI + 1).findFirst();
        }

        assertTrue(appUserAddressEntity.isPresent());
        assertTrue(appUserAddressDto.isPresent());
        assertEquals(appUserAddressEntity.get().getId(), appUserAddressDto.get().getId());
        assertEquals(
            appUserAddressEntity.get().getAddressType(), appUserAddressDto.get().getAddressType());
        assertEquals(appUserAddressEntity.get().getStreet(), appUserAddressDto.get().getStreet());
      }

      // check roles and permissions
      List<AppRoleDto> appRoleDtos = appUserDto.get().getRoles();
      assertEquals(1, appRoleDtos.size());
      assertEquals(1, appRoleDtos.getFirst().getPermissions().size());

      AppUserRoleEntity appUserRoleEntity =
          appUserRoleEntities.stream()
              .filter(x -> Objects.equals(x.getAppUser().getId(), appUserDto.get().getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(appUserRoleEntity);
      assertEquals(appRoleDtos.getFirst().getId(), appUserRoleEntity.getAppRole().getId());

      AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionEntities.stream()
              .filter(y -> Objects.equals(y.getAppRole().getId(), appRoleDtos.getFirst().getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(appRolePermissionEntity);
      assertEquals(
          appRoleDtos.getFirst().getPermissions().getFirst().getId(),
          appRolePermissionEntity.getAppPermission().getId());
    }
  }

  @Test
  void testConvertEntitiesToDtosAppUser_NonEmptyList_NotIncludeRoles() {
    List<AppUserDto> appUserDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppUser(appUserEntities, false);
    assertNotNull(appUserDtos);
    assertEquals(3, appUserDtos.size());

    for (AppUserDto appUserDto : appUserDtos) {
      assertNull(appUserDto.getRoles());
    }
  }

  @Test
  void testGetResponseErrorAppUser() {
    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseErrorAppUser(new UserNotAuthorizedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getUsers().isEmpty());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppUser() {
    ResponseEntity<AppUserResponse> response = entityDtoConvertUtils.getResponseDeleteAppUser();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getUsers().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppUser_NullEntity() {
    ResponseEntity<AppUserResponse> response = entityDtoConvertUtils.getResponseSingleAppUser(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertTrue(response.getBody().getUsers().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppUser_NonNullEntity() {
    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseSingleAppUser(appUserEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertEquals(1, response.getBody().getUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppUser_EmptyList() {
    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppUser(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertTrue(response.getBody().getUsers().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppUser_NonEmptyList() {
    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppUser(appUserEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertEquals(3, response.getBody().getUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppRolePermission_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppRolePermission(null));
  }

  @Test
  void testConvertEntityToDtoAppRolePermission_NonNullEntity() {
    AppRolePermissionEntity appRolePermissionEntity = appRolePermissionEntities.getFirst();
    AppRolePermissionDto appRolePermissionDto =
        entityDtoConvertUtils.convertEntityToDtoAppRolePermission(appRolePermissionEntity);
    assertNotNull(appRolePermissionDto);

    // check dto against entity
    assertEquals(appRolePermissionDto.getAssignedDate(), appRolePermissionEntity.getAssignedDate());
    assertEquals(
        appRolePermissionDto.getRole().getId(), appRolePermissionEntity.getAppRole().getId());
    assertEquals(
        appRolePermissionDto.getPermission().getId(),
        appRolePermissionEntity.getAppPermission().getId());
  }

  @Test
  void testConvertEntitiesToDtosAppRolePermission_EmptyList() {
    assertTrue(
        entityDtoConvertUtils
            .convertEntitiesToDtosAppRolePermission(Collections.emptyList())
            .isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppRolePermission_NonEmptyList() {
    List<AppRolePermissionDto> appRolePermissionDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppRolePermission(appRolePermissionEntities);

    assertNotNull(appRolePermissionDtos);
    assertEquals(3, appRolePermissionDtos.size());

    for (int i = 0; i < appRolePermissionEntities.size(); i++) {
      final int finalI = i + 1;
      Optional<AppRolePermissionEntity> appRolePermissionEntity =
          appRolePermissionEntities.stream()
              .filter(x -> x.getAppRole().getId() == finalI)
              .findFirst();
      Optional<AppRolePermissionDto> appRolePermissionDto =
          appRolePermissionDtos.stream().filter(x -> x.getRole().getId() == finalI).findFirst();

      assertTrue(appRolePermissionEntity.isPresent());
      assertTrue(appRolePermissionDto.isPresent());
      assertEquals(
          appRolePermissionEntity.get().getAppRole().getId(),
          appRolePermissionDto.get().getRole().getId());
      assertEquals(
          appRolePermissionEntity.get().getAppPermission().getId(),
          appRolePermissionDto.get().getPermission().getId());
      assertEquals(
          appRolePermissionEntity.get().getAssignedDate(),
          appRolePermissionDto.get().getAssignedDate());
    }
  }

  @Test
  void testGetResponseErrorAppRolePermission() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseErrorAppRolePermission(
            new NullPointerException("something was null"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getRolesPermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppRolePermission() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseDeleteAppRolePermission();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getRolesPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppRolePermission_NullEntity() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseSingleAppRolePermission(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRolesPermissions());
    assertTrue(response.getBody().getRolesPermissions().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppRolePermission_NonNullEntity() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseSingleAppRolePermission(
            appRolePermissionEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRolesPermissions());
    assertEquals(1, response.getBody().getRolesPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppRolePermission_EmptyList() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppRolePermission(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRolesPermissions());
    assertTrue(response.getBody().getRolesPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppRolePermission_NonEmptyList() {
    ResponseEntity<AppRolePermissionResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRolesPermissions());
    assertEquals(3, response.getBody().getRolesPermissions().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppUserRole_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppUserRole(null));
  }

  @Test
  void testConvertEntityToDtoAppUserRole_NonNullEntity() {
    AppUserRoleEntity appUserRoleEntity = appUserRoleEntities.getFirst();
    AppUserRoleDto appUserRoleDto =
        entityDtoConvertUtils.convertEntityToDtoAppUserRole(appUserRoleEntity);
    assertNotNull(appUserRoleDto);

    // check dto against entity
    assertEquals(appUserRoleDto.getAssignedDate(), appUserRoleEntity.getAssignedDate());
    assertEquals(appUserRoleDto.getRole().getId(), appUserRoleEntity.getAppRole().getId());
    assertEquals(appUserRoleDto.getUser().getId(), appUserRoleEntity.getAppUser().getId());

    // check permissions
    assertEquals(1, appUserRoleDto.getRole().getPermissions().size());
    assertEquals(
        appRolePermissionEntities.getFirst().getAppPermission().getId(),
        appUserRoleDto.getRole().getPermissions().getFirst().getId());
  }

  @Test
  void testConvertEntitiesToDtosAppUserRole_EmptyList() {
    assertTrue(
        entityDtoConvertUtils.convertEntitiesToDtosAppUserRole(Collections.emptyList()).isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppUserRole_NonEmptyList() {
    List<AppUserRoleDto> appUserRoleDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppUserRole(appUserRoleEntities);

    assertNotNull(appUserRoleDtos);
    assertEquals(3, appUserRoleDtos.size());

    for (int i = 0; i < appUserRoleEntities.size(); i++) {
      final int finalI = i + 1;
      AppUserRoleEntity appUserRoleEntity =
          appUserRoleEntities.stream()
              .filter(x -> x.getAppRole().getId() == finalI)
              .findFirst()
              .orElse(null);
      AppUserRoleDto appUserRoleDto =
          appUserRoleDtos.stream()
              .filter(x -> x.getRole().getId() == finalI)
              .findFirst()
              .orElse(null);

      assertNotNull(appUserRoleEntity);
      assertNotNull(appUserRoleDto);
      assertEquals(appUserRoleEntity.getAppRole().getId(), appUserRoleDto.getRole().getId());
      assertEquals(appUserRoleEntity.getAppUser().getId(), appUserRoleDto.getUser().getId());
      assertEquals(appUserRoleEntity.getAssignedDate(), appUserRoleDto.getAssignedDate());

      AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionEntities.stream()
              .filter(y -> Objects.equals(y.getAppRole().getId(), appUserRoleDto.getRole().getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(appRolePermissionEntity);
      assertEquals(
          appUserRoleDto.getRole().getPermissions().getFirst().getId(),
          appRolePermissionEntity.getAppPermission().getId());
    }
  }

  @Test
  void testGetResponseErrorAppUserRole() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseErrorAppUserRole(
            new NullPointerException("something was null"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getUsersRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppUserRole() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseDeleteAppUserRole();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getUsersRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppUserRole_NullEntity() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseSingleAppUserRole(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsersRoles());
    assertTrue(response.getBody().getUsersRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppUserRole_NonNullEntity() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsersRoles());
    assertEquals(1, response.getBody().getUsersRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppUserRole_EmptyList() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppUserRole(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsersRoles());
    assertTrue(response.getBody().getUsersRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppUserRole_NonEmptyList() {
    ResponseEntity<AppUserRoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsersRoles());
    assertEquals(3, response.getBody().getUsersRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testConvertEntityToDtoAppsAppUser_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppsAppUser(null));
  }

  @Test
  void testConvertEntityToDtoAppsAppUser_NonNullEntity() {
    AppsAppUserEntity appsAppUserEntity = appsAppUserEntities.getFirst();
    AppsAppUserDto appsAppUserDto =
        entityDtoConvertUtils.convertEntityToDtoAppsAppUser(appsAppUserEntity);
    assertNotNull(appsAppUserDto);

    // check dto against entity
    assertEquals(appsAppUserDto.getAssignedDate(), appsAppUserEntity.getAssignedDate());
    assertEquals(appsAppUserDto.getApp().getId(), appsAppUserEntity.getApp().getId());
    assertEquals(appsAppUserDto.getUser().getId(), appsAppUserEntity.getAppUser().getId());
  }

  @Test
  void testConvertEntitiesToDtosAppsAppUser_EmptyList() {
    assertTrue(
        entityDtoConvertUtils.convertEntitiesToDtosAppsAppUser(Collections.emptyList()).isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosAppsAppUser_NonEmptyList() {
    List<AppsAppUserDto> appsAppUserDtos =
        entityDtoConvertUtils.convertEntitiesToDtosAppsAppUser(appsAppUserEntities);

    assertNotNull(appsAppUserDtos);
    assertEquals(3, appsAppUserDtos.size());

    for (int i = 0; i < appsAppUserEntities.size(); i++) {
      final int finalI = i + 1;
      AppsAppUserEntity appsAppUserEntity =
          appsAppUserEntities.stream()
              .filter(x -> x.getAppUser().getId() == finalI)
              .findFirst()
              .orElse(null);
      AppsAppUserDto appsAppUserDto =
          appsAppUserDtos.stream()
              .filter(x -> x.getUser().getId() == finalI)
              .findFirst()
              .orElse(null);

      assertNotNull(appsAppUserEntity);
      assertNotNull(appsAppUserDto);
      assertEquals(appsAppUserEntity.getApp().getId(), appsAppUserDto.getApp().getId());
      assertEquals(appsAppUserEntity.getAppUser().getId(), appsAppUserDto.getUser().getId());
      assertEquals(appsAppUserEntity.getAssignedDate(), appsAppUserDto.getAssignedDate());
    }
  }

  @Test
  void testGetResponseErrorAppsAppUser() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseErrorAppsAppUser(
            new NullPointerException("something was null"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertTrue(response.getBody().getAppsUsers().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseDeleteAppsAppUser() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseDeleteAppsAppUser();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseCrudInfo());
    assertTrue(response.getBody().getAppsUsers().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getResponseCrudInfo().getDeletedRowsCount());
  }

  @Test
  void testGetResponseSingleAppsAppUser_NullEntity() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseSingleAppsAppUser(null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAppsUsers());
    assertTrue(response.getBody().getAppsUsers().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleAppsAppUser_NonNullEntity() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseSingleAppsAppUser(appsAppUserEntities.getFirst());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAppsUsers());
    assertEquals(1, response.getBody().getAppsUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppsAppUser_EmptyList() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppsAppUser(Collections.emptyList());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAppsUsers());
    assertTrue(response.getBody().getAppsUsers().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleAppsAppUser_NonEmptyList() {
    ResponseEntity<AppsAppUserResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppsAppUser(appsAppUserEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getAppsUsers());
    assertEquals(3, response.getBody().getAppsUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorAppUserLogin() {
    ResponseEntity<UserLoginResponse> response =
        entityDtoConvertUtils.getResponseErrorAppUserLogin(new UserNotAuthorizedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo());
    assertNull(response.getBody().getAToken());
    assertNull(response.getBody().getRToken());
    assertNull(response.getBody().getUser());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorResponseStatusInfo() {
    ResponseEntity<ResponseStatusInfo> response =
        entityDtoConvertUtils.getResponseErrorResponseStatusInfo(new UserForbiddenException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getErrMsg());
    assertNull(response.getBody().getMessage());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseValidateUser_Validated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = true;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateUser(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=true", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateUser_NotValidated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = false;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateUser(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateUser_EmptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class, () -> entityDtoConvertUtils.getResponseValidateUser("", true));
  }

  @Test
  void testGetResponseResetUser_Reset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = true;
    String email = "user@example.com";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetUser(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_reset=true&to_reset=" + email,
        response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetUser_NotReset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = false;
    String email = "";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetUser(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(redirectUrl + "?is_reset=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetUser_EmptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseResetUser("", true, "some@email.com"));
  }
}
