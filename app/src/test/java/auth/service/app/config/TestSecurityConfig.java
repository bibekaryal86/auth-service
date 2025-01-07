package auth.service.app.config;

import static auth.service.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_SELF_USERNAME;

import auth.service.app.service.ProfileService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityConfig extends SecurityConfigBase {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  protected TestSecurityConfig(
      final ProfileService profileService, final BCryptPasswordEncoder bCryptPasswordEncoder) {
    super(profileService);
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
