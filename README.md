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
  * Unit and Integration tests
    * Parameterized tests
    * Integration test for AuthEnvServiceConnector without mocking WebClient
    * Add test for cacheable
  * Update Dockerfile to make it multi stage build
    * Run tests and build jar file
    * Then only copy the freshly created jar file
  * Implement ResponseCrudInfo and ResponsePageInfo
    * Also implement RequestMetadata
