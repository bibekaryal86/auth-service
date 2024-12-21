package auth.service.app.service;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.entity.PermissionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionServiceTest extends BaseTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    void testReadPermissions() {
        List<PermissionEntity> permissionEntities = permissionService.readPermissions();
        assertEquals(6, permissionEntities.size());
        // check sorted by name
        assertEquals("PERMISSION_CREATE", permissionEntities.getFirst().getPermissionName());
        assertEquals("PERMISSION_UPDATE-1", permissionEntities.getLast().getPermissionName());
    }

    @Test
    void testPermissionService_CRUD() {
        String newName = "PermissionNewName";
        String newDesc = "PermissionNewDesc";
        String updatedDesc = "PermissionUpdatedDesc";
        PermissionRequest request = new PermissionRequest(newName, newDesc);

        // Create
        Long id = assertCreate(request);

        // Update
        request = new PermissionRequest(newName, updatedDesc);
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

    private Long assertCreate(PermissionRequest request) {
        PermissionEntity entity = permissionService.createPermission(request);
        assertNotNull(entity);
        assertNotNull(entity.getId());
        return entity.getId();
    }

    private void assertUpdate(Long id, PermissionRequest request) {
        PermissionEntity entity = permissionService.updatePermission(id, request);
        assertNotNull(entity);
    }

    private void assertRead(Long id, String expectedName, String expectedDescription) {
        PermissionEntity entity = permissionService.readPermission(id);
        assertNotNull(entity);
        assertEquals(expectedName, entity.getPermissionName());
        assertEquals(expectedDescription, entity.getPermissionDesc());
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteSoft(Long id) {
        PermissionEntity entity = permissionService.softDeletePermission(id);
        assertNotNull(entity);
        assertNotNull(entity.getDeletedDate());
    }

    private void assertRestoreSoftDeleted(Long id) {
        PermissionEntity entity = permissionService.restoreSoftDeletedPermission(id);
        assertNotNull(entity);
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteHard(Long id) {
        permissionService.hardDeletePermission(id);
        ElementNotFoundException exception =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> permissionService.readPermission(id),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Permission Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
    }
}
