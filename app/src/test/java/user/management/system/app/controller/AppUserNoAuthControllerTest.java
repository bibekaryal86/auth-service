package user.management.system.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user.management.system.BaseTest;
import user.management.system.app.connector.AuthenvServiceConnector;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.JwtUtils;

public class AppUserNoAuthControllerTest extends BaseTest {

  private static final String REDIRECT_URL = "https://app-1-redirect-url.com/home/";
  private static String encodedEmail;

  @MockitoBean private AuditService auditService;
  @MockitoBean private AuthenvServiceConnector authenvServiceConnector;

  @BeforeAll
  static void setUpBeforeAll() {
    encodedEmail = JwtUtils.encodeEmailAddress(APP_USER_EMAIL);
  }

  @BeforeEach
  void setUpBeforeEach() {
    when(authenvServiceConnector.getRedirectUrls()).thenReturn(Map.of(APP_ID, REDIRECT_URL));
  }

  @AfterEach
  void tearDown() {
    reset(auditService, authenvServiceConnector);
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

    verify(authenvServiceConnector, after(100).times(1)).getRedirectUrls();
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

    verify(authenvServiceConnector, after(100).times(1)).getRedirectUrls();
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

    verify(authenvServiceConnector, after(100).times(1)).getRedirectUrls();
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

    verify(authenvServiceConnector, after(100).times(1)).getRedirectUrls();
    verify(auditService, after(100).times(1)).auditAppUserResetFailure(any(), any(), any(), any());
  }
}
