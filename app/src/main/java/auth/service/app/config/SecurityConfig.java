package auth.service.app.config;

import auth.service.app.util.ConstantUtils;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends SecurityConfigBase {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public SecurityConfig(final BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public InMemoryUserDetailsManager userDetailsManager() {
    UserDetails user =
        User.builder()
            .username(CommonUtilities.getSystemEnvProperty(ConstantUtils.ENV_SELF_USERNAME))
            .password(
                bCryptPasswordEncoder.encode(
                    CommonUtilities.getSystemEnvProperty(ConstantUtils.ENV_SELF_PASSWORD)))
            .build();
    return new InMemoryUserDetailsManager(user);
  }
}
