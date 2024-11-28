package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppUserAddressEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.enums.StatusEnums;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class AppUserAddressRepositoryTest extends BaseTest {

  @Autowired private AppUserAddressRepository appUserAddressRepository;

  @Test
  void testUniqueConstraints() {
    AppUserEntity appUserEntity = TestData.getAppUserEntities().getFirst();
    AppUserAddressEntity appUserAddressEntity = appUserEntity.getAddresses().getFirst();
    appUserAddressEntity.setId(null);
    appUserAddressEntity.setAppUser(appUserEntity);

    // throws exception because another address exists for user with same address type
    assertThrows(
        DataIntegrityViolationException.class,
        () -> appUserAddressRepository.save(appUserAddressEntity));

    appUserAddressEntity.setAddressType(StatusEnums.AddressTypes.BILLING.name());
    assertDoesNotThrow(
        () -> {
          // does not throw exception because address type is different
          AppUserAddressEntity appUserAddressEntityNew =
              appUserAddressRepository.save(appUserAddressEntity);
          // cleanup
          appUserAddressRepository.deleteById(appUserAddressEntityNew.getId());
        });
  }
}
