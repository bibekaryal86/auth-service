package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.ProfileEntity;
import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class ProfileRepositoryTest extends BaseTest {

  @Autowired private ProfileRepository profileRepository;

  @Test
  public void testFindByEmail() {
    Optional<ProfileEntity> profileEntityOptional =
        profileRepository.findByEmail("firstlast@one.com");

    assertTrue(profileEntityOptional.isPresent());
    assertEquals("password-one", profileEntityOptional.get().getPassword());
  }

  @Test
  void testUniqueConstraint_email() {
    // create (email cannot be duplicate)
    ProfileEntity profileEntityInput = TestData.getProfileEntities().getLast();
    final String original = profileEntityInput.getEmail();
    ProfileEntity profileEntityOutput = new ProfileEntity();
    BeanUtils.copyProperties(profileEntityInput, profileEntityOutput, "id", "addresses");

    // Variable used in lambda expression should be final or effectively final
    final ProfileEntity finalProfileEntityOutput = profileEntityOutput;
    // throws exception for same name
    assertThrows(
        DataIntegrityViolationException.class,
        () -> profileRepository.save(finalProfileEntityOutput));

    // partial index not supported in H2 database used for testing
    // update (phone number cannot be duplicate if entered)
    // profileEntityInput.setId(1L);
    // profileEntityInput.setPhone("9876543210");
    // assertThrows(
    //    DataIntegrityViolationException.class, () -> profileRepository.save(profileEntityInput));

    // does not throw exception for different name
    profileEntityOutput.setEmail("something-new@email.com");
    profileEntityOutput = profileRepository.save(profileEntityOutput);
    assertEquals("something-new@email.com", profileEntityOutput.getEmail());

    // make sure original entity remains unchanged as its used in other tests
    assertEquals(original, profileEntityInput.getEmail());

    // reset
    profileRepository.deleteById(profileEntityOutput.getId());
  }
}
