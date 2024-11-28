package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppRoleRequest;
import auth.service.app.model.entity.AppRoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    assertEquals("R Name 101", appRoleEntity.getName());
    assertEquals("R Desc 101 Updated", appRoleEntity.getDescription());
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
