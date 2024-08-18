package user.management.system.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("User Management System")
                .description("Create and Manage Users, Roles and Permissions")
                .contact(new Contact().name("Bibek Aryal"))
                .license(new License().name("Personal Use Only"))
                .version("1.0.1"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "Token",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("Token"));
  }
}
