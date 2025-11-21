package auth.service.app.service;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.util.FileReaderUtils;
import auth.service.app.util.JwtUtils;
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

  private final FileReaderUtils fileReaderUtils;
  private final Email email;

  @Autowired
  public EmailService(final FileReaderUtils fileReaderUtils) {
    this.fileReaderUtils = fileReaderUtils;
    this.email = new Email();
  }

  // to be used in tests
  @Deprecated
  public EmailService(final FileReaderUtils fileReaderUtils, final Email email) {
    this.fileReaderUtils = fileReaderUtils;
    this.email = email;
  }

  public void sendProfileValidationEmail(
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String baseUrl) {
    final String platformName = convertAppNameToTitleCase(platformEntity.getPlatformName());
    final String encodedEmail = JwtUtils.encodeEmailAddress(profileEntity.getEmail());
    final String activationLink =
        String.format(
            "%s/api/v1/na_profiles/platform/%s/validate_exit?toValidate=%s",
            baseUrl, platformEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/profile_validate_email.html")
            .replace("{activation_link}", activationLink)
            .replace("{platform_name}", platformName);
    final String fullName =
        String.format("%s %s", profileEntity.getFirstName(), profileEntity.getLastName());
    final String subject = String.format("[%s] Profile Validation", platformName);
    final EmailRequest emailRequest =
        emailRequest(platformName, profileEntity.getEmail(), fullName, subject, emailHtmlContent);
    email.sendEmail(emailRequest);
  }

  public void sendProfileResetEmail(
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String baseUrl) {
    final String platformName = convertAppNameToTitleCase(platformEntity.getPlatformName());
    final String encodedEmail = JwtUtils.encodeEmailAddress(profileEntity.getEmail());
    final String resetLink =
        String.format(
            "%s/api/v1/na_profiles/platform/%s/reset_exit?toReset=%s",
            baseUrl, platformEntity.getId(), encodedEmail);
    final String emailHtmlContent =
        fileReaderUtils
            .readFileContents("email/templates/profile_reset_email.html")
            .replace("{reset_link}", resetLink)
            .replace("{platform_name}", platformName);
    final String fullName =
        String.format("%s %s", profileEntity.getFirstName(), profileEntity.getLastName());
    final String subject = String.format("[%s] Profile Reset", platformName);
    final EmailRequest emailRequest =
        emailRequest(platformName, profileEntity.getEmail(), fullName, subject, emailHtmlContent);
    email.sendEmail(emailRequest);
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
    final EmailRequest emailRequest =
        emailRequest(platformName, profileEntity.getEmail(), fullName, subject, emailHtmlContent);
    email.sendEmail(emailRequest);
  }

  private String convertAppNameToTitleCase(final String platformName) {
    final String[] words = platformName.replace('-', ' ').split("\\s+");
    for (int i = 0; i < words.length; i++) {
      words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
    }
    return String.join(" ", words);
  }

  private EmailRequest emailRequest(
      final String platformName,
      final String emailTo,
      final String fullName,
      final String subject,
      final String emailHtmlContent) {
    final String platformNameSubject = "[" + platformName + "] " + subject;
    return new EmailRequest(
        new EmailRequest.EmailContact("not@required@email@api", platformName),
        List.of(new EmailRequest.EmailContact(emailTo, fullName)),
        Collections.emptyList(),
        new EmailRequest.EmailContent(platformNameSubject, null, emailHtmlContent),
        Collections.emptyList());
  }
}
