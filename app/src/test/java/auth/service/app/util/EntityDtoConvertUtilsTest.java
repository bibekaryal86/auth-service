package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import helper.EntityDtoComparator;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EntityDtoConvertUtilsTest extends BaseTest {

  @Autowired private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<PermissionEntity> permissionEntities;
  private static List<RoleEntity> roleEntities;
  private static List<PlatformEntity> platformEntities;
  private static List<ProfileEntity> profileEntities;
  private static List<PlatformProfileRoleEntity> platformProfileRoleEntities;

  @BeforeAll
  static void setUp() {
    permissionEntities = TestData.getPermissionEntities();
    roleEntities = TestData.getRoleEntities();
    platformEntities = TestData.getPlatformEntities();
    profileEntities = TestData.getProfileEntities();
    platformProfileRoleEntities = TestData.getPlatformProfileRoleEntities();
  }

  // TODO in permissions check empty response metadata, null response crud info, null response page

  @Test
  void testGetResponseErrorPlatform() {
    ResponseEntity<ProfileResponse> response = entityDtoConvertUtils.getResponseErrorProfile(new JwtInvalidException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_nullEntity() {
    ResponseEntity<PlatformResponse> response =
            entityDtoConvertUtils.getResponseSinglePlatform(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_nonNullEntity() {
    PlatformEntity entity = platformEntities.getFirst();
    ResponseEntity<PlatformResponse> response = entityDtoConvertUtils.getResponseSinglePlatform(entity, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getPlatforms().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformDto dto = response.getBody().getPlatforms().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertFalse(dto.getProfileRoles().isEmpty());
  }

  @Test
  void testGetResponseMultiplePlatforms_emptyList() {
    ResponseEntity<PlatformResponse> response =
            entityDtoConvertUtils.getResponseMultiplePlatforms(
                    Collections.emptyList(), Boolean.TRUE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatforms_noProfiles() {
    ResponseEntity<PlatformResponse> response =
            entityDtoConvertUtils.getResponseMultiplePlatforms(platformEntities, Boolean.FALSE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, response.getBody().getPlatforms().size());

    for (PlatformDto platformDto : platformDtos) {
      assertEquals(0, platformDto.getProfileRoles().size());
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_withProfiles() {
    ResponseEntity<PlatformResponse> response = entityDtoConvertUtils.getResponseMultiplePlatforms(platformEntities, Boolean.TRUE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, platformDtos.size());

    PlatformDto platformDto1st = platformDtos.stream().filter(platformDto -> Objects.equals(platformDto.getId(), ID)).findFirst().orElse(null);
    assertNotNull(platformDto1st);
    assertEquals(1, platformDto1st.getProfileRoles().size());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getProfile().getId());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getRoles().size());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getRoles().getFirst().getId());

    PlatformDto platformDto4th = platformDtos.stream().filter(platformDto -> platformDto.getId() == 4L).findFirst().orElse(null);
    assertNotNull(platformDto4th);
    assertEquals(4L, platformDto4th.getProfileRoles().getFirst().getProfile().getId());
    assertEquals(3, platformDto4th.getProfileRoles().getFirst().getRoles().size());
    assertEquals(4, platformDto4th.getProfileRoles().getFirst().getRoles().getFirst().getId());
    assertEquals(6, platformDto4th.getProfileRoles().getFirst().getRoles().getLast().getId());
  }

  @Test
  void testGetResponseSingleProfile_nullEntity() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_nonNullEntity() {
    ProfileEntity entity = profileEntities.getFirst();
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(entity, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getProfiles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ProfileDto dto = response.getBody().getProfiles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertFalse(dto.getPlatformRoles().isEmpty());
  }

  @Test
  void testGetResponseMultipleProfiles_emptyList() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            Collections.emptyList(), Boolean.TRUE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleProfiles_noRoles() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(profileEntities, Boolean.FALSE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, response.getBody().getProfiles().size());

    for (ProfileDto profileDto : profileDtos) {
      assertEquals(0, profileDto.getPlatformRoles().size());
    }

    assertNotNull(profileDtos.getFirst().getProfileAddress());
  }

  @Test
  void testGetResponseMultipleProfiles_withRoles() {
    ResponseEntity<ProfileResponse> response = entityDtoConvertUtils.getResponseMultipleProfiles(profileEntities, Boolean.TRUE, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    assertNotNull(profileDtos.getFirst().getProfileAddress());

    ProfileDto profileDto1st = profileDtos.stream().filter(profileDto -> Objects.equals(profileDto.getId(), ID)).findFirst().orElse(null);
    assertNotNull(profileDto1st);
    assertEquals(1, profileDto1st.getPlatformRoles().size());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getPlatform().getId());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getRoles().size());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getRoles().getFirst().getId());

    ProfileDto profileDto4th = profileDtos.stream().filter(profileDto -> profileDto.getId() == 4L).findFirst().orElse(null);
    assertNotNull(profileDto4th);
    assertEquals(4L, profileDto4th.getPlatformRoles().getFirst().getPlatform().getId());
    assertEquals(3, profileDto4th.getPlatformRoles().getFirst().getRoles().size());
    assertEquals(4, profileDto4th.getPlatformRoles().getFirst().getRoles().getFirst().getId());
    assertEquals(6, profileDto4th.getPlatformRoles().getFirst().getRoles().getLast().getId());
  }

  @Test
  void testGetResponseErrorProfile() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseErrorProfile(new ProfileNotValidatedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorProfilePassword() {
    ResponseEntity<ProfilePasswordTokenResponse> response =
        entityDtoConvertUtils.getResponseErrorProfilePassword(new ProfileNotActiveException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertNull(response.getBody().getProfile());
    assertTrue(response.getBody().getAToken() == null && response.getBody().getRToken() == null);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorResponseMetadata() {
    ResponseEntity<ResponseMetadata> response =
        entityDtoConvertUtils.getResponseErrorResponseMetadata(new ProfileLockedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseStatusInfo().getErrMsg());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseValidateProfile_validated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = true;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=true", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateProfile_notValidated() {
    String redirectUrl = "https://example.com/redirect";
    boolean isValidated = false;
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, isValidated);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_validated=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseValidateProfile_emptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseValidateProfile("", true));
  }

  @Test
  void testGetResponseResetProfile_reset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = true;
    String email = "user@example.com";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetProfile(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(
        redirectUrl + "?is_reset=true&to_reset=" + email,
        response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetProfile_rotReset() {
    String redirectUrl = "https://example.com/redirect";
    boolean isReset = false;
    String email = "";
    ResponseEntity<Void> response =
        entityDtoConvertUtils.getResponseResetProfile(redirectUrl, isReset, email);

    assertNotNull(response);
    assertNotNull(response.getHeaders().getLocation());
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals(redirectUrl + "?is_reset=false", response.getHeaders().getLocation().toString());
  }

  @Test
  void testGetResponseResetProfile_emptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseResetProfile("", true, "some-email"));
  }

  @Test
  void testGetResponseErrorPlatformProfileRole() {
    ResponseEntity<PlatformProfileRoleResponse> response =
        entityDtoConvertUtils.getResponseErrorPlatformProfileRole(
            new RuntimeException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
