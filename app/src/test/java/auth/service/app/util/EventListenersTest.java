package auth.service.app.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.service.EmailService;
import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class EventListenersTest extends BaseTest {

  @MockitoBean private EmailService emailService;

  @Autowired private EventListeners eventListeners;

  @Test
  void testHandleProfileEventForValidation_CreateEvent() {
    ProfileEvent profileEvent = createProfileEvent(TypeEnums.EventType.CREATE);
    eventListeners.handleProfileEventForValidation(profileEvent);
    verify(emailService)
        .sendProfileValidationEmail(
            profileEvent.getPlatformEntity(),
            profileEvent.getProfileEntity(),
            profileEvent.getBaseUrl());
    verifyNoMoreInteractions(emailService);
  }

  @Test
  void testHandleProfileEventForValidation_UpdateEmail() {
    ProfileEvent profileEvent = createProfileEvent(TypeEnums.EventType.UPDATE_EMAIL);
    eventListeners.handleProfileEventForValidation(profileEvent);
    verify(emailService)
        .sendProfileValidationEmail(
            profileEvent.getPlatformEntity(),
            profileEvent.getProfileEntity(),
            profileEvent.getBaseUrl());
    verifyNoMoreInteractions(emailService);
  }

  @Test
  void testHandleProfileEventForValidation_UpdatePassword() {
    ProfileEvent profileEvent = createProfileEvent(TypeEnums.EventType.UPDATE_PASSWORD);
    eventListeners.handleProfileEventForValidation(profileEvent);
    verify(emailService)
        .sendProfilePasswordEmail(
            profileEvent.getPlatformEntity(), profileEvent.getProfileEntity());
    verifyNoMoreInteractions(emailService);
  }

  private ProfileEvent createProfileEvent(TypeEnums.EventType eventType) {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileEntity profileEntity = TestData.getProfileEntities().getFirst();
    return new ProfileEvent(
        this, eventType, profileEntity, platformEntity, "https://some-url.com/");
  }
}
