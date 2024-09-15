package user.management.system.app.config;

import static user.management.system.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_SELF_USERNAME;
import static user.management.system.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import user.management.system.app.service.AppUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends SecurityConfigBase {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public SecurityConfig(
      final BCryptPasswordEncoder bCryptPasswordEncoder, final AppUserService appUserService) {
    super(appUserService);
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public InMemoryUserDetailsManager userDetailsManager() {
    UserDetails user =
        User.builder()
            .username(getSystemEnvProperty(ENV_SELF_USERNAME))
            .password(bCryptPasswordEncoder.encode(getSystemEnvProperty(ENV_SELF_PASSWORD)))
            .build();
    return new InMemoryUserDetailsManager(user);
  }
}
