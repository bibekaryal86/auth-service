package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.RoleEntity;

import java.util.Collections;
import java.util.List;

import auth.service.app.model.token.AuthToken;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class RoleServiceTest extends BaseTest {

    @Mock
    private SecurityContext securityContext;

    @Autowired private RoleService roleService;

    @Test
    void testReadRoles_noRequestMetadata() {
        Page<RoleEntity> roleEntityPage = roleService.readRoles(null);
        List<RoleEntity> roleEntities = roleEntityPage.toList();
        assertEquals(9, roleEntities.size());
        assertEquals(1, roleEntityPage.getTotalPages());
        assertEquals(100, roleEntityPage.getSize());
        // check sorted by name
        assertEquals("ROLE-01", roleEntities.getFirst().getRoleName());
        assertEquals("ROLE-13", roleEntities.getLast().getRoleName());
    }

    @Test
    void testReadRoles_requestMetadata() {
        reset(securityContext);
        SecurityContextHolder.setContext(securityContext);
        AuthToken authToken = TestData.getAuthToken();
        authToken.setSuperUser(true);
        Authentication authentication = new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
        authentication.setAuthenticated(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Page<RoleEntity> roleEntityPage = roleService.readRoles(RequestMetadata.builder().isIncludeDeleted(true).build());
        List<RoleEntity> roleEntities = roleEntityPage.toList();
        assertEquals(13, roleEntities.size());
        assertEquals(1, roleEntityPage.getTotalPages());
        assertEquals(100, roleEntityPage.getSize());
        // check sorted by name
        assertEquals("ROLE-01", roleEntities.getFirst().getRoleName());
        assertEquals("ROLE-13", roleEntities.getLast().getRoleName());
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

        // Update fails for deleted Role
        assertUpdateFailsForDeletedRole(ID_DELETED, request);

        // read
        // readRole has private access

        // Soft delete
        assertDeleteSoft(id);

        // Soft delete fails for deleted Role
        assertDeleteSoftFailsForDeletedRole(ID_DELETED);

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

    private void assertUpdateFailsForDeletedRole(Long id, RoleRequest request) {
        ElementNotActiveException exception =
                assertThrows(
                        ElementNotActiveException.class,
                        () -> roleService.updateRole(id, request),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Active Role Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
    }

    private void assertDeleteSoft(Long id) {
        RoleEntity entity = roleService.softDeleteRole(id);
        assertNotNull(entity);
        assertNotNull(entity.getDeletedDate());
    }

    private void assertDeleteSoftFailsForDeletedRole(Long id) {
        ElementNotActiveException exception =
                assertThrows(
                        ElementNotActiveException.class,
                        () -> roleService.softDeleteRole(id),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Active Role Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
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
                        () -> roleService.hardDeleteRole(id),
                        "Expected ElementNotFoundException after hard delete...");
        assertEquals(
                String.format("Role Not Found for [%s]", id),
                exception.getMessage(),
                "Exception message mismatch...");
    }
}
