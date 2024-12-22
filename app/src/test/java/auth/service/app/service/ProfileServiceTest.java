package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_GUEST;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.ProfileAddressRequest;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.TokenRepository;
import auth.service.app.util.JwtUtils;
import auth.service.app.util.PasswordUtils;
import helper.TestData;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ProfileServiceTest extends BaseTest {

  private static final String OLD_EMAIL = "old@email.com";
  private static final String NEW_EMAIL = "new@email.com";
  private static final String OLD_PASSWORD = "old_password";
  private static final String NEW_PASSWORD = "new_password";
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";
  private static final String USER_EMAIL_ENCODED = "very-encoded-email-address";
  private static PlatformEntity platformEntity;
  private static PlatformProfileRoleId platformProfileRoleId;
  private static ProfileEntity profileEntityRV;

  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private ProfileRepository profileRepository;
  @Autowired private ProfileService profileService;
  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;
  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private CircularDependencyService circularDependencyService;
  @Autowired private PasswordUtils passwordUtils;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileEntityRV = TestData.getProfileEntities().getLast();
  }

  @BeforeEach
  void setUpBeforeEach() {
    clearInvocations(applicationEventPublisher);
    doNothing().when(applicationEventPublisher).publishEvent(any(ProfileEvent.class));
  }

  @AfterAll
  static void tearDown(
      @Autowired TokenRepository tokenRepository,
      @Autowired PlatformProfileRoleService platformProfileRoleService) {
    tokenRepository.deleteAll();
    platformProfileRoleService.deletePlatformProfileRole(
        platformProfileRoleId.getPlatformId(),
        platformProfileRoleId.getProfileId(),
        platformProfileRoleId.getRoleId());
  }

  @Test
  void testCreateProfile_FailureOnMissingPassword() {
    ProfileRequest profileRequestNoPassword =
        TestData.getProfileRequest("First", "Last", "some@email.com", null);
    ElementMissingException exception =
        assertThrows(
            ElementMissingException.class,
            () ->
                profileService.createProfile(
                    platformEntity, profileRequestNoPassword, BASE_URL_FOR_EMAIL));
    assertEquals("[password] is Missing in [Profile] request", exception.getMessage());
  }

  @Test
  void testReadProfiles() {
    List<ProfileEntity> profileEntities = profileService.readProfiles();
    assertEquals(6, profileEntities.size());
    // check sorted by last name
    assertEquals("Last One", profileEntities.getFirst().getLastName());
    assertEquals("Last Two-1", profileEntities.getLast().getLastName());
  }

  @Test
  void testReadProfileByEmail() {
    ProfileEntity profileEntity = profileService.readProfileByEmail(EMAIL);
    assertNotNull(profileEntity);
    assertEquals(ID, profileEntity.getId());
  }

  @Test
  void testReadProfileByEmail_NotFound() {
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> profileService.readProfileByEmail("some@email.com"));
    assertEquals("Profile Not Found for [some@email.com]", exception.getMessage());
  }

  @Test
  void testReadProfileByEmailNoException() {
    assertDoesNotThrow(() -> profileService.readProfileByEmailNoException("some@email.com"));
  }

  @Test
  void testProfileService_CRUD() {
    String oldFirstName = "Old First";
    String oldLastName = "Old Last";
    String newFirstName = "New First";
    String newLastName = "New Last";

    // Create
    ProfileRequest profileRequest =
        TestData.getProfileRequest(oldFirstName, oldLastName, OLD_EMAIL, OLD_PASSWORD);
    ProfileEntity profileEntityCreated = assertCreate(profileRequest);

    Long profileId = profileEntityCreated.getId();
    final String oldHashedPassword = profileEntityCreated.getPassword();

    // Update
    ProfileAddressRequest profileAddressRequest =
        TestData.getProfileAddressRequest(profileId, 1L, "some-street");
    profileRequest = TestData.getProfileRequest(newFirstName, newLastName, NEW_EMAIL, NEW_PASSWORD);
    profileRequest.getAddresses().add(profileAddressRequest);
    assertUpdate(profileId, profileRequest, oldHashedPassword);

    // Read
    ProfileAddressEntity profileAddressEntity = assertRead(profileId, newFirstName, newLastName);
    Long addressId = profileAddressEntity.getId();

    // Update Email
    ProfileEmailRequest profileEmailRequest = new ProfileEmailRequest(OLD_EMAIL, NEW_EMAIL);
    assertUpdateEmail(profileId, profileEmailRequest);

    // Update Password
    ProfilePasswordRequest profilePasswordRequest =
        new ProfilePasswordRequest(NEW_EMAIL, NEW_PASSWORD);
    assertUpdatePassword(profileId, profilePasswordRequest, oldHashedPassword);

    // Soft delete
    assertDeleteSoft(profileId);

    // Restore
    assertRestoreSoftDeleted(profileId);

    // Delete Profile Address
    assertDeleteProfileAddress(profileId, addressId);

    // Hard Delete
    assertDeleteHard(profileId);
  }

  private ProfileEntity assertCreate(ProfileRequest request) {
    ProfileEntity profileEntity =
        profileService.createProfile(platformEntity, request, BASE_URL_FOR_EMAIL);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getId());
    assertNull(profileEntity.getAddresses());

    // verify platform profile role created
    PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.readPlatformProfileRole(
            platformEntity.getId(), profileEntity.getEmail());
    assertEquals(platformEntity.getId(), platformProfileRoleEntity.getPlatform().getId());
    assertEquals(profileEntity.getId(), platformProfileRoleEntity.getProfile().getId());
    assertEquals(ROLE_NAME_GUEST, platformProfileRoleEntity.getRole().getRoleName());

    // verify application event publisher called
    verify(applicationEventPublisher, times(1))
        .publishEvent(
            argThat(
                event -> {
                  ProfileEvent profileEvent = (ProfileEvent) event;
                  assertEquals(TypeEnums.EventType.CREATE, profileEvent.getEventType());
                  return true;
                }));

    // return profile profileEntity
    return profileEntity;
  }

  private void assertUpdate(Long id, ProfileRequest request, String oldHashedPassword) {
    ProfileEntity profileEntity = profileService.updateProfile(id, request);
    assertNotNull(profileEntity);
    // email, password doesn't change on update
    assertEquals(OLD_EMAIL, profileEntity.getEmail());
    assertEquals(oldHashedPassword, profileEntity.getPassword());
  }

  private ProfileAddressEntity assertRead(Long id, String expectedName, String expectedLastName) {
    ProfileEntity profileEntity = profileService.readProfile(id);
    assertNotNull(profileEntity);
    assertEquals(expectedName, profileEntity.getFirstName());
    assertEquals(expectedLastName, profileEntity.getLastName());
    assertNull(profileEntity.getDeletedDate());
    assertFalse(profileEntity.getAddresses().isEmpty());
    return profileEntity.getAddresses().getFirst();
  }

  private void assertUpdateEmail(Long id, ProfileEmailRequest profileEmailRequest) {
    reset(applicationEventPublisher);
    ProfileEntity profileEntity =
        profileService.updateProfileEmail(
            id, profileEmailRequest, platformEntity, BASE_URL_FOR_EMAIL);
    assertNotNull(profileEntity);
    assertEquals(NEW_EMAIL, profileEntity.getEmail());

    // verify application event publisher called
    verify(applicationEventPublisher, times(1))
        .publishEvent(
            argThat(
                event -> {
                  ProfileEvent profileEvent = (ProfileEvent) event;
                  assertEquals(TypeEnums.EventType.UPDATE_EMAIL, profileEvent.getEventType());
                  return true;
                }));
  }

  private void assertUpdatePassword(
      Long id, ProfilePasswordRequest profilePasswordRequest, String oldHashedPassword) {
    reset(applicationEventPublisher);
    ProfileEntity profileEntity =
        profileService.updateProfilePassword(id, profilePasswordRequest, platformEntity);
    assertNotNull(profileEntity);
    assertNotEquals(oldHashedPassword, profileEntity.getPassword());

    // verify application event publisher called
    verify(applicationEventPublisher, times(1))
        .publishEvent(
            argThat(
                event -> {
                  ProfileEvent profileEvent = (ProfileEvent) event;
                  assertEquals(TypeEnums.EventType.UPDATE_PASSWORD, profileEvent.getEventType());
                  return true;
                }));
  }

  private void assertDeleteSoft(Long id) {
    ProfileEntity profileEntity = profileService.softDeleteProfile(id);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getDeletedDate());
  }

  private void assertRestoreSoftDeleted(Long id) {
    ProfileEntity profileEntity = profileService.restoreSoftDeletedProfile(id);
    assertNotNull(profileEntity);
    assertNull(profileEntity.getDeletedDate());
  }

  private void assertDeleteProfileAddress(Long profileId, Long addressId) {
    ProfileEntity profileEntity = profileService.deleteProfileAddress(profileId, addressId);
    assertNotNull(profileEntity);
    assertTrue(profileEntity.getAddresses().isEmpty());
  }

  private void assertDeleteHard(Long profileId) {
    // throws exception because used in platform profile role table
    assertThrows(
        DataIntegrityViolationException.class, () -> profileService.hardDeleteProfile(profileId));
    // delete from platform profile role service first
    Long roleId = circularDependencyService.readRoleByName(ROLE_NAME_GUEST).getId();
    platformProfileRoleService.deletePlatformProfileRole(platformEntity.getId(), profileId, roleId);

    // delete profile
    profileService.hardDeleteProfile(profileId);
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> profileService.readProfile(profileId),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Profile Not Found for [%s]", profileId),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  @Test
  void testResetUser() {
    RoleEntity roleEntity = TestData.getRoleEntities().getLast();
    PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleRepository
            .findById(
                new PlatformProfileRoleId(
                    platformEntity.getId(), profileEntityRV.getId(), roleEntity.getId()))
            .orElseGet(
                () ->
                    platformProfileRoleService.createPlatformProfileRole(
                        new PlatformProfileRoleRequest(
                            platformEntity.getId(), profileEntityRV.getId(), roleEntity.getId())));
    platformProfileRoleId = platformProfileRoleEntity.getId();

    profileEntityRV =
        profileService.resetProfile(
            platformEntity.getId(),
            new ProfilePasswordRequest(profileEntityRV.getEmail(), NEW_PASSWORD));
    assertNotNull(profileEntityRV);
    assertFalse(passwordUtils.verifyPassword(OLD_PASSWORD, profileEntityRV.getPassword()));
    assertTrue(passwordUtils.verifyPassword(NEW_PASSWORD, profileEntityRV.getPassword()));
  }

  @Test
  void testValidateAndResetProfile() {
    RoleEntity roleEntity = TestData.getRoleEntities().getLast();
    Long profileId = TestData.getProfileEntities().getLast().getId();
    ProfileEntity profileEntity = profileRepository.findById(profileId).orElse(null);
    assertNotNull(profileEntity);

    // set it as false for testing
    profileEntity.setIsValidated(false);
    profileRepository.save(profileEntity);

    try (MockedStatic<JwtUtils> mockedStatic = mockStatic(JwtUtils.class)) {
      mockedStatic
          .when(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED))
          .thenReturn(profileEntity.getEmail());

      PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleRepository
              .findById(
                  new PlatformProfileRoleId(
                      platformEntity.getId(), profileEntityRV.getId(), roleEntity.getId()))
              .orElseGet(
                  () ->
                      platformProfileRoleService.createPlatformProfileRole(
                          new PlatformProfileRoleRequest(
                              platformEntity.getId(),
                              profileEntityRV.getId(),
                              roleEntity.getId())));
      platformProfileRoleId = platformProfileRoleEntity.getId();

      profileEntity =
          profileService.validateAndResetProfile(platformEntity.getId(), USER_EMAIL_ENCODED, false);
      assertNotNull(profileEntity);
      assertFalse(profileEntity.getIsValidated());

      profileEntity =
          profileService.validateAndResetProfile(platformEntity.getId(), USER_EMAIL_ENCODED, true);
      assertNotNull(profileEntity);
      assertTrue(profileEntity.getIsValidated());

      mockedStatic.verify(() -> JwtUtils.decodeEmailAddress(USER_EMAIL_ENCODED), times(2));
    }
  }
}
