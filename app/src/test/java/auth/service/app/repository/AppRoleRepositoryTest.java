package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppRoleEntity;
import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

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
