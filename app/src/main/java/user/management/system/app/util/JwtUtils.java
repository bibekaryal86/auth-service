package user.management.system.app.util;

import static user.management.system.app.util.CommonUtils.getSystemEnvProperty;
import static user.management.system.app.util.ConstantUtils.ENV_SECRET_KEY;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import user.management.system.app.model.dto.AppUserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

  private static final String SECRET_KEY = getSystemEnvProperty(ENV_SECRET_KEY, null);

  private static SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
  }

  public static String encodeEmailAddress(final String email) {
    return Jwts.builder()
        .claim("sub", email)
        .issuer("USER-MGMT-SYS")
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
        .signWith(getSigningKey())
        .compact();
  }

  public static String decodeEmailAddress(final String encodedEmail) {
    try {
      String emailToken =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(encodedEmail)
              .getPayload()
              .get("sub", String.class);

      if (emailToken == null) {
        throw new IllegalArgumentException("Incorrect Email Credentials");
      }

      return emailToken;
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("Token has expired", e);
    } catch (JwtException e) {
      throw new IllegalArgumentException("Invalid Email Credentials", e);
    }
  }

  public static String encodeAuthCredentials(final AppUserDto appUserDto) {
    Map<String, Object> tokenClaim = new HashMap<>();
    tokenClaim.put("sub", appUserDto.toAuthToken());
    tokenClaim.put("exp", LocalDateTime.now().plusHours(24));
    return Jwts.builder()
        .claims(tokenClaim)
        .issuer("USER-MGMT-SYS")
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
        .signWith(getSigningKey())
        .compact();
  }
}
