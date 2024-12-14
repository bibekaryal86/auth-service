package auth.service.app.service;

import auth.service.BaseTest;

public class AppUserRoleServiceTest extends BaseTest {

  //  @Autowired private AppUserRoleService appUserRoleService;
  //
  //  @Test
  //  void testAppUserRoleService_CRUD() {
  //    int userId = 6;
  //    int roleId = 6;
  //
  //    AppUserRoleRequest appUserRoleRequest = new AppUserRoleRequest(userId, roleId);
  //
  //    // create
  //    AppUserRoleEntity appsAppUserEntity =
  // appUserRoleService.createAppUserRole(appUserRoleRequest);
  //    assertNotNull(appsAppUserEntity);
  //    assertNotNull(appsAppUserEntity.getId());
  //
  //    // update, not available
  //
  //    // read
  //    appsAppUserEntity = appUserRoleService.readAppUserRole(userId, roleId);
  //    assertNotNull(appsAppUserEntity);
  //    assertNotNull(appsAppUserEntity.getId());
  //    assertEquals("First Ninety Nine3", appsAppUserEntity.getAppUser().getFirstName());
  //    assertEquals("Role V", appsAppUserEntity.getAppRole().getName());
  //    assertNotNull(appsAppUserEntity.getAssignedDate());
  //
  //    // hard delete
  //    appUserRoleService.deleteAppUserRole(userId, roleId);
  //
  //    // throws not found exception after delete
  //    ElementNotFoundException exception =
  //        assertThrows(
  //            ElementNotFoundException.class,
  //            () -> appUserRoleService.readAppUserRole(userId, roleId));
  //    assertEquals(
  //        String.format("App User Role Not Found for [%s,%s]", userId, roleId),
  //        exception.getMessage());
  //  }
  //
  //  @Test
  //  void testReadAppUserRoles() {
  //    assertEquals(6, appUserRoleService.readAppUserRoles().size());
  //  }
  //
  //  @Test
  //  void testReadAppUserRoles_ByAppUserId() {
  //    assertEquals(3, appUserRoleService.readAppUserRoles(4).size());
  //  }
  //
  //  @Test
  //  void testReadAppUserRoles_ByAppUserIds() {
  //    assertEquals(4, appUserRoleService.readAppUserRoles(List.of(1, 4)).size());
  //  }
}
