package auth.service.app.connector;

import static auth.service.app.util.ConstantUtils.ENV_ENVSVC_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_ENVSVC_USERNAME;
import static auth.service.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import auth.service.app.model.client.EnvDetailsResponse;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.OkHttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EnvServiceConnector {

  private final String getPropertiesUrl;
  private final Environment environment;

  public EnvServiceConnector(
      @Value("${endpoint.env_service.get_properties}") final String getPropertiesUrl,
      final Environment environment) {
    this.getPropertiesUrl = getPropertiesUrl;
    this.environment = environment;
  }

  private List<EnvDetailsResponse.EnvDetails> getAuthServiceEnvProperties() {
    final String url = UriComponentsBuilder.fromUriString(getPropertiesUrl).toUriString();
    final String credentials = getSystemEnvProperty(ENV_ENVSVC_USERNAME) + ":" + getSystemEnvProperty(ENV_ENVSVC_PASSWORD);
    final String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

    final OkHttpUtils.HttpResponse httpResponse = OkHttpUtils.sendRequest(url, "GET", "", Collections.emptyMap(), base64Credentials);

    if (httpResponse.statusCode() == 200) {
      EnvDetailsResponse envDetailsResponse = CommonUtils.getGson().fromJson(httpResponse.responseBody(), EnvDetailsResponse.class);
      return envDetailsResponse.getEnvDetails();
    }

    return Collections.emptyList();
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
