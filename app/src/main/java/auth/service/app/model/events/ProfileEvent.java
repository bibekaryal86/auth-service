package auth.service.app.model.events;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.EventEnums;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProfileEvent extends ApplicationEvent {
  private final EventEnums.EventType eventType;
  private final ProfileEntity profileEntity;
  private final PlatformEntity platformEntity;
  private final String baseUrl;

  public ProfileEvent(
      final Object source,
      final EventEnums.EventType eventType,
      final ProfileEntity profileEntity,
      final PlatformEntity platformEntity,
      final String baseUrl) {
    super(source);
    this.eventType = eventType;
    this.profileEntity = profileEntity;
    this.platformEntity = platformEntity;
    this.baseUrl = baseUrl;
  }
}
