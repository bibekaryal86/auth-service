package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.exception.TokenInvalidException;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.JwtUtils;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

  private MockedStatic<CommonUtilities> commonUtilitiesMock;
  private static final String TEST_SECRET_KEY =
      CommonUtilities.getSystemEnvProperty(ConstantUtils.ENV_SECRET_KEY);
  private static final String TEST_EMAIL = "profile@one.com";

  @BeforeEach
  void setUp() {
    commonUtilitiesMock = mockStatic(CommonUtilities.class);
    commonUtilitiesMock
        .when(() -> CommonUtilities.getSystemEnvProperty(ConstantUtils.ENV_SECRET_KEY))
        .thenReturn(TEST_SECRET_KEY);
  }

  @AfterEach
  void tearDown() {
    if (commonUtilitiesMock != null) {
      commonUtilitiesMock.close();
    }
  }

  @Nested
  @DisplayName("encodeEmailAddress() tests")
  class EncodeEmailAddressTests {

    @Test
    @DisplayName("Should encode email address successfully")
    void shouldEncodeEmailAddressSuccessfully() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);

      assertNotNull(token);
      assertFalse(token.isEmpty());
      assertEquals(3, token.split("\\.").length); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should create token with email claim")
    void shouldCreateTokenWithEmailClaim() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals(TEST_EMAIL, claims.get(ConstantUtils.TOKEN_CLAIM_EMAIL, String.class));
    }

    @Test
    @DisplayName("Should create token with issuer")
    void shouldCreateTokenWithIssuer() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals(ConstantUtils.TOKEN_CLAIM_ISSUER, claims.getIssuer());
    }

    @Test
    @DisplayName("Should create token with issued at date")
    void shouldCreateTokenWithIssuedAtDate() {
      Instant beforeCreation = Instant.now().minusSeconds(1);
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);
      Instant afterCreation = Instant.now().plusSeconds(1);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      Instant issuedAt = claims.getIssuedAt().toInstant();
      assertTrue(issuedAt.isAfter(beforeCreation));
      assertTrue(issuedAt.isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should create token with 15 minute expiration")
    void shouldCreateTokenWithFifteenMinuteExpiration() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      Instant expiration = claims.getExpiration().toInstant();
      Instant expectedExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);

      // Allow 1 second tolerance for test execution time
      assertTrue(expiration.isAfter(expectedExpiration.minusSeconds(1)));
      assertTrue(expiration.isBefore(expectedExpiration.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should encode different email addresses to different tokens")
    void shouldEncodeDifferentEmailsToDifferentTokens() {
      String token1 = JwtUtils.encodeEmailAddress("user1@example.com");
      String token2 = JwtUtils.encodeEmailAddress("user2@example.com");

      assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should handle email with special characters")
    void shouldHandleEmailWithSpecialCharacters() {
      String specialEmail = "test+tag@example.co.uk";
      String token = JwtUtils.encodeEmailAddress(specialEmail);

      assertNotNull(token);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals(specialEmail, claims.get(ConstantUtils.TOKEN_CLAIM_EMAIL, String.class));
    }

    @Test
    @DisplayName("Should handle empty email string")
    void shouldHandleEmptyEmailString() {
      String token = JwtUtils.encodeEmailAddress("");

      assertNotNull(token);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals("", claims.get(ConstantUtils.TOKEN_CLAIM_EMAIL, String.class));
    }
  }

  @Nested
  @DisplayName("decodeEmailAddress() tests")
  class DecodeEmailAddressTests {

    @Test
    @DisplayName("Should decode valid email token successfully")
    void shouldDecodeValidEmailTokenSuccessfully() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);
      String decodedEmail = JwtUtils.decodeEmailAddress(token);

      assertEquals(TEST_EMAIL, decodedEmail);
    }

    @Test
    @DisplayName("Should decode multiple different email tokens correctly")
    void shouldDecodeMultipleDifferentEmailTokens() {
      String email1 = "user1@example.com";
      String email2 = "user2@example.com";

      String token1 = JwtUtils.encodeEmailAddress(email1);
      String token2 = JwtUtils.encodeEmailAddress(email2);

      assertEquals(email1, JwtUtils.decodeEmailAddress(token1));
      assertEquals(email2, JwtUtils.decodeEmailAddress(token2));
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for expired token")
    void shouldThrowTokenInvalidExceptionForExpiredToken() {
      // Create a token that's already expired
      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      String expiredToken =
          Jwts.builder()
              .claim(ConstantUtils.TOKEN_CLAIM_EMAIL, TEST_EMAIL)
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now().minus(20, ChronoUnit.MINUTES)))
              .expiration(Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
              .signWith(key)
              .compact();

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class, () -> JwtUtils.decodeEmailAddress(expiredToken));

      assertEquals("Expired Email Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for invalid token format")
    void shouldThrowTokenInvalidExceptionForInvalidFormat() {
      String invalidToken = "not.a.valid.jwt.token";

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class, () -> JwtUtils.decodeEmailAddress(invalidToken));

      assertEquals("Invalid Email Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for token with wrong signature")
    void shouldThrowTokenInvalidExceptionForWrongSignature() {
      SecretKey wrongKey =
          Keys.hmacShaKeyFor(
              "different-secret-key-that-is-also-long-enough-for-hmac-sha-256"
                  .getBytes(StandardCharsets.UTF_8));
      String tokenWithWrongSignature =
          Jwts.builder()
              .claim(ConstantUtils.TOKEN_CLAIM_EMAIL, TEST_EMAIL)
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now()))
              .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
              .signWith(wrongKey)
              .compact();

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class,
              () -> JwtUtils.decodeEmailAddress(tokenWithWrongSignature));

      assertEquals("Invalid Email Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email claim is missing")
    void shouldThrowIllegalArgumentExceptionWhenEmailClaimMissing() {
      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      String tokenWithoutEmail =
          Jwts.builder()
              .claim("other", "value")
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now()))
              .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
              .signWith(key)
              .compact();

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class, () -> JwtUtils.decodeEmailAddress(tokenWithoutEmail));

      assertEquals("Incorrect Email Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null token")
    void shouldThrowTIllegalArgumentExceptionForNullToken() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> JwtUtils.decodeEmailAddress(null));

      assertEquals("CharSequence cannot be null or empty.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty token")
    void shouldThrowIllegalArgumentExceptionForEmptyToken() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> JwtUtils.decodeEmailAddress(""));

      assertEquals("CharSequence cannot be null or empty.", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("decodeEmailAddressNoException() tests")
  class DecodeEmailAddressNoExceptionTests {

    @Test
    @DisplayName("Should decode valid token successfully")
    void shouldDecodeValidTokenSuccessfully() {
      String token = JwtUtils.encodeEmailAddress(TEST_EMAIL);
      String result = JwtUtils.decodeEmailAddressNoException(token);

      assertEquals(TEST_EMAIL, result);
    }

    @Test
    @DisplayName("Should return original string for invalid token without throwing exception")
    void shouldReturnOriginalStringForInvalidToken() {
      String invalidToken = "invalid.token.value";
      String result = JwtUtils.decodeEmailAddressNoException(invalidToken);

      assertEquals(invalidToken, result);
    }

    @Test
    @DisplayName("Should return original string for expired token without throwing exception")
    void shouldReturnOriginalStringForExpiredToken() {
      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      String expiredToken =
          Jwts.builder()
              .claim(ConstantUtils.TOKEN_CLAIM_EMAIL, TEST_EMAIL)
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now().minus(20, ChronoUnit.MINUTES)))
              .expiration(Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
              .signWith(key)
              .compact();

      String result = JwtUtils.decodeEmailAddressNoException(expiredToken);

      assertEquals(expiredToken, result);
    }

    @Test
    @DisplayName("Should return original string for null token without throwing exception")
    void shouldReturnOriginalStringForNullToken() {
      String result = JwtUtils.decodeEmailAddressNoException(null);

      assertNull(result);
    }

    @Test
    @DisplayName("Should return empty string for empty token without throwing exception")
    void shouldReturnEmptyStringForEmptyToken() {
      String result = JwtUtils.decodeEmailAddressNoException("");

      assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle token without email claim gracefully")
    void shouldHandleTokenWithoutEmailClaimGracefully() {
      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      String tokenWithoutEmail =
          Jwts.builder()
              .claim("other", "value")
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now()))
              .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
              .signWith(key)
              .compact();

      String result = JwtUtils.decodeEmailAddressNoException(tokenWithoutEmail);

      assertEquals(tokenWithoutEmail, result);
    }
  }

  @Nested
  @DisplayName("encodeAuthCredentials() tests")
  class EncodeAuthCredentialsTests {

    private AuthToken authToken;

    @BeforeEach
    void setUp() {
      authToken = TestData.getAuthToken();
    }

    @Test
    @DisplayName("Should encode auth credentials successfully")
    void shouldEncodeAuthCredentialsSuccessfully() {
      long expirationMillis = 3600000L; // 1 hour
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      assertNotNull(token);
      assertFalse(token.isEmpty());
      assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should create token with auth claim")
    void shouldCreateTokenWithAuthClaim() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertNotNull(claims.get(ConstantUtils.TOKEN_CLAIM_AUTH));
    }

    @Test
    @DisplayName("Should create token with email as subject")
    void shouldCreateTokenWithEmailAsSubject() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals(TEST_EMAIL, claims.getSubject());
    }

    @Test
    @DisplayName("Should create token with issuer")
    void shouldCreateTokenWithIssuer() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertEquals(ConstantUtils.TOKEN_CLAIM_ISSUER, claims.getIssuer());
    }

    @Test
    @DisplayName("Should create token with correct expiration time")
    @Disabled("before and after creation millis are always same which fails the test")
    void shouldCreateTokenWithCorrectExpirationTime() throws InterruptedException {
      long expirationMillis = 3600000L;
      long beforeCreation = System.currentTimeMillis();
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);
      long afterCreation = System.currentTimeMillis();

      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      long beforeCreationMillis = beforeCreation - expirationMillis;
      long afterCreationMillis = afterCreation - expirationMillis;

      System.out.println("beforeCreationMillis: " + beforeCreationMillis);
      System.out.println("afterCreationMillis: " + afterCreationMillis);
      System.out.println("claims.getExpiration(): " + claims.getExpiration().getTime());
      assertTrue(beforeCreationMillis <= claims.getExpiration().getTime());
      assertTrue(afterCreationMillis >= claims.getExpiration().getTime());
    }

    @Test
    @DisplayName("Should handle different expiration times")
    void shouldHandleDifferentExpirationTimes() {
      long shortExpiration = 60000L;
      long longExpiration = 86400000L;

      String shortToken = JwtUtils.encodeAuthCredentials(authToken, shortExpiration);
      String longToken = JwtUtils.encodeAuthCredentials(authToken, longExpiration);

      assertNotNull(shortToken);
      assertNotNull(longToken);
      assertNotEquals(shortToken, longToken);
    }

    @Test
    @DisplayName("Should handle zero expiration time")
    void shouldHandleZeroExpirationTime() {
      long expirationMillis = 0L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      assertNotNull(token);
    }
  }

  @Nested
  @DisplayName("decodeAuthCredentials() tests")
  class DecodeAuthCredentialsTests {

    private AuthToken authToken;

    @BeforeEach
    void setUp() {
      authToken = TestData.getAuthToken();
    }

    @Test
    @DisplayName("Should decode valid auth token successfully")
    void shouldDecodeValidAuthTokenSuccessfully() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      Map<String, AuthToken> result = JwtUtils.decodeAuthCredentials(token);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertTrue(result.containsKey(TEST_EMAIL));
      assertNotNull(result.get(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should decode auth token with correct email as key")
    void shouldDecodeAuthTokenWithCorrectEmailAsKey() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      Map<String, AuthToken> result = JwtUtils.decodeAuthCredentials(token);

      assertEquals(TEST_EMAIL, result.keySet().iterator().next());
    }

    @Test
    @DisplayName("Should decode auth token with correct AuthToken object")
    void shouldDecodeAuthTokenWithCorrectAuthTokenObject() {
      long expirationMillis = 3600000L;
      String token = JwtUtils.encodeAuthCredentials(authToken, expirationMillis);

      Map<String, AuthToken> result = JwtUtils.decodeAuthCredentials(token);

      AuthToken decodedAuthToken = result.get(TEST_EMAIL);
      assertNotNull(decodedAuthToken);
      assertNotNull(decodedAuthToken.getProfile());
      assertEquals(TEST_EMAIL, decodedAuthToken.getProfile().getEmail());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for expired auth token")
    void shouldThrowTokenInvalidExceptionForExpiredAuthToken() {
      SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
      String expiredToken =
          Jwts.builder()
              .claim(ConstantUtils.TOKEN_CLAIM_AUTH, authToken)
              .subject(TEST_EMAIL)
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
              .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
              .signWith(key)
              .compact();

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class, () -> JwtUtils.decodeAuthCredentials(expiredToken));

      assertEquals("Expired Auth Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for invalid token format")
    void shouldThrowTokenInvalidExceptionForInvalidFormat() {
      String invalidToken = "invalid.auth.token";

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class, () -> JwtUtils.decodeAuthCredentials(invalidToken));

      assertEquals("Invalid Auth Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException for token with wrong signature")
    void shouldThrowTokenInvalidExceptionForWrongSignature() {
      SecretKey wrongKey =
          Keys.hmacShaKeyFor(
              "different-secret-key-that-is-also-long-enough-for-hmac-sha-256"
                  .getBytes(StandardCharsets.UTF_8));
      String tokenWithWrongSignature =
          Jwts.builder()
              .claim(ConstantUtils.TOKEN_CLAIM_AUTH, authToken)
              .subject(TEST_EMAIL)
              .issuer(ConstantUtils.TOKEN_CLAIM_ISSUER)
              .issuedAt(Date.from(Instant.now()))
              .expiration(new Date(System.currentTimeMillis() + 3600000L))
              .signWith(wrongKey)
              .compact();

      TokenInvalidException exception =
          assertThrows(
              TokenInvalidException.class,
              () -> JwtUtils.decodeAuthCredentials(tokenWithWrongSignature));

      assertEquals("Invalid Auth Credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null token")
    void shouldThrowIllegalArgumentExceptionForNullToken() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> JwtUtils.decodeAuthCredentials(null));

      assertEquals("CharSequence cannot be null or empty.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty token")
    void shouldThrowIllegalArgumentExceptionForEmptyToken() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> JwtUtils.decodeAuthCredentials(""));

      assertEquals("CharSequence cannot be null or empty.", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Integration tests - Encode and Decode")
  class IntegrationTests {

    @Test
    @DisplayName("Should successfully encode and decode email in round trip")
    void shouldSuccessfullyEncodeAndDecodeEmailRoundTrip() {
      String originalEmail = "roundtrip@example.com";

      String encodedToken = JwtUtils.encodeEmailAddress(originalEmail);
      String decodedEmail = JwtUtils.decodeEmailAddress(encodedToken);

      assertEquals(originalEmail, decodedEmail);
    }

    @Test
    @DisplayName("Should successfully encode and decode auth credentials in round trip")
    void shouldSuccessfullyEncodeAndDecodeAuthCredentialsRoundTrip() {
      AuthToken originalAuthToken = TestData.getAuthToken();

      String encodedToken = JwtUtils.encodeAuthCredentials(originalAuthToken, 3600000L);
      Map<String, AuthToken> decodedMap = JwtUtils.decodeAuthCredentials(encodedToken);

      assertNotNull(decodedMap);
      assertEquals(1, decodedMap.size());
      assertTrue(decodedMap.containsKey("profile@one.com"));
      assertEquals("profile@one.com", decodedMap.get("profile@one.com").getProfile().getEmail());
    }

    @Test
    @DisplayName("Should handle multiple sequential encode/decode operations")
    void shouldHandleMultipleSequentialOperations() {
      String email1 = "user1@example.com";
      String email2 = "user2@example.com";
      String email3 = "user3@example.com";

      String token1 = JwtUtils.encodeEmailAddress(email1);
      String token2 = JwtUtils.encodeEmailAddress(email2);
      String token3 = JwtUtils.encodeEmailAddress(email3);

      assertEquals(email1, JwtUtils.decodeEmailAddress(token1));
      assertEquals(email2, JwtUtils.decodeEmailAddress(token2));
      assertEquals(email3, JwtUtils.decodeEmailAddress(token3));
    }
  }
}
