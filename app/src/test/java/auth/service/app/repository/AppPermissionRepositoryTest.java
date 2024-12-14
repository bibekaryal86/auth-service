package auth.service.app.repository;

import auth.service.BaseTest;

public class AppPermissionRepositoryTest extends BaseTest {

  //  @Autowired private AppPermissionRepository appPermissionRepository;
  //
  //  @Test
  //  public void testFindByAppIdOrderByNameAsc() {
  //    List<AppPermissionEntity> permissions =
  //        appPermissionRepository.findByAppIdOrderByNameAsc("app-99");
  //
  //    assertNotNull(permissions);
  //    assertEquals(3, permissions.size());
  //    assertEquals("Permission A", permissions.get(0).getName());
  //    assertEquals("Permission V", permissions.get(1).getName());
  //    assertEquals("Permission Z", permissions.get(2).getName());
  //  }
  //
  //  @Test
  //  void testUniqueConstraints() {
  //    AppPermissionEntity appPermissionEntity = TestData.getAppPermissionEntities().getFirst();
  //    appPermissionEntity.setId(null);
  //
  //    // throws exception for same name and appId
  //    assertThrows(
  //        DataIntegrityViolationException.class,
  //        () -> appPermissionRepository.save(appPermissionEntity));
  //
  //    appPermissionEntity.setAppId("app-99");
  //    assertDoesNotThrow(
  //        () -> {
  //          // does not throw exception because appId is different for same name
  //          AppPermissionEntity appPermissionEntityNew =
  //              appPermissionRepository.save(appPermissionEntity);
  //          // cleanup
  //          appPermissionRepository.deleteById(appPermissionEntityNew.getId());
  //        });
  //  }
}
