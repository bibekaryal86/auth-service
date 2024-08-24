package user.management.system.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

@Slf4j
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() throws IOException {
    ClassPathResource openApiResource = new ClassPathResource("openapi.json");
    String openApiJson =
        StreamUtils.copyToString(openApiResource.getInputStream(), StandardCharsets.UTF_8);
    return new OpenAPIV3Parser().readContents(openApiJson, null, null).getOpenAPI();
  }
}
