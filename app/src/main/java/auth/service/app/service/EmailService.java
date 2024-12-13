package auth.service.app.service;

import static auth.service.app.util.ConstantUtils.ENV_MAILJET_EMAIL_ADDRESS;
import static auth.service.app.util.JwtUtils.encodeEmailAddress;
import static auth.service.app.util.SystemEnvPropertyUtils.getSystemEnvProperty;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.util.FileReaderUtils;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final FileReaderUtils fileReaderUtils;
  private final MailjetClient mailjetClient;

  public void sendEmail(
      final String platformName,
      final String emailTo,
      final String emailToFullName,
      final String subject,
      final String text,
      final String html,
      final String attachmentFileName,
      final String attachment) {
    log.debug(
        "Sending Email: [{}], [{}], [{}], [{}]", platformName, emailTo, emailToFullName, subject);

    try {
      final String emailFrom = getSystemEnvProperty(ENV_MAILJET_EMAIL_ADDRESS, null);

      final JSONObject message =
          new JSONObject()
              .put(Emailv31.Message.CUSTOMID, UUID.randomUUID().toString())
              .put(
                  Emailv31.Message.FROM,
                  new JSONObject().put("Email", emailFrom).put("Name", platformName))
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

      final MailjetResponse response = mailjetClient.post(request);

      if (response.getStatus() == 200) {
        log.info("Send Email Response Success...");
      } else {
        log.info("Send Email Response Failure:  [ {} ]", response.getData());
      }
    } catch (Exception ex) {
      log.error("Send Email Error...", ex);
    }
  }

  public void sendProfileValidationEmail(
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String baseUrl) {
    final String platformName = convertAppNameToTitleCase(platformEntity.getPlatformName());
    final String encodedEmail = encodeEmailAddress(profileEntity.getEmail());
    final String activationLink =
        String.format(
            "%s/api/v1/na_profile/platform/%s/validate_exit?toValidate=%s",
            baseUrl, platformEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/profile_validate_email.html")
            .replace("{activation_link}", activationLink)
            .replace("{platform_name}", platformName);
    final String fullName =
        String.format("%s %s", profileEntity.getFirstName(), profileEntity.getLastName());
    final String subject = String.format("[%s] Profile Validation", platformName);
    sendEmail(
        platformName,
        profileEntity.getEmail(),
        fullName,
        subject,
        null,
        emailHtmlContent,
        null,
        null);
  }

  public void sendProfileResetEmail(
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String baseUrl) {
    final String platformName = convertAppNameToTitleCase(platformEntity.getPlatformName());
    final String encodedEmail = encodeEmailAddress(profileEntity.getEmail());
    final String resetLink =
        String.format(
            "%s/api/v1/na_profile/platform/%s/reset_exit?toReset=%s",
            baseUrl, platformEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/profile_reset_email.html")
            .replace("{reset_link}", resetLink)
            .replace("{platform_name}", platformName);
    final String fullName =
        String.format("%s %s", profileEntity.getFirstName(), profileEntity.getLastName());
    final String subject = String.format("[%s] Profile Reset", platformName);
    sendEmail(
        platformName,
        profileEntity.getEmail(),
        fullName,
        subject,
        null,
        emailHtmlContent,
        null,
        null);
  }

  public void sendProfilePasswordEmail(
      final PlatformEntity platformEntity, final ProfileEntity profileEntity) {
    final String platformName = convertAppNameToTitleCase(platformEntity.getPlatformName());
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/password_change_email.html")
            .replace("{platform_name}", platformName);
    final String fullName =
        String.format("%s %s", profileEntity.getFirstName(), profileEntity.getLastName());
    final String subject = String.format("[%s] Password Change Notification", platformName);
    sendEmail(
        platformName,
        profileEntity.getEmail(),
        fullName,
        subject,
        null,
        emailHtmlContent,
        null,
        null);
  }

  private String convertAppNameToTitleCase(final String platformName) {
    final String[] words = platformName.replace('-', ' ').split("\\s+");
    for (int i = 0; i < words.length; i++) {
      words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
    }
    return String.join(" ", words);
  }
}
