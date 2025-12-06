package integration;

import static auth.service.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_SELF_USERNAME;

import auth.service.App;
import helper.BaseTestExtension;
import integration.auth.service.app.config.TestConfigs;
import integration.auth.service.app.config.TestDatasourceConfig;
import integration.auth.service.app.config.TestSecurityConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("springboottest")
@ExtendWith(BaseTestExtension.class)
@Import({TestDatasourceConfig.class, TestSecurityConfig.class, TestConfigs.class})
@AutoConfigureWebTestClient
public abstract class BaseTest {

  @LocalServerPort protected int localServerPort;

  @Autowired protected WebTestClient webTestClient;

  protected static final Long ID = 1L;
  protected static final Long ID_DELETED = 9L;
  protected static final Long ID_NOT_FOUND = 99L;
  protected static final String EMAIL = "profile@one.com";
  protected static final boolean INCLUDE_DELETED = true;

  protected String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", ENV_SELF_USERNAME, ENV_SELF_PASSWORD)
                  .getBytes(StandardCharsets.UTF_8));
}
