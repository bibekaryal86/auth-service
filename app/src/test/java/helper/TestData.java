package helper;

import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SECRET_KEY;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;

import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleDto;
import auth.service.app.model.dto.ProfileAddressRequest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  public static EnvDetailsResponse getEnvDetailsResponse() {
    String fixtureAsString = FixtureReader.readFixture("envsvc_response.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, EnvDetailsResponse.class);
    } catch (JsonProcessingException ex) {
      return new EnvDetailsResponse();
    }
  }

  public static List<PermissionEntity> getPermissionEntities() {
    String fixtureAsString = FixtureReader.readFixture("permission_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PermissionEntity getNewPermissionEntity() {
    PermissionEntity permissionEntity = new PermissionEntity();
    permissionEntity.setPermissionName("Permission 99");
    permissionEntity.setPermissionDesc("This is Permission Ninety Nine");
    return permissionEntity;
  }

  public static List<AuditPermissionEntity> getAuditPermissionEntities() {
    String fixtureAsString = FixtureReader.readFixture("audit_permission_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<RoleEntity> getRoleEntities() {
    String fixtureAsString = FixtureReader.readFixture("role_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static RoleEntity getNewRoleEntity() {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setRoleName("Role 99");
    roleEntity.setRoleDesc("This is Role Ninety Nine");
    return roleEntity;
  }

  public static List<AuditRoleEntity> getAuditRoleEntities() {
    String fixtureAsString = FixtureReader.readFixture("audit_role_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<PlatformEntity> getPlatformEntities() {
    String fixtureAsString = FixtureReader.readFixture("platform_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PlatformEntity getNewPlatformEntity() {
    PlatformEntity platformEntity = new PlatformEntity();
    platformEntity.setPlatformName("Platform 99");
    platformEntity.setPlatformDesc("This is Platform Ninety Nine");
    return platformEntity;
  }

  public static List<AuditPlatformEntity> getAuditPlatformEntities() {
    String fixtureAsString = FixtureReader.readFixture("audit_platform_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static List<ProfileAddressEntity> getProfileAddressEntities() {
    String fixtureAsString = FixtureReader.readFixture("profile_address_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static ProfileAddressEntity getNewProfileAddressEntity() {
    ProfileAddressEntity profileAddressEntity = new ProfileAddressEntity();
    profileAddressEntity.setStreet("99 Street");
    profileAddressEntity.setCity("CityNinetyNine");
    profileAddressEntity.setState("99S");
    profileAddressEntity.setCountry("USA");
    profileAddressEntity.setPostalCode("99999");
    profileAddressEntity.setProfile(null);
    return profileAddressEntity;
  }

  public static ProfileAddressRequest getProfileAddressRequest(
      Long id, Long profileId, String street, boolean isDeleteAddress) {
    ProfileAddressEntity profileAddressEntity = getNewProfileAddressEntity();
    return new ProfileAddressRequest(
        id,
        profileId,
        street,
        profileAddressEntity.getCity(),
        profileAddressEntity.getState(),
        profileAddressEntity.getCountry(),
        profileAddressEntity.getPostalCode(),
        isDeleteAddress);
  }

  public static List<ProfileEntity> getProfileEntities() {
    String fixtureAsString = FixtureReader.readFixture("profile_entities.json");
    try {
      List<ProfileEntity> profileEntities =
          ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});

      List<ProfileAddressEntity> profileAddressEntities = getProfileAddressEntities();

      Map<Long, ProfileAddressEntity> addressMap =
          profileAddressEntities.stream()
              .collect(
                  Collectors.toMap(
                      profileAddressEntity -> profileAddressEntity.getProfile().getId(),
                      profileAddressEntity -> profileAddressEntity));

      profileEntities.forEach(
          profile -> {
            ProfileAddressEntity profileAddress = addressMap.getOrDefault(profile.getId(), null);
            profile.setProfileAddress(profileAddress);
          });

      return profileEntities;
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static ProfileEntity getNewProfileEntity() {
    ProfileEntity profileEntity = new ProfileEntity();
    profileEntity.setFirstName("Profile 99");
    profileEntity.setLastName("99 Profile");
    profileEntity.setEmail("profile@ninetynine.com");
    profileEntity.setPassword("password99");
    profileEntity.setIsValidated(false);
    profileEntity.setLoginAttempts(0);
    profileEntity.setProfileAddress(null);
    return profileEntity;
  }

  public static List<AuditProfileEntity> getAuditProfileEntities() {
    String fixtureAsString = FixtureReader.readFixture("audit_profile_entities.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static ProfileRequest getProfileRequest(
      String firstName,
      String lastName,
      String email,
      String password,
      ProfileAddressRequest addressRequest) {
    return new ProfileRequest(firstName, lastName, email, null, password, true, addressRequest);
  }

  public static ProfileDto getProfileDto() {
    ProfileEntity profileEntity = getProfileEntities().getFirst();
    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    RoleEntity roleEntity = getRoleEntities().getFirst();

    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "profileAddress");

    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    List<PlatformProfileRoleDto> pprDtos =
        List.of(
            PlatformProfileRoleDto.builder()
                .platform(platformDto)
                .profile(profileDto)
                .role(roleDto)
                .build());
    profileDto.setPlatformProfileRoles(pprDtos);
    return profileDto;
  }

  public static ProfileDto getProfileDtoWithSuperUserRole(final ProfileDto profileDtoInput) {
    ProfileDto profileDtoOutput = new ProfileDto();
    BeanUtils.copyProperties(profileDtoInput, profileDtoOutput);

    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);

    RoleEntity roleEntity = getRoleEntities().getFirst();
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);
    roleDto.setRoleName(ConstantUtils.ROLE_NAME_SUPERUSER);

    List<PlatformProfileRoleDto> pprDtos =
        List.of(
            PlatformProfileRoleDto.builder()
                .platform(platformDto)
                .profile(profileDtoOutput)
                .role(roleDto)
                .build());
    profileDtoOutput.setPlatformProfileRoles(pprDtos);

    return profileDtoOutput;
  }

  public static PlatformProfileRoleEntity getPlatformProfileRoleEntity(
      PlatformEntity platformEntity,
      ProfileEntity profileEntity,
      RoleEntity roleEntity,
      LocalDateTime unassignedDate) {
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    PlatformProfileRoleId platformProfileRoleId =
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId());
    platformProfileRoleEntity.setId(platformProfileRoleId);
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now().minusDays(1L));
    platformProfileRoleEntity.setUnassignedDate(unassignedDate);
    return platformProfileRoleEntity;
  }

  public static PlatformRolePermissionEntity getPlatformRolePermissionEntity(
      PlatformEntity platformEntity,
      RoleEntity roleEntity,
      PermissionEntity permissionEntity,
      LocalDateTime unassignedDate) {
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    PlatformRolePermissionId platformRolePermissionId =
        new PlatformRolePermissionId(
            platformEntity.getId(), roleEntity.getId(), permissionEntity.getId());
    platformRolePermissionEntity.setId(platformRolePermissionId);
    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    platformRolePermissionEntity.setAssignedDate(LocalDateTime.now().minusDays(1L));
    platformRolePermissionEntity.setUnassignedDate(unassignedDate);
    return platformRolePermissionEntity;
  }

  public static List<PlatformProfileRoleEntity> getPlatformProfileRoleEntities() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities = new ArrayList<>();
    List<PlatformEntity> platformEntities = getPlatformEntities();
    List<ProfileEntity> profileEntities = getProfileEntities();
    List<RoleEntity> roleEntities = getRoleEntities();

    // match test data sql
    // 1, 1, 1
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(0), profileEntities.get(0), roleEntities.get(0), null));
    // 1, 2, 2
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(0), profileEntities.get(1), roleEntities.get(1), null));
    // 1, 3, 3
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(0),
            profileEntities.get(2),
            roleEntities.get(2),
            LocalDateTime.now()));
    // 5, 5, 5
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(4), profileEntities.get(4), roleEntities.get(4), null));
    // 5, 6, 6
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(4), profileEntities.get(5), roleEntities.get(5), null));
    // 5, 7, 7
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(4),
            profileEntities.get(6),
            roleEntities.get(6),
            LocalDateTime.now()));

    // 9, 7, 7
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(8), profileEntities.get(6), roleEntities.get(6), null));
    // 9, 8, 8
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(8), profileEntities.get(7), roleEntities.get(7), null));
    // 9, 9, 9
    platformProfileRoleEntities.add(
        getPlatformProfileRoleEntity(
            platformEntities.get(8),
            profileEntities.get(8),
            roleEntities.get(8),
            LocalDateTime.now()));

    return platformProfileRoleEntities;
  }

  public static List<PlatformRolePermissionEntity> getPlatformRolePermissionEntities() {
    List<PlatformRolePermissionEntity> platformRolePermissionEntities = new ArrayList<>();
    List<PlatformEntity> platformEntities = getPlatformEntities();
    List<RoleEntity> roleEntities = getRoleEntities();
    List<PermissionEntity> permissionEntities = getPermissionEntities();

    // match test data sql
    // 1, 1, 1
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(0), roleEntities.get(0), permissionEntities.get(0), null));
    // 1, 2, 2
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(0), roleEntities.get(1), permissionEntities.get(1), null));
    // 1, 3, 3
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(0),
            roleEntities.get(2),
            permissionEntities.get(2),
            LocalDateTime.now()));
    // 5, 5, 5
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(4), roleEntities.get(4), permissionEntities.get(4), null));
    // 5, 6, 6
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(4), roleEntities.get(5), permissionEntities.get(5), null));
    // 5, 7, 7
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(4),
            roleEntities.get(6),
            permissionEntities.get(6),
            LocalDateTime.now()));

    // 9, 7, 7
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(8), roleEntities.get(6), permissionEntities.get(6), null));
    // 9, 8, 8
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(8), roleEntities.get(7), permissionEntities.get(7), null));
    // 9, 9, 9
    platformRolePermissionEntities.add(
        getPlatformRolePermissionEntity(
            platformEntities.get(8),
            roleEntities.get(8),
            permissionEntities.get(8),
            LocalDateTime.now()));

    return platformRolePermissionEntities;
  }

  public static AuthToken getAuthToken() {
    return new AuthToken(
        new AuthToken.AuthTokenPlatform(1L, "Platform 1"),
        new AuthToken.AuthTokenProfile(1L, "profile@one.com"),
        List.of(new AuthToken.AuthTokenRole(1L, "Role 1")),
        List.of(new AuthToken.AuthTokenPermission(1L, "Permission 1")),
        Boolean.FALSE);
  }

  public static AuthToken getAuthTokenWithPermissions(List<String> permissionNames) {
    final AuthToken authToken = getAuthToken();
    final List<AuthToken.AuthTokenPermission> permissions =
        IntStream.range(0, permissionNames.size())
            .mapToObj(i -> new AuthToken.AuthTokenPermission(i, permissionNames.get(i)))
            .toList();
    authToken.getPermissions().addAll(permissions);
    return authToken;
  }

  public static String getBearerAuthCredentialsForTest() {
    return JwtUtils.encodeAuthCredentials(getAuthToken(), 1000 * 60 * 15);
  }

  public static TokenEntity getTokenEntity(
      int integer, PlatformEntity platformEntity, ProfileEntity profileEntity) {
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setRefreshToken("some-refresh-token" + integer);
    tokenEntity.setCsrfToken("some-csrf-token");
    tokenEntity.setIpAddress("some-ip-address" + integer);
    tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1L));
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    return tokenEntity;
  }
}
