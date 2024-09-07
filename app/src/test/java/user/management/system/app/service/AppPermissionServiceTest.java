package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.entity.AppPermissionEntity;

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
    assertEquals(appPermissionEntity.getAppId(), "app-99");
    assertEquals(appPermissionEntity.getName(), "P Name 101");
    assertEquals(appPermissionEntity.getDescription(), "P Desc 101 Updated");
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
    List<AppPermissionEntity> appPermissionEntities = appPermissionService.readAppPermissions();
    assertEquals(6, appPermissionEntities.size());
  }

  @Test
  void testReadAppPermissions_ByAppId() {
    List<AppPermissionEntity> appPermissionEntities =
        appPermissionService.readAppPermissions("app-99");
    assertEquals(3, appPermissionEntities.size());
  }
}
