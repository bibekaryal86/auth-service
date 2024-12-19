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
    PermissionEntity permissionEntityInput = TestData.getPermissionEntities().getFirst();
    permissionEntityInput.setId(null);

    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> permissionRepository.save(permissionEntityInput));

    // does not throw exception for different name
    permissionEntityInput.setPermissionName("Some New Permission");
    PermissionEntity permissionEntityOutput = permissionRepository.save(permissionEntityInput);

    // cleanup
    permissionRepository.deleteById(permissionEntityOutput.getId());
  }
}
