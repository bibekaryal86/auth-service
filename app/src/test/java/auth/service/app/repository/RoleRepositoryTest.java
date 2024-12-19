package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.RoleEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class RoleRepositoryTest extends BaseTest {

  @Autowired private RoleRepository roleRepository;

  @Test
  void testUniqueConstraint_roleName() {
    RoleEntity roleEntityInput = TestData.getRoleEntities().getFirst();
    roleEntityInput.setId(null);

    // throws exception for same name
    assertThrows(DataIntegrityViolationException.class, () -> roleRepository.save(roleEntityInput));

    // does not throw exception for different name
    roleEntityInput.setRoleName("Some Role Name");
    RoleEntity roleEntityOutput = roleRepository.save(roleEntityInput);

    // cleanup
    roleRepository.deleteById(roleEntityOutput.getId());
  }
}
