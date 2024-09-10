package user.management.system.app.config;

import static user.management.system.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_SELF_USERNAME;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import user.management.system.app.service.AppUserService;

@TestConfiguration
public class TestSecurityConfig extends SecurityConfigBase {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  protected TestSecurityConfig(
      final AppUserService appUserService, final BCryptPasswordEncoder bCryptPasswordEncoder) {
    super(appUserService);
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public InMemoryUserDetailsManager userDetailsManager() {
    UserDetails testUser =
        User.builder()
            .username(ENV_SELF_USERNAME)
            .password(bCryptPasswordEncoder.encode(ENV_SELF_PASSWORD))
            .build();
    return new InMemoryUserDetailsManager(testUser);
  }
}
