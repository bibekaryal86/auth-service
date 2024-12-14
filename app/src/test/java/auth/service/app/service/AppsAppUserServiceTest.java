package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppsAppUserRequest;
import auth.service.app.model.entity.AppsAppUserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppsAppUserServiceTest extends BaseTest {

  @Autowired private AppsAppUserService appsAppUserService;

  @Test
  void testAppsAppUserService_CRUD() {
    int userId = 6;
    String userEmail = "firstlast@ninetynine3.com";
    AppsAppUserRequest appsAppUserRequest = new AppsAppUserRequest(PLATFORM_ID, userId);

    // create
    AppsAppUserEntity appsAppUserEntity = appsAppUserService.createAppsAppUser(appsAppUserRequest);
    assertNotNull(appsAppUserEntity);
    assertNotNull(appsAppUserEntity.getId());

    // update, not available

    // read
    appsAppUserEntity = appsAppUserService.readAppsAppUser(PLATFORM_ID, userEmail);
    assertNotNull(appsAppUserEntity);
    assertNotNull(appsAppUserEntity.getId());
    assertEquals("App Description One", appsAppUserEntity.getApp().getDescription());
    assertEquals("First Ninety Nine3", appsAppUserEntity.getAppUser().getFirstName());
    assertNotNull(appsAppUserEntity.getAssignedDate());

    // hard delete
    appsAppUserService.deleteAppsAppUser(PLATFORM_ID, userEmail);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> appsAppUserService.readAppsAppUser(PLATFORM_ID, userEmail));
    assertEquals(
        String.format("Apps App User Not Found for [%s,%s]", PLATFORM_ID, userEmail),
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
