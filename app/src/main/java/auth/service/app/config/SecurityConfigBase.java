package auth.service.app.config;

import static org.springframework.security.config.Customizer.withDefaults;

import auth.service.app.exception.handler.CustomAccessDeniedHandler;
import auth.service.app.exception.handler.CustomAuthenticationEntrypoint;
import auth.service.app.filter.JwtAuthFilter;
import auth.service.app.service.ProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public abstract class SecurityConfigBase {

  protected final ProfileService profileService;

  protected SecurityConfigBase(ProfileService profileService) {
    this.profileService = profileService;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .securityMatcher(
            request -> {
              boolean matches =
                  request
                      .getRequestURI()
                      .matches(
                          "^.*(?:/swagger-ui/|/v3/api-docs|/tests/ping|/na_app_users/|/error).*");
              //              if (matches) {
              //                System.out.println("noAuthSecurityFilterChain: " +
              // request.getRequestURI());
              //              }
              return matches;
            })
        // .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/tests/ping",
        // "/api/v1/na_app_users/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain basicAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .securityMatcher(
            request -> {
              boolean matches =
                  request
                      .getRequestURI()
                      .matches("^.*(?:/actuator/|/tests/reset|/basic_app_users/).*");
              //              if (matches) {
              //                System.out.println("basicAuthSecurityFilterChain: " +
              // request.getRequestURI());
              //              }
              return matches;
            })
        // .securityMatcher("/api/v1/basic_app_users/**", "/actuator/**", "/tests/reset")
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
        .securityMatcher(
            request -> {
              // System.out.println("bearerAuthSecurityFilterChain: " + request.getRequestURI());
              return true;
            })
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .addFilterBefore(
            new JwtAuthFilter(profileService), UsernamePasswordAuthenticationFilter.class)
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
    return new InMemoryUserDetailsManager();
  }
}
