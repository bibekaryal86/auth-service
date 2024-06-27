# user-management-system

* Things to update:
 * Things to add:
     * `DatasourceConfig`
         * See: `health-data-java` for JPA example
     * `SwaggerConfig` if using SwaggerUI
         * See: `app-dependency-update` for example
         * Also, will have to update `SecurityConfig` to allow SwaggerUI

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`



enums for actions in history
enums for statuses
aop for audit logging
only superuser can modify roles

team_admin
team_member
team_guest

project_admin
project_member
project_guest

super
admin
member
guest