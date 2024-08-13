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


user, user_address
    superuser - CRUD
    self - RU (user can view their account and update it)
ref_tables (address_type, permission, project_status, role, user_status)
    superuser - CRUD
    poweruser - R
project
    superuser - CRUD
    poweruser - CRU
        can view all projects
    standard - RU
        can only view projects they're assigned to
    guest - R
        can only view projects they're assigned to

ResponseCrudInfo and ResponsePageInfo need to be implemented
    ResponsePageInfo requires RequestMetadata implemented
        Do it at last

Remaining (thoughts)
    -> UserAddress in User
    -> List of Permissions in Role
        -> Map<String, Permission> appName, appPermission
    -> List of Roles in User
