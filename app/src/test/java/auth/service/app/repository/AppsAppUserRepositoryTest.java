package auth.service.app.repository;

import auth.service.BaseTest;

public class AppsAppUserRepositoryTest extends BaseTest {

  //  @Autowired private AppsAppUserRepository appsAppUserRepository;
  //
  //  @Test
  //  public void testFindByAppIdAndAppUserEmail() {
  //    Optional<AppsAppUserEntity> appsAppUserEntityOptional =
  //        appsAppUserRepository.findByAppIdAndAppUserEmail("app-99", "firstlast@ninetynine1.com");
  //
  //    assertTrue(appsAppUserEntityOptional.isPresent());
  //    assertEquals("app-99", appsAppUserEntityOptional.get().getApp().getId());
  //    assertEquals(
  //        "password-ninetynine1", appsAppUserEntityOptional.get().getAppUser().getPassword());
  //    assertEquals(
  //        new AppsAppUserId(
  //            appsAppUserEntityOptional.get().getApp().getId(),
  //            appsAppUserEntityOptional.get().getAppUser().getId()),
  //        appsAppUserEntityOptional.get().getId());
  //  }
  //
  //  @Test
  //  public void testFindAllByAppIdOrderByAppUserLastNameDesc() {
  //    List<AppsAppUserEntity> appsAppUserEntities =
  //        appsAppUserRepository.findAllByAppIdOrderByAppUserLastNameDesc("app-99");
  //
  //    assertNotNull(appsAppUserEntities);
  //    assertEquals(3, appsAppUserEntities.size());
  //    assertEquals("Last Ninety Nine3", appsAppUserEntities.get(0).getAppUser().getLastName());
  //    assertEquals("Last Ninety Nine2", appsAppUserEntities.get(1).getAppUser().getLastName());
  //    assertEquals("Last Ninety Nine1", appsAppUserEntities.get(2).getAppUser().getLastName());
  //  }
  //
  //  @Test
  //  public void testFindAllByAppUserIdOrderByAppNameAsc() {
  //    List<AppsAppUserEntity> appsAppUserEntities =
  //        appsAppUserRepository.findAllByAppUserIdOrderByAppNameAsc(4);
  //
  //    assertNotNull(appsAppUserEntities);
  //    assertEquals(1, appsAppUserEntities.size());
  //    assertEquals("Last Ninety Nine1",
  // appsAppUserEntities.getFirst().getAppUser().getLastName());
  //  }
}
