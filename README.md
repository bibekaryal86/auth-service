# auth-service

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


redo audit service like in python
replace User with Profile (esp in exceptions and messages)


There are 2 database instances created to support local development and production data.
This uses free instance from `neon.tech` for database requirements. In neon tech it is possible
to create multiple instances of database under one project, just like branching out code in a repo.
So, for this service, there are two branches in `authsvc` project.
* MAIN
  * This branch is used for production instance
  * When a pull request is merged to main branch, flyway migration is run in this branch
* DEV
  * This branch is used for local/development instances
  * When a pull request is created, flyway migration is run in this branch to validate schema changes


TODO:
* GitHub actions for PR and merge to main, check if variables work
* create a scheduled job to check for user login and set as inactive
* create caching for platform only
* when login, if login is unsuccessful, update login_attempts
  * is login_attempts = 5, disable the profile
  * if login successful, set login_attempts to 0
* CrudInfo implementation in ResponseMetadata



remove builder from Dtos
move profileService.readPlatformProfileRole somewhere else
