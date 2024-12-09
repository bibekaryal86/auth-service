package helper;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_SUPERUSER;
import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SECRET_KEY;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;

import auth.service.app.model.client.EnvDetails;
import auth.service.app.model.dto.AppPermissionDto;
import auth.service.app.model.dto.AppRoleDto;
import auth.service.app.model.dto.AppUserDto;
import auth.service.app.model.dto.AppUserRequest;
import auth.service.app.model.entity.AppPermissionEntity;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppRolePermissionId;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppUserRoleEntity;
import auth.service.app.model.entity.AppUserRoleId;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsAppUserId;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.enums.StatusEnums;
import auth.service.app.model.token.AuthToken;
import auth.service.app.model.token.AuthTokenPermission;
import auth.service.app.model.token.AuthTokenRole;
import auth.service.app.model.token.AuthTokenUser;
import auth.service.app.util.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

public class TestData {

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
    String fixtureAsString = FixtureReader.readFixture("env-service_getPropertiesResponse.json");
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

  public static AppUserEntity getNewAppUserEntity() {
    AppUserEntity appUserEntity = new AppUserEntity();
    appUserEntity.setFirstName("New App User F");
    appUserEntity.setLastName("New App User L");
    appUserEntity.setEmail("new_app@user.com");
    appUserEntity.setPassword("some-password");
    appUserEntity.setStatus("ACTIVE");
    appUserEntity.setIsValidated(true);
    return appUserEntity;
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

  public static AppRoleEntity getNewAppRoleEntity() {
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    appRoleEntity.setName("New Role");
    appRoleEntity.setDescription("New Role Entity for Test");
    return appRoleEntity;
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

  public static AppPermissionEntity getNewAppPermissionEntity() {
    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setAppId("app-99");
    appPermissionEntity.setName("New Permission");
    appPermissionEntity.setDescription("New Permission Entity for Test");
    return appPermissionEntity;
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

  public static AppsEntity getNewAppsEntity() {
    AppsEntity appsEntity = new AppsEntity();
    appsEntity.setId("app-1001");
    appsEntity.setName("New App");
    appsEntity.setDescription("New App Entity for Test");
    return appsEntity;
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

  public static AppUserRoleEntity getNewAppUserRoleEntity(
      AppUserEntity appUserEntity, AppRoleEntity appRoleEntity) {
    AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
    appUserRoleEntity.setId(new AppUserRoleId(appUserEntity.getId(), appRoleEntity.getId()));
    appUserRoleEntity.setAppUser(appUserEntity);
    appUserRoleEntity.setAppRole(appRoleEntity);
    appUserRoleEntity.setAssignedDate(LocalDateTime.now());
    return appUserRoleEntity;
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

  public static AppRolePermissionEntity getNewAppRolePermissionEntity(
      AppRoleEntity appRoleEntity, AppPermissionEntity appPermissionEntity) {
    AppRolePermissionEntity appRolePermissionEntity = new AppRolePermissionEntity();
    appRolePermissionEntity.setAppRole(appRoleEntity);
    appRolePermissionEntity.setAppPermission(appPermissionEntity);
    appRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    appRolePermissionEntity.setId(
        new AppRolePermissionId(appRoleEntity.getId(), appPermissionEntity.getId()));
    return appRolePermissionEntity;
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

  public static AppsAppUserEntity getNewAppsAppUserEntity(
      AppsEntity appsEntity, AppUserEntity appUserEntity) {
    AppsAppUserEntity appsAppUserEntity = new AppsAppUserEntity();
    appsAppUserEntity.setId(new AppsAppUserId(appsEntity.getId(), appUserEntity.getId()));
    appsAppUserEntity.setApp(appsEntity);
    appsAppUserEntity.setAppUser(appUserEntity);
    appsAppUserEntity.setAssignedDate(LocalDateTime.now());
    return appsAppUserEntity;
  }

  public static AuthToken getAuthToken() {
    return AuthToken.builder()
        .appId("app-1")
        .user(
            AuthTokenUser.builder()
                .id(1)
                .email("firstlast@one.com")
                .isDeleted(false)
                .isValidated(true)
                .status(StatusEnums.AppUserStatus.ACTIVE.name())
                .build())
        .roles(List.of(AuthTokenRole.builder().id(1).name("Role One").build()))
        .permissions(List.of(AuthTokenPermission.builder().id(1).name("Permission One").build()))
        .build();
  }

  public static AppUserDto getAppUserDto() {
    AppUserEntity appUserEntity = getAppUserEntities().getFirst();
    AppRoleEntity appRoleEntity = getAppRoleEntities().getFirst();
    AppPermissionEntity appPermissionEntity = getAppPermissionEntities().getFirst();

    AppUserDto appUserDto = new AppUserDto();
    BeanUtils.copyProperties(appUserEntity, appUserDto, "password", "addresses");
    AppRoleDto appRoleDto = new AppRoleDto();
    BeanUtils.copyProperties(appRoleEntity, appRoleDto);
    AppPermissionDto appPermissionDto = new AppPermissionDto();
    BeanUtils.copyProperties(appPermissionEntity, appPermissionDto);
    appRoleDto.setPermissions(List.of(appPermissionDto));
    appUserDto.setRoles(List.of(appRoleDto));

    return appUserDto;
  }

  public static AppUserRequest getAppUserRequest(String password) {
    AppUserEntity appUserEntity = getNewAppUserEntity();
    return new AppUserRequest(
        appUserEntity.getFirstName(),
        appUserEntity.getLastName(),
        appUserEntity.getEmail(),
        appUserEntity.getPhone(),
        password == null ? appUserEntity.getPassword() : password,
        appUserEntity.getStatus(),
        true,
        null);
  }

  public static AppUserDto getAppUserDtoWithSuperUserRole(final AppUserDto appUserDtoInput) {
    AppUserDto appUserDtoOutput = new AppUserDto();
    BeanUtils.copyProperties(appUserDtoInput, appUserDtoOutput, "roles");

    AppRoleDto appRoleDto = new AppRoleDto(-1, ROLE_NAME_SUPERUSER, ROLE_NAME_SUPERUSER);
    appRoleDto.setPermissions(Collections.emptyList());
    List<AppRoleDto> appRoleDtos = new ArrayList<>(appUserDtoInput.getRoles());
    appRoleDtos.add(appRoleDto);

    appUserDtoOutput.setRoles(appRoleDtos);
    return appUserDtoOutput;
  }

  public static AppUserDto getAppUserDtoWithPermission(
      final String appId, final String permissionName, final AppUserDto appUserDtoInput) {
    AppUserDto appUserDtoOutput = new AppUserDto();
    BeanUtils.copyProperties(appUserDtoInput, appUserDtoOutput, "roles");

    List<AppRoleDto> appRoleDtos = new ArrayList<>(appUserDtoInput.getRoles());
    AppPermissionDto appPermissionDto =
        new AppPermissionDto(-1, appId, permissionName, permissionName);
    List<AppPermissionDto> appPermissionDtos =
        new ArrayList<>(appRoleDtos.getFirst().getPermissions());
    appPermissionDtos.add(appPermissionDto);
    appRoleDtos.getFirst().setPermissions(appPermissionDtos);

    appUserDtoOutput.setRoles(appRoleDtos);
    return appUserDtoOutput;
  }

  public static String getBearerAuthCredentialsForTest(
      final String appId, final AppUserDto appUserDto) {
    return JwtUtils.encodeAuthCredentials(appId, appUserDto, 1000 * 60 * 15);
  }
}
