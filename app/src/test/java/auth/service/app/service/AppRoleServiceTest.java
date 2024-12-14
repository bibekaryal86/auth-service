package auth.service.app.service;

import auth.service.BaseTest;

public class AppRoleServiceTest extends BaseTest {

  //  @Autowired private AppRoleService appRoleService;
  //
  //  @Test
  //  void testAppRoleService_CRUD() {
  //    int id;
  //    AppRoleRequest appRoleRequest = new AppRoleRequest("R Name 101", "R Desc 101");
  //
  //    // create
  //    AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
  //    assertNotNull(appRoleEntity);
  //    assertNotNull(appRoleEntity.getId());
  //
  //    id = appRoleEntity.getId();
  //
  //    // update
  //    appRoleRequest.setDescription("R Desc 101 Updated");
  //    appRoleEntity = appRoleService.updateAppRole(appRoleEntity.getId(), appRoleRequest);
  //    assertNotNull(appRoleEntity);
  //
  //    // read
  //    appRoleEntity = appRoleService.readAppRole(id);
  //    assertNotNull(appRoleEntity);
  //    assertEquals("R Name 101", appRoleEntity.getName());
  //    assertEquals("R Desc 101 Updated", appRoleEntity.getDescription());
  //    assertNull(appRoleEntity.getDeletedDate());
  //
  //    // soft delete
  //    appRoleEntity = appRoleService.softDeleteAppRole(id);
  //    assertNotNull(appRoleEntity);
  //    assertNotNull(appRoleEntity.getDeletedDate());
  //
  //    // restore
  //    appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
  //    assertNotNull(appRoleEntity);
  //    assertNull(appRoleEntity.getDeletedDate());
  //
  //    // hard delete
  //    appRoleService.hardDeleteAppRole(id);
  //
  //    // throws not found exception after delete
  //    ElementNotFoundException exception =
  //        assertThrows(ElementNotFoundException.class, () -> appRoleService.readAppRole(id));
  //    assertEquals(String.format("Role Not Found for [%s]", id), exception.getMessage());
  //  }
  //
  //  @Test
  //  void testReadAppRoles() {
  //    assertEquals(7, appRoleService.readAppRoles().size());
  //  }
}
