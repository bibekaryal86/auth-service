package auth.service.app.config;

import static auth.service.app.util.ConstantUtils.ENV_MAILJET_PRIVATE_KEY;
import static auth.service.app.util.ConstantUtils.ENV_MAILJET_PUBLIC_KEY;
import static auth.service.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailjetConfig {

  @Bean
  public MailjetClient mailjetClient() {
    return new MailjetClient(
        ClientOptions.builder()
            .apiKey(getSystemEnvProperty(ENV_MAILJET_PUBLIC_KEY))
            .apiSecretKey(getSystemEnvProperty(ENV_MAILJET_PRIVATE_KEY))
            .build());
  }
}
