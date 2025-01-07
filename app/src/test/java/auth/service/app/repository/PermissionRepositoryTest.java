package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.PermissionEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class PermissionRepositoryTest extends BaseTest {

  @Autowired private PermissionRepository permissionRepository;

  @Test
  void testUniqueConstraint_permissionName() {
    PermissionEntity permissionEntityInput = TestData.getPermissionEntities().getFirst();
    final String original = permissionEntityInput.getPermissionName();
    PermissionEntity permissionEntityOutput = new PermissionEntity();
    BeanUtils.copyProperties(permissionEntityInput, permissionEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final PermissionEntity finalPermissionEntityOutput = permissionEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> permissionRepository.save(finalPermissionEntityOutput));

    // does not throw exception for different name
    permissionEntityOutput.setPermissionName("Some New Permission");
    permissionEntityOutput = permissionRepository.save(permissionEntityOutput);
    assertEquals("Some New Permission", permissionEntityOutput.getPermissionName());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, permissionEntityInput.getPermissionName());

    // cleanup
    permissionRepository.deleteById(permissionEntityOutput.getId());
  }
}
