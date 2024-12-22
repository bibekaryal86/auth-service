package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
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
}
