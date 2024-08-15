package user.management.system.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/swagger-ui/**", "/v3/api-docs/**", "/na_app_users/**"))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/na_app_users/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    return http.build();
  }
}