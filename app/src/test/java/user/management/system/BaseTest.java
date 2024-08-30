package user.management.system;

import helper.BaseTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import user.management.system.app.config.TestDatasourceConfig;
import user.management.system.app.config.TestSecurityConfig;

@SpringBootTest
@ActiveProfiles("springboottest")
@ExtendWith(BaseTestExtension.class)
@Import({TestDatasourceConfig.class, TestSecurityConfig.class})
public abstract class BaseTest {}
