package auth.service.app.filter;

import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.token.AuthToken;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.JwtUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
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
    final ResponseWithMetadata responseWithMetadata =
        new ResponseWithMetadata(
            new ResponseMetadata(
                new ResponseMetadata.ResponseStatusInfo(errMsg),
                ResponseMetadata.emptyResponseCrudInfo(),
                ResponseMetadata.emptyResponsePageInfo()));

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final String jsonResponse = ConstantUtils.GSON.toJson(responseWithMetadata);
    response.getWriter().write(jsonResponse);
  }
}
