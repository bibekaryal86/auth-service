package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppUserRoleEntity;

public class AppUserRoleRepositoryTest extends BaseTest {

  @Autowired private AppUserRoleRepository appUserRoleRepository;

  @Test
  public void testFindByIdAppUserIdOrderByAppRoleNameAsc() {
    List<AppUserRoleEntity> appUserRoleEntities =
        appUserRoleRepository.findByIdAppUserIdOrderByAppRoleNameAsc(4);

    assertNotNull(appUserRoleEntities);
    assertEquals(3, appUserRoleEntities.size());
    assertEquals(appUserRoleEntities.get(0).getAppRole().getName(), "Role A");
    assertEquals(appUserRoleEntities.get(1).getAppRole().getName(), "Role V");
    assertEquals(appUserRoleEntities.get(2).getAppRole().getName(), "Role Z");
  }

  @Test
  public void testFindByIdAppUserIdInOrderByAppRoleNameAsc() {
    List<AppUserRoleEntity> appUserRoleEntities =
        appUserRoleRepository.findByIdAppUserIdInOrderByAppRoleNameAsc(List.of(1, 4));

    assertNotNull(appUserRoleEntities);
    assertEquals(4, appUserRoleEntities.size());

    assertEquals(appUserRoleEntities.get(0).getAppRole().getName(), "Role A");
    assertEquals(appUserRoleEntities.get(1).getAppRole().getName(), "Role One");
    assertEquals(appUserRoleEntities.get(2).getAppRole().getName(), "Role V");
    assertEquals(appUserRoleEntities.get(3).getAppRole().getName(), "Role Z");
  }
}
