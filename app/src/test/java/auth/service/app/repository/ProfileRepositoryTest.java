package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.ProfileEntity;
import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
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
    ProfileEntity profileEntityInput = TestData.getProfileEntities().getFirst();
    profileEntityInput.setId(null);
    assertThrows(
        DataIntegrityViolationException.class, () -> profileRepository.save(profileEntityInput));

    // partial index not supported in H2 database used for testing
    // update (phone number cannot be duplicate if entered)
    // profileEntityInput.setId(1L);
    // profileEntityInput.setPhone("9876543210");
    // assertThrows(
    //    DataIntegrityViolationException.class, () -> profileRepository.save(profileEntityInput));

    profileEntityInput.setEmail("something-new@email.com");
    profileEntityInput.setAddresses(null);
    ProfileEntity profileEntityOutput = profileRepository.save(profileEntityInput);
    assertEquals("something-new@email.com", profileEntityOutput.getEmail());

    // reset
    profileRepository.deleteById(profileEntityOutput.getId());
  }
}
