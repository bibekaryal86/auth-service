package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppRoleEntity;

public class AppRoleRepositoryTest extends BaseTest {

  @Autowired private AppRoleRepository appRoleRepository;

  @Test
  public void testFindByName() {
    Optional<AppRoleEntity> appRoleEntityOptional = appRoleRepository.findByName("Role A");

    assertTrue(appRoleEntityOptional.isPresent());
    assertEquals(appRoleEntityOptional.get().getDescription(), "Role Description A");
  }
}
