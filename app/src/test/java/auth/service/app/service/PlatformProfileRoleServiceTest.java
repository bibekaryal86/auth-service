package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import java.util.List;

import auth.service.app.repository.PlatformProfileRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformProfileRoleServiceTest extends BaseTest {

    @Autowired private PlatformProfileRoleService platformProfileRoleService;
    @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;

    @Test
    void testReadPlatformProfileRolesByProfileIds() {
        assertEquals(4, platformProfileRoleService.readPlatformProfileRolesByProfileIds(List.of(1L, 4L)).size());
    }

    @Test
    void testReadPlatformProfileRole_ByPlatformIdProfileEmail() {
        assertNotNull(platformProfileRoleService.readPlatformProfileRole(3L, "firstlast@three.com"));
    }

    @Test
    void testReadPlatformProfileRole_ByPlatformIdProfileEmail_NotFound() {
        assertThrows(ElementNotFoundException.class, () -> platformProfileRoleService.readPlatformProfileRole(3L, "firstlast-1@nine.com"));
    }

    @Test
    void testPlatformProfileRoleService_assignUnassign() {
        Long platformId = 7L;
        Long profileId = 7L;
        Long roleId = 7L;
        PlatformProfileRoleRequest platformProfileRoleRequest = new PlatformProfileRoleRequest(platformId, profileId, roleId);

        // assign
        PlatformProfileRoleEntity platformProfileRoleEntity = platformProfileRoleService.assignPlatformProfileRole(platformProfileRoleRequest);
        assertNotNull(platformProfileRoleEntity);
        assertNotNull(platformProfileRoleEntity.getId());
        assertNotNull(platformProfileRoleEntity.getAssignedDate());

        // unassign
        platformProfileRoleEntity = platformProfileRoleService.unassignPlatformProfileRole(platformId, profileId, roleId);
        assertNotNull(platformProfileRoleEntity);
        assertNotNull(platformProfileRoleEntity.getId());
        assertNotNull(platformProfileRoleEntity.getAssignedDate());
        assertNotNull(platformProfileRoleEntity.getUnassignedDate());

        // cleanup
        platformProfileRoleRepository.delete(platformProfileRoleEntity);
    }
    
    @Test
    void testAssignPlatformProfileRole_platformErrors() {
        ElementNotFoundException elementNotFoundException = assertThrows(ElementNotFoundException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID_NOT_FOUND, ID, ID)));
        assertEquals("Platform Not Found for [99]", elementNotFoundException.getMessage());
        
        ElementNotActiveException elementNotActiveException = assertThrows(ElementNotActiveException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID_DELETED, ID, ID)));
        assertEquals(String.format("Active Platform Not Found for [%s]", ID_DELETED), elementNotActiveException.getMessage());
    }

    @Test
    void testAssignPlatformProfileRole_profileErrors() {
        ElementNotFoundException elementNotFoundException = assertThrows(ElementNotFoundException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID, ID_NOT_FOUND, ID)));
        assertEquals("Profile Not Found for [99]", elementNotFoundException.getMessage());

        ElementNotActiveException elementNotActiveException = assertThrows(ElementNotActiveException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID, ID_DELETED, ID)));
        assertEquals(String.format("Active Profile Not Found for [%s]", ID_DELETED), elementNotActiveException.getMessage());
    }

    @Test
    void testAssignPlatformProfileRole_roleErrors() {
        ElementNotFoundException elementNotFoundException = assertThrows(ElementNotFoundException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID, ID, ID_NOT_FOUND)));
        assertEquals("Role Not Found for [99]", elementNotFoundException.getMessage());

        ElementNotActiveException elementNotActiveException = assertThrows(ElementNotActiveException.class, () -> platformProfileRoleService.assignPlatformProfileRole(new PlatformProfileRoleRequest(ID, ID, ID_DELETED)));
        assertEquals(String.format("Active Role Not Found for [%s]", ID_DELETED), elementNotActiveException.getMessage());
    }
}
