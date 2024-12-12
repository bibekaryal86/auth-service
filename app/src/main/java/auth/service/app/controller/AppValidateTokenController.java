package auth.service.app.controller;

import auth.service.app.model.token.AuthToken;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/api/v1/validate")
public class AppValidateTokenController {

  @GetMapping(value = "/token/{platformId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthToken> validateToken(@PathVariable final Long platformId) {
    // Token is validated by Spring Security already, now need to return AuthToken
    try {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null
          && authentication.getCredentials() != null
          && authentication.getCredentials() instanceof AuthToken authToken
          && authToken.getPlatform() != null
          && Objects.equals(platformId, authToken.getPlatform().getId())) {
        return ResponseEntity.ok(authToken);
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception ex) {
      log.error("Validate Token Error", ex);
      return ResponseEntity.internalServerError().build();
    }
  }
}
