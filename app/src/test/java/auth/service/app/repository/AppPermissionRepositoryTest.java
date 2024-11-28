package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppPermissionEntity;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class AppPermissionRepositoryTest extends BaseTest {

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Test
  public void testFindByAppIdOrderByNameAsc() {
    List<AppPermissionEntity> permissions =
        appPermissionRepository.findByAppIdOrderByNameAsc("app-99");

    assertNotNull(permissions);
    assertEquals(3, permissions.size());
    assertEquals("Permission A", permissions.get(0).getName());
    assertEquals("Permission V", permissions.get(1).getName());
    assertEquals("Permission Z", permissions.get(2).getName());
  }

  @Test
  void testUniqueConstraints() {
    AppPermissionEntity appPermissionEntity = TestData.getAppPermissionEntities().getFirst();
    appPermissionEntity.setId(null);

    // throws exception for same name and appId
    assertThrows(
        DataIntegrityViolationException.class,
        () -> appPermissionRepository.save(appPermissionEntity));

    appPermissionEntity.setAppId("app-99");
    assertDoesNotThrow(
        () -> {
          // does not throw exception because appId is different for same name
          AppPermissionEntity appPermissionEntityNew =
              appPermissionRepository.save(appPermissionEntity);
          // cleanup
          appPermissionRepository.deleteById(appPermissionEntityNew.getId());
        });
  }
}
