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
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class RoleServiceTest extends BaseTest {

  @MockitoBean private SecurityContext securityContext;

  @Autowired private RoleService roleService;
  @Autowired private PermissionService permissionService;
  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private CircularDependencyService circularDependencyService;

  @Test
  void testReadRoles_NoRequestMetadata() {
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
  void testReadRoles_RequestMetadata() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RequestMetadata requestMetadata = TestData.defaultRequestMetadata("roleName");
    requestMetadata.setSortDirection(Sort.Direction.ASC);
    requestMetadata.setIncludeDeleted(true);
    Page<RoleEntity> roleEntityPage = roleService.readRoles(requestMetadata);
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
    // setup
    // create PPRs
    PlatformProfileRoleRequest pprRequest = new PlatformProfileRoleRequest(ID, ID, id);
    PlatformProfileRoleEntity pprEntity =
        platformProfileRoleService.assignPlatformProfileRole(pprRequest);
    assertNotNull(pprEntity.getId());
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(ID, EMAIL));

    pprRequest = new PlatformProfileRoleRequest(2L, 2L, id);
    pprEntity = platformProfileRoleService.assignPlatformProfileRole(pprRequest);
    assertNotNull(pprEntity.getId());
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(2L, "firstlast@two.com"));

    // create permissions
    List<Long> permissionEntityIds = new ArrayList<>();
    PermissionRequest permissionRequest = new PermissionRequest(id, "Name1", "Desc1");
    PermissionEntity permissionEntity = permissionService.createPermission(permissionRequest);
    assertNotNull(permissionEntity);
    assertNotNull(circularDependencyService.readPermission(permissionEntity.getId(), false));
    permissionEntityIds.add(permissionEntity.getId());

    permissionRequest = new PermissionRequest(id, "Name2", "Desc2");
    permissionEntity = permissionService.createPermission(permissionRequest);
    assertNotNull(permissionEntity);
    assertNotNull(circularDependencyService.readPermission(permissionEntity.getId(), false));
    permissionEntityIds.add(permissionEntity.getId());

    roleService.hardDeleteRole(id);

    // assert Role is deleted
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readRole(id, false),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Role Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");

    // assert PPRs are Deleted
    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> platformProfileRoleService.readPlatformProfileRole(id, EMAIL),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Platform Profile Role Not Found for [%s,%s]", id, EMAIL),
        exception.getMessage(),
        "Exception message mismatch...");

    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> platformProfileRoleService.readPlatformProfileRole(id, "firstlast@two.com"),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Platform Profile Role Not Found for [%s,%s]", id, "firstlast@two.com"),
        exception.getMessage(),
        "Exception message mismatch...");

    // assert Permission are deleted
    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readPermission(permissionEntityIds.getFirst(), false),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Permission Not Found for [%s]", permissionEntityIds.getFirst()),
        exception.getMessage(),
        "Exception message mismatch...");

    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readPermission(permissionEntityIds.getLast(), false),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Permission Not Found for [%s]", permissionEntityIds.getLast()),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
