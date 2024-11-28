package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppPermissionRequest;
import auth.service.app.model.entity.AppPermissionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppPermissionServiceTest extends BaseTest {

  @Autowired private AppPermissionService appPermissionService;

  @Test
  void testAppPermissionService_CRUD() {
    int id;
    AppPermissionRequest appPermissionRequest =
        new AppPermissionRequest("P Name 101", "P Desc 101");

    // create
    AppPermissionEntity appPermissionEntity =
        appPermissionService.createAppPermission("app-99", appPermissionRequest);
    assertNotNull(appPermissionEntity);
    assertNotNull(appPermissionEntity.getId());

    id = appPermissionEntity.getId();

    // update
    appPermissionRequest.setDescription("P Desc 101 Updated");
    appPermissionEntity =
        appPermissionService.updateAppPermission(appPermissionEntity.getId(), appPermissionRequest);
    assertNotNull(appPermissionEntity);

    // read
    appPermissionEntity = appPermissionService.readAppPermission(id);
    assertNotNull(appPermissionEntity);
    assertEquals("app-99", appPermissionEntity.getAppId());
    assertEquals("P Name 101", appPermissionEntity.getName());
    assertEquals("P Desc 101 Updated", appPermissionEntity.getDescription());
    assertNull(appPermissionEntity.getDeletedDate());

    // soft delete
    appPermissionEntity = appPermissionService.softDeleteAppPermission(id);
    assertNotNull(appPermissionEntity);
    assertNotNull(appPermissionEntity.getDeletedDate());

    // restore
    appPermissionEntity = appPermissionService.restoreSoftDeletedAppPermission(id);
    assertNotNull(appPermissionEntity);
    assertNull(appPermissionEntity.getDeletedDate());

    // hard delete
    appPermissionService.hardDeleteAppPermission(id);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class, () -> appPermissionService.readAppPermission(id));
    assertEquals(String.format("Permission Not Found for [%s]", id), exception.getMessage());
  }

  @Test
  void testReadAppPermissions() {
    assertEquals(6, appPermissionService.readAppPermissions().size());
  }

  @Test
  void testReadAppPermissions_ByAppId() {
    assertEquals(3, appPermissionService.readAppPermissions("app-99").size());
  }
}
