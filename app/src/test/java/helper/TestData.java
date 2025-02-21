package helper;

import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SECRET_KEY;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;

import auth.service.app.model.client.EnvDetailsResponse;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.ProfileAddressRequest;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfileDtoPlatformRole;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.TokenEntity;
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

  public static EnvDetailsResponse getEnvDetailsResponse() {
    String fixtureAsString = FixtureReader.readFixture("env-service_getPropertiesResponse.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, EnvDetailsResponse.class);
    } catch (JsonProcessingException ex) {
      return new EnvDetailsResponse();
    }
  }

  public static List<PermissionEntity> getPermissionEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-permission.json");
    try {
      return ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PermissionEntity getNewPermissionEntity() {
    PermissionEntity permissionEntity = new PermissionEntity();
    permissionEntity.setRole(getRoleEntities().getFirst());
    permissionEntity.setPermissionName("PERMISSION-99");
    permissionEntity.setPermissionDesc("PERMISSION Ninety Nine");
    return permissionEntity;
  }

  public static List<RoleEntity> getRoleEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-role.json");
    try {
      return ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static RoleEntity getNewRoleEntity() {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setRoleName("ROLE-99");
    roleEntity.setRoleDesc("ROLE NINETY NINE");
    return roleEntity;
  }

  public static List<PlatformEntity> getPlatformEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-platform.json");
    try {
      return ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static PlatformEntity getNewPlatformEntity() {
    PlatformEntity platformEntity = new PlatformEntity();
    platformEntity.setPlatformName("PLATFORM-99");
    platformEntity.setPlatformDesc("PLATFORM NINETY NINE");
    return platformEntity;
  }

  public static List<ProfileAddressEntity> getProfileAddressEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-profile-address.json");
    try {
      return ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }

  public static ProfileAddressEntity getNewProfileAddressEntity() {
    ProfileAddressEntity profileAddressEntity = new ProfileAddressEntity();
    profileAddressEntity.setStreet("Street-99");
    profileAddressEntity.setCity("City-99");
    profileAddressEntity.setState("State-99");
    profileAddressEntity.setCountry("Country-99");
    profileAddressEntity.setPostalCode("Postal-99");
    profileAddressEntity.setProfile(null);
    return profileAddressEntity;
  }

  public static ProfileAddressRequest getProfileAddressRequest(Long profileId, String street) {
    ProfileAddressEntity profileAddressEntity = getNewProfileAddressEntity();
    return new ProfileAddressRequest(
        null,
        profileId,
        street,
        profileAddressEntity.getCity(),
        profileAddressEntity.getState(),
        profileAddressEntity.getCountry(),
        profileAddressEntity.getPostalCode());
  }

  public static List<ProfileEntity> getProfileEntities() {
    String fixtureAsString = FixtureReader.readFixture("entities-profile.json");
    try {
      List<ProfileEntity> profileEntities =
          ObjectMapperProvider.objectMapper().readValue(fixtureAsString, new TypeReference<>() {});

      List<ProfileAddressEntity> profileAddressEntities = getProfileAddressEntities();

      Map<Long, ProfileAddressEntity> addressMap =
              profileAddressEntities.stream()
                      .collect(
                              Collectors.toMap(
                                      profileAddressEntity -> profileAddressEntity.getProfile().getId(),
                                      profileAddressEntity -> profileAddressEntity
                              )
                      );

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
    profileEntity.setFirstName("First Ninety Nine");
    profileEntity.setLastName("Last Ninety Nine");
    profileEntity.setEmail("firstlast@ninetynine.com");
    profileEntity.setPassword("password-99");
    profileEntity.setIsValidated(false);
    profileEntity.setLoginAttempts(0);
    profileEntity.setProfileAddress(null);
    return profileEntity;
  }

  public static ProfileRequest getProfileRequest(
      String firstName, String lastName, String email, String password) {
    return new ProfileRequest(firstName, lastName, email, null, password, true, null);
  }

  public static ProfileDto getProfileDto() {
    ProfileEntity profileEntity = getProfileEntities().getFirst();
    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    RoleEntity roleEntity = getRoleEntities().getLast();

    ProfileDto profileDto = new ProfileDto();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "addresses", "status", "platformRoles");

    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    profileDto.setPlatformRoles(
        List.of(
            ProfileDtoPlatformRole.builder()
                .platform(platformDto)
                .roles(List.of(roleDto))
                .build()));

    return profileDto;
  }

  public static ProfileDto getProfileDtoWithSuperUserRole(final ProfileDto profileDtoInput) {
    ProfileDto profileDtoOutput = new ProfileDto();
    BeanUtils.copyProperties(profileDtoInput, profileDtoOutput, "platformRoles");

    PlatformEntity platformEntity = getPlatformEntities().getFirst();
    PlatformDto platformDto = new PlatformDto();
    BeanUtils.copyProperties(platformEntity, platformDto);

    RoleEntity roleEntity = getRoleEntities().getFirst();
    RoleDto roleDto = new RoleDto();
    BeanUtils.copyProperties(roleEntity, roleDto);

    profileDtoOutput.setPlatformRoles(
        List.of(
            ProfileDtoPlatformRole.builder()
                .platform(platformDto)
                .roles(List.of(roleDto))
                .build()));
    return profileDtoOutput;
  }

  public static ProfileDto getProfileDtoWithPermission(
      final String permissionName, final ProfileDto profileDtoInput) {
    ProfileDto profileDtoOutput = new ProfileDto();
    BeanUtils.copyProperties(profileDtoInput, profileDtoOutput, "platformRoles");

    PlatformDto platformDto = profileDtoInput.getPlatformRoles().getFirst().getPlatform();

    PermissionDto permissionDto =
        PermissionDto.builder()
            .id(-1L)
            .permissionName(permissionName)
            .permissionDesc(permissionName)
            .build();

    List<RoleDto> roleDtos =
            profileDtoInput.getPlatformRoles().stream()
                    .flatMap(profileDtoPlatformRole -> profileDtoPlatformRole.getRoles().stream())
                    .toList();
    roleDtos.forEach(roleDto -> roleDto.setPermissions(List.of(permissionDto)));
    profileDtoOutput.setPlatformRoles(
        List.of(ProfileDtoPlatformRole.builder().platform(platformDto).roles(roleDtos).build()));
    return profileDtoOutput;
  }

  public static PlatformProfileRoleEntity getPlatformProfileRoleEntity(PlatformEntity platformEntity, ProfileEntity profileEntity, RoleEntity roleEntity) {
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    PlatformProfileRoleId platformProfileRoleId = new PlatformProfileRoleId(platformEntity.getId(), profileEntity.getId(), roleEntity.getId());
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

    // 1, 1, 1
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(0), profileEntities.get(0), roleEntities.get(0)));
    // 2, 2, 2
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(1), profileEntities.get(1), roleEntities.get(1)));
    // 3, 3, 3
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(2), profileEntities.get(2), roleEntities.get(2)));
    // 4, 4, 4
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(3), profileEntities.get(3), roleEntities.get(3)));
    // 4, 4, 5
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(3), profileEntities.get(3), roleEntities.get(4)));
    // 4, 4, 6
    platformProfileRoleEntities.add(getPlatformProfileRoleEntity(platformEntities.get(3), profileEntities.get(3), roleEntities.get(5)));

    return platformProfileRoleEntities;
  }

  public static AuthToken getAuthToken() {
    return AuthToken.builder()
        .platform(AuthTokenPlatform.builder().id(1L).platformName("PLATFORM-1").build())
        .profile(AuthTokenProfile.builder().id(1L).email("firstlast@one.com").build())
        .roles(List.of(AuthTokenRole.builder().id(1L).roleName("ROLE-1").build()))
        .permissions(
            List.of(AuthTokenPermission.builder().id(2L).permissionName("PERMISSION-1").build()))
        .build();
  }

  public static String getBearerAuthCredentialsForTest(
      final PlatformEntity platformEntity, final ProfileDto profileDto) {
    return JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 1000 * 60 * 15);
  }

  public static TokenEntity getTokenEntity(
      int integer, PlatformEntity platformEntity, ProfileEntity profileEntity) {
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setAccessToken("some-access-token" + integer);
    tokenEntity.setRefreshToken("some-refresh-token" + integer);
    tokenEntity.setIpAddress("some-ip-address" + integer);
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    return tokenEntity;
  }
}
