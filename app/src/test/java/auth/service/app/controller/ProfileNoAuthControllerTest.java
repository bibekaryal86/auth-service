package auth.service.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.util.JwtUtils;
import helper.TestData;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ProfileNoAuthControllerTest extends BaseTest {

    private static final String REDIRECT_URL = "https://some-app-redirect-url.com/home/";
    private static String encodedEmail;

    @MockitoBean private AuditService auditService;
    @MockitoBean private EnvServiceConnector envServiceConnector;

    @BeforeAll
    static void setUpBeforeAll() {
        encodedEmail = JwtUtils.encodeEmailAddress(EMAIL);
    }

    @BeforeEach
    void setUpBeforeEach() {
        PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
        when(envServiceConnector.getRedirectUrls()).thenReturn(Map.of(platformEntity.getPlatformName(), REDIRECT_URL));
    }

    @AfterEach
    void tearDown() {
        reset(auditService, envServiceConnector);
    }

    @Test
    public void testValidateProfileExit_Success() {
        webTestClient
                .get()
                .uri(
                        String.format(
                                "/api/v1/na_profiles/platform/%s/validate_exit?toValidate=%s", ID, encodedEmail))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location(REDIRECT_URL + "?is_validated=true");

        verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
        verify(auditService, after(200).times(1))
                .auditProfile(
                        any(HttpServletRequest.class),
                        any(ProfileEntity.class),
                        argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_EXIT)),
                        any(String.class));
    }

    @Test
    public void testValidateProfileExit_Failure() {
        webTestClient
                .get()
                .uri(
                        String.format("/api/v1/na_profiles/platform/%s/validate_exit?toValidate=%s", ID, EMAIL))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location(REDIRECT_URL + "?is_validated=false");

        verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
        verify(auditService, after(200).times(1))
                .auditProfile(
                        any(HttpServletRequest.class),
                        any(ProfileEntity.class),
                        argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR)),
                        any(String.class));
    }

    @Test
    public void testResetProfileExit_Success() {
        webTestClient
                .get()
                .uri(
                        String.format(
                                "/api/v1/na_profiles/platform/%s/reset_exit?toReset=%s", ID, encodedEmail))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location(REDIRECT_URL + "?is_reset=true&to_reset=" + EMAIL);

        verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
        verify(auditService, after(200).times(1))
                .auditProfile(
                        any(HttpServletRequest.class),
                        any(ProfileEntity.class),
                        argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_EXIT)),
                        any(String.class));
    }

    @Test
    public void testResetProfileExit_Failure() {
        webTestClient
                .get()
                .uri(String.format("/api/v1/na_profiles/platform/%s/reset_exit?toReset=%s", ID, EMAIL))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location(REDIRECT_URL + "?is_reset=false");

        verify(envServiceConnector, after(100).times(1)).getRedirectUrls();
        verify(auditService, after(200).times(1))
                .auditProfile(
                        any(HttpServletRequest.class),
                        any(ProfileEntity.class),
                        argThat(eventType -> eventType.equals(AuditEnums.AuditProfile.PROFILE_RESET_ERROR)),
                        any(String.class));
    }
}