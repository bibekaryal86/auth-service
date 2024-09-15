package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppRoleEntity;

public class AppRoleRepositoryTest extends BaseTest {

  @Autowired private AppRoleRepository appRoleRepository;

  @Test
  public void testFindByName() {
    Optional<AppRoleEntity> appRoleEntityOptional = appRoleRepository.findByName("Role A");

    assertTrue(appRoleEntityOptional.isPresent());
    assertEquals("Role Description A", appRoleEntityOptional.get().getDescription());
  }

  @Test
  void testUniqueConstraints() {
    AppRoleEntity appRoleEntity = TestData.getAppRoleEntities().getFirst();
    appRoleEntity.setId(null);
    assertThrows(
        DataIntegrityViolationException.class, () -> appRoleRepository.save(appRoleEntity));
  }
}
