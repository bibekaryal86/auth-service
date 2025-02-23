package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.RoleEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class RoleRepositoryTest extends BaseTest {

  @Autowired private RoleRepository roleRepository;

  @Test
  void testUniqueConstraint_RoleName() {
    RoleEntity roleEntityInput = TestData.getRoleEntities().getFirst();
    final String original = roleEntityInput.getRoleName();
    RoleEntity roleEntityOutput = new RoleEntity();
    BeanUtils.copyProperties(roleEntityInput, roleEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final RoleEntity finalRoleEntityOutput = roleEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class, () -> roleRepository.save(finalRoleEntityOutput));

    // does not throw exception for different name
    roleEntityOutput.setRoleName("Some New Role");
    roleEntityOutput = roleRepository.save(roleEntityOutput);
    assertEquals("Some New Role", roleEntityOutput.getRoleName());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, roleEntityInput.getRoleName());

    // cleanup
    roleRepository.deleteById(roleEntityOutput.getId());
  }
}
