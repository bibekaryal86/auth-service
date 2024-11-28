package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppsEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class AppsRepositoryTest extends BaseTest {

  @Autowired private AppsRepository appsRepository;

  @Test
  void testUniqueConstraints() {
    AppsEntity appsEntity = TestData.getAppsEntities().getFirst();
    appsEntity.setId("app-1001");
    assertThrows(DataIntegrityViolationException.class, () -> appsRepository.save(appsEntity));
  }
}
