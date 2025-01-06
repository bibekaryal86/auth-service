# auth-service

A small utility for authentication and authorization, and users, roles and permissions management

## Local Development

* Navigate to project root
* `./gradlew bootrun`
* The bootrun process reads environment variables from gcp folder's `app-credentials.yaml` file
* There is an example `app-credentials_DUMMY.yaml` file provided, create `app-credentials.yaml` file and update values
* These environment variables are checked during application start, and if not present the application won't start
* During the build process, these variables are used in flyway and bootrun scripts

* Flyway
  * Run flyway command as `./gradlew flywayMigrate`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayClean -Dflyway.cleanDisabled=false`
  * Flyway migration is configured to not trigger automatically, it only validates
    * So migration command needs to be given manually
  * Flyway migration is controlled via github actions to main DB branch
  * There are 2 database instances created to support local development and production data.
    This uses free instance from `neon.tech` for database requirements. In neon tech it is possible
    to create multiple instances of database under one project, just like branching out code in a repo.
    So, for this service, there are two branches in `authsvc` project.
      * MAIN
        * This branch is used for production instance
        * When a pull request is merged to main branch, flyway migration is run in this branch
      * DEV
        * This branch is used for local/development instances
        * When a pull request is created, flyway migration is run in this branch to validate schema changes


* Remaining (thoughts)
  * Update this README.md for proper documentation
  * Implement ResponseCrudInfo and ResponsePageInfo
    * Also implement RequestMetadata


TODO:
* GitHub actions for PR and merge to main, check if variables work
* create a scheduled job to check for user login and set as inactive
* CrudInfo implementation in ResponseMetadata
* EntityDtoConvertUtilsTest
* RoleController -> add query param (default false) to include permissions
  * include tests
* ProfileController -> add query param (default false) to include roles
  * include tests

