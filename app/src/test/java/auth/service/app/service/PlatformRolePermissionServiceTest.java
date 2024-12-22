package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformRolePermissionServiceTest extends BaseTest {

  @Autowired private PlatformRolePermissionService platformRolePermissionService;

  @Test
  void testReadPlatformRolePermissions() {
    assertEquals(6, platformRolePermissionService.readPlatformRolePermissions().size());
  }

  @Test
  void testReadPlatformRolePermissionsByRoleId() {
    assertEquals(3, platformRolePermissionService.readPlatformRolePermissionsByRoleId(4L).size());
  }

  @Test
  void testReadPlatformRolePermissionsByRoleIds() {
    assertEquals(
        4,
        platformRolePermissionService.readPlatformRolePermissionsByRoleIds(List.of(1L, 4L)).size());
  }

  @Test
  void testPlatformRolePermissionService_CRUD() {
    Long platformId = 6L;
    Long roleId = 6L;
    Long permissionId = 6L;
    PlatformRolePermissionRequest platformRolePermissionRequest =
        new PlatformRolePermissionRequest(platformId, roleId, permissionId);

    // create
    PlatformRolePermissionEntity platformRolePermissionEntity =
        platformRolePermissionService.createPlatformRolePermission(platformRolePermissionRequest);
    assertNotNull(platformRolePermissionEntity);
    assertNotNull(platformRolePermissionEntity.getId());

    // update, not available

    // read
    platformRolePermissionEntity =
        platformRolePermissionService.readPlatformRolePermission(platformId, roleId, permissionId);
    assertNotNull(platformRolePermissionEntity);
    assertNotNull(platformRolePermissionEntity.getId());
    assertEquals(
        "Personal Expenses Tracking System-1",
        platformRolePermissionEntity.getPlatform().getPlatformName());
    assertEquals("GUEST-1", platformRolePermissionEntity.getRole().getRoleName());
    assertEquals(
        "PERMISSION_UPDATE-1", platformRolePermissionEntity.getPermission().getPermissionName());
    assertNotNull(platformRolePermissionEntity.getAssignedDate());

    // hard delete
    platformRolePermissionService.deletePlatformRolePermission(platformId, roleId, permissionId);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () ->
                platformRolePermissionService.readPlatformRolePermission(
                    platformId, roleId, permissionId));
    assertEquals("Platform Role Permission Not Found for [6,6,6]", exception.getMessage());
  }
}
