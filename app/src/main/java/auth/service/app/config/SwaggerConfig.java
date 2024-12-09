package auth.service.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            .info(
                    new Info()
                            .title("Auth Service")
                            .description("Manage apps, users, roles and permissions")
                            .contact(new Contact().name("Bibek Aryal"))
                            .license(new License().name("Personal Use Only"))
                            .version("1.0.1"));
  }
}
