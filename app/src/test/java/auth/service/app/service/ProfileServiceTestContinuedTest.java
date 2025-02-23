package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.util.JwtUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileServiceTestContinuedTest extends BaseTest {

  private static Long profileId;
  private static Long platformId;
  private static Long roleId;

  private static final String USER_EMAIL = "new@email.com";
  private static final String USER_EMAIL_ENCODED = "very-encoded-email-address";
  private static final String OLD_PASSWORD = "old-password";
  private static final String NEW_PASSWORD = "new-password";

  @Autowired private ProfileService profileService;
  @Autowired private PasswordUtils passwordUtils;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private PlatformRepository platformRepository;

  @BeforeAll
  static void setUp(
      @Autowired ProfileRepository profileRepository,
      @Autowired PlatformProfileRoleRepository platformProfileRoleRepository,
      @Autowired PasswordUtils passwordUtils) {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getLast();
    ProfileEntity profileEntity = TestData.getProfileEntities().getLast();
    RoleEntity roleEntity = TestData.getRoleEntities().getLast();

    profileEntity.setEmail(USER_EMAIL);
    profileEntity.setPassword(passwordUtils.hashPassword(OLD_PASSWORD));
    profileEntity.setId(null);
    profileEntity.setIsValidated(true);
    profileEntity = profileRepository.save(profileEntity);

    profileId = profileEntity.getId();
    platformId = platformEntity.getId();
    roleId = roleEntity.getId();

    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setId(new PlatformProfileRoleId(platformId, profileId, roleId));
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    platformProfileRoleRepository.save(platformProfileRoleEntity);
  }

  @AfterAll
  static void tearDown(
      @Autowired ProfileRepository profileRepository,
      @Autowired PlatformProfileRoleRepository platformProfileRoleRepository,
      @Autowired TokenRepository tokenRepository) {
    tokenRepository.deleteAll();
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(platformId, profileId, roleId));
    profileRepository.deleteById(profileId);
  }

  @Test
  void testLoginProfile_Success() {
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        profileService.loginProfile(
            platformId, new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD), "some-ip-address");
    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getAToken());
    assertNotNull(profilePasswordTokenResponse.getRToken());
  }

  @Test
  void testLoginProfile_Failure() {
    ProfileNotAuthorizedException exception =
        assertThrows(
            ProfileNotAuthorizedException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, NEW_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        "Unauthorized Profile, email and/or password not found in the system...",
        exception.getMessage());
  }

  @Test
  void testLoginProfile_DeletedPlatform() {
    PlatformEntity platformEntity = platformRepository.findById(platformId).orElse(null);
    assertNotNull(platformEntity);
    platformEntity.setDeletedDate(LocalDateTime.now());
    platformRepository.save(platformEntity);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        String.format("Active Platform Not Found for [%s]", platformId), exception.getMessage());

    // reset
    platformEntity.setDeletedDate(null);
    platformEntity = platformRepository.save(platformEntity);
    assertNotNull(platformEntity);
    assertNull(platformEntity.getDeletedDate());
  }

  @Test
  void testLoginProfile_DeletedProfile() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setDeletedDate(LocalDateTime.now());
    profileRepository.save(profileEntity);

    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        String.format("Active Profile Not Found for [%s]", USER_EMAIL), exception.getMessage());

    // reset
    profileEntity.setDeletedDate(null);
    profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
  }

  @Test
  void testLoginProfile_NotValidatedProfile() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setIsValidated(false);
    profileRepository.save(profileEntity);

    ProfileNotValidatedException exception =
        assertThrows(
            ProfileNotValidatedException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        "Profile not validated, please check your email for instructions to validate account!",
        exception.getMessage());

    // reset
    profileEntity.setIsValidated(true);
    profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
  }

  @Test
  void testLoginProfile_NotActiveProfile() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setLastLogin(LocalDateTime.now().minusDays(46));
    profileRepository.save(profileEntity);

    ProfileNotActiveException exception =
        assertThrows(
            ProfileNotActiveException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        "Profile is not active, please revalidate or reset your account!", exception.getMessage());

    // reset
    profileEntity.setLastLogin(null);
    profileRepository.save(profileEntity);
  }

  @Test
  void testLoginProfile_ExceedLoginAttempts() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setLoginAttempts(5);
    profileRepository.save(profileEntity);

    ProfileLockedException exception =
        assertThrows(
            ProfileLockedException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals("Profile is locked, please reset your account!", exception.getMessage());

    // reset
    profileEntity.setLoginAttempts(0);
    profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
  }

  @Test
  void testLoginProfile_LastLoginBefore45Days() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);
    profileEntity.setLastLogin(LocalDateTime.now().minusDays(46));
    profileRepository.save(profileEntity);

    ProfileNotActiveException exception =
        assertThrows(
            ProfileNotActiveException.class,
            () ->
                profileService.loginProfile(
                    platformId,
                    new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD),
                    "some-ip-address"));
    assertEquals(
        "Profile is not active, please revalidate or reset your account!", exception.getMessage());

    // reset
    profileEntity.setLoginAttempts(0);
    profileEntity.setLastLogin(LocalDateTime.now().minusDays(1));
    profileRepository.save(profileEntity);
    assertNotNull(profileEntity);
  }

  @Test
  void testResetProfile() {
    ProfileEntity profileEntity =
        profileService.resetProfile(
            platformId, new ProfilePasswordRequest(USER_EMAIL, NEW_PASSWORD));
    assertNotNull(profileEntity);
    assertFalse(passwordUtils.verifyPassword(OLD_PASSWORD, profileEntity.getPassword()));
    assertTrue(passwordUtils.verifyPassword(NEW_PASSWORD, profileEntity.getPassword()));

    // reset so that other test cases pass
    profileEntity =
        profileService.resetProfile(
            platformId, new ProfilePasswordRequest(USER_EMAIL, OLD_PASSWORD));
    assertNotNull(profileEntity);
    assertTrue(passwordUtils.verifyPassword(OLD_PASSWORD, profileEntity.getPassword()));
    assertFalse(passwordUtils.verifyPassword(NEW_PASSWORD, profileEntity.getPassword()));
  }

  @Test
  void testValidateAndResetProfile() {
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);

    // set it as false for testing
    profileEntity.setIsValidated(false);
    profileRepository.save(profileEntity);

    try (MockedStatic<JwtUtils> mockedStatic = mockStatic(JwtUtils.class)) {
      mockedStatic
          .when(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED))
          .thenReturn(USER_EMAIL);

      profileEntity = profileService.validateAndResetProfile(platformId, USER_EMAIL_ENCODED, false);
      assertNotNull(profileEntity);
      assertFalse(profileEntity.getIsValidated());

      profileEntity = profileService.validateAndResetProfile(platformId, USER_EMAIL_ENCODED, true);
      assertNotNull(profileEntity);
      assertTrue(profileEntity.getIsValidated());

      mockedStatic.verify(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED), times(2));
    }
  }
}
