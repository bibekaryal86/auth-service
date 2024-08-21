package user.management.system.app.connector;

import static user.management.system.app.util.ConstantUtils.ENV_AUTHENV_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_AUTHENV_USERNAME;
import static user.management.system.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import user.management.system.app.model.client.EnvDetails;

@Component
public class AuthenvServiceConnector {

  private final String getPropertiesUrl;
  private final WebClient webClient;
  private final Environment environment;

  public AuthenvServiceConnector(
      @Value("${endpoint.authenv_service.get_properties}") final String getPropertiesUrl,
      @Qualifier("webClient") final WebClient webClient,
      final Environment environment) {
    this.getPropertiesUrl = getPropertiesUrl;
    this.webClient = webClient;
    this.environment = environment;
  }

  private List<EnvDetails> getUserMgmtSvcEnvProperties() {
    final String url = UriComponentsBuilder.fromHttpUrl(getPropertiesUrl).toUriString();
    final String credentials =
        getSystemEnvProperty(ENV_AUTHENV_USERNAME)
            + ":"
            + getSystemEnvProperty(ENV_AUTHENV_PASSWORD);
    final String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

    return webClient
        .get()
        .uri(url)
        .header("Authorization", "Basic " + base64Credentials)
        .retrieve()
        .bodyToFlux(EnvDetails.class)
        .collectList()
        .blockOptional()
        .orElse(Collections.emptyList());
  }

  @Cacheable("redirectUrls")
  public Map<String, String> getRedirectUrls() {
    final boolean isDevelopment = environment.matchesProfiles("development");
    final String envDetailsName =
        String.format("redirectUrls_%s", isDevelopment ? "development" : "production");
    List<EnvDetails> envDetails = getUserMgmtSvcEnvProperties();
    EnvDetails withRedirectUrls =
        envDetails.stream()
            .filter(envDetail -> envDetail.getName().equals(envDetailsName))
            .findFirst()
            .orElse(null);
    if (withRedirectUrls == null) {
      return Collections.emptyMap();
    }
    return withRedirectUrls.getMapValue();
  }
}
