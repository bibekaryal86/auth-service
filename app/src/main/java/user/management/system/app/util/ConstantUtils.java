package user.management.system.app.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtils {

  // provided at runtime
  public static final String SERVER_PORT = "PORT";
  public static final String ENV_DB_USERNAME = "DB_USERNAME";
  public static final String ENV_DB_PASSWORD = "DB_PASSWORD";
  public static final String ENV_SECRET_KEY = "SECRET_KEY";
  public static final String ENV_MAILJET_PUBLIC_KEY = "MJ_PUBLIC";
  public static final String ENV_MAILJET_PRIVATE_KEY = "MJ_PRIVATE";
  public static final String ENV_MAILJET_EMAIL_ADDRESS = "MJ_EMAIL";

  // ROLE NAMES
  public static final String APP_ROLE_NAME_GUEST = "GUEST";
  public static final String APP_ROLE_NAME_STANDARD = "STANDARD";

  // messages
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Something went wrong, please try again later!";
}
