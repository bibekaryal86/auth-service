package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class PlatformRepositoryTest extends BaseTest {

  @Autowired private PlatformRepository platformRepository;

  @Test
  void testUniqueConstraint_platformName() {
    PlatformEntity platformEntityInput = TestData.getPlatformEntities().getFirst();
    final String original = platformEntityInput.getPlatformName();
    ;
    PlatformEntity platformEntityOutput = new PlatformEntity();
    BeanUtils.copyProperties(platformEntityInput, platformEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    PlatformEntity finalPlatformEntityOutput = platformEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> platformRepository.save(finalPlatformEntityOutput));

    // does not throw exception for different name
    platformEntityOutput.setPlatformName("Some New Platform");
    platformEntityOutput = platformRepository.save(platformEntityOutput);
    assertEquals("Some New Platform", platformEntityOutput.getPlatformName());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, platformEntityInput.getPlatformName());

    // cleanup
    platformRepository.deleteById(platformEntityOutput.getId());
  }
}
