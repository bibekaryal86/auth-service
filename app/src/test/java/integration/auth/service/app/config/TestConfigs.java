package integration.auth.service.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfigs {

  @Bean
  @Primary
  public ApplicationEventPublisher applicationEventPublisher() {
    return new ApplicationEventPublisher() {
      @Override
      public void publishEvent(Object event) {
        // Do nothing to prevent event consumption
      }

      @Override
      public void publishEvent(ApplicationEvent event) {
        // Do nothing to prevent event consumption
      }
    };
  }
}
