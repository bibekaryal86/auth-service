# user-management-system

* Things to update:
 * Things to add:
     * `SwaggerConfig` if using SwaggerUI
         * See: `app-dependency-update` for example
         * Also, will have to update `SecurityConfig` to allow SwaggerUI
     * `SecurityConfig`

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`



enums for actions in history
enums for statuses
aop for audit logging
only superuser can modify roles

team_admin - can delete
team_member - can update
team_guest - can view

project_admin - can delete
project_member - can update
project_guest - can view

guest - can view
member - can update
admin - can create and delete
super - can hard delete and restore
