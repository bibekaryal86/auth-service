package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.StatusTypeEntity;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class StatusTypeRepositoryTest extends BaseTest {

  @Autowired private StatusTypeRepository statusTypeRepository;

  @Test
  void testUniqueConstraint_statusTypeName() {
    StatusTypeEntity statusTypeEntityOne = TestData.getStatusTypeEntities().getFirst();
    statusTypeEntityOne.setId(null);

    // throws exception for same name and component
    assertThrows(
        DataIntegrityViolationException.class,
        () -> statusTypeRepository.save(statusTypeEntityOne));

    // does not throw for same component different status name
    StatusTypeEntity statusTypeEntityTwo = TestData.getStatusTypeEntities().getFirst();
    statusTypeEntityTwo.setId(null);
    statusTypeEntityTwo.setStatusName("Some New Status");
    statusTypeEntityTwo = statusTypeRepository.save(statusTypeEntityTwo);

    // does not throw exception for different component same status name
    StatusTypeEntity statusTypeEntityThree = TestData.getStatusTypeEntities().getFirst();
    statusTypeEntityThree.setStatusName("Some Other Component");
    statusTypeEntityThree.setId(null);
    statusTypeEntityThree = statusTypeRepository.save(statusTypeEntityThree);

    // cleanup
    statusTypeRepository.deleteAllById(
        List.of(statusTypeEntityTwo.getId(), statusTypeEntityThree.getId()));
  }
}
