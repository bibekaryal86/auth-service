package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppUserEntity;

public class AppUserRepositoryTest extends BaseTest {

  @Autowired private AppUserRepository appUserRepository;

  @Test
  public void testFindByEmail() {
    Optional<AppUserEntity> appUserEntityOptional =
        appUserRepository.findByEmail("firstlast@ninetynine1.com");

    assertTrue(appUserEntityOptional.isPresent());
    assertEquals("password-ninetynine1", appUserEntityOptional.get().getPassword());
  }

  @Test
  void testUniqueConstraints() {
    // create (email cannot be duplicate)
    AppUserEntity appUserEntity = TestData.getAppUserEntities().getFirst();
    appUserEntity.setId(null);
    assertThrows(
        DataIntegrityViolationException.class, () -> appUserRepository.save(appUserEntity));

    // update (phone number cannot be duplicate if entered)
    appUserEntity.setId(1);
    appUserEntity.setPhone("9876543210");
    assertThrows(
        DataIntegrityViolationException.class, () -> appUserRepository.save(appUserEntity));
  }
}
