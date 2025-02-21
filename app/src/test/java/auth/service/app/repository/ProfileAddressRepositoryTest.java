package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class ProfileAddressRepositoryTest extends BaseTest {

  @Autowired private ProfileAddressRepository profileAddressRepository;

  @Test
  void testUniqueConstraint_profile() {
    // setup
    ProfileEntity profileEntity = TestData.getProfileEntities().getLast();
    ProfileAddressEntity profileAddressEntityInput =
        TestData.getProfileAddressEntities().getFirst();
    final Long original = profileAddressEntityInput.getProfile().getId();
    ProfileAddressEntity profileAddressEntityOutput = new ProfileAddressEntity();
    BeanUtils.copyProperties(profileAddressEntityInput, profileAddressEntityOutput, "id");

    // Variable used in lambda expression should be final or effectively final
    final ProfileAddressEntity finalProfileAddressEntityOutput = profileAddressEntityOutput;
    // throws exception for same profile same type
    assertThrows(
        DataIntegrityViolationException.class,
        () -> profileAddressRepository.save(finalProfileAddressEntityOutput));

    // does not throw exception for same address, different profile
    profileAddressEntityOutput.setProfile(profileEntity);
    profileAddressEntityOutput = profileAddressRepository.save(profileAddressEntityOutput);
    assertEquals(13L, profileAddressEntityOutput.getProfile().getId());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, profileAddressEntityInput.getProfile().getId());

    // cleanup
    profileAddressRepository.deleteById(profileAddressEntityOutput.getId());
  }
}
