package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.entity.AppRoleEntity;

public class AppRoleServiceTest extends BaseTest {

  @Autowired private AppRoleService appRoleService;

  @Test
  void testAppRoleService_CRUD() {
    int id;
    AppRoleRequest appRoleRequest = new AppRoleRequest("R Name 101", "R Desc 101");

    // create
    AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
    assertNotNull(appRoleEntity);
    assertNotNull(appRoleEntity.getId());

    id = appRoleEntity.getId();

    // update
    appRoleRequest.setDescription("R Desc 101 Updated");
    appRoleEntity = appRoleService.updateAppRole(appRoleEntity.getId(), appRoleRequest);
    assertNotNull(appRoleEntity);

    // read
    appRoleEntity = appRoleService.readAppRole(id);
    assertNotNull(appRoleEntity);
    assertEquals(appRoleEntity.getName(), "R Name 101");
    assertEquals(appRoleEntity.getDescription(), "R Desc 101 Updated");
    assertNull(appRoleEntity.getDeletedDate());

    // soft delete
    appRoleEntity = appRoleService.softDeleteAppRole(id);
    assertNotNull(appRoleEntity);
    assertNotNull(appRoleEntity.getDeletedDate());

    // restore
    appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
    assertNotNull(appRoleEntity);
    assertNull(appRoleEntity.getDeletedDate());

    // hard delete
    appRoleService.hardDeleteAppRole(id);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(ElementNotFoundException.class, () -> appRoleService.readAppRole(id));
    assertEquals(String.format("Role Not Found for [%s]", id), exception.getMessage());
  }

  @Test
  void testReadAppRoles() {
    assertEquals(7, appRoleService.readAppRoles().size());
  }
}
