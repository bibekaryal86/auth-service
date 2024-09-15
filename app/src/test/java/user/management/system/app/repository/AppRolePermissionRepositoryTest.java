package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppRolePermissionEntity;

public class AppRolePermissionRepositoryTest extends BaseTest {

  @Autowired private AppRolePermissionRepository appRolePermissionRepository;

  @Test
  public void testFindByAppRoleIdOrderByAppPermissionNameAsc() {
    List<AppRolePermissionEntity> rolePermissionEntities =
        appRolePermissionRepository.findByAppRoleIdOrderByAppPermissionNameAsc(4);

    assertNotNull(rolePermissionEntities);
    assertEquals(3, rolePermissionEntities.size());
    assertEquals("Permission A", rolePermissionEntities.get(0).getAppPermission().getName());
    assertEquals("Permission V", rolePermissionEntities.get(1).getAppPermission().getName());
    assertEquals("Permission Z", rolePermissionEntities.get(2).getAppPermission().getName());
  }

  @Test
  public void testFindByAppRoleIdInOrderByAppPermissionNameAsc() {
    List<AppRolePermissionEntity> rolePermissionEntities =
        appRolePermissionRepository.findByAppRoleIdInOrderByAppPermissionNameAsc(List.of(1, 4));

    assertNotNull(rolePermissionEntities);
    assertEquals(4, rolePermissionEntities.size());
    assertEquals("Permission A", rolePermissionEntities.get(0).getAppPermission().getName());
    assertEquals("Permission One", rolePermissionEntities.get(1).getAppPermission().getName());
    assertEquals("Permission V", rolePermissionEntities.get(2).getAppPermission().getName());
    assertEquals("Permission Z", rolePermissionEntities.get(3).getAppPermission().getName());
  }

  @Test
  public void testFindByAppPermissionAppIdAndAppRoleIdInOrderByAppPermissionNameAsc() {
    List<AppRolePermissionEntity> rolePermissionEntities =
        appRolePermissionRepository
            .findByAppPermissionAppIdAndAppRoleIdInOrderByAppPermissionNameAsc(
                "app-99", List.of(1, 4));

    assertNotNull(rolePermissionEntities);
    assertEquals(3, rolePermissionEntities.size());
    assertEquals("Permission A", rolePermissionEntities.get(0).getAppPermission().getName());
    assertEquals("Permission V", rolePermissionEntities.get(1).getAppPermission().getName());
    assertEquals("Permission Z", rolePermissionEntities.get(2).getAppPermission().getName());
  }
}
