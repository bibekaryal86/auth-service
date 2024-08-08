# user-management-system

* Things to update:
 * Things to add:
     * `SwaggerConfig` if using SwaggerUI
         * See: `app-dependency-update` for example
         * Also, will have to update `SecurityConfig` to allow SwaggerUI
     * `SecurityConfig`

* Flyway
  * Run flyway command as `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
  * Clear database (DELETES EVERYTHING)
    * `./gradlew flywayMigrate -Dflyway.user=xxx -Dflyway.password=xxx -Dflyway.cleanDisabled=false`


enums for address_type
    MAILING
    SHIPPING
    BILLING

enums for user_statuses
    PENDING
    ACTIVE
    VALIDATE_INIT
    VALIDATE_ERROR
    RESET_INIT
    RESET_ERROR
    INACTIVE
    DELETED

enums for project_statuses
    PROPOSED
    APPROVED
    IN_PROGRESS
    ON_HOLD
    DELAYED
    REJECTED
    CANCELLED
    COMPLETED
    DELETED

enums for audit_users
    CREATE_USER
    UPDATE_USER
    DELETE_USER
    ASSIGN_ROLE
    UNASSIGN_ROLE
    ASSIGN_PROJECT
    UNASSIGN_PROJECT
    ASSIGN_PROJECT_ROLE
    UNASSIGN_PROJECT_ROLE
    ADD_ADDRESS
    UPDATE_ADDRESS
    DELETE_ADDRESS
    USER_LOGIN
    USER_LOGOUT
    USER_VALIDATE_INIT
    USER_VALIDATE_EXIT
    USER_VALIDATE_ERROR
    USER_RESET_INIT
    USER_RESET_MID
    USER_RESET_EXIT
    USER_RESET_ERROR

enums for audit_projects
    CREATE_PROJECT
    UPDATE_PROJECT
    DELETE_PROJECT

enums for audit_roles
    CREATE_ROLE
    UPDATE_ROLE
    DELETE_ROLE
    ASSIGN_PERMISSION
    UNASSIGN_PERMISSION

enums for audit_permissions
    CREATE_PERMISSION
    UPDATE_PERMISSION
    DELETE_PERMISSION
    

only superuser can modify roles and permissions table
project_admin - can delete
project_member - can update
project_guest - can view

guest - can view
member - can update
admin - can create and delete
super - can hard delete and restore

for entities in model.entities package, check what lombok annotations are actually needed
    maybe we don't need any, maybe we need just getters and all args
