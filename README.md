# user-management-system

* Database
  * Create two databases
    * postgres: for production data
      * application.yml
    * postgres_test: for running tests
      * application-test.yml
* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


* Remaining (thoughts)
  * Tests
    * In Repository Tests, add tests for unique constraints
      * For eg: phone number unique, join tables, etc
  * Update Dockerfile to make it multi stage build
    * Run tests and build jar file
    * Then only copy the freshly created jar file
  * Implement ResponseCrudInfo and ResponsePageInfo
    * Also implement RequestMetadata
