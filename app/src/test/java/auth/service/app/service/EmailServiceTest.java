package auth.service.app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.util.FileReaderUtils;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class EmailServiceTest extends BaseTest {

  @Mock private FileReaderUtils fileReaderUtils;
  @Mock private Email email;

  private EmailService emailService;

  private static ProfileEntity profileEntity;
  private static PlatformEntity platformEntity;
  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";

  @BeforeAll
  static void setUpBeforeAll() {
    profileEntity = TestData.getProfileEntities().getFirst();
    platformEntity = TestData.getPlatformEntities().getFirst();
  }

  @BeforeEach
  void setUpBeforeEach() {
    emailService = new EmailService(fileReaderUtils, email); // now mocks are initialized
  }

  @AfterEach
  void tearDown() {
    reset(fileReaderUtils);
  }

  @Test
  void testSendProfileValidationEmail() throws Exception {
    when(email.sendEmail(any(EmailRequest.class)))
        .thenReturn(new EmailResponse("request-id", 200, 1, 1, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString()))
        .thenReturn("{platform_name} : {activation_link}");

    emailService.sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL_FOR_EMAIL);
    verify(email, times(1)).sendEmail(any(EmailRequest.class));
    verify(fileReaderUtils, times(1))
        .readFileContents(eq("email/templates/profile_validate_email.html"));
  }

  @Test
  void testSendProfileResetEmail() throws Exception {
    when(email.sendEmail(any(EmailRequest.class)))
        .thenReturn(new EmailResponse("request-id", 200, 1, 1, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString()))
        .thenReturn("{platform_name} : {reset_link}");

    emailService.sendProfileResetEmail(platformEntity, profileEntity, BASE_URL_FOR_EMAIL);
    verify(email, times(1)).sendEmail(any(EmailRequest.class));
    verify(fileReaderUtils, times(1))
        .readFileContents(eq("email/templates/profile_reset_email.html"));
  }

  @Test
  void testSendProfilePasswordEmail() throws Exception {
    when(email.sendEmail(any(EmailRequest.class)))
        .thenReturn(new EmailResponse("request-id", 200, 1, 1, "{\"status\": \"OK\"}"));
    when(fileReaderUtils.readFileContents(anyString())).thenReturn("{platform_name}");

    emailService.sendProfilePasswordEmail(platformEntity, profileEntity);
    verify(email, times(1)).sendEmail(any(EmailRequest.class));
    verify(fileReaderUtils, times(1))
        .readFileContents(eq("email/templates/password_change_email.html"));
  }
}
