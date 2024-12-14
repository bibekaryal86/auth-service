package auth.service.app.repository;

import auth.service.BaseTest;

public class AppUserAddressRepositoryTest extends BaseTest {

  //  @Autowired private AppUserAddressRepository appUserAddressRepository;
  //
  //  @Test
  //  void testUniqueConstraints() {
  //    AppUserEntity appUserEntity = TestData.getAppUserEntities().getFirst();
  //    AppUserAddressEntity appUserAddressEntity = appUserEntity.getAddresses().getFirst();
  //    appUserAddressEntity.setId(null);
  //    appUserAddressEntity.setAppUser(appUserEntity);
  //
  //    // throws exception because another address exists for user with same address type
  //    assertThrows(
  //        DataIntegrityViolationException.class,
  //        () -> appUserAddressRepository.save(appUserAddressEntity));
  //
  //    appUserAddressEntity.setAddressType(StatusEnums.AddressTypes.BILLING.name());
  //    assertDoesNotThrow(
  //        () -> {
  //          // does not throw exception because address type is different
  //          AppUserAddressEntity appUserAddressEntityNew =
  //              appUserAddressRepository.save(appUserAddressEntity);
  //          // cleanup
  //          appUserAddressRepository.deleteById(appUserAddressEntityNew.getId());
  //        });
  //  }
}
