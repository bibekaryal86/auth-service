package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppsAppUserRequest;
import user.management.system.app.model.entity.AppsAppUserEntity;

public class AppsAppUserServiceTest extends BaseTest {

  @Autowired private AppsAppUserService appsAppUserService;

  @Test
  void testAppsAppUserService_CRUD() {
    String appId = "app-1";
    int userId = 6;
    String userEmail = "firstlast@ninetynine3.com";
    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest(appId, userId);

    // create
    AppsAppUserEntity appsAppUserEntity = appsAppUserService.createAppsAppUser(appsAppUserRequest);
    assertNotNull(appsAppUserEntity);
    assertNotNull(appsAppUserEntity.getId());

    // update, not available

    // read
    appsAppUserEntity = appsAppUserService.readAppsAppUser(appId, userEmail);
    assertNotNull(appsAppUserEntity);
    assertNotNull(appsAppUserEntity.getId());
    assertEquals("App Description One", appsAppUserEntity.getApp().getDescription());
    assertEquals("First Ninety Nine3", appsAppUserEntity.getAppUser().getFirstName());
    assertNotNull(appsAppUserEntity.getAssignedDate());

    // hard delete
    appsAppUserService.deleteAppsAppUser(appId, userEmail);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> appsAppUserService.readAppsAppUser(appId, userEmail));
    assertEquals(
        String.format("Apps App User Not Found for [%s,%s]", appId, userEmail),
        exception.getMessage());
  }

  @Test
  void testReadAppsAppUsers() {
    assertEquals(6, appsAppUserService.readAppsAppUsers().size());
  }

  @Test
  void testReadAppsAppUsers_ByAppId() {
    assertEquals(3, appsAppUserService.readAppsAppUsersByAppId("app-99").size());
  }

  @Test
  void testReadAppsAppUsers_ByUserId() {
    assertEquals(1, appsAppUserService.readAppsAppUsersByUserId(1).size());
  }
}
