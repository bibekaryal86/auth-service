package auth.service.app.service;

import auth.service.BaseTest;

public class EmailServiceTest extends BaseTest {

  //  @MockitoBean private FileReaderUtils fileReaderUtils;
  //
  //  @MockitoBean private MailjetClient mailjetClient;
  //
  //  @Autowired private EmailService emailService;
  //
  //  private static AppUserEntity appUserEntity;
  //  private static AppsEntity appsEntity;
  //  private static final String BASE_URL_FOR_EMAIL = "https://some-url.com/";
  //
  //  @BeforeAll
  //  static void setUp() {
  //    appUserEntity = TestData.getAppUserEntities().getFirst();
  //    appsEntity = TestData.getAppsEntities().getFirst();
  //  }
  //
  //  @AfterEach
  //  void tearDown() {
  //    reset(mailjetClient);
  //  }
  //
  //  @Test
  //  void testSendEmail_Success() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
  //
  //    String appName = "Test App";
  //    String emailTo = "test@example.com";
  //    String emailToFullName = "Test User";
  //    String subject = "Test Email";
  //    String text = "Test email text";
  //    String html = "<p>Test email html</p>";
  //    String attachmentFileName = "test.txt";
  //    String attachment = "Test attachment content";
  //
  //    assertDoesNotThrow(
  //        () ->
  //            emailService.sendEmail(
  //                appName,
  //                emailTo,
  //                emailToFullName,
  //                subject,
  //                text,
  //                html,
  //                attachmentFileName,
  //                attachment));
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //  }
  //
  //  @Test
  //  void testSendEmail_Failure_ErrorNotThrown() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenThrow(new MailjetException("something happened"));
  //
  //    String appName = "Test App";
  //    String emailTo = "test@example.com";
  //    String emailToFullName = "Test User";
  //    String subject = "Test Email";
  //    String text = "Test email text";
  //    String html = "<p>Test email html</p>";
  //    String attachmentFileName = "test.txt";
  //    String attachment = "Test attachment content";
  //
  //    assertDoesNotThrow(
  //        () ->
  //            emailService.sendEmail(
  //                appName,
  //                emailTo,
  //                emailToFullName,
  //                subject,
  //                text,
  //                html,
  //                attachmentFileName,
  //                attachment));
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //  }
  //
  //  @Test
  //  void testHandleUserCreated() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
  //    when(fileReaderUtils.readFileContents(anyString()))
  //        .thenReturn("{app_name} : {activation_link}");
  //
  //    AppUserCreatedEvent appUserCreatedEvent =
  //        new AppUserCreatedEvent(this, appUserEntity, appsEntity, BASE_URL_FOR_EMAIL);
  //    emailService.handleUserCreated(appUserCreatedEvent);
  //
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //    verify(fileReaderUtils, times(1)).readFileContents(anyString());
  //  }
  //
  //  @Test
  //  void testHandleUserEmailUpdated() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
  //    when(fileReaderUtils.readFileContents(anyString()))
  //        .thenReturn("{app_name} : {activation_link}");
  //
  //    AppUserUpdatedEvent appUserUpdatedEvent =
  //        new AppUserUpdatedEvent(this, appUserEntity, appsEntity, BASE_URL_FOR_EMAIL);
  //    emailService.handleUserEmailUpdated(appUserUpdatedEvent);
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //    verify(fileReaderUtils, times(1)).readFileContents(anyString());
  //  }
  //
  //  @Test
  //  void testSendProfileValidationEmail() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
  //    when(fileReaderUtils.readFileContents(anyString()))
  //        .thenReturn("{app_name} : {activation_link}");
  //
  //    emailService.sendProfileValidationEmail(appsEntity, appUserEntity, BASE_URL_FOR_EMAIL);
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //    verify(fileReaderUtils, times(1))
  //        .readFileContents(eq("email/templates/profile_validate_email.html"));
  //  }
  //
  //  @Test
  //  void testSendProfileResetEmail() throws Exception {
  //    when(mailjetClient.post(any(MailjetRequest.class)))
  //        .thenReturn(new MailjetResponse(200, "{\"status\": \"OK\"}"));
  //    when(fileReaderUtils.readFileContents(anyString())).thenReturn("{app_name} : {reset_link}");
  //
  //    emailService.sendProfileResetEmail(appsEntity, appUserEntity, BASE_URL_FOR_EMAIL);
  //    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  //    verify(fileReaderUtils, times(1))
  //        .readFileContents(eq("email/templates/profile_reset_email.html"));
  //  }
}
