# user-management-system

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


user, user_address
    superuser - CRUD
    others - can only view/update their own
roles
    superuser - CRUD
    poweruser - R
permissions
    superuser - CRUD
    poweruser - R


ResponseCrudInfo and ResponsePageInfo need to be implemented
    ResponsePageInfo requires RequestMetadata implemented
        Do it at last, for users and permissions


Remaining (thoughts)
    -> Audits
    -> Unit and Integration tests
