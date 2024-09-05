package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.model.dto.AppRoleDto;
import user.management.system.app.model.dto.AppUserAddressDto;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.AppsDto;
import user.management.system.app.model.dto.AppsResponse;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserAddressEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.service.AppUserRoleService;

@ExtendWith(MockitoExtension.class)
public class EntityDtoConvertUtilsTest {

  @Mock private AppUserRoleService appUserRoleService;

  @Mock private AppRolePermissionService appRolePermissionService;

  @InjectMocks private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<AppsEntity> appsEntities;
  private static List<AppUserEntity> appUserEntities;
  private static List<AppUserRoleEntity> appUserRoleEntities;
  private static List<AppRolePermissionEntity> appRolePermissionEntities;

  @BeforeAll
  static void setUpBeforeAll() {
    appsEntities = TestData.getAppsEntities();
    appUserEntities = TestData.getAppUserEntities();
    appUserRoleEntities = TestData.getAppUserRoleEntities();
    appRolePermissionEntities = TestData.getAppRolePermissionEntities();
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
    assertTrue(
            entityDtoConvertUtils
                    .convertEntitiesToDtosApps(Collections.emptyList())
                    .isEmpty());
  }

  @Test
  void testConvertEntitiesToDtosApps_NonEmptyList() {
    List<AppsDto> appsDtos = entityDtoConvertUtils.convertEntitiesToDtosApps(appsEntities);
    assertNotNull(appsDtos);
    assertEquals(appsDtos.size(), appsEntities.size());

    for (int i=0; i<appsEntities.size(); i++) {
      final int finalI = i+1;
      AppsEntity appsEntity = appsEntities.stream().filter(x -> x.getId().equals("app-"+finalI)).findFirst().orElse(null);
      AppsDto appsDto = appsDtos.stream().filter(x -> x.getId().equals("app-"+finalI)).findFirst().orElse(null);
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
            entityDtoConvertUtils.getResponseErrorApps(new ElementNotFoundException("something", "anything"));

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
  void testConvertEntityToDtoAppUser_NullEntity() {
    assertNull(entityDtoConvertUtils.convertEntityToDtoAppUser(null, true));
  }

  @Test
  void testConvertEntityToDtoAppUser_NonNullEntity() {
    when(appUserRoleService.readAppUserRoles(eq(1)))
        .thenReturn(List.of(appUserRoleEntities.getFirst()));
    when(appRolePermissionService.readAppRolePermissions(any(), anyList()))
        .thenReturn(appRolePermissionEntities);

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
    when(appUserRoleService.readAppUserRoles(anyList())).thenReturn(appUserRoleEntities);
    when(appRolePermissionService.readAppRolePermissions(eq(null), anyList()))
        .thenReturn(appRolePermissionEntities);

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

    // verify services called
    verify(appUserRoleService, times(1))
        .readAppUserRoles(appUserEntities.stream().map(AppUserEntity::getId).toList());
    verify(appRolePermissionService, times(1))
        .readAppRolePermissions(null, appUserEntities.stream().map(AppUserEntity::getId).toList());
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

    verifyNoInteractions(appUserRoleService, appRolePermissionService);
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
}
