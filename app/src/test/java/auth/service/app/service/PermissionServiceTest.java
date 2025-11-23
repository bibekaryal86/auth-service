package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.PermissionRepository;
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

public class PermissionServiceTest extends BaseTest {

  @MockitoBean private SecurityContext securityContext;

  @Autowired private PermissionService permissionService;
  @Autowired private PermissionRepository permissionRepository;

  @Test
  void testReadPermissions_NoRequestMetadata() {
    Page<PermissionEntity> permissionEntityPage = permissionService.readPermissions(null);
    List<PermissionEntity> permissionEntities = permissionEntityPage.toList();
    assertEquals(9, permissionEntities.size());
    assertEquals(1, permissionEntityPage.getTotalPages());
    assertEquals(100, permissionEntityPage.getSize());
    // check sorted by name
    assertEquals("PERMISSION-01", permissionEntities.getFirst().getPermissionName());
    assertEquals("PERMISSION-13", permissionEntities.getLast().getPermissionName());
  }

  @Test
  void testReadPermissions_RequestMetadata() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    AuthToken authToken = TestData.getAuthToken();
    authToken.setIsSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RequestMetadata requestMetadata = TestData.defaultRequestMetadata("permissionName");
    requestMetadata.setSortDirection(Sort.Direction.ASC);
    requestMetadata.setIncludeDeleted(true);
    Page<PermissionEntity> permissionEntityPage =
        permissionService.readPermissions(requestMetadata);
    List<PermissionEntity> permissionEntities = permissionEntityPage.toList();
    assertEquals(13, permissionEntities.size());
    assertEquals(1, permissionEntityPage.getTotalPages());
    assertEquals(100, permissionEntityPage.getSize());
    // check sorted by name
    assertEquals("PERMISSION-01", permissionEntities.getFirst().getPermissionName());
    assertEquals("PERMISSION-13", permissionEntities.getLast().getPermissionName());
  }

  @Test
  void testReadPermissionsByRoleIds() {
    List<PermissionEntity> permissionEntities =
        permissionService.readPermissionsByRoleIds(List.of(ID, 13L));
    List<PermissionEntity> permissionEntities1 =
        permissionEntities.stream()
            .filter(permissionEntity -> permissionEntity.getRole().getId() == ID)
            .toList();
    List<PermissionEntity> permissionEntities13 =
        permissionEntities.stream()
            .filter(permissionEntity -> permissionEntity.getRole().getId() == 13L)
            .toList();

    assertEquals(4, permissionEntities1.size());
    assertEquals(1, permissionEntities13.size());
  }

  @Test
  void testHardDeletePermissionsByRoleId() {
    // setup
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setId(11L);

    List<Long> permissionIds = new ArrayList<>();
    for (int i = 1; i < 4; i++) {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setRole(roleEntity);
      permissionEntity.setPermissionName("P_NAME_" + i);
      permissionEntity.setPermissionDesc("P_DESC_" + i);
      permissionEntity = permissionRepository.save(permissionEntity);
      permissionIds.add(permissionEntity.getId());
    }

    permissionService.hardDeletePermissionsByRoleId(roleEntity.getId());

    for (Long permissionId : permissionIds) {
      assertTrue(permissionRepository.findById(permissionId).isEmpty());
    }
  }

  @Test
  void testPermissionService_CRUD() {
    String newName = "PermissionNewName";
    String newDesc = "PermissionNewDesc";
    String updatedDesc = "PermissionUpdatedDesc";
    PermissionRequest request = new PermissionRequest(ID, newName, newDesc);

    // Create
    Long id = assertCreate(request);

    // Create fails
    request = new PermissionRequest(ID_DELETED, newName, newDesc);
    assertCreateFailsForDeletedRole(request);

    // Update
    request = new PermissionRequest(ID, newName, updatedDesc);
    assertUpdate(id, request);

    // Update fails for deleted Permission
    assertUpdateFailsForDeletedPermission(ID_DELETED, request);

    // Update fails for deleted Role
    request = new PermissionRequest(ID_DELETED, newName, newDesc);
    assertUpdateFailsForDeletedRole(id, request);

    // Read
    // readPermission has private access

    // Soft delete
    assertDeleteSoft(id);

    // Soft delete fails for deleted Permission
    assertDeleteSoftFailsForDeletedPermission(ID_DELETED);

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

  private void assertCreateFailsForDeletedRole(PermissionRequest request) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> permissionService.createPermission(request),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Role Not Found for [%s]", request.getRoleId()),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  private void assertUpdate(Long id, PermissionRequest request) {
    PermissionEntity entity = permissionService.updatePermission(id, request);
    assertNotNull(entity);
  }

  private void assertUpdateFailsForDeletedPermission(Long id, PermissionRequest request) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> permissionService.updatePermission(id, request),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Permission Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  private void assertUpdateFailsForDeletedRole(Long id, PermissionRequest request) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> permissionService.updatePermission(id, request),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Role Not Found for [%s]", request.getRoleId()),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  private void assertDeleteSoft(Long id) {
    PermissionEntity entity = permissionService.softDeletePermission(id);
    assertNotNull(entity);
    assertNotNull(entity.getDeletedDate());
  }

  private void assertDeleteSoftFailsForDeletedPermission(Long id) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> permissionService.softDeletePermission(id),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Permission Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
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
            () -> permissionService.hardDeletePermission(id),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Permission Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
