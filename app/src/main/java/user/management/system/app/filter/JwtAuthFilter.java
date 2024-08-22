package user.management.system.app.filter;

import static user.management.system.app.util.CommonUtils.convertResponseStatusInfoToJson;
import static user.management.system.app.util.JwtUtils.decodeAuthCredentials;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.service.AppUserService;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final AppUserService appUserService;

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring(7);

      try {
        Map<String, AuthToken> emailAuthToken = decodeAuthCredentials(token);

        if (emailAuthToken.size() != 1) {
          sendUnauthorizedResponse(response, "Malformed Auth Token");
          return;
        }

        Map.Entry<String, AuthToken> firstEntry = emailAuthToken.entrySet().iterator().next();
        String email = firstEntry.getKey();
        AuthToken authToken = firstEntry.getValue();

        if (!validateUserEntity(email, authToken)) {
          sendUnauthorizedResponse(response, "Incorrect Auth Token");
          return;
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, authToken, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtInvalidException ex) {
        sendUnauthorizedResponse(response, ex.getMessage());
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendUnauthorizedResponse(final HttpServletResponse response, String errMsg)
      throws IOException {
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(errMsg).build();

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final String jsonResponse = convertResponseStatusInfoToJson(responseStatusInfo);
    response.getWriter().write(jsonResponse);
  }

  private boolean validateUserEntity(final String email, final AuthToken authToken) {
    final AppUserEntity appUserEntity = appUserService.readAppUser(email);
    return Objects.equals(appUserEntity.getEmail(), authToken.getUser().getEmail());
  }
}
