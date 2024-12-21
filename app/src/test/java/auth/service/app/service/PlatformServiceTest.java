package auth.service.app.service;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.entity.PlatformEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlatformServiceTest extends BaseTest {

    @Autowired
    private PlatformService platformService;

    @Test
    void testReadPlatforms() {
        List<PlatformEntity> platformEntities = platformService.readPlatforms();
        assertEquals(6, platformEntities.size());
        // check sorted by name
        assertEquals("Auth Service", platformEntities.getFirst().getPlatformName());
        assertEquals("Personal Expenses Tracking System-1", platformEntities.getLast().getPlatformName());
    }

    @Test
    void testPlatformService_CRUD() {
        String newName = "PlatformNewName";
        String newDesc = "PlatformNewDesc";
        String updatedDesc = "PlatformUpdatedDesc";
        PlatformRequest request = new PlatformRequest(newName, newDesc);

        // Create
        Long id = assertCreate(request);

        // Update
        request = new PlatformRequest(newName, updatedDesc);
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

    private Long assertCreate(PlatformRequest request) {
        PlatformEntity entity = platformService.createPlatform(request);
        assertNotNull(entity);
        assertNotNull(entity.getId());
        return entity.getId();
    }

    private void assertUpdate(Long id, PlatformRequest request) {
        PlatformEntity entity = platformService.updatePlatform(id, request);
        assertNotNull(entity);
    }

    private void assertRead(Long id, String expectedName, String expectedDescription) {
        PlatformEntity entity = platformService.readPlatform(id);
        assertNotNull(entity);
        assertEquals(expectedName, entity.getPlatformName());
        assertEquals(expectedDescription, entity.getPlatformDesc());
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteSoft(Long id) {
        PlatformEntity entity = platformService.softDeletePlatform(id);
        assertNotNull(entity);
        assertNotNull(entity.getDeletedDate());
    }

    private void assertRestoreSoftDeleted(Long id) {
        PlatformEntity entity = platformService.restoreSoftDeletedPlatform(id);
        assertNotNull(entity);
        assertNull(entity.getDeletedDate());
    }

    private void assertDeleteHard(Long id) {
        platformService.hardDeletePlatform(id);
        ElementNotFoundException exception =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> platformService.readPlatform(id),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Platform Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
    }
}
