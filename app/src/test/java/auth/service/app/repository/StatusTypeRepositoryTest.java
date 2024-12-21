package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.StatusTypeEntity;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class StatusTypeRepositoryTest extends BaseTest {

  @Autowired private StatusTypeRepository statusTypeRepository;

  @Test
  void testUniqueConstraint_statusTypeName() {
    StatusTypeEntity statusTypeEntityInput = TestData.getStatusTypeEntities().getFirst();
    final String originalComponentName = statusTypeEntityInput.getComponentName();
    final String originalStatusName = statusTypeEntityInput.getStatusName();

    StatusTypeEntity statusTypeEntityOutputOne = new StatusTypeEntity();
    StatusTypeEntity statusTypeEntityOutputTwo = new StatusTypeEntity();
    BeanUtils.copyProperties(statusTypeEntityInput, statusTypeEntityOutputOne, "id");
    BeanUtils.copyProperties(statusTypeEntityInput, statusTypeEntityOutputTwo, "id");

    // Variable used in lambda expression should be final or effectively final
    final StatusTypeEntity finalStatusTypeEntity = statusTypeEntityOutputOne;
    // throws exception for same name and component
    assertThrows(
        DataIntegrityViolationException.class,
        () -> statusTypeRepository.save(finalStatusTypeEntity));

    // does not throw exception for same component different status name
    statusTypeEntityOutputOne.setStatusName("Some New Status");
    statusTypeEntityOutputOne = statusTypeRepository.save(statusTypeEntityOutputOne);

    // does not throw exception for different component same status name
    statusTypeEntityOutputTwo.setComponentName("Some New Component");
    statusTypeEntityOutputTwo = statusTypeRepository.save(statusTypeEntityOutputTwo);

    assertEquals("Some New Status", statusTypeEntityOutputOne.getStatusName());
    assertEquals(originalComponentName, statusTypeEntityOutputOne.getComponentName());
    assertEquals(originalStatusName, statusTypeEntityOutputTwo.getStatusName());
    assertEquals("Some New Component", statusTypeEntityOutputTwo.getComponentName());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(originalComponentName, statusTypeEntityInput.getComponentName());
    assertEquals(originalStatusName, statusTypeEntityInput.getStatusName());

    // cleanup
    statusTypeRepository.deleteAllById(
        List.of(statusTypeEntityOutputOne.getId(), statusTypeEntityOutputTwo.getId()));
  }
}
