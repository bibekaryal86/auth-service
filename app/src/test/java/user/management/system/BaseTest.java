package user.management.system;

import static user.management.system.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_SELF_USERNAME;

import helper.BaseTestExtension;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import user.management.system.app.config.TestConfigs;
import user.management.system.app.config.TestDatasourceConfig;
import user.management.system.app.config.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("springboottest")
@ExtendWith(BaseTestExtension.class)
@Import({TestDatasourceConfig.class, TestSecurityConfig.class, TestConfigs.class})
public abstract class BaseTest {

  @LocalServerPort protected int localServerPort;

  @Autowired protected WebTestClient webTestClient;

  protected String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", ENV_SELF_USERNAME, ENV_SELF_PASSWORD)
                  .getBytes(StandardCharsets.UTF_8));

  protected static final String APP_ID = "app-1";
  protected static final String DECODED_EMAIL = "firstlast@one.com";
}
