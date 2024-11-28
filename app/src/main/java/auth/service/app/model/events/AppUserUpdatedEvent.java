package auth.service.app.model.events;

import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppUserUpdatedEvent extends ApplicationEvent {
  private final AppUserEntity appUserEntity;
  private final AppsEntity appsEntity;
  private final String baseUrl;

  public AppUserUpdatedEvent(
      final Object source,
      final AppUserEntity appUserEntity,
      final AppsEntity appsEntity,
      final String baseUrl) {
    super(source);
    this.appUserEntity = appUserEntity;
    this.appsEntity = appsEntity;
    this.baseUrl = baseUrl;
  }
}
