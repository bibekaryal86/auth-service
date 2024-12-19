package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AddressTypeEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class AddressTypeRepositoryTest extends BaseTest {

  @Autowired private AddressTypeRepository addressTypeRepository;

  @Test
  void testUniqueConstraint_addressTypeName() {
    AddressTypeEntity addressTypeEntityInput = TestData.getAddressTypeEntities().getFirst();
    addressTypeEntityInput.setId(null);

    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> addressTypeRepository.save(addressTypeEntityInput));

    // does not throw exception for different name
    addressTypeEntityInput.setTypeName("Some New Type");
    AddressTypeEntity addressTypeEntityOutput = addressTypeRepository.save(addressTypeEntityInput);

    // cleanup
    addressTypeRepository.deleteById(addressTypeEntityOutput.getId());
  }
}
