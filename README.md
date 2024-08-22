# user-management-system

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


Remaining (thoughts)
    -> Test addresses
        -> How does updating it work
        -> How does deleting it work
    -> Audits
    -> Implement ResponseCrudInfo and ResponsePageInfo
        -> ResponseCrudInfo for all
        -> ResponsePageInfo and RequestMetadata for Users and Permissions
    -> Unit and Integration tests
