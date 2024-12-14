package auth.service.app.repository;

import auth.service.BaseTest;

public class AppUserRepositoryTest extends BaseTest {

  //  @Autowired private AppUserRepository appUserRepository;
  //
  //  @Test
  //  public void testFindByEmail() {
  //    Optional<AppUserEntity> appUserEntityOptional =
  //        appUserRepository.findByEmail("firstlast@ninetynine1.com");
  //
  //    assertTrue(appUserEntityOptional.isPresent());
  //    assertEquals("password-ninetynine1", appUserEntityOptional.get().getPassword());
  //  }
  //
  //  @Test
  //  void testUniqueConstraints() {
  //    // create (email cannot be duplicate)
  //    AppUserEntity appUserEntity = TestData.getAppUserEntities().getFirst();
  //    appUserEntity.setId(null);
  //    assertThrows(
  //        DataIntegrityViolationException.class, () -> appUserRepository.save(appUserEntity));
  //
  //    // partial index not supported in H2 database used for testing
  //    // update (phone number cannot be duplicate if entered)
  //    // appUserEntity.setId(1);
  //    // appUserEntity.setPhone("9876543210");
  //    // assertThrows(
  //    //    DataIntegrityViolationException.class, () -> appUserRepository.save(appUserEntity));
  //  }
}
