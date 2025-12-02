# auth-service

A small utility for authentication and authorization with platforms, profiles, roles and permissions management

## Local Development
  * Navigate to project root
  * `./gradlew bootrun`
  * The bootrun process reads environment variables from gcp folder's `app-credentials.yaml` file
  * There is an example `app-credentials_DUMMY.yaml` file provided, create `app-credentials.yaml` file and update values
  * These environment variables are checked during application start, and if not present the application won't start
  * During the build process, these variables are used in flyway and bootrun scripts

## Flyway
  * Run flyway command as `./gradlew flywayMigrate`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
    * PRE-REQUISITES for flyway
      * Build needs to be run first
        * `./gradlew clean build -x test`
      * Flyway plugin does not work with gradle's configuration cache
        * `./gradlew -Dorg.gradle.configuration-cache=false flywayMigrate`
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayClean -Dflyway.cleanDisabled=false`
  * Flyway migration is configured to not trigger automatically, it only validates
    * This means that migration command needs to be given manually
  * Flyway migration is controlled via github actions to main DB branch
  * There are 2 database instances created to support local development and production data
      * `auth-service`
          * This instance is used for production instance
        * When a pull request is merged to main branch, flyway migration is run in this branch
      * `auth-service-sandbox`
        * This branch is used for local/development instances
        * When a pull request is created, flyway migration is run in this branch to validate schema changes

## TODO
  * Update this README.md for proper documentation


readPlatformProfileRoles
readPlatformRolePermissions
readTokenByRefreshTokenNoException
ConstantUtils
CommonUtils