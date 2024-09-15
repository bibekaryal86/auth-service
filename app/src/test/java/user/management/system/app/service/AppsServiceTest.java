package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.entity.AppsEntity;

public class AppsServiceTest extends BaseTest {

  @Autowired private AppsService appsService;

  @Test
  void testAppsService_CRUD() {
    String id;
    AppsRequest appsRequest = new AppsRequest("A Name 101", "A Desc 101");

    // create
    AppsEntity appRoleEntity = appsService.createApp(appsRequest);
    assertNotNull(appRoleEntity);
    assertNotNull(appRoleEntity.getId());

    id = appRoleEntity.getId();

    // update
    appsRequest.setDescription("A Desc 101 Updated");
    appRoleEntity = appsService.updateApps(appRoleEntity.getId(), appsRequest);
    assertNotNull(appRoleEntity);

    // read
    appRoleEntity = appsService.readApp(id);
    assertNotNull(appRoleEntity);
    assertEquals("A Name 101", appRoleEntity.getName());
    assertEquals("A Desc 101 Updated", appRoleEntity.getDescription());
    assertNull(appRoleEntity.getDeletedDate());

    // soft delete
    appRoleEntity = appsService.softDeleteApps(id);
    assertNotNull(appRoleEntity);
    assertNotNull(appRoleEntity.getDeletedDate());

    // restore
    appRoleEntity = appsService.restoreSoftDeletedApps(id);
    assertNotNull(appRoleEntity);
    assertNull(appRoleEntity.getDeletedDate());

    // hard delete
    appsService.hardDeleteApps(id);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(ElementNotFoundException.class, () -> appsService.readApp(id));
    assertEquals(String.format("App Not Found for [%s]", id), exception.getMessage());
  }

  @Test
  void testReadApps() {
    assertEquals(4, appsService.readApps().size());
  }
}
