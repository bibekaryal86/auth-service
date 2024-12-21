package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Autowired private ProfileAddressRepository profileAddressRepository;

  @Test
  void testUniqueConstraint_profileAddressType() {
    // setup
    List<AddressTypeEntity> addressTypeEntities = TestData.getAddressTypeEntities();

    ProfileAddressEntity profileAddressEntityInput =
        TestData.getProfileAddressEntities().getFirst();
    final Long original = profileAddressEntityInput.getType().getId();
    ProfileAddressEntity profileAddressEntityOutput = new ProfileAddressEntity();
    BeanUtils.copyProperties(profileAddressEntityInput, profileAddressEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final ProfileAddressEntity finalProfileAddressEntityOutput = profileAddressEntityOutput;
    // throws exception for same profile same type
    assertThrows(
        DataIntegrityViolationException.class,
        () -> profileAddressRepository.save(finalProfileAddressEntityOutput));

    // does not throw exception for same profile different type
    profileAddressEntityOutput.setType(addressTypeEntities.get(1));
    profileAddressEntityOutput = profileAddressRepository.save(profileAddressEntityOutput);
    assertEquals(2L, profileAddressEntityOutput.getType().getId());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, profileAddressEntityInput.getType().getId());

    // cleanup
    profileAddressRepository.deleteById(profileAddressEntityOutput.getId());
  }
}
