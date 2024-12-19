package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class PlatformRepositoryTest extends BaseTest {

  @Autowired private PlatformRepository platformRepository;

  @Test
  void testUniqueConstraint_platformName() {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    platformEntity.setId(null);

    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class, () -> platformRepository.save(platformEntity));

    // does not throw exception for different name
    platformEntity.setPlatformName("Some Platform Name");
    PlatformEntity platformEntity1 = platformRepository.save(platformEntity);

    // cleanup
    platformRepository.deleteById(platformEntity1.getId());
  }
}
