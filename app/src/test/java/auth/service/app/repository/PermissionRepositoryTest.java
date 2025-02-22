package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.RoleEntity;
import helper.TestData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;

public class PermissionRepositoryTest extends BaseTest {

  @Autowired private PermissionRepository permissionRepository;

  @Test
  void testUniqueConstraint_roleIdPermissionName() {
    RoleEntity roleEntity = TestData.getRoleEntities().getLast();
    PermissionEntity permissionEntityInput = TestData.getPermissionEntities().getFirst();
    final Long original = permissionEntityInput.getRole().getId();
    PermissionEntity permissionEntityOutput = new PermissionEntity();
    BeanUtils.copyProperties(permissionEntityInput, permissionEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final PermissionEntity finalPermissionEntityOutput = permissionEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> permissionRepository.save(finalPermissionEntityOutput));

    // does not throw exception for same name, different role
    permissionEntityOutput.setRole(roleEntity);
    permissionEntityOutput = permissionRepository.save(permissionEntityOutput);
    assertEquals(roleEntity, permissionEntityOutput.getRole());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, permissionEntityInput.getRole().getId());

    // cleanup
    permissionRepository.deleteById(permissionEntityOutput.getId());
  }

  @Test
  void testDeleteByRoleId() {
    // setup
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setId(11L);

    List<Long> permissionIds = new ArrayList<>();
    for (int i = 1; i < 4; i++) {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setRole(roleEntity);
      permissionEntity.setPermissionName("P_NAME_" + i);
      permissionEntity.setPermissionDesc("P_DESC_" + i);
      permissionEntity = permissionRepository.save(permissionEntity);
      permissionIds.add(permissionEntity.getId());
    }

    permissionRepository.deleteByRoleId(roleEntity.getId());

    for (Long permissionId : permissionIds) {
      assertTrue(permissionRepository.findById(permissionId).isEmpty());
    }
  }

  @Test
  void testFindByRoleIds() {
    List<PermissionEntity> permissionEntities =
        permissionRepository.findByRoleIds(List.of(1L, 13L));
    assertFalse(CollectionUtils.isEmpty(permissionEntities));

    List<PermissionEntity> permissionEntities1 =
        permissionEntities.stream()
            .filter(permissionEntity -> permissionEntity.getRole().getId() == 1L)
            .toList();
    List<PermissionEntity> permissionEntities13 =
        permissionEntities.stream()
            .filter(permissionEntity -> permissionEntity.getRole().getId() == 13L)
            .toList();

    assertEquals(4, permissionEntities1.size());
    assertEquals(1, permissionEntities13.size());
  }
}
