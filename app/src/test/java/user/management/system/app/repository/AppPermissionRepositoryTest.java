package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppPermissionEntity;

public class AppPermissionRepositoryTest extends BaseTest {

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Test
  public void testFindByAppIdOrderByNameAsc() {
    List<AppPermissionEntity> permissions =
        appPermissionRepository.findByAppIdOrderByNameAsc("app-99");

    assertNotNull(permissions);
    assertEquals(3, permissions.size());

    assertEquals(permissions.get(0).getName(), "Permission A");
    assertEquals(permissions.get(1).getName(), "Permission V");
    assertEquals(permissions.get(2).getName(), "Permission Z");
  }
}
