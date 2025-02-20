package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.RoleEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

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
}
