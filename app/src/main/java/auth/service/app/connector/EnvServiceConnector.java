package auth.service.app.connector;

import io.github.bibekaryal86.shdsvc.AppEnvProperty;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvServiceConnector {

  private final Environment environment;

  public EnvServiceConnector(final Environment environment) {
    this.environment = environment;
  }

  private List<EnvDetailsResponse.EnvDetails> getAuthServiceEnvProperties() {
    return AppEnvProperty.getEnvDetailsList("authsvc", true);
  }

  @Cacheable("redirectUrls")
  public Map<String, String> getRedirectUrls() {
    final boolean isDevelopment = environment.matchesProfiles("development");
    final String envDetailsName =
        String.format("redirectUrls_%s", isDevelopment ? "development" : "production");
    final List<EnvDetailsResponse.EnvDetails> envDetails = getAuthServiceEnvProperties();
    EnvDetailsResponse.EnvDetails withRedirectUrls =
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

  @CacheEvict("baseUrlForLinkInEmail")
  public void evictBaseUrlForLinkInEmailCache() {}

  @Cacheable("baseUrlForLinkInEmail")
  public String getBaseUrlForLinkInEmail() {
    final boolean isDevelopment = environment.matchesProfiles("development");
    final String envDetailsName = "baseUrlForLinkInEmail";
    final List<EnvDetailsResponse.EnvDetails> envDetails = getAuthServiceEnvProperties();
    EnvDetailsResponse.EnvDetails withRedirectUrls =
        envDetails.stream()
            .filter(envDetail -> envDetail.getName().equals(envDetailsName))
            .findFirst()
            .orElse(null);
    if (withRedirectUrls == null) {
      return null;
    }
    return withRedirectUrls.getMapValue().get(isDevelopment ? "development" : "production");
  }
}
