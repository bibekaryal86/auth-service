package helper;

import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SECRET_KEY;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;

import auth.service.app.model.client.EnvDetails;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import auth.service.app.model.entity.ProfileAddressEntity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

public class TestData {

  private static final List<EnvDetails> ENV_DETAILS_RESPONSE_FIXTURE;
  private static final List<StatusTypeEntity> STATUS_TYPE_ENTITIES_FIXTURE;
  private static final List<AddressTypeEntity> ADDRESS_TYPE_ENTITIES_FIXTURE;
  private static final List<PermissionEntity> PERMISSION_ENTITIES_FIXTURE;
  private static final List<RoleEntity> ROLE_ENTITIES_FIXTURE;
  private static final List<PlatformEntity> PLATFORM_ENTITIES_FIXTURE;
  private static final List<ProfileAddressEntity> PROFILE_ADDRESS_ENTITIES_FIXTURE;
  private static final List<ProfileEntity> PROFILE_ENTITIES_FIXTURE;

  static {
    ENV_DETAILS_RESPONSE_FIXTURE = getEnvDetailsResponseFixture();
    STATUS_TYPE_ENTITIES_FIXTURE = getStatusTypeEntitiesFixture();
    ADDRESS_TYPE_ENTITIES_FIXTURE = getAddressTypeEntitiesFixture();
    PERMISSION_ENTITIES_FIXTURE = getPermissionEntitiesFixture();
    ROLE_ENTITIES_FIXTURE = getRoleEntitiesFixture();
    PLATFORM_ENTITIES_FIXTURE = getPlatformEntitiesFixture();
    PROFILE_ADDRESS_ENTITIES_FIXTURE = getProfileAddressEntitiesFixture();
    PROFILE_ENTITIES_FIXTURE = getProfileEntitiesFixture();
  }

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

  private static List<EnvDetails> getEnvDetailsResponseFixture() {
    String fixtureAsString = FixtureReader.readFixture("env-service_getPropertiesResponse.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<EnvDetails> getEnvDetailsResponse() {
    return ENV_DETAILS_RESPONSE_FIXTURE;
  }

  private static List<StatusTypeEntity> getStatusTypeEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-status-type.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<StatusTypeEntity> getStatusTypeEntities() {
    return STATUS_TYPE_ENTITIES_FIXTURE;
  }

  public static StatusTypeEntity getNewStatusTypeEntity() {
    StatusTypeEntity statusTypeEntity = new StatusTypeEntity();
    statusTypeEntity.setStatusName("New Status Type");
    statusTypeEntity.setStatusDesc("New Status Type Entity for Test");
    return statusTypeEntity;
  }

  private static List<AddressTypeEntity> getAddressTypeEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-address-type.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<AddressTypeEntity> getAddressTypeEntities() {
    return ADDRESS_TYPE_ENTITIES_FIXTURE;
  }

  public static AddressTypeEntity getNewAddressTypeEntity() {
    AddressTypeEntity addressTypeEntity = new AddressTypeEntity();
    addressTypeEntity.setTypeName("New Address Type");
    addressTypeEntity.setTypeDesc("New Address Type Entity for Test");
    return addressTypeEntity;
  }

  private static List<PermissionEntity> getPermissionEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-permission.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<PermissionEntity> getPermissionEntities() {
    return PERMISSION_ENTITIES_FIXTURE;
  }

  public static PermissionEntity getNewPermissionEntity() {
    PermissionEntity permissionEntity = new PermissionEntity();
    permissionEntity.setPermissionName("New Permission");
    permissionEntity.setPermissionDesc("New Permission Entity for Test");
    return permissionEntity;
  }

  private static List<RoleEntity> getRoleEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-role.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<RoleEntity> getRoleEntities() {
    return ROLE_ENTITIES_FIXTURE;
  }

  public static RoleEntity getNewRoleEntity() {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setRoleName("New Role");
    roleEntity.setRoleDesc("New Role Entity for Test");
    return roleEntity;
  }

  private static List<PlatformEntity> getPlatformEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-platform.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<PlatformEntity> getPlatformEntities() {
    return PLATFORM_ENTITIES_FIXTURE;
  }

  public static PlatformEntity getNewPlatformEntity() {
    PlatformEntity platformEntity = new PlatformEntity();
    platformEntity.setPlatformName("New Platform");
    platformEntity.setPlatformDesc("New Platform Entity for Test");
    return platformEntity;
  }

  private static List<ProfileAddressEntity> getProfileAddressEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-profile-address.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<ProfileAddressEntity> getProfileAddressEntities() {
    return PROFILE_ADDRESS_ENTITIES_FIXTURE;
  }

  public static ProfileAddressEntity getNewProfileAddressEntity() {
    ProfileAddressEntity profileAddressEntity = new ProfileAddressEntity();
    profileAddressEntity.setStreet("New Street");
    profileAddressEntity.setCity("New City");
    profileAddressEntity.setState("NS");
    profileAddressEntity.setCountry("NC");
    profileAddressEntity.setPostalCode("13579");
    profileAddressEntity.setProfile(null);
    profileAddressEntity.setType(null);
    return profileAddressEntity;
  }

  private static List<ProfileEntity> getProfileEntitiesFixture() {
    String fixtureAsString = FixtureReader.readFixture("entities-profile.json");
    try {
      List<ProfileEntity> profileEntities =
          ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});

      List<ProfileAddressEntity> profileAddressEntities = getProfileAddressEntities();

      Map<Long, List<ProfileAddressEntity>> addressMap =
          profileAddressEntities.stream()
              .collect(
                  Collectors.groupingBy(
                      profileAddressEntity -> profileAddressEntity.getProfile().getId()));

      profileEntities.forEach(
          profile -> {
            List<ProfileAddressEntity> addresses =
                addressMap.getOrDefault(profile.getId(), Collections.emptyList());
            profile.setAddresses(addresses);
          });

      return profileEntities;
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<ProfileEntity> getProfileEntities() {
    return PROFILE_ENTITIES_FIXTURE;
  }

  public static ProfileEntity getNewProfileEntity() {
    ProfileEntity profileEntity = new ProfileEntity();
    profileEntity.setFirstName("New App Profile F");
    profileEntity.setLastName("New App Profile L");
    profileEntity.setEmail("new_app@profile.com");
    profileEntity.setPassword("some-password");
    profileEntity.setIsValidated(false);
    profileEntity.setLoginAttempts(0);
    profileEntity.setStatusType(null);
    profileEntity.setAddresses(null);
    return profileEntity;
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

  public static ProfileDto getProfileDto() {
    ProfileEntity profileEntity = getProfileEntities().getFirst();
    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    RoleEntity roleEntity = getRoleEntities().getFirst();
    PermissionEntity permissionEntity = getPermissionEntities().getFirst();

    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(
        profileEntity, profileDto, "password", "addresses", "status", "platformRolesMap");

    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);
    PermissionDto permissionDto = new PermissionDto();
    BeanUtils.copyProperties(permissionEntity, permissionDto);

    Map<PlatformDto, List<PermissionDto>> platformPermissionsMap = new HashMap<>();
    platformPermissionsMap.put(platformDto, List.of(permissionDto));
    roleDto.setPlatformPermissionsMap(platformPermissionsMap);
    Map<PlatformDto, List<RoleDto>> platformRolesMap = new HashMap<>();
    platformRolesMap.put(platformDto, List.of(roleDto));
    profileDto.setPlatformRolesMap(platformRolesMap);

    return profileDto;
  }

  public static ProfileDto getProfileDtoWithSuperUserRole(final ProfileDto profileDtoInput) {
    ProfileDto profileDtoOutput = new ProfileDto();
    BeanUtils.copyProperties(profileDtoInput, profileDtoOutput, "platformRolesMap");

    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);

    RoleEntity roleEntity = getRoleEntities().getFirst();
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    Map<PlatformDto, List<RoleDto>> platformRolesMap = new HashMap<>();
    platformRolesMap.put(platformDto, List.of(roleDto));

    profileDtoOutput.setPlatformRolesMap(platformRolesMap);
    return profileDtoOutput;
  }

