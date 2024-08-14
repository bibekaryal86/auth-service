package user.management.system.app.service;

import static user.management.system.app.util.CommonUtils.getSystemEnvProperty;
import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_EMAIL_ADDRESS;
import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_PRIVATE_KEY;
import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_PUBLIC_KEY;
import static user.management.system.app.util.JwtUtils.encodeEmailAddress;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.util.FileReaderUtils;

@Slf4j
@Service
public class EmailService {

  private final FileReaderUtils fileReaderUtils;

  public EmailService(final FileReaderUtils fileReaderUtils) {
    this.fileReaderUtils = fileReaderUtils;
  }

  private MailjetClient mailjetClient() {
    return new MailjetClient(
        ClientOptions.builder()
            .apiKey(getSystemEnvProperty(ENV_MAILJET_PUBLIC_KEY, null))
            .apiSecretKey(getSystemEnvProperty(ENV_MAILJET_PRIVATE_KEY, null))
            .build());
  }

  public void sendEmail(
      final String appName,
      final String emailTo,
      final String emailToFullName,
      final String subject,
      final String text,
      final String html,
      final String attachmentFileName,
      final String attachment) {
    log.debug("Sending Email: [{}], [{}], [{}], [{}]", appName, emailTo, emailToFullName, subject);

    try {
      final String emailFrom = getSystemEnvProperty(ENV_MAILJET_EMAIL_ADDRESS, null);

      final JSONObject message =
          new JSONObject()
              .put(Emailv31.Message.CUSTOMID, UUID.randomUUID().toString())
              .put(
                  Emailv31.Message.FROM,
                  new JSONObject()
                      .put("Email", emailFrom)
                      .put("Name", String.format("[%s] %s", appName, emailFrom)))
              .put(
                  Emailv31.Message.TO,
                  new JSONArray()
                      .put(new JSONObject().put("Email", emailTo).put("Name", emailToFullName)))
              .put(Emailv31.Message.SUBJECT, subject);

      if (StringUtils.hasText(text)) {
        message.put(Emailv31.Message.TEXTPART, text);
      }

      if (StringUtils.hasText(html)) {
        message.put(Emailv31.Message.HTMLPART, html);
      }

      if (StringUtils.hasText(attachmentFileName) && StringUtils.hasText(attachment)) {
        message.put(
            Emailv31.Message.ATTACHMENTS,
            new JSONArray()
                .put(
                    new JSONObject()
                        .put("ContentType", "text/plain")
                        .put("Filename", attachmentFileName)
                        .put("Base64Content", attachment)));
      }

      final MailjetRequest request =
          new MailjetRequest(Emailv31.resource)
              .property(Emailv31.MESSAGES, new JSONArray().put(message));

      final MailjetResponse response = mailjetClient().post(request);

      if (response.getStatus() == 200) {
        log.info("Send Email Response Success...");
      } else {
        log.info("Send Email Response Failure:  [ {} ]", response.getData());
      }
    } catch (Exception ex) {
      log.error("Send Email Error...", ex);
    }
  }

  public void sendUserValidationEmail(final AppUserEntity appUserEntity, final String baseUrl) {
    final String encodedEmail = encodeEmailAddress(appUserEntity.getEmail(), 15);
    final String activationLink = String.format("%s/na_app_users/validate_exit/?toValidate=%s", baseUrl, encodedEmail);
    final String emailHtmlContent = fileReaderUtils.readFileContents("email/templates/email_validate_user.html").replace("{activation_link}", activationLink);
    final String fullName = String.format("%s %s", appUserEntity.getFirstName(), appUserEntity.getLastName());
    final String subject = String.format("[%s] User Activation", appUserEntity.getApp());
    sendEmail(appUserEntity.getApp(), appUserEntity.getEmail(), fullName, subject, null, emailHtmlContent, null, null);
  }

  public void sendUserResetEmail(final AppUserEntity appUserEntity, final String baseUrl) {
    final String encodedEmail = encodeEmailAddress(appUserEntity.getEmail(), 15);
    final String resetLink = String.format("%s/na_app_users/reset_mid/?toReset=%s", baseUrl, encodedEmail);
    final String emailHtmlContent = fileReaderUtils.readFileContents("email/templates/email_reset_user.html").replace("{reset_link}", resetLink);
    final String fullName = String.format("%s %s", appUserEntity.getFirstName(), appUserEntity.getLastName());
    final String subject = String.format("[%s] User Reset", appUserEntity.getApp());
    sendEmail(appUserEntity.getApp(), appUserEntity.getEmail(), fullName, subject, null, emailHtmlContent, null, null);
  }
}
