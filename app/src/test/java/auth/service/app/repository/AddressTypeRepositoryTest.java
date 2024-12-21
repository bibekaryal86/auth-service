package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AddressTypeEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class AddressTypeRepositoryTest extends BaseTest {

  @Autowired private AddressTypeRepository addressTypeRepository;

  @Test
  void testUniqueConstraint_addressTypeName() {
    AddressTypeEntity addressTypeEntityInput = TestData.getAddressTypeEntities().getFirst();
    final String original = addressTypeEntityInput.getTypeName();
    AddressTypeEntity addressTypeEntityOutput = new AddressTypeEntity();
    BeanUtils.copyProperties(addressTypeEntityInput, addressTypeEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final AddressTypeEntity finalAddressTypeEntityOutput = addressTypeEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> addressTypeRepository.save(finalAddressTypeEntityOutput));

    // does not throw exception for different name
    addressTypeEntityOutput.setTypeName("Some New Type");
    addressTypeEntityOutput = addressTypeRepository.save(addressTypeEntityOutput);
    assertEquals("Some New Type", addressTypeEntityOutput.getTypeName());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, addressTypeEntityInput.getTypeName());

    // cleanup
    addressTypeRepository.deleteById(addressTypeEntityOutput.getId());
  }
}
