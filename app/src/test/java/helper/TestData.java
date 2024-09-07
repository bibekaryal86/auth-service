package helper;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;
import static user.management.system.app.util.ConstantUtils.ENV_SECRET_KEY;
import static user.management.system.app.util.ConstantUtils.ENV_SERVER_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import user.management.system.app.model.client.EnvDetails;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppRolePermissionId;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsAppUserId;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.enums.StatusEnums;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.model.token.AuthTokenPermission;
import user.management.system.app.model.token.AuthTokenRole;
import user.management.system.app.model.token.AuthTokenUser;

public class TestData {

  public static final String TEST_APP_ID = "app-1";
  public static final String TEST_EMAIL = "firstlast@one.com";

  public static Map<String, String> getSystemEnvPropertyTestData() {
    return ENV_KEY_NAMES.stream()
        .collect(Collectors.toMap(someKeyName -> someKeyName, someKeyName -> someKeyName));
  }

  public static void setSystemEnvPropertyTestData() {
    ENV_KEY_NAMES.forEach(
        env -> {
          if (!ENV_SERVER_PORT.equals(env)) {
            System.setProperty(env, env);
          }
        });
    System.setProperty(ENV_SECRET_KEY, "test_secret_key_for_jwt_testing_purposes_only");
  }

  public static List<EnvDetails> getEnvDetailsResponse() {
    String fixtureAsString =
        FixtureReader.readFixture("authenv-service_getPropertiesResponse.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AppUserEntity> getAppUserEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-user.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AppRoleEntity> getAppRoleEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-role.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AppPermissionEntity> getAppPermissionEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-permission.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AppsEntity> getAppsEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-apps.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AppUserRoleEntity> getAppUserRoleEntities() {
    List<AppUserEntity> appUserEntities = getAppUserEntities();
    List<AppRoleEntity> appRoleEntities = getAppRoleEntities();
    List<AppUserRoleEntity> appUserRoleEntities = new ArrayList<>();

    for (int i = 0; i < appUserEntities.size(); i++) {
      AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
      appUserRoleEntity.setId(
          new AppUserRoleId(appUserEntities.get(i).getId(), appRoleEntities.get(i).getId()));
      appUserRoleEntity.setAppUser(appUserEntities.get(i));
      appUserRoleEntity.setAppRole(appRoleEntities.get(i));
      appUserRoleEntity.setAssignedDate(LocalDateTime.now());
      appUserRoleEntities.add(appUserRoleEntity);
    }

    return appUserRoleEntities;
  }

  public static List<AppRolePermissionEntity> getAppRolePermissionEntities() {
    List<AppRoleEntity> appRoleEntities = getAppRoleEntities();
    List<AppPermissionEntity> appPermissionEntities = getAppPermissionEntities();
    List<AppRolePermissionEntity> appRolePermissionEntities = new ArrayList<>();

    for (int i = 0; i < appRoleEntities.size(); i++) {
      AppRolePermissionEntity appRolePermissionEntity = new AppRolePermissionEntity();
      appRolePermissionEntity.setId(
          new AppRolePermissionId(
              appRoleEntities.get(i).getId(), appPermissionEntities.get(i).getId()));
      appRolePermissionEntity.setAppRole(appRoleEntities.get(i));
      appRolePermissionEntity.setAppPermission(appPermissionEntities.get(i));
      appRolePermissionEntity.setAssignedDate(LocalDateTime.now());
      appRolePermissionEntities.add(appRolePermissionEntity);
    }

    return appRolePermissionEntities;
  }

  public static List<AppsAppUserEntity> getAppsAppUserEntities() {
    List<AppsEntity> appsEntities = getAppsEntities();
    List<AppUserEntity> appUserEntities = getAppUserEntities();
    List<AppsAppUserEntity> appsAppUserEntities = new ArrayList<>();

    for (int i = 0; i < appsEntities.size(); i++) {
      AppsAppUserEntity appsAppUserEntity = new AppsAppUserEntity();
      appsAppUserEntity.setId(
          new AppsAppUserId(appsEntities.get(i).getId(), appUserEntities.get(i).getId()));
      appsAppUserEntity.setApp(appsEntities.get(i));
      appsAppUserEntity.setAppUser(appUserEntities.get(i));
      appsAppUserEntity.setAssignedDate(LocalDateTime.now());
      appsAppUserEntities.add(appsAppUserEntity);
    }

    return appsAppUserEntities;
  }

  public static AuthToken getAuthToken() {
    return AuthToken.builder()
        .appId(TEST_APP_ID)
        .user(
            AuthTokenUser.builder()
                .id(1)
                .email(TEST_EMAIL)
                .isDeleted(false)
                .isValidated(true)
                .status(StatusEnums.AppUserStatus.ACTIVE.name())
                .build())
        .roles(List.of(AuthTokenRole.builder().id(1).name("Role One").build()))
        .permissions(List.of(AuthTokenPermission.builder().id(1).name("Permission One").build()))
        .build();
  }
}
