# user-management-system

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


Remaining (thoughts)
    -> Test
      -> How does adding address work
      -> How does updating address work
        -> How does updating 1 out of 3 addresses work
      -> How does deleting address work
      -> How does clearing phone work (phone can be empty/null in DB)
    -> Implement ResponseCrudInfo and ResponsePageInfo
        -> ResponseCrudInfo for all
        -> ResponsePageInfo and RequestMetadata for Users and Permissions
    -> Unit and Integration tests
