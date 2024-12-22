package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformProfileRoleServiceTest extends BaseTest {

  @Autowired private PlatformProfileRoleService platformProfileRoleService;

  @Test
  void testReadPlatformProfileRoles() {
    assertEquals(6, platformProfileRoleService.readPlatformProfileRolesByProfileId().size());
  }

  @Test
  void testReadPlatformProfileRolesByProfileId() {
    assertEquals(3, platformProfileRoleService.readPlatformProfileRolesByProfileId(4L).size());
  }

  @Test
  void testReadPlatformProfileRolesByProfileIds() {
    assertEquals(
        4, platformProfileRoleService.readPlatformProfileRolesByProfileIds(List.of(1L, 4L)).size());
  }

  @Test
  void testReadPlatformProfileRole_ByPlatformIdProfileEmail() {
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(3L, "firstlast@three.com"));
  }

  @Test
  void testReadPlatformProfileRole_ByPlatformIdProfileEmail_NotFound() {
    assertThrows(
        ElementNotFoundException.class,
        () -> platformProfileRoleService.readPlatformProfileRole(3L, "firstlast-1@nine.com"));
  }

  @Test
  void testPlatformProfileRoleService_CRUD() {
    Long platformId = 6L;
    Long profileId = 6L;
    Long roleId = 6L;
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(platformId, profileId, roleId);

    // create
    PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.createPlatformProfileRole(platformProfileRoleRequest);
    assertNotNull(platformProfileRoleEntity);
    assertNotNull(platformProfileRoleEntity.getId());

    // update, not available

    // read
    platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(platformId, profileId, roleId);
    assertNotNull(platformProfileRoleEntity);
    assertNotNull(platformProfileRoleEntity.getId());
    assertEquals(
        "Personal Expenses Tracking System-1",
        platformProfileRoleEntity.getPlatform().getPlatformName());
    assertEquals("firstlast-1@three.com", platformProfileRoleEntity.getProfile().getEmail());
    assertEquals("GUEST-1", platformProfileRoleEntity.getRole().getRoleName());
    assertNotNull(platformProfileRoleEntity.getAssignedDate());

    // hard delete
    platformProfileRoleService.deletePlatformProfileRole(platformId, profileId, roleId);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () ->
                platformProfileRoleService.readPlatformProfileRole(platformId, profileId, roleId));
    assertEquals("Platform Profile Role Not Found for [6,6,6]", exception.getMessage());
  }
}
