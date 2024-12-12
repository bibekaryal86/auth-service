package auth.service.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.service.AuditService;
import auth.service.app.util.JwtUtils;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ProfileNoAuthControllerTest extends BaseTest {

  private static final String REDIRECT_URL = "https://app-1-redirect-url.com/home/";
  private static String encodedEmail;

  @MockitoBean private AuditService auditService;
  @MockitoBean private EnvServiceConnector envServiceConnector;

  @BeforeAll
  static void setUpBeforeAll() {
    encodedEmail = JwtUtils.encodeEmailAddress(APP_USER_EMAIL);
  }

  @BeforeEach
  void setUpBeforeEach() {
    when(envServiceConnector.getRedirectUrls()).thenReturn(Map.of(APP_ID, REDIRECT_URL));
  }

  @AfterEach
  void tearDown() {
    reset(auditService, envServiceConnector);
  }

  @Test
  public void testValidateAppUserExit_Success() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/na_app_users/user/%s/validate_exit?toValidate=%s", APP_ID, encodedEmail))
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .location(REDIRECT_URL + "?is_validated=true");

    verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
    verify(auditService, after(100).times(1)).auditAppUserValidateExit(any(), any(), any());
  }

  @Test
  public void testValidateAppUserExit_Failure() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/na_app_users/user/%s/validate_exit?toValidate=%s", APP_ID, APP_USER_EMAIL))
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .location(REDIRECT_URL + "?is_validated=false");

    verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
    verify(auditService, after(100).times(1))
        .auditAppUserValidateFailure(any(), any(), any(), any());
  }

  @Test
  public void testResetAppUserExit_Success() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/na_app_users/user/%s/reset_exit?toReset=%s", APP_ID, encodedEmail))
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .location(REDIRECT_URL + "?is_reset=true&to_reset=" + APP_USER_EMAIL);

    verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
    verify(auditService, after(100).times(1)).auditAppUserResetExit(any(), any(), any());
  }

  @Test
  public void testResetAppUserExit_Failure() {
    webTestClient
        .get()
        .uri(
            String.format(
                "/api/v1/na_app_users/user/%s/reset_exit?toReset=%s", APP_ID, APP_USER_EMAIL))
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .location(REDIRECT_URL + "?is_reset=false");

    verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
    verify(auditService, after(100).times(1)).auditAppUserResetFailure(any(), any(), any(), any());
  }
}
