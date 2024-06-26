# user-management-system

* Things to update:
 * Things to add:
     * `DatasourceConfig`
         * See: `health-data-java` for JPA example
     * `SwaggerConfig` if using SwaggerUI
         * See: `app-dependency-update` for example
         * Also, will have to update `SecurityConfig` to allow SwaggerUI

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.baselineOnMigrate=true`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * 



enums for actions in history
enums for statuses
aop for audit logging
