package auth.service.app.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtils {

  public static final Gson GSON =
      new GsonBuilder()
          .setExclusionStrategies(
              new ExclusionStrategy() {
                public boolean shouldSkipField(FieldAttributes f) {
                  return (f == null);
                }

                public boolean shouldSkipClass(Class<?> clazz) {
                  return false;
                }
              })
          .create();

  // provided at runtime
  public static final String ENV_SERVER_PORT = "PORT";
  public static final String SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";
  public static final String ENV_SELF_USERNAME = "SELF_USERNAME";
  public static final String ENV_SELF_PASSWORD = "SELF_PASSWORD";
  public static final String ENV_DB_HOST_PROD = "DB_HOST_PROD";
  public static final String ENV_DB_NAME_PROD = "DB_NAME_PROD";
  public static final String ENV_DB_USERNAME_PROD = "DB_USERNAME_PROD";
  public static final String ENV_DB_PASSWORD_PROD = "DB_PASSWORD_PROD";
  public static final String ENV_DB_HOST_SANDBOX = "DB_HOST_SANDBOX";
  public static final String ENV_DB_NAME_SANDBOX = "DB_NAME_SANDBOX";
  public static final String ENV_DB_USERNAME_SANDBOX = "DB_USERNAME_SANDBOX";
  public static final String ENV_DB_PASSWORD_SANDBOX = "DB_PASSWORD_SANDBOX";
  public static final String ENV_SECRET_KEY = "SECRET_KEY";
  public static final String ENV_MAILJET_PUBLIC_KEY = "MJ_PUBLIC";
  public static final String ENV_MAILJET_PRIVATE_KEY = "MJ_PRIVATE";
  public static final String ENV_MAILJET_EMAIL_ADDRESS = "MJ_EMAIL";
  public static final String ENV_ENVSVC_BASE_URL = "ENVSVC_BASE_URL";
  public static final String ENV_ENVSVC_USERNAME = "ENVSVC_USR";
  public static final String ENV_ENVSVC_PASSWORD = "ENVSVC_PWD";
  public static final List<String> ENV_KEY_NAMES =
      List.of(
          ENV_SERVER_PORT,
          SPRING_PROFILES_ACTIVE,
          ENV_SELF_USERNAME,
          ENV_SELF_PASSWORD,
          ENV_DB_HOST_PROD,
          ENV_DB_NAME_PROD,
          ENV_DB_USERNAME_PROD,
          ENV_DB_PASSWORD_PROD,
          ENV_DB_HOST_SANDBOX,
          ENV_DB_NAME_SANDBOX,
          ENV_DB_USERNAME_SANDBOX,
          ENV_DB_PASSWORD_SANDBOX,
          ENV_SECRET_KEY,
          ENV_MAILJET_PUBLIC_KEY,
          ENV_MAILJET_PRIVATE_KEY,
          ENV_MAILJET_EMAIL_ADDRESS,
          ENV_ENVSVC_BASE_URL,
          ENV_ENVSVC_USERNAME,
          ENV_ENVSVC_PASSWORD);

  public static final List<String> ENV_KEY_NAMES_PROD =
      List.of(ENV_DB_HOST_PROD, ENV_DB_NAME_PROD, ENV_DB_USERNAME_PROD, ENV_DB_PASSWORD_PROD);
  public static final List<String> ENV_KEY_NAMES_SANDBOX =
      List.of(
          ENV_DB_HOST_SANDBOX,
          ENV_DB_NAME_SANDBOX,
          ENV_DB_USERNAME_SANDBOX,
          ENV_DB_PASSWORD_SANDBOX);

  // ROLE NAMES
  public static final String ROLE_NAME_GUEST = "GUEST";
  public static final String ROLE_NAME_STANDARD = "STANDARD";
  public static final String ROLE_NAME_SUPERUSER = "SUPERUSER";

  // JWT TOKEN
  public static final String TOKEN_CLAIM_EMAIL = "emailToken";
  public static final String TOKEN_CLAIM_ISSUER = "authsvc";
  public static final String TOKEN_CLAIM_AUTH = "authToken";

  // ID for elements not found
  public static final Long ELEMENT_ID_NOT_FOUND = -1L;

  // messages
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Something went wrong, please try again later!";

  // fields
  public static final String DELETED_DATE = "deletedDate";
  public static final String NOT_NULL_VALUE = "NOT_NULL";
}
