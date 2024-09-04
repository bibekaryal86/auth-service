package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import helper.TestData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import user.management.system.app.model.dto.AppUserAddressDto;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserAddressEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.service.AppUserRoleService;

@ExtendWith(MockitoExtension.class)
public class EntityDtoConvertUtilsTest {

  @Mock private AppUserRoleService appUserRoleService;

  @Mock private AppRolePermissionService appRolePermissionService;

  @InjectMocks private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<AppUserEntity> appUserEntities;
  private static List<AppUserRoleEntity> appUserRoleEntities;
  private static List<AppRolePermissionEntity> appRolePermissionEntities;

  @BeforeAll
  static void setUpBeforeAll() {
    appUserEntities = TestData.getAppUserEntities();
    appUserRoleEntities = TestData.getAppUserRoleEntities();
    appRolePermissionEntities = TestData.getAppRolePermissionEntities();
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
    when(appUserRoleService.readAppUserRoles(anyInt())).thenReturn(appUserRoleEntities);
    when(appRolePermissionService.readAppRolePermissions(any(), anyList()))
        .thenReturn(appRolePermissionEntities);

    AppUserEntity appUserEntity = appUserEntities.getFirst();
    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertEquals(1, response.getBody().getUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    AppUserDto appUserDto = response.getBody().getUsers().getFirst();
    assertEquals(appUserDto.getId(), appUserEntity.getId());
    assertEquals(appUserDto.getEmail(), appUserEntity.getEmail());
    assertEquals(appUserDto.getCreatedDate(), appUserEntity.getCreatedDate());

    List<AppUserAddressEntity> appUserAddressEntities = appUserEntity.getAddresses();
    List<AppUserAddressDto> appUserAddressDtos = appUserDto.getAddresses();

    assertEquals(appUserAddressEntities.size(), appUserAddressDtos.size());

    for (int i = 0; i < appUserAddressEntities.size(); i++) {
      AppUserAddressEntity appUserAddressEntity = appUserAddressEntities.get(i);
      AppUserAddressDto appUserAddressDto = appUserAddressDtos.get(i);

      assertEquals(appUserAddressEntity.getId(), appUserAddressDto.getId());
      assertEquals(appUserAddressEntity.getAddressType(), appUserAddressDto.getAddressType());
      assertEquals(appUserAddressEntity.getStreet(), appUserAddressDto.getStreet());
    }
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
    when(appUserRoleService.readAppUserRoles(anyList())).thenReturn(appUserRoleEntities);
    when(appRolePermissionService.readAppRolePermissions(eq(null), anyList()))
        .thenReturn(appRolePermissionEntities);

    ResponseEntity<AppUserResponse> response =
        entityDtoConvertUtils.getResponseMultipleAppUser(appUserEntities);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getUsers());
    assertEquals(3, response.getBody().getUsers().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    verify(appUserRoleService, times(1))
        .readAppUserRoles(appUserEntities.stream().map(AppUserEntity::getId).toList());
    verify(appRolePermissionService, times(1))
        .readAppRolePermissions(null, appUserEntities.stream().map(AppUserEntity::getId).toList());

    List<AppUserDto> appUserDtos = response.getBody().getUsers();
    for (int i = 0; i < appUserEntities.size(); i++) {
      AppUserDto appUserDto = appUserDtos.get(i);
      AppUserEntity appUserEntity = appUserEntities.get(i);

      assertEquals(appUserDto.getId(), appUserEntity.getId());
      assertEquals(appUserDto.getEmail(), appUserEntity.getEmail());
      assertEquals(appUserDto.getCreatedDate(), appUserEntity.getCreatedDate());
    }
  }
}
