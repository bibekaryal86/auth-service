package user.management.system.app.util;

import static user.management.system.app.util.ConstantUtils.ENV_SECRET_KEY;
import static user.management.system.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.token.AuthToken;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

  private static final String SECRET_KEY = getSystemEnvProperty(ENV_SECRET_KEY);

  private static SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
  }

  public static String encodeEmailAddress(final String email) {
    return Jwts.builder()
        .claim("emailToken", email)
        .issuer("USER-MGMT-SYS")
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
        .signWith(getSigningKey())
        .compact();
  }

  public static String decodeEmailAddress(final String encodedEmail) {
    try {
      final String emailToken =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(encodedEmail)
              .getPayload()
              .get("emailToken", String.class);

      if (emailToken == null) {
        throw new IllegalArgumentException("Incorrect Email Credentials");
      }

      return emailToken;
    } catch (ExpiredJwtException e) {
      throw new JwtInvalidException("Email Token has expired");
    } catch (JwtException e) {
      throw new JwtInvalidException("Invalid Email Credentials");
    }
  }

  public static String decodeEmailAddressNoException(final String encodedEmail) {
    try {
      return decodeEmailAddress(encodedEmail);
    } catch (Exception ex) {
      // ignore exception
      return encodedEmail;
    }
  }

  public static String encodeAuthCredentials(
      final String appId, final AppUserDto appUserDto, final long expirationMillis) {
    AuthToken authToken = appUserDto.toAuthToken();
    authToken.setAppId(appId);
    Map<String, Object> tokenClaim = new HashMap<>();
    tokenClaim.put("authToken", authToken);
    return Jwts.builder()
        .claims(tokenClaim)
        .subject(appUserDto.getEmail())
        .issuer("USER-MGMT-SYS")
        .issuedAt(Date.from(Instant.now()))
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .signWith(getSigningKey())
        .compact();
  }

  public static Map<String, AuthToken> decodeAuthCredentials(final String token) {
    try {
      final String subject =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getSubject();
      final Claims claims =
          Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

      final AuthToken authToken =
          new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .convertValue(claims.get("authToken"), AuthToken.class);

      return Map.of(subject, authToken);
    } catch (ExpiredJwtException e) {
      throw new JwtInvalidException("Auth Token has expired");
    } catch (JwtException e) {
      throw new JwtInvalidException("Invalid Auth Credentials");
    }
  }
}
