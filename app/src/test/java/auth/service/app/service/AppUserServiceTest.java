package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_GUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppUserAddressDto;
import auth.service.app.model.dto.AppUserRequest;
import auth.service.app.model.dto.UserLoginRequest;
import auth.service.app.model.dto.UserUpdateEmailRequest;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.events.AppUserCreatedEvent;
import auth.service.app.model.events.AppUserUpdatedEvent;
import auth.service.app.repository.AppUserRoleRepository;
import auth.service.app.repository.AppsAppUserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class AppUserServiceTest extends BaseTest {

  private static final String OLD_EMAIL = "old@email.com";
  private static final String NEW_EMAIL = "new@email.com";
  private static final String OLD_PASSWORD = "old_password";
  private static final String NEW_PASSWORD = "new_password";
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";

  private static final AppsEntity appsEntity = new AppsEntity();
  private static AppUserRequest appUserRequestNoPassword;
  private static AppUserRequest appUserRequest;
  private static AppUserAddressDto appUserAddressDto;
  private static UserUpdateEmailRequest userUpdateEmailRequest;
  private static UserLoginRequest userLoginRequestForUpdatePassword;

  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private AppUserService appUserService;
  @Autowired private AppsAppUserRepository appsAppUserRepository;
  @Autowired private AppUserRoleRepository appUserRoleRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    appsEntity.setId("app-99");
    appUserRequestNoPassword =
        new AppUserRequest(
            "F Name", "L Name", OLD_EMAIL, null, "", "PENDING", true, Collections.emptyList());
    appUserRequest =
        new AppUserRequest(
            "F Name",
            "L Name",
            OLD_EMAIL,
            null,
            OLD_PASSWORD,
            "PENDING",
            true,
            Collections.emptyList());
    appUserAddressDto =
        new AppUserAddressDto(null, "MAILING", "F Street", "F City", "FS", "US", "13579");
    userUpdateEmailRequest = new UserUpdateEmailRequest(OLD_EMAIL, NEW_EMAIL);
    userLoginRequestForUpdatePassword = new UserLoginRequest(NEW_EMAIL, NEW_PASSWORD);
  }

  @BeforeEach
  void setUpBeforeEach() {
    clearInvocations(applicationEventPublisher);
    doNothing().when(applicationEventPublisher).publishEvent(any(AppUserCreatedEvent.class));
  }

  @Test
  void testCreateAppUser_FailureOnMissingPassword() {
    ElementMissingException exception =
        assertThrows(
            ElementMissingException.class,
            () ->
                appUserService.createAppUser(
                    appsEntity, appUserRequestNoPassword, BASE_URL_FOR_EMAIL));
    assertEquals("[password] is Missing in [User] request", exception.getMessage());
  }

  @Test
  void testAppUserService_CRUD() {
    int userId;
    int addressId;
    String oldHashedPassword;

    /* create */
    AppUserEntity appUserEntity =
        appUserService.createAppUser(appsEntity, appUserRequest, BASE_URL_FOR_EMAIL);
    assertNotNull(appUserEntity);
    assertNotNull(appUserEntity.getId());
    assertNull(appUserEntity.getAddresses());

    userId = appUserEntity.getId();
    oldHashedPassword = appUserEntity.getPassword();

    // verify apps app user entity created
    Optional<AppsAppUserEntity> appsAppUserEntityOptional =
        appsAppUserRepository.findByAppIdAndAppUserEmail(
            appsEntity.getId(), appUserEntity.getEmail());
    assertTrue(appsAppUserEntityOptional.isPresent());
    assertEquals("app-99", appsAppUserEntityOptional.get().getApp().getId());

    // verify app user role entity created
    List<AppUserRoleEntity> appUserRoleEntities =
        appUserRoleRepository.findByIdAppUserIdOrderByAppRoleNameAsc(appUserEntity.getId());
    assertNotNull(appUserRoleEntities);
    assertEquals(1, appUserRoleEntities.size());
    assertEquals(ROLE_NAME_GUEST, appUserRoleEntities.getFirst().getAppRole().getName());

    // verify application event publisher called
    verify(applicationEventPublisher, times(1)).publishEvent(any(AppUserCreatedEvent.class));

    /* update user entity */
    appUserRequest.setAddresses(List.of(appUserAddressDto));
    appUserRequest.setEmail(NEW_EMAIL);

    appUserEntity = appUserService.updateAppUser(userId, appUserRequest);
    assertNotNull(appUserEntity);
    // email, password doesn't change on update
    assertEquals(OLD_EMAIL, appUserEntity.getEmail());
    assertEquals(oldHashedPassword, appUserEntity.getPassword());

    /* read user entity by id */
    appUserEntity = appUserService.readAppUser(userId);
    assertNotNull(appUserEntity);
    assertEquals(1, appUserEntity.getAddresses().size());

    addressId = appUserEntity.getAddresses().getFirst().getId();

    /* update user email */
    appUserEntity =
        appUserService.updateAppUserEmail(
            userId, userUpdateEmailRequest, appsEntity, BASE_URL_FOR_EMAIL);
    assertNotNull(appUserEntity);
    assertEquals(NEW_EMAIL, appUserEntity.getEmail());

    // verify application event publisher called
    verify(applicationEventPublisher, times(1)).publishEvent(any(AppUserUpdatedEvent.class));

    /* update user password */
    appUserEntity = appUserService.updateAppUserPassword(userId, userLoginRequestForUpdatePassword);
    assertNotNull(appUserEntity);
    assertNotEquals(oldHashedPassword, appUserEntity.getPassword());

    /* soft delete user */
    appUserEntity = appUserService.softDeleteAppUser(userId);
    assertNotNull(appUserEntity);
    assertNotNull(appUserEntity.getDeletedDate());

    /* restore soft deleted user */
    appUserEntity = appUserService.restoreSoftDeletedAppUser(userId);
    assertNotNull(appUserEntity);
    assertNull(appUserEntity.getDeletedDate());

    /* read user entity by email */
    appUserEntity = appUserService.readAppUser(NEW_EMAIL);
    assertNotNull(appUserEntity);

    /* delete app user address */
    appUserEntity = appUserService.deleteAppUserAddress(userId, addressId);
    assertNotNull(appUserEntity);
    assertTrue(appUserEntity.getAddresses().isEmpty());

    /* hard delete user */
    // throws exception because used in app_user_app and app_user_role tables
    assertThrows(
        DataIntegrityViolationException.class, () -> appUserService.hardDeleteAppUser(userId));
    appsAppUserRepository.delete(appsAppUserEntityOptional.get());
    appUserRoleRepository.delete(appUserRoleEntities.getFirst());
    appUserService.hardDeleteAppUser(userId);

    // throws not found exception after delete
    ElementNotFoundException exception =
        assertThrows(ElementNotFoundException.class, () -> appUserService.readAppUser(userId));
    assertEquals(String.format("User Not Found for [%s]", userId), exception.getMessage());

    exception =
        assertThrows(ElementNotFoundException.class, () -> appUserService.readAppUser(NEW_EMAIL));
    assertEquals(String.format("User Not Found for [%s]", NEW_EMAIL), exception.getMessage());
  }

  @Test
  void testReadAppUsers() {
    assertEquals(6, appUserService.readAppUsers().size());
  }
}
