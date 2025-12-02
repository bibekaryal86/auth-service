package auth.service.app.util;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListeners {

  private final EmailService emailService;

  @EventListener
  public void handleProfileEventForValidation(final ProfileEvent profileEvent) {
    final TypeEnums.EventType eventType = profileEvent.getEventType();
    final PlatformEntity platformEntity = profileEvent.getPlatformEntity();
    final ProfileEntity profileEntity = profileEvent.getProfileEntity();
    final String baseUrl = profileEvent.getBaseUrl();
    log.info(
        "Handle Profile Event: EvenType=[{}], PlatformId=[{}], ProfileId=[{}]",
        eventType,
        platformEntity.getId(),
        profileEntity.getId());

    if (eventType == TypeEnums.EventType.CREATE || eventType == TypeEnums.EventType.UPDATE_EMAIL) {
      emailService.sendProfileValidationEmail(platformEntity, profileEntity, baseUrl);
    } else if (eventType == TypeEnums.EventType.UPDATE_PASSWORD) {
      emailService.sendProfilePasswordEmail(platformEntity, profileEntity);
    }
  }
}
