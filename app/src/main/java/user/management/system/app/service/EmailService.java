package user.management.system.app.service;

import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_EMAIL_ADDRESS;
import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_PRIVATE_KEY;
import static user.management.system.app.util.ConstantUtils.ENV_MAILJET_PUBLIC_KEY;
import static user.management.system.app.util.JwtUtils.encodeEmailAddress;
import static user.management.system.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.events.AppUserCreatedEvent;
import user.management.system.app.model.events.AppUserEmailUpdatedEvent;
import user.management.system.app.util.FileReaderUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final FileReaderUtils fileReaderUtils;

  private MailjetClient mailjetClient() {
    return new MailjetClient(
        ClientOptions.builder()
            .apiKey(getSystemEnvProperty(ENV_MAILJET_PUBLIC_KEY))
            .apiSecretKey(getSystemEnvProperty(ENV_MAILJET_PRIVATE_KEY))
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
                  new JSONObject().put("Email", emailFrom).put("Name", appName))
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

  @EventListener
  public void handleUserCreated(final AppUserCreatedEvent appUserCreatedEvent) {
    final AppsEntity appsEntity = appUserCreatedEvent.getAppsEntity();
    final AppUserEntity appUserEntity = appUserCreatedEvent.getAppUserEntity();
    final String baseUrl = appUserCreatedEvent.getBaseUrl();
    log.info("Handle User Created: [{}], [{}]", appsEntity.getName(), appUserEntity.getId());
    sendUserValidationEmail(appsEntity, appUserEntity, baseUrl);
  }

  @EventListener
  public void handleUserEmailUpdated(final AppUserEmailUpdatedEvent appUserEmailUpdatedEvent) {
    final AppsEntity appsEntity = appUserEmailUpdatedEvent.getAppsEntity();
    final AppUserEntity appUserEntity = appUserEmailUpdatedEvent.getAppUserEntity();
    final String baseUrl = appUserEmailUpdatedEvent.getBaseUrl();
    log.info("Handle User Email Updated: [{}], [{}]", appsEntity.getName(), appUserEntity.getId());
    sendUserValidationEmail(appsEntity, appUserEntity, baseUrl);
  }

  public void sendUserValidationEmail(
      final AppsEntity appsEntity, final AppUserEntity appUserEntity, final String baseUrl) {
    final String appName = convertAppNameToTitleCase(appsEntity.getName());
    final String encodedEmail = encodeEmailAddress(appUserEntity.getEmail());
    final String activationLink =
        String.format(
            "%s/api/v1/na_app_users/user/%s/validate_exit?toValidate=%s",
            baseUrl, appsEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/email_validate_user.html")
            .replace("{activation_link}", activationLink)
            .replace("{app_name}", appName);
    final String fullName =
        String.format("%s %s", appUserEntity.getFirstName(), appUserEntity.getLastName());
    final String subject = String.format("[%s] User Validation", appName);
    sendEmail(
        appName, appUserEntity.getEmail(), fullName, subject, null, emailHtmlContent, null, null);
  }

  public void sendUserResetEmail(
      final AppsEntity appsEntity, final AppUserEntity appUserEntity, final String baseUrl) {
    final String appName = convertAppNameToTitleCase(appsEntity.getName());
    final String encodedEmail = encodeEmailAddress(appUserEntity.getEmail());
    final String resetLink =
        String.format(
            "%s/api/v1/na_app_users/user/%s/reset_exit?toReset=%s",
            baseUrl, appsEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/email_reset_user.html")
            .replace("{reset_link}", resetLink)
            .replace("{app_name}", appName);
    final String fullName =
        String.format("%s %s", appUserEntity.getFirstName(), appUserEntity.getLastName());
    final String subject = String.format("[%s] User Reset", appName);
    sendEmail(
        appName, appUserEntity.getEmail(), fullName, subject, null, emailHtmlContent, null, null);
  }

  private String convertAppNameToTitleCase(final String appName) {
    final String[] words = appName.replace('-', ' ').split("\\s+");
    for (int i = 0; i < words.length; i++) {
      words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
    }
    return String.join(" ", words);
  }
}
