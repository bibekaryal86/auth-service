package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsAppUserId;

public class AppsAppUserRepositoryTest extends BaseTest {

  @Autowired private AppsAppUserRepository appsAppUserRepository;

  @Test
  public void testFindByAppIdAndAppUserEmail() {
    Optional<AppsAppUserEntity> appsAppUserEntityOptional =
        appsAppUserRepository.findByAppIdAndAppUserEmail("app-99", "firstlast@ninetynine1.com");

    assertTrue(appsAppUserEntityOptional.isPresent());
    assertEquals(appsAppUserEntityOptional.get().getApp().getId(), "app-99");
    assertEquals(
        appsAppUserEntityOptional.get().getAppUser().getPassword(), "password-ninetynine1");
    assertEquals(
        appsAppUserEntityOptional.get().getId(),
        new AppsAppUserId(
            appsAppUserEntityOptional.get().getApp().getId(),
            appsAppUserEntityOptional.get().getAppUser().getId()));
  }

  @Test
  public void testFindAllByAppIdOrderByAppUserLastNameDesc() {
    List<AppsAppUserEntity> appsAppUserEntities =
        appsAppUserRepository.findAllByAppIdOrderByAppUserLastNameDesc("app-99");

    assertNotNull(appsAppUserEntities);
    assertEquals(3, appsAppUserEntities.size());

    assertEquals(appsAppUserEntities.get(0).getAppUser().getLastName(), "Last Ninety Nine3");
    assertEquals(appsAppUserEntities.get(1).getAppUser().getLastName(), "Last Ninety Nine2");
    assertEquals(appsAppUserEntities.get(2).getAppUser().getLastName(), "Last Ninety Nine1");
  }

  @Test
  public void testFindAllByAppUserIdOrderByAppNameAsc() {
    List<AppsAppUserEntity> appsAppUserEntities =
        appsAppUserRepository.findAllByAppUserIdOrderByAppNameAsc(4);

    assertNotNull(appsAppUserEntities);
    assertEquals(1, appsAppUserEntities.size());

    assertEquals(appsAppUserEntities.getFirst().getAppUser().getLastName(), "Last Ninety Nine1");
  }
}
