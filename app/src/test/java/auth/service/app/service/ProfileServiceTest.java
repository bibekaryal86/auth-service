package auth.service.app.service;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.repository.PlatformProfileRoleRepository;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ProfileServiceTest extends BaseTest {

  private static final String OLD_EMAIL = "old@email.com";
  private static final String NEW_EMAIL = "new@email.com";
  private static final String OLD_PASSWORD = "old_password";
  private static final String NEW_PASSWORD = "new_password";
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";

  private static PlatformEntity platformEntity;
  private static ProfileRequest profileRequest;

  @MockitoBean private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private ProfileService profileService;
  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;

  @BeforeAll
  static void setUpBeforeAll() {
    platformEntity = TestData.getPlatformEntities().getFirst();
    profileRequest = TestData.getProfileRequest("some-password");
  }

  @BeforeEach
  void setUpBeforeEach() {
    clearInvocations(applicationEventPublisher);
    doNothing().when(applicationEventPublisher).publishEvent(any(ProfileEvent.class));
  }

  @Test
  void testCreateProfile_FailureOnMissingPassword() {
    ProfileRequest profileRequestNoPassword =
        new ProfileRequest(
            profileRequest.getFirstName(),
            profileRequest.getLastName(),
            profileRequest.getEmail(),
            profileRequest.getPhone(),
            null,
            profileRequest.getStatusId(),
            true,
            profileRequest.getAddresses());
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
}
