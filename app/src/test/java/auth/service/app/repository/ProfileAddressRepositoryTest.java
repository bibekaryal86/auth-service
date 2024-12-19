package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class ProfileAddressRepositoryTest extends BaseTest {

  // TODO this needs reworking after including profile and address in sql
  @Autowired private ProfileAddressRepository profileAddressRepository;

  @Test
  void testUniqueConstraint_profileAddressType() {
    // setup
    List<AddressTypeEntity> addressTypeEntities = TestData.getAddressTypeEntities();

    ProfileAddressEntity profileAddressEntityOneInput =
        TestData.getProfileAddressEntities().getFirst();
    profileAddressEntityOneInput.setId(null);

    // throws exception for same profile same type
    assertThrows(
        DataIntegrityViolationException.class,
        () -> profileAddressRepository.save(profileAddressEntityOneInput));

    // does not throw exception for same profile different type
    ProfileAddressEntity profileAddressEntityTwoInput = new ProfileAddressEntity();
    BeanUtils.copyProperties(profileAddressEntityOneInput, profileAddressEntityTwoInput);
    profileAddressEntityTwoInput.setType(addressTypeEntities.get(1));
    ProfileAddressEntity profileAddressEntityTwoOutput =
        profileAddressRepository.save(profileAddressEntityTwoInput);

    // cleanup
    profileAddressRepository.deleteById(profileAddressEntityTwoOutput.getId());
  }
}