  public static PlatformProfileRoleEntity getPlatformProfileRoleEntity(
      PlatformEntity platformEntity, ProfileEntity profileEntity, RoleEntity roleEntity) {
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    PlatformProfileRoleId platformProfileRoleId =
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId());
    platformProfileRoleEntity.setId(platformProfileRoleId);
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    return platformProfileRoleEntity;
  }

  public static List<PlatformProfileRoleEntity> getPlatformProfileRoleEntities() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities = new ArrayList<>();
    List<PlatformEntity> platformEntities = getPlatformEntities();
    List<ProfileEntity> profileEntities = getProfileEntities();
    List<RoleEntity> roleEntities = getRoleEntities();

    for (int i = 0; i < platformEntities.size(); i++) {
      platformProfileRoleEntities.add(
          getPlatformProfileRoleEntity(
              platformEntities.get(i), profileEntities.get(i), roleEntities.get(i)));
    }

    return platformProfileRoleEntities;
  }

  public static PlatformRolePermissionEntity getPlatformRolePermissionEntity(
      PlatformEntity platformEntity, RoleEntity roleEntity, PermissionEntity permissionEntity) {
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    PlatformRolePermissionId platformRolePermissionId =
        new PlatformRolePermissionId(
            platformEntity.getId(), roleEntity.getId(), permissionEntity.getId());
    platformRolePermissionEntity.setId(platformRolePermissionId);
    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    return platformRolePermissionEntity;
  }

  public static List<PlatformRolePermissionEntity> getPlatformRolePermissionEntities() {
    List<PlatformRolePermissionEntity> platformRolePermissionEntities = new ArrayList<>();
    List<PlatformEntity> platformEntities = getPlatformEntities();
    List<RoleEntity> roleEntities = getRoleEntities();
    List<PermissionEntity> permissionEntities = getPermissionEntities();

    for (int i = 0; i < platformEntities.size(); i++) {
      platformRolePermissionEntities.add(
          getPlatformRolePermissionEntity(
              platformEntities.get(i), roleEntities.get(i), permissionEntities.get(i)));
    }

    return platformRolePermissionEntities;
  }

  public static AuthToken getAuthToken() {
    return AuthToken.builder()
        .platform(AuthTokenPlatform.builder().id(1L).platformName("Auth Service").build())
        .profile(
            AuthTokenProfile.builder()
                .id(1L)
                .email("firstlast@one.com")
                .isDeleted(false)
                .isValidated(true)
                .statusId(1L)
                .build())
        .roles(List.of(AuthTokenRole.builder().id(3L).roleName("STANDARD").build()))
        .permissions(
            List.of(AuthTokenPermission.builder().id(2L).permissionName("PERMISSION_READ").build()))
        .build();
  }

  public static String getBearerAuthCredentialsForTest(
      final PlatformEntity platformEntity, final ProfileDto profileDto) {
    return JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 1000 * 60 * 15);
  }
}
