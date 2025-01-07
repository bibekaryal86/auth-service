package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.RoleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleServiceTest extends BaseTest {

  @Autowired private RoleService roleService;

  @Test
  void testReadRoles() {
    List<RoleEntity> roleEntities = roleService.readRoles();
    assertEquals(6, roleEntities.size());
    // check sorted by name
    assertEquals("GUEST", roleEntities.getFirst().getRoleName());
    assertEquals("SUPERUSER-1", roleEntities.getLast().getRoleName());
  }

  @Test
  void testRoleService_CRUD() {
    String newName = "RoleNewName";
    String newDesc = "RoleNewDesc";
    String updatedDesc = "RoleUpdatedDesc";
    RoleRequest request = new RoleRequest(newName, newDesc);

    // Create
    Long id = assertCreate(request);

    // Update
    request = new RoleRequest(newName, updatedDesc);
    assertUpdate(id, request);

    // Read
    assertRead(id, newName, updatedDesc);

    // Soft delete
    assertDeleteSoft(id);

    // Restore
    assertRestoreSoftDeleted(id);

    // deleteHard
    assertDeleteHard(id);
  }

  private Long assertCreate(RoleRequest request) {
    RoleEntity entity = roleService.createRole(request);
    assertNotNull(entity);
    assertNotNull(entity.getId());
    return entity.getId();
  }

  private void assertUpdate(Long id, RoleRequest request) {
    RoleEntity entity = roleService.updateRole(id, request);
    assertNotNull(entity);
  }

  private void assertRead(Long id, String expectedName, String expectedDescription) {
    RoleEntity entity = roleService.readRole(id);
    assertNotNull(entity);
    assertEquals(expectedName, entity.getRoleName());
    assertEquals(expectedDescription, entity.getRoleDesc());
    assertNull(entity.getDeletedDate());
  }

  private void assertDeleteSoft(Long id) {
    RoleEntity entity = roleService.softDeleteRole(id);
    assertNotNull(entity);
    assertNotNull(entity.getDeletedDate());
  }

  private void assertRestoreSoftDeleted(Long id) {
    RoleEntity entity = roleService.restoreSoftDeletedRole(id);
    assertNotNull(entity);
    assertNull(entity.getDeletedDate());
  }

  private void assertDeleteHard(Long id) {
    roleService.hardDeleteRole(id);
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> roleService.readRole(id),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Role Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
