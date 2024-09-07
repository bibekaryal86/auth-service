package user.management.system.app.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@TestConfiguration
@EnableConfigurationProperties(JpaProperties.class)
public class TestDatasourceConfig {

  @Value("${TEST_DB_USERNAME}")
  private String testDbUsername;

  @Value("${TEST_DB_PASSWORD}")
  private String testDbPassword;

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(
        "jdbc:postgresql://expensively-roused-flathead.data-1.usw2.tembo.io:5432/postgres_test");
    dataSource.setUsername(testDbUsername);
    dataSource.setPassword(testDbPassword);
    return dataSource;
  }

  @Bean
  public Flyway flyway(final DataSource dataSource) {
    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/test_migration")
            .baselineOnMigrate(true)
            .cleanDisabled(false)
            .load();

    flyway.clean();
    flyway.migrate();

    return flyway;
  }

  // no longer required as clean/migrate handled in flyway bean itself
  //  @Bean
  //  public org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
  // flywayMigrationInitializer(final Flyway flyway) {
  //    // This bean ensures Flyway runs automatically on startup
  //    return new org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer(flyway);
  //  }

  @Bean
  public Map<String, Object> jpaProperties(final JpaProperties jpaProperties) {
    Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
    properties.put("hibernate.hbm2ddl.auto", "validate");
    properties.put("hibernate.show_sql", true);
    properties.put("hibernate.open-in-view", false);
    return properties;
  }
}
