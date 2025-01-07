package auth.service.app.model.events;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.TypeEnums;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProfileEvent extends ApplicationEvent {
  private final TypeEnums.EventType eventType;
  private final ProfileEntity profileEntity;
  private final PlatformEntity platformEntity;
  private final String baseUrl;

  public ProfileEvent(
      final Object source,
      final TypeEnums.EventType eventType,
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
