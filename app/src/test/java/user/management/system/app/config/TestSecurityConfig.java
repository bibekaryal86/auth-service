package user.management.system.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import user.management.system.app.service.AppUserService;

@Configuration
public class TestSecurityConfig extends SecurityConfigBase {

  protected TestSecurityConfig(final AppUserService appUserService) {
    super(appUserService);
  }

  @Override
  public InMemoryUserDetailsManager userDetailsManager() {
    UserDetails testUser =
        User.builder().username("test_user").password("{noop}test_password").build();
    return new InMemoryUserDetailsManager(testUser);
  }
}
