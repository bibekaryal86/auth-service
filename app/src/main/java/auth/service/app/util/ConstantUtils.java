package auth.service.app.util;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtils {

  // provided at runtime
  public static final String ENV_SERVER_PORT = "PORT";
  public static final String ENV_SELF_USERNAME = "SELF_USERNAME";
  public static final String ENV_SELF_PASSWORD = "SELF_PASSWORD";
  public static final String ENV_DB_USERNAME = "DB_USERNAME";
  public static final String ENV_DB_PASSWORD = "DB_PASSWORD";
  public static final String ENV_SECRET_KEY = "SECRET_KEY";
  public static final String ENV_MAILJET_PUBLIC_KEY = "MJ_PUBLIC";
  public static final String ENV_MAILJET_PRIVATE_KEY = "MJ_PRIVATE";
  public static final String ENV_MAILJET_EMAIL_ADDRESS = "MJ_EMAIL";
  public static final String ENV_AUTHENV_USERNAME = "AUTHENV_USERNAME";
  public static final String ENV_AUTHENV_PASSWORD = "AUTHENV_PASSWORD";
  public static final List<String> ENV_KEY_NAMES =
      List.of(
          ENV_SERVER_PORT,
          ENV_SELF_USERNAME,
          ENV_SELF_PASSWORD,
          ENV_DB_USERNAME,
          ENV_DB_PASSWORD,
          ENV_SECRET_KEY,
          ENV_MAILJET_PUBLIC_KEY,
          ENV_MAILJET_PRIVATE_KEY,
          ENV_MAILJET_EMAIL_ADDRESS,
          ENV_AUTHENV_USERNAME,
          ENV_AUTHENV_PASSWORD);

  // ROLE NAMES
  public static final String APP_ROLE_NAME_GUEST = "GUEST";
  public static final String APP_ROLE_NAME_STANDARD = "STANDARD";
  public static final String APP_ROLE_NAME_SUPERUSER = "SUPERUSER";

  // messages
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Something went wrong, please try again later!";
}
