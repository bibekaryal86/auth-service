# user-management-system

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


user, user_address, user_role, role_permission
    superuser - CRUD
    others - can only view their own
roles
    superuser - CRUD
    poweruser - R
permissions
    superuser - CRUD
    poweruser - R, for the app they belong to


ResponseCrudInfo and ResponsePageInfo need to be implemented
    ResponsePageInfo requires RequestMetadata implemented
        Do it at last

Remaining (thoughts)
    -> Security implementation and SecurityConfig
    -> No more 24 hours JWT, use refresh tokens
    -> UserAddress in User
    -> List of Permissions in Role
        -> Map<String, Permission> appName, appPermission
    -> List of Roles in User
    -> Audits
    -> When inserting/updating permissions, validate `app`
        -> cache `app` in authenv_service
        -> Do same for users
