package helper;

import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SECRET_KEY;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;
import static auth.service.app.util.ConstantUtils.ROLE_NAME_SUPERUSER;

import auth.service.app.model.client.EnvDetails;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.model.token.AuthTokenPermission;
import auth.service.app.model.token.AuthTokenPlatform;
import auth.service.app.model.token.AuthTokenProfile;
import auth.service.app.model.token.AuthTokenRole;
import auth.service.app.util.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
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

  public static List<ProfileEntity> getProfileEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-profile.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static ProfileEntity getNewProfileEntity() {
    ProfileEntity profileEntity = new ProfileEntity();
    profileEntity.setFirstName("New App Profile F");
    profileEntity.setLastName("New App Profile L");
    profileEntity.setEmail("new_app@profile.com");
    profileEntity.setPassword("some-password");
    profileEntity.setIsValidated(false);
    profileEntity.setLoginAttempts(0);

    StatusTypeEntity statusType = new StatusTypeEntity();
    statusType.setId(1L);
    statusType.setStatusName("ACTIVE");
    statusType.setStatusDesc("Status Type Desc One");
    return profileEntity;
  }

  public static List<RoleEntity> getRoleEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-role.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static RoleEntity getNewRoleEntity() {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setRoleName("New Role");
    roleEntity.setRoleDesc("New Role Entity for Test");
    return roleEntity;
  }

  public static List<PermissionEntity> getPermissionEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-permission.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PermissionEntity getNewPermissionEntity() {
    PermissionEntity permissionEntity = new PermissionEntity();
    permissionEntity.setPermissionName("New Permission");
    permissionEntity.setPermissionDesc("New Permission Entity for Test");
    return permissionEntity;
  }

  public static List<PlatformEntity> getPlatformEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-platform.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PlatformEntity getNewPlatformEntity() {
    PlatformEntity platformEntity = new PlatformEntity();
    platformEntity.setPlatformName("New Platform");
    platformEntity.setPlatformDesc("New Platform Entity for Test");
    return platformEntity;
  }

  public static List<AddressTypeEntity> getAddressTypeEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-address-type.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static AddressTypeEntity getNewAddressTypeEntity() {
    AddressTypeEntity addressTypeEntity = new AddressTypeEntity();
    addressTypeEntity.setTypeName("New Address Type");
    addressTypeEntity.setTypeDesc("New Address Type Entity for Test");
    return addressTypeEntity;
  }

  public static List<StatusTypeEntity> getStatusTypeEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-status-type.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static StatusTypeEntity getNewStatusTypeEntity() {
    StatusTypeEntity statusTypeEntity = new StatusTypeEntity();
    statusTypeEntity.setStatusName("New Status Type");
    statusTypeEntity.setStatusDesc("New Status Type Entity for Test");
    return statusTypeEntity;
  }

  public static AuthToken getAuthToken() {
    return AuthToken.builder()
        .platform(AuthTokenPlatform.builder().id(1L).platformName("Platform Name One").build())
        .profile(
            AuthTokenProfile.builder()
                .id(1L)
                .email("firstlast@one.com")
                .isDeleted(false)
                .isValidated(true)
                .statusId(1L)
                .build())
        .roles(List.of(AuthTokenRole.builder().id(1L).roleName("Role One").build()))
        .permissions(List.of(AuthTokenPermission.builder().id(1L).permissionName("Permission One").build()))
        .build();
  }

  public static ProfileDto getProfileDto() {
    ProfileEntity profileEntity = getProfileEntities().getFirst();
    RoleEntity roleEntity = getRoleEntities().getFirst();
    PermissionEntity permissionEntity = getPermissionEntities().getFirst();

    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(
        profileEntity, profileDto, "password", "addresses", "status", "platformRolesMap");

    PlatformDto platformDto =
        PlatformDto.builder()
            .id(-1L)
            .platformName(ROLE_NAME_SUPERUSER)
            .platformDesc(ROLE_NAME_SUPERUSER)
            .build();
    RoleDto roleDto =
        RoleDto.builder()
            .id(-1L)
            .roleName(ROLE_NAME_SUPERUSER)
            .roleDesc(ROLE_NAME_SUPERUSER)
            .build();
    PermissionDto permissionDto =
        PermissionDto.builder()
            .id(-1L)
            .permissionName(ROLE_NAME_SUPERUSER)
            .permissionDesc(ROLE_NAME_SUPERUSER)
            .build();
    Map<PlatformDto, List<PermissionDto>> platformPermissionsMap = new HashMap<>();
    platformPermissionsMap.put(platformDto, List.of(permissionDto));
    roleDto.setPlatformPermissionsMap(platformPermissionsMap);
    Map<PlatformDto, List<RoleDto>> platformRolesMap = new HashMap<>();
    platformRolesMap.put(platformDto, List.of(roleDto));
    profileDto.setPlatformRolesMap(platformRolesMap);

    return profileDto;
  }

  public static ProfileRequest getProfileRequest(String password) {
    ProfileEntity profileEntity = getNewProfileEntity();
    return new ProfileRequest(
        profileEntity.getFirstName(),
        profileEntity.getLastName(),
        profileEntity.getEmail(),
        profileEntity.getPhone(),
        password == null ? profileEntity.getPassword() : password,
        profileEntity.getStatusType().getId(),
        true,
        null);
  }

  public static ProfileDto getProfileDtoWithSuperUserRole(final ProfileDto profileDtoInput) {
    ProfileDto profileDtoOutput = new ProfileDto();
    BeanUtils.copyProperties(profileDtoInput, profileDtoOutput, "platformRolesMap");

    PlatformDto platformDto =
        PlatformDto.builder()
            .id(-1L)
            .platformName(ROLE_NAME_SUPERUSER)
            .platformDesc(ROLE_NAME_SUPERUSER)
            .build();
    RoleDto roleDto =
        RoleDto.builder()
            .id(-1L)
            .roleName(ROLE_NAME_SUPERUSER)
            .roleDesc(ROLE_NAME_SUPERUSER)
            .build();
    Map<PlatformDto, List<RoleDto>> platformRolesMap = new HashMap<>();
    platformRolesMap.put(platformDto, List.of(roleDto));

    profileDtoOutput.setPlatformRolesMap(platformRolesMap);
    return profileDtoOutput;
  }

  public static String getBearerAuthCredentialsForTest(
      final PlatformEntity platformEntity, final ProfileDto profileDto) {
    return JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 1000 * 60 * 15);
  }
}
