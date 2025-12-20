package auth.service.app.config;

import auth.service.app.exception.handler.CustomAccessDeniedHandler;
import auth.service.app.exception.handler.CustomAuthenticationEntrypoint;
import auth.service.app.filter.JwtAuthFilter;
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
                          "^.*(?:/swagger-ui/|/v3/api-docs|/tests/ping|/auth/|/cors/|/error).*");
              //              if (matches) {
              //                System.out.println("noAuthSecurityFilterChain: " +
              // request.getRequestURI());
              //              }
              return matches;
            })
        // .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/tests/ping",
        // "/api/v1/na_profiles/**")
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
                  request.getRequestURI().matches("^.*(?:/actuator/|/tests/reset|/ba_profiles/).*");
              //              if (matches) {
              //                System.out.println("basicAuthSecurityFilterChain: " +
              // request.getRequestURI());
              //              }
              return matches;
            })
        // .securityMatcher("/api/v1/ba_profiles/**", "/actuator/**", "/tests/reset")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(
            httpBasic ->
                httpBasic.authenticationEntryPoint(
                    new CustomAuthenticationEntrypoint(Boolean.TRUE)))
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling.accessDeniedHandler(new CustomAccessDeniedHandler()))
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
        .addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .accessDeniedHandler(new CustomAccessDeniedHandler())
                    .authenticationEntryPoint(new CustomAuthenticationEntrypoint(Boolean.FALSE)));
    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsManager() {
    return new InMemoryUserDetailsManager();
  }
}
