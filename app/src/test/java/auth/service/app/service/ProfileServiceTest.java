package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_GUEST;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfileAddressRequest;
import auth.service.app.model.dto.ProfileEmailRequest;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.util.PasswordUtils;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ProfileServiceTest extends BaseTest {

  private static final String OLD_EMAIL = "old@email.com";
  private static final String NEW_EMAIL = "new@email.com";
  private static final String OLD_PASSWORD = "old_password";
  private static final String NEW_PASSWORD = "new_password";
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";
  private static final String USER_EMAIL_ENCODED = "very-encoded-email-address";
  private static PlatformEntity platformEntity;

  @Mock private SecurityContext securityContext;
  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private ProfileRepository profileRepository;
  @Autowired private ProfileService profileService;
  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;
  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private CircularDependencyService circularDependencyService;
  @Autowired private RoleService roleService;
  @Autowired private PasswordUtils passwordUtils;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
  }

  @BeforeEach
  void setUpBeforeEach() {
    clearInvocations(applicationEventPublisher);
    doNothing().when(applicationEventPublisher).publishEvent(any(ProfileEvent.class));
  }

  @Test
  void testReadProfiles_noRequestMetadata() {
    Page<ProfileEntity> profileEntityPage = profileService.readProfiles(null);
    List<ProfileEntity> profileEntities = profileEntityPage.toList();
    assertEquals(9, profileEntities.size());
    assertEquals(1, profileEntityPage.getTotalPages());
    assertEquals(100, profileEntityPage.getSize());
    // check sorted by last name
    assertEquals("Last Eight", profileEntities.getFirst().getLastName());
    assertEquals("Last Two", profileEntities.getLast().getLastName());
  }

  @Test
  void testReadProfiles_requestMetadata() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    AuthToken authToken = TestData.getAuthToken();
    authToken.setSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    Page<ProfileEntity> profileEntityPage =
        profileService.readProfiles(RequestMetadata.builder().isIncludeDeleted(true).build());
    List<ProfileEntity> profileEntities = profileEntityPage.toList();
    assertEquals(13, profileEntities.size());
    assertEquals(1, profileEntityPage.getTotalPages());
    assertEquals(100, profileEntityPage.getSize());
    // check sorted by name
    assertEquals("Last Eight", profileEntities.getFirst().getLastName());
    assertEquals("Last Two", profileEntities.getLast().getLastName());
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
    // setup
    String oldFirstName = "Old First";
    String oldLastName = "Old Last";
    String newFirstName = "New First";
    String newLastName = "New Last";

    // insert GUEST role as its used in create profile
    RoleEntity roleEntity = roleService.createRole(new RoleRequest(ROLE_NAME_GUEST, "something"));

    // Create
    ProfileRequest profileRequest =
        TestData.getProfileRequest(oldFirstName, oldLastName, OLD_EMAIL, null, null);
    assertCreate_FailureOnMissingPassword(profileRequest);

    profileRequest =
        TestData.getProfileRequest(oldFirstName, oldLastName, OLD_EMAIL, OLD_PASSWORD, null);
    ProfileEntity profileEntityCreated = assertCreate(profileRequest);

    Long profileId = profileEntityCreated.getId();
    final String oldHashedPassword = profileEntityCreated.getPassword();

    // Update
    ProfileAddressRequest profileAddressRequest =
        TestData.getProfileAddressRequest(null, profileId, "some-street", false);
    profileRequest =
        TestData.getProfileRequest(
            newFirstName, newLastName, NEW_EMAIL, NEW_PASSWORD, profileAddressRequest);
    ProfileEntity profileEntityUpdated = assertUpdate(profileId, profileRequest, oldHashedPassword);

    // Read
    // read Profile has private access

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
    ProfileAddressEntity profileAddressEntity = profileEntityUpdated.getProfileAddress();
    profileAddressRequest =
        TestData.getProfileAddressRequest(
            profileAddressEntity.getId(), profileId, profileAddressEntity.getStreet(), true);
    profileRequest =
        TestData.getProfileRequest(
            newFirstName, newLastName, NEW_EMAIL, NEW_PASSWORD, profileAddressRequest);
    assertDeleteProfileAddress(profileId, profileRequest);

    // Hard Delete
    assertDeleteHard(profileId);

    // cleanup
    // delete GUEST role
    roleService.hardDeleteRole(roleEntity.getId());
  }

  void assertCreate_FailureOnMissingPassword(ProfileRequest profileRequest) {
    ElementMissingException exception =
        assertThrows(
            ElementMissingException.class,
            () -> profileService.createProfile(platformEntity, profileRequest, BASE_URL_FOR_EMAIL));
    assertEquals("[password] is Missing in [Profile] request", exception.getMessage());
  }

  private ProfileEntity assertCreate(ProfileRequest request) {
    ProfileEntity profileEntity =
        profileService.createProfile(platformEntity, request, BASE_URL_FOR_EMAIL);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getId());
    assertNull(profileEntity.getProfileAddress());

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

  private ProfileEntity assertUpdate(Long id, ProfileRequest request, String oldHashedPassword) {
    ProfileEntity profileEntity = profileService.updateProfile(id, request);
    assertNotNull(profileEntity);
    assertNotNull(profileEntity.getProfileAddress());
    // email, password doesn't change on update
    assertEquals(OLD_EMAIL, profileEntity.getEmail());
    assertEquals(oldHashedPassword, profileEntity.getPassword());

    return profileEntity;
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

  private void assertDeleteProfileAddress(Long id, ProfileRequest request) {
    ProfileEntity profileEntity = profileService.updateProfile(id, request);
    assertNotNull(profileEntity);
    assertNull(profileEntity.getProfileAddress());
  }

  private void assertDeleteHard(Long profileId) {
    Long roleId = circularDependencyService.readRoleByName(ROLE_NAME_GUEST, false).getId();
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(platformEntity.getId(), profileId, roleId));

    // delete profile
    profileService.hardDeleteProfile(profileId);
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> profileService.hardDeleteProfile(profileId),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Profile Not Found for [%s]", profileId),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
