package user.management.system.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static user.management.system.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_SELF_USERNAME;
import static user.management.system.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import user.management.system.app.exception.handler.CustomAccessDeniedHandler;
import user.management.system.app.exception.handler.CustomAuthenticationEntrypoint;
import user.management.system.app.filter.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  @Order(1)
  public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/tests/ping")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain basicAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .securityMatcher("/api/v1/basic_app_users/**", "/actuator/**", "/tests/reset")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(withDefaults())
        .build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain bearerAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .accessDeniedHandler(new CustomAccessDeniedHandler())
                    .authenticationEntryPoint(new CustomAuthenticationEntrypoint()));
    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsManager() {
    UserDetails user =
        User.builder()
            .username(getSystemEnvProperty(ENV_SELF_USERNAME))
            .password(bCryptPasswordEncoder().encode(getSystemEnvProperty(ENV_SELF_PASSWORD)))
            .roles("")
            .build();
    return new InMemoryUserDetailsManager(user);
  }
}
