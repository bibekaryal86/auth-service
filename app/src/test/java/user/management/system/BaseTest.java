package user.management.system;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import user.management.system.app.config.SecurityConfigTest;
import user.management.system.app.config.TestDatasourceConfig;

@SpringBootTest
@ActiveProfiles("springboottest")
@ExtendWith(BaseTestExtension.class)
@Import({SecurityConfigTest.class, TestDatasourceConfig.class})
public abstract class BaseTest {}
