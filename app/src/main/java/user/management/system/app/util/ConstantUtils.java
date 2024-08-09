package user.management.system.app.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtils {

  // provided at runtime
  public static final String SERVER_PORT = "PORT";

  // messages
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Something went wrong, please try again later!";
}
