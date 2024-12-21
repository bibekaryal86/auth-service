package auth.service.app.service;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.StatusTypeRequest;
import auth.service.app.model.entity.StatusTypeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatusTypeServiceTest extends BaseTest {

    @Autowired
    private StatusTypeService statusTypeService;

    @Test
    void testReadStatusTypes() {
        List<StatusTypeEntity> statusTypeEntities = statusTypeService.readStatusTypes();
        assertEquals(6, statusTypeEntities.size());
        // check sorted by name
        assertEquals("Active", statusTypeEntities.getFirst().getStatusName());
        assertEquals("Pending-1", statusTypeEntities.getLast().getStatusName());
    }

    @Test
    void testStatusTypeService_CRUD() {
        String newComponent = "StatusTypeNewComponent";
        String newName = "StatusTypeNewName";
        String newDesc = "StatusTypeNewDesc";
        String updatedDesc = "StatusTypeUpdatedDesc";
        StatusTypeRequest request = new StatusTypeRequest(newComponent, newName, newDesc);

        // Create
        Long id = assertCreate(request);

        // Update
        request = new StatusTypeRequest(newComponent, newName, updatedDesc);
        assertUpdate(id, request);

        // Read
        assertRead(id, newComponent, newName, updatedDesc);

        // Soft delete
        assertDeleteSoft(id);

        // Restore
        assertRestoreSoftDeleted(id);

        // deleteHard
        assertDeleteHard(id);
    }

    private Long assertCreate(StatusTypeRequest request) {
        StatusTypeEntity entity = statusTypeService.createStatusType(request);
        assertNotNull(entity);
        assertNotNull(entity.getId());
        return entity.getId();
    }

    private void assertUpdate(Long id, StatusTypeRequest request) {
        StatusTypeEntity entity = statusTypeService.updateStatusType(id, request);
        assertNotNull(entity);
    }

    private void assertRead(Long id, String expectedComponent, String expectedName, String expectedDescription) {
        StatusTypeEntity entity = statusTypeService.readStatusType(id);
        assertNotNull(entity);
        assertEquals(expectedComponent, entity.getComponentName());
        assertEquals(expectedName, entity.getStatusName());
        assertEquals(expectedDescription, entity.getStatusDesc());
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteSoft(Long id) {
        StatusTypeEntity entity = statusTypeService.softDeleteStatusType(id);
        assertNotNull(entity);
        assertNotNull(entity.getDeletedDate());
    }

    private void assertRestoreSoftDeleted(Long id) {
        StatusTypeEntity entity = statusTypeService.restoreSoftDeletedStatusType(id);
        assertNotNull(entity);
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteHard(Long id) {
        statusTypeService.hardDeleteStatusType(id);
        ElementNotFoundException exception =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> statusTypeService.readStatusType(id),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Status Type Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
    }
}
