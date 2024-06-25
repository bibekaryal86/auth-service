# user-management-system

* Things to update:
    * GCP configurations, in `gcp` folder as necessary
        * esp `app-credentials.yaml` and `app-credentials_DUMMY.yaml`
    * `README.md` i.e. this file to add the program's readme
    * `.gitignore` if necessary
 * Things to add:
     * `DatasourceConfig` if using MONGO/JPA/JDBC
         * See: `pets-database-layer` for MongoDB example
             * https://github.com/bibekaryal86/pets-database-layer
         * See: `health-data-java` for JPA example
             * https://github.com/bibekaryal86/health-data-java
         * For JDBC, only need to set `Datasource` from above examples
     * `RestTemplateConfig` if using `RestTemplate`
         * See: `pets-service-layer` for example
             * https://github.com/bibekaryal86/pets-service-layer
     * `SwaggerConfig` if using SwaggerUI
         * See: `pets-service-layer` / `pets-database-layer` for example
             * https://github.com/bibekaryal86/pets-service-layer
             * https://github.com/bibekaryal86/pets-database-layer
         * Also, will have to update `SecurityConfig` to allow SwaggerUI
 * Things to remove:
     * If not using cache
         * Remove `CacheConfig` from config package
         * Remove `spring-boot-starter-cache` dependency from `build.gradle`
