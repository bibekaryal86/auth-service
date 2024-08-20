package user.management.system.app.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsEntity;

@Getter
public class AppUserCreatedEvent extends ApplicationEvent {
  private final AppUserEntity appUserEntity;
  private final AppsEntity appsEntity;
  private final boolean isGuestUser;
  private final String baseUrl;

  public AppUserCreatedEvent(
      final Object source,
      final AppUserEntity appUserEntity,
      final AppsEntity appsEntity,
      final boolean isGuestUser,
      final String baseUrl) {
    super(source);
    this.appUserEntity = appUserEntity;
    this.appsEntity = appsEntity;
    this.isGuestUser = isGuestUser;
    this.baseUrl = baseUrl;
  }
}
