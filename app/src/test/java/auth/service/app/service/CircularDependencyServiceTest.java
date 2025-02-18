package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.AddressTypeRepository;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import helper.TestData;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class CircularDependencyServiceTest extends BaseTest {

  @MockitoBean private AddressTypeRepository addressTypeRepository;
  @MockitoBean private PermissionRepository permissionRepository;
  @MockitoBean private PlatformRepository platformRepository;
  @MockitoBean private RoleRepository roleRepository;
  @MockitoBean private ProfileRepository profileRepository;

  @Autowired private CircularDependencyService circularDependencyService;

  private static List<AddressTypeEntity> addressTypeEntities;
  private static List<PermissionEntity> permissionEntities;
  private static List<PlatformEntity> platformEntities;
  private static List<RoleEntity> roleEntities;
  private static ProfileEntity profileEntity;

  @BeforeAll
  public static void setUpBeforeAll() {
    addressTypeEntities = TestData.getAddressTypeEntities();
    permissionEntities = TestData.getPermissionEntities();
    platformEntities = TestData.getPlatformEntities();
    roleEntities = TestData.getRoleEntities();
    profileEntity = TestData.getProfileEntities().getFirst();
  }

  @AfterEach
  void tearDown() {
    reset(addressTypeRepository);
    reset(permissionRepository);
    reset(platformRepository);
    reset(roleRepository);
    reset(profileRepository);
  }

  @Test
  void testReadAddressType_Service() {
    AddressTypeEntity result = circularDependencyService.readAddressType(ID);

    assertNotNull(result);
    assertEquals(addressTypeEntities.getFirst(), result);
    verifyNoInteractions(addressTypeRepository);
  }

  @Test
  void testReadAddressType_Repository() {
    when(addressTypeRepository.findById(ID))
        .thenReturn(Optional.of(addressTypeEntities.getFirst()));

    AddressTypeEntity result = circularDependencyService.readAddressType(ID);

    assertNotNull(result);
    assertEquals(addressTypeEntities.getFirst(), result);
    verify(addressTypeRepository).findById(ID);
  }

  @Test
  void testReadAddressType_Exception() {
    when(addressTypeRepository.findById(ID)).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class, () -> circularDependencyService.readAddressType(ID));

    assertEquals(String.format("Address Type Not Found for [%s]", ID), exception.getMessage());
    verify(addressTypeRepository).findById(ID);
  }

  @Test
  void testReadPermission_Service() {
    PermissionEntity result = circularDependencyService.readPermission(ID);

    assertNotNull(result);
    assertEquals(permissionEntities.getFirst(), result);
    verifyNoInteractions(permissionRepository);
  }

  @Test
  void testReadPermission_Repository() {
    when(permissionRepository.findById(ID)).thenReturn(Optional.of(permissionEntities.getFirst()));

    PermissionEntity result = circularDependencyService.readPermission(ID);

    assertNotNull(result);
    assertEquals(permissionEntities.getFirst(), result);
    verify(permissionRepository).findById(ID);
  }

  @Test
  void testReadPermission_Exception() {
    when(permissionRepository.findById(ID)).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class, () -> circularDependencyService.readPermission(ID));

    assertEquals(String.format("Permission Not Found for [%s]", ID), exception.getMessage());
    verify(permissionRepository).findById(ID);
  }

  @Test
  void testReadPlatform_Service() {
    PlatformEntity result = circularDependencyService.readPlatform(ID);

    assertNotNull(result);
    assertEquals(platformEntities.getFirst(), result);
    verifyNoInteractions(platformRepository);
  }

  @Test
  void testReadPlatform_Repository() {
    when(platformRepository.findById(ID)).thenReturn(Optional.of(platformEntities.getFirst()));

    PlatformEntity result = circularDependencyService.readPlatform(ID);

    assertNotNull(result);
    assertEquals(platformEntities.getFirst(), result);
    verify(platformRepository).findById(ID);
  }

  @Test
  void testReadPlatform_Exception() {
    when(platformRepository.findById(ID)).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class, () -> circularDependencyService.readPlatform(ID));

    assertEquals(String.format("Platform Not Found for [%s]", ID), exception.getMessage());
    verify(platformRepository).findById(ID);
  }

  @Test
  void testReadRole_Service() {
    RoleEntity result = circularDependencyService.readRole(ID);

    assertNotNull(result);
    assertEquals(roleEntities.getFirst(), result);
    verifyNoInteractions(roleRepository);
  }

  @Test
  void testReadRole_Repository() {
    when(roleRepository.findById(ID)).thenReturn(Optional.of(roleEntities.getFirst()));

    RoleEntity result = circularDependencyService.readRole(ID);

    assertNotNull(result);
    assertEquals(roleEntities.getFirst(), result);
    verify(roleRepository).findById(ID);
  }

  @Test
  void testReadRole_Exception() {
    when(roleRepository.findById(ID)).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(ElementNotFoundException.class, () -> circularDependencyService.readRole(ID));

    assertEquals(String.format("Role Not Found for [%s]", ID), exception.getMessage());
    verify(roleRepository).findById(ID);
  }

  @Test
  void testReadRoleByName_Service() {
    RoleEntity result = circularDependencyService.readRoleByName("SUPERUSER");

    assertNotNull(result);
    assertEquals(roleEntities.getFirst(), result);
    verifyNoInteractions(roleRepository);
  }

  @Test
  void testReadRoleByName_Repository() {
    when(roleRepository.findOne(any())).thenReturn(Optional.of(roleEntities.getFirst()));

    RoleEntity result = circularDependencyService.readRoleByName("SUPERUSER");

    assertNotNull(result);
    assertEquals(roleEntities.getFirst(), result);
    verify(roleRepository).findOne(any());
  }

  @Test
  void testReadRoleByName_Exception() {
    when(roleRepository.findOne(any())).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readRoleByName("SUPERUSER"));

    assertEquals("Role Not Found for [SUPERUSER]", exception.getMessage());
    verify(roleRepository).findOne(any());
  }

  @Test
  void testReadProfile_Repository() {
    when(profileRepository.findById(ID)).thenReturn(Optional.of(profileEntity));

    ProfileEntity result = circularDependencyService.readProfile(ID);

    assertNotNull(result);
    assertEquals(profileEntity, result);
    verify(profileRepository).findById(ID);
  }

  @Test
  void testReadProfile_Exception() {
    when(profileRepository.findById(ID)).thenReturn(Optional.empty());

    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class, () -> circularDependencyService.readProfile(ID));

    assertEquals(String.format("Profile Not Found for [%s]", ID), exception.getMessage());
    verify(profileRepository).findById(ID);
  }
}
