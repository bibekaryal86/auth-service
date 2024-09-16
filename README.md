# user-management-system

* Database
  * postgres:
    * application.yml
* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`
  * Flyway migration is configured to not trigger automatically, it only validates
    * So migration command needs to be given manually


* Remaining (thoughts)
  * Update this README.md for proper documentation
  * Implement ResponseCrudInfo and ResponsePageInfo
    * Also implement RequestMetadata
