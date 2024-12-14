package auth.service.app.service;

import auth.service.BaseTest;

public class AppRolePermissionServiceTest extends BaseTest {

  //  @Autowired private AppRolePermissionService appRolePermissionService;
  //
  //  @Test
  //  void testAppRolePermissionService_CRUD() {
  //    int roleId = 6;
  //    int permissionId = 6;
  //    AppRolePermissionRequest appRolePermissionRequest =
  //        new AppRolePermissionRequest(roleId, permissionId);
  //
  //    // create
  //    AppRolePermissionEntity appRolePermissionEntity =
  //        appRolePermissionService.createAppRolePermission(appRolePermissionRequest);
  //    assertNotNull(appRolePermissionEntity);
  //    assertNotNull(appRolePermissionEntity.getId());
  //
  //    // update, not available
  //
  //    // read
  //    appRolePermissionEntity = appRolePermissionService.readAppRolePermission(roleId,
  // permissionId);
  //    assertNotNull(appRolePermissionEntity);
  //    assertNotNull(appRolePermissionEntity.getId());
  //    assertEquals("Role V", appRolePermissionEntity.getAppRole().getName());
  //    assertEquals("Permission V", appRolePermissionEntity.getAppPermission().getName());
  //    assertNotNull(appRolePermissionEntity.getAssignedDate());
  //
  //    // hard delete
  //    appRolePermissionService.deleteAppRolePermission(roleId, permissionId);
  //
  //    // throws not found exception after delete
  //    ElementNotFoundException exception =
  //        assertThrows(
  //            ElementNotFoundException.class,
  //            () -> appRolePermissionService.readAppRolePermission(roleId, permissionId));
  //    assertEquals("App Role Permission Not Found for [6,6]", exception.getMessage());
  //  }
  //
  //  @Test
  //  void testReadAppRolePermissions() {
  //    assertEquals(6, appRolePermissionService.readAppRolePermissions().size());
  //  }
  //
  //  @Test
  //  void testReadAppRolePermissions_ByAppRoleId() {
  //    assertEquals(3, appRolePermissionService.readAppRolePermissions(4).size());
  //  }
  //
  //  @Test
  //  void testReadAppRolePermissions_ByAppIdAndAppRoleIds() {
  //    assertEquals(
  //        3, appRolePermissionService.readAppRolePermissions("app-99", List.of(1, 4)).size());
  //  }
  //
  //  @Test
  //  void testReadAppRolePermissions_ByAppRoleIds() {
  //    assertEquals(4, appRolePermissionService.readAppRolePermissions("", List.of(1, 4)).size());
  //  }
}
