package user.management.system.app.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import user.management.system.app.model.entity.AppUserEntity;

@Getter
public class AppUserCreatedEvent extends ApplicationEvent {
  private final AppUserEntity appUserEntity;
  private final boolean isGuestUser;
  private final String baseUrl;

  public AppUserCreatedEvent(
      final Object source,
      final AppUserEntity appUserEntity,
      final boolean isGuestUser,
      final String baseUrl) {
    super(source);
    this.appUserEntity = appUserEntity;
    this.isGuestUser = isGuestUser;
    this.baseUrl = baseUrl;
  }
}
