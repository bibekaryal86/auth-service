package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.PermissionEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class PermissionRepositoryTest extends BaseTest {

  @Autowired private PermissionRepository permissionRepository;

  @Test
  void testUniqueConstraint_permissionName() {
    PermissionEntity permissionEntity = TestData.getPermissionEntities().getFirst();
    permissionEntity.setId(null);

    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class, () -> permissionRepository.save(permissionEntity));

    // does not throw exception for different name
    permissionEntity.setPermissionName("Some New Permission");
    PermissionEntity permissionEntity1 = permissionRepository.save(permissionEntity);

    // cleanup
    permissionRepository.deleteById(permissionEntity1.getId());
  }
}
