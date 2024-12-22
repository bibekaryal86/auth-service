package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformRolePermissionRepositoryTest extends BaseTest {

  @Autowired private PlatformRolePermissionRepository platformRolePermissionRepository;

  @Test
  void testFindByRoleId() {
    List<PlatformRolePermissionEntity> platformRolePermissionEntities =
        platformRolePermissionRepository.findByRoleId(4L);
    assertEquals(3, platformRolePermissionEntities.size());
  }

  @Test
  void testFindByRoleIds() {
    List<PlatformRolePermissionEntity> platformRolePermissionEntities =
        platformRolePermissionRepository.findByRoleIds(List.of(1L, 2L, 3L, 4L));
    assertEquals(6, platformRolePermissionEntities.size());
    // test order by platform name, profile email and role name
    assertAll(
        "Platform Profile Role Entities Find By Profile Ids",
        () ->
            assertAll(
                "Entity 0",
                () ->
                    assertEquals(
                        "Auth Service",
                        platformRolePermissionEntities.get(0).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "SUPERUSER", platformRolePermissionEntities.get(0).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_CREATE",
                        platformRolePermissionEntities.get(0).getPermission().getPermissionName())),
        () ->
            assertAll(
                "Entity 1",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformRolePermissionEntities.get(1).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "SUPERUSER-1",
                        platformRolePermissionEntities.get(1).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_CREATE-1",
                        platformRolePermissionEntities.get(1).getPermission().getPermissionName())),
        () ->
            assertAll(
                "Entity 2",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformRolePermissionEntities.get(2).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "SUPERUSER-1",
                        platformRolePermissionEntities.get(2).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_READ-1",
                        platformRolePermissionEntities.get(2).getPermission().getPermissionName())),
        () ->
            assertAll(
                "Entity 3",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformRolePermissionEntities.get(3).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "SUPERUSER-1",
                        platformRolePermissionEntities.get(3).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_UPDATE-1",
                        platformRolePermissionEntities.get(3).getPermission().getPermissionName())),
        () ->
            assertAll(
                "Entity 4",
                () ->
                    assertEquals(
                        "Env Service",
                        platformRolePermissionEntities.get(4).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "POWERUSER", platformRolePermissionEntities.get(4).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_READ",
                        platformRolePermissionEntities.get(4).getPermission().getPermissionName())),
        () ->
            assertAll(
                "Entity 5",
                () ->
                    assertEquals(
                        "Personal Expenses Tracking System",
                        platformRolePermissionEntities.get(5).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "GUEST", platformRolePermissionEntities.get(5).getRole().getRoleName()),
                () ->
                    assertEquals(
                        "PERMISSION_UPDATE",
                        platformRolePermissionEntities
                            .get(5)
                            .getPermission()
                            .getPermissionName())));
  }
}
