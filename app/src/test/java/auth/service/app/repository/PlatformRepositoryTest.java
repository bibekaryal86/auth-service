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
    PlatformEntity platformEntityInput = TestData.getPlatformEntities().getFirst();
    platformEntityInput.setId(null);

    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class, () -> platformRepository.save(platformEntityInput));

    // does not throw exception for different name
    platformEntityInput.setPlatformName("Some Platform Name");
    PlatformEntity platformEntityOutput = platformRepository.save(platformEntityInput);

    // cleanup
    platformRepository.deleteById(platformEntityOutput.getId());
  }
}
