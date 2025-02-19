package auth.service.app.filter;

import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.service.ProfileService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.JwtUtils;
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

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final ProfileService profileService;

  @Override
  protected void doFilterInternal(
      @NotNull final HttpServletRequest request,
      @NotNull final HttpServletResponse response,
      @NotNull final FilterChain filterChain)
      throws ServletException, IOException {
    final String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      final String token = authorizationHeader.substring(7);

      try {
        final Map<String, AuthToken> emailAuthToken = JwtUtils.decodeAuthCredentials(token);

        if (emailAuthToken.size() != 1) {
          sendUnauthorizedResponse(response, "Malformed Auth Token");
          return;
        }

        final Map.Entry<String, AuthToken> firstEntry = emailAuthToken.entrySet().iterator().next();
        final String email = firstEntry.getKey();
        final AuthToken authToken = firstEntry.getValue();

        if (!validateUserEntity(email, authToken)) {
          sendUnauthorizedResponse(response, "Incorrect Auth Token");
          return;
        }

        final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, authToken, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtInvalidException ex) {
        sendUnauthorizedResponse(response, ex.getMessage());
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendUnauthorizedResponse(final HttpServletResponse response, final String errMsg)
      throws IOException {
    final ResponseMetadata responseMetadata =
        ResponseMetadata.builder()
            .responseStatusInfo(ResponseStatusInfo.builder().errMsg(errMsg).build())
            .responsePageInfo(CommonUtils.emptyResponsePageInfo())
            .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
            .build();

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final String jsonResponse = CommonUtils.GSON.toJson(responseMetadata);
    response.getWriter().write(jsonResponse);
  }

  private boolean validateUserEntity(final String email, final AuthToken authToken) {
    final ProfileEntity profileEntity = profileService.readProfileByEmail(email);
    return Objects.equals(profileEntity.getEmail(), authToken.getProfile().getEmail());
  }
}
