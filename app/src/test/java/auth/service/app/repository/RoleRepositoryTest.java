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
    RoleEntity roleEntity = TestData.getRoleEntities().getFirst();
    roleEntity.setId(null);

    // throws exception for same name
    assertThrows(DataIntegrityViolationException.class, () -> roleRepository.save(roleEntity));

    // does not throw exception for different name
    roleEntity.setRoleName("Some Role Name");
    RoleEntity roleEntity1 = roleRepository.save(roleEntity);

    // cleanup
    roleRepository.deleteById(roleEntity1.getId());
  }
}
