package helper;

import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class BaseTestExtension implements BeforeAllCallback, AfterAllCallback {

  private static boolean isSetupDone = false;
  private static final AtomicInteger flywayCleanCount = new AtomicInteger(0);
  private static final AtomicInteger allTestClassesCount =
      new AtomicInteger(TestClassesFinder.findTestClasses().size());

  @Override
  public void beforeAll(final ExtensionContext extensionContext) {
    if (!isSetupDone) {
      TestData.setSystemEnvPropertyTestData();

      ApplicationContext applicationContext =
          SpringExtension.getApplicationContext(extensionContext);
      Flyway flyway = applicationContext.getBean(Flyway.class);
      flyway.migrate();

      isSetupDone = true;
    }
  }

  @Override
  public void afterAll(final ExtensionContext extensionContext) {
    allTestClassesCount.decrementAndGet();

    if (allTestClassesCount.get() == 0) {
      flywayCleanCount.incrementAndGet();
      ApplicationContext applicationContext =
          SpringExtension.getApplicationContext(extensionContext);
      Flyway flyway = applicationContext.getBean(Flyway.class);
      flyway.clean();
    }

    if (flywayCleanCount.get() > 1) {
      throw new IllegalStateException(
          String.format("FlywayCleanCount is more than 1: [%s]", flywayCleanCount.get()));
    }

    if (allTestClassesCount.get() < 0) {
      throw new IllegalStateException(
          String.format("AllTestClassesCount is less than 0: [%s]", allTestClassesCount.get()));
    }
  }
}
