package user.management.system.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
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
                        .bearerFormat("JWT"))
                .addSecuritySchemes(
                    "Basic", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("Basic")))
        .tags(
            List.of(
                new Tag().name("Apps Management").description("Create, View and Manage Apps"),
                new Tag().name("Users Management").description("Create, View and Manage Users"),
                new Tag()
                    .name("Apps Users Management")
                    .description("View, Assign and Unassign Users from Apps"),
                new Tag().name("Roles Management").description("Create, View and Manage Roles"),
                new Tag()
                    .name("Users Roles Management")
                    .description("View, Assign and Unassign Users from Roles"),
                new Tag()
                    .name("Permissions Management")
                    .description("Create, View and Manage Permissions"),
                new Tag()
                    .name("Roles Permissions Management")
                    .description("View, Assign and Unassign Permissions from Roles"),
                new Tag().name("Tests").description("Miscellaneous")));
  }
}
