package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppsEntity;

public class AppsRepositoryTest extends BaseTest {

  @Autowired private AppsRepository appsRepository;

  @Test
  void testUniqueConstraints() {
    AppsEntity appsEntity = TestData.getAppsEntities().getFirst();
    appsEntity.setId("app-1001");
    assertThrows(DataIntegrityViolationException.class, () -> appsRepository.save(appsEntity));
  }
}
