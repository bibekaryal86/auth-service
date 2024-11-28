package auth.service.app.connector;

import static auth.service.app.util.ConstantUtils.ENV_ENVSVC_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_ENVSVC_USERNAME;
import static auth.service.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import auth.service.app.model.client.EnvDetails;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EnvServiceConnector {

  private final String getPropertiesUrl;
  private final WebClient webClient;
  private final Environment environment;

  public EnvServiceConnector(
      @Value("${endpoint.env_service.get_properties}") final String getPropertiesUrl,
      @Qualifier("webClient") final WebClient webClient,
      final Environment environment) {
    this.getPropertiesUrl = getPropertiesUrl;
    this.webClient = webClient;
    this.environment = environment;
  }

  private List<EnvDetails> getAuthServiceEnvProperties() {
    final String url = UriComponentsBuilder.fromUriString(getPropertiesUrl).toUriString();
    final String credentials =
        getSystemEnvProperty(ENV_ENVSVC_USERNAME) + ":" + getSystemEnvProperty(ENV_ENVSVC_PASSWORD);
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
    final List<EnvDetails> envDetails = getAuthServiceEnvProperties();
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

  @CacheEvict("redirectUrls")
  public void evictRedirectUrlCache() {}
}
