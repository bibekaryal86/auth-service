package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import helper.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.events.AppUserCreatedEvent;
import user.management.system.app.model.events.AppUserUpdatedEvent;
import user.management.system.app.util.FileReaderUtils;

public class EmailServiceTest extends BaseTest {

  @MockBean private FileReaderUtils fileReaderUtils;

  @MockBean private MailjetClient mailjetClient;

  @Autowired private EmailService emailService;

  private static AppUserEntity appUserEntity;
  private static AppsEntity appsEntity;
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";

  @BeforeAll
  static void setUp() {
    appUserEntity = TestData.getAppUserEntities().getFirst();
    appsEntity = TestData.getAppsEntities().getFirst();
  }

  @AfterEach
  void tearDown() {
    reset(mailjetClient);
  }

  @Test
  void testSendEmail_Success() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));

    String appName = "Test App";
    String emailTo = "test@example.com";
    String emailToFullName = "Test User";
    String subject = "Test Email";
    String text = "Test email text";
    String html = "<p>Test email html</p>";
    String attachmentFileName = "test.txt";
    String attachment = "Test attachment content";

    assertDoesNotThrow(
        () ->
            emailService.sendEmail(
                appName,
                emailTo,
                emailToFullName,
                subject,
                text,
                html,
                attachmentFileName,
                attachment));
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  }

  @Test
  void testSendEmail_Failure_ErrorNotThrown() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenThrow(new MailjetException("something happened"));

    String appName = "Test App";
    String emailTo = "test@example.com";
    String emailToFullName = "Test User";
    String subject = "Test Email";
    String text = "Test email text";
    String html = "<p>Test email html</p>";
    String attachmentFileName = "test.txt";
    String attachment = "Test attachment content";

    assertDoesNotThrow(
        () ->
            emailService.sendEmail(
                appName,
                emailTo,
                emailToFullName,
                subject,
                text,
                html,
                attachmentFileName,
                attachment));
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  }

  @Test
  void testHandleUserCreated() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString()))
        .thenReturn("{app_name} : {activation_link}");

    AppUserCreatedEvent appUserCreatedEvent =
        new AppUserCreatedEvent(this, appUserEntity, appsEntity, BASE_URL_FOR_EMAIL);
    emailService.handleUserCreated(appUserCreatedEvent);

    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
    verify(fileReaderUtils, times(1)).readFileContents(anyString());
  }

  @Test
  void testHandleUserEmailUpdated() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString()))
        .thenReturn("{app_name} : {activation_link}");

    AppUserUpdatedEvent appUserUpdatedEvent =
        new AppUserUpdatedEvent(this, appUserEntity, appsEntity, BASE_URL_FOR_EMAIL);
    emailService.handleUserEmailUpdated(appUserUpdatedEvent);
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
    verify(fileReaderUtils, times(1)).readFileContents(anyString());
  }

  @Test
  void testSendUserValidationEmail() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString()))
        .thenReturn("{app_name} : {activation_link}");

    emailService.sendUserValidationEmail(appsEntity, appUserEntity, BASE_URL_FOR_EMAIL);
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
    verify(fileReaderUtils, times(1))
        .readFileContents(eq("email/templates/email_validate_user.html"));
  }

  @Test
  void testSendUserResetEmail() throws Exception {
    when(mailjetClient.post(any(MailjetRequest.class)))
        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString())).thenReturn("{app_name} : {reset_link}");

    emailService.sendUserResetEmail(appsEntity, appUserEntity, BASE_URL_FOR_EMAIL);
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
    verify(fileReaderUtils, times(1)).readFileContents(eq("email/templates/email_reset_user.html"));
  }
}
