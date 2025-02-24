package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import helper.EntityDtoComparator;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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

  @BeforeAll
  static void setUp() {
    permissionEntities = TestData.getPermissionEntities();
    roleEntities = TestData.getRoleEntities();
    platformEntities = TestData.getPlatformEntities();
    profileEntities = TestData.getProfileEntities();
  }

  @Test
  void testGetResponseSinglePermission_NullEntity() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePermission(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertEquals(
        response.getBody().getResponseMetadata().getResponseCrudInfo(),
        CommonUtils.emptyResponseCrudInfo());
    assertEquals(
        response.getBody().getResponseMetadata().getResponsePageInfo(),
        CommonUtils.emptyResponsePageInfo());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePermission_NonNullEntity() {
    PermissionEntity entity = permissionEntities.getFirst();
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePermission(entity, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(1, response.getBody().getPermissions().size());
    assertNotNull(response.getBody().getPermissions().getFirst().getRole());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PermissionDto dto = response.getBody().getPermissions().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
  }

  @Test
  void testGetResponseMultiplePermissions_EmptyList() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePermissions(Collections.emptyList(), null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePermissions_NonEmptyListWithResponsePageInfoRequestMetadata() {
    ResponsePageInfo defaultResponsePageInfo =
        ResponsePageInfo.builder().totalItems(13).totalPages(1).pageNumber(1).perPage(13).build();
    RequestMetadata defaultRequestMetadata = CommonUtils.defaultRequestMetadata("permissionName");
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePermissions(
            permissionEntities, defaultResponsePageInfo, defaultRequestMetadata);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(13, response.getBody().getPermissions().size());

    assertEquals(
        response.getBody().getResponseMetadata().getResponsePageInfo(), defaultResponsePageInfo);
    assertEquals(response.getBody().getRequestMetadata(), defaultRequestMetadata);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PermissionDto> permissionDtos = response.getBody().getPermissions();

    Map<PermissionEntity, PermissionDto> entityDtoMap =
        permissionEntities.stream()
            .filter(entity -> entity.getId() != null)
            .collect(
                Collectors.toMap(
                    entity -> entity,
                    entity ->
                        permissionDtos.stream()
                            .filter(dto -> Objects.equals(entity.getId(), dto.getId()))
                            .findFirst()
                            .orElse(new PermissionDto())));

    for (Map.Entry<PermissionEntity, PermissionDto> entry : entityDtoMap.entrySet()) {
      PermissionEntity entity = entry.getKey();
      PermissionDto dto = entry.getValue();
      assertTrue(EntityDtoComparator.areEqual(entity, dto));
    }
  }

  @Test
  void testGetResponseErrorPermission() {
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseErrorPermission(
            new ElementNotActiveException("something", "anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertEquals(
        response.getBody().getResponseMetadata().getResponseCrudInfo(),
        CommonUtils.emptyResponseCrudInfo());
    assertEquals(
        response.getBody().getResponseMetadata().getResponsePageInfo(),
        CommonUtils.emptyResponsePageInfo());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NullEntity() {
    ResponseEntity<RoleResponse> response = entityDtoConvertUtils.getResponseSingleRole(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NonNullEntity() {
    RoleEntity entity = roleEntities.getFirst();
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseSingleRole(entity, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    RoleDto dto = response.getBody().getRoles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertFalse(dto.getPermissions().isEmpty());
    assertFalse(dto.getPlatformProfiles().isEmpty());
  }

  @Test
  void testGetResponseMultipleRoles_EmptyList() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            Collections.emptyList(), Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatforms_NoPermissionsNoPlatforms() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.FALSE, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (RoleDto roleDto : roleDtos) {
      assertEquals(0, roleDto.getPermissions().size());
      assertEquals(0, roleDto.getPlatformProfiles().size());
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_WithPermissionsNoPlatforms() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (int i = 1; i <= roleDtos.size(); i++) {
      int finalI = i;
      RoleDto roleDto =
          roleDtos.stream().filter(rd -> rd.getId() == (long) finalI).findFirst().orElse(null);
      assertNotNull(roleDto);
      assertTrue(roleDto.getPlatformProfiles().isEmpty());

      if (List.of(1, 2, 5, 6, 10, 13).contains(i)) {
        assertFalse(roleDto.getPermissions().isEmpty());
        if (i == 1) {
          assertEquals(4, roleDto.getPermissions().size());
        }
      }
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_NoPermissionsWithPlatforms() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.FALSE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (int i = 1; i <= roleDtos.size(); i++) {
      int finalI = i;
      RoleDto roleDto =
          roleDtos.stream().filter(rd -> rd.getId() == (long) finalI).findFirst().orElse(null);
      assertNotNull(roleDto);
      assertTrue(roleDto.getPermissions().isEmpty());

      if (List.of(1, 2, 3, 4, 5, 6).contains(i)) {
        assertEquals(1, roleDto.getPlatformProfiles().size());
        if (i == 1 || i == 2 || i == 3) {
          assertEquals(i, roleDto.getPlatformProfiles().getFirst().getPlatform().getId());
          assertEquals(1, roleDto.getPlatformProfiles().getFirst().getProfiles().size());
          assertEquals(
              i, roleDto.getPlatformProfiles().getFirst().getProfiles().getFirst().getId());
        } else {
          assertEquals(4L, roleDto.getPlatformProfiles().getFirst().getPlatform().getId());
          assertEquals(1, roleDto.getPlatformProfiles().getFirst().getProfiles().size());
          assertEquals(
              4L, roleDto.getPlatformProfiles().getFirst().getProfiles().getFirst().getId());
        }
      } else {
        assertTrue(roleDto.getPlatformProfiles().isEmpty());
      }
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_WithPermissionsWithPlatforms() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (int i = 1; i <= roleDtos.size(); i++) {
      int finalI = i;
      RoleDto roleDto =
          roleDtos.stream().filter(rd -> rd.getId() == (long) finalI).findFirst().orElse(null);
      assertNotNull(roleDto);

      if (List.of(1, 2, 5, 6, 10, 13).contains(i)) {
        assertFalse(roleDto.getPermissions().isEmpty());
        if (i == 1) {
          assertEquals(4, roleDto.getPermissions().size());
        }
      }

      if (List.of(1, 2, 3, 4, 5, 6).contains(i)) {
        assertEquals(1, roleDto.getPlatformProfiles().size());
        if (i == 1 || i == 2 || i == 3) {
          assertEquals(i, roleDto.getPlatformProfiles().getFirst().getPlatform().getId());
          assertEquals(1, roleDto.getPlatformProfiles().getFirst().getProfiles().size());
          assertEquals(
              i, roleDto.getPlatformProfiles().getFirst().getProfiles().getFirst().getId());
        } else {
          assertEquals(4L, roleDto.getPlatformProfiles().getFirst().getPlatform().getId());
          assertEquals(1, roleDto.getPlatformProfiles().getFirst().getProfiles().size());
          assertEquals(
              4L, roleDto.getPlatformProfiles().getFirst().getProfiles().getFirst().getId());
        }
      } else {
        assertTrue(roleDto.getPlatformProfiles().isEmpty());
      }
    }
  }

  @Test
  void testGetResponseErrorRole() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseErrorRole(
            new CheckPermissionException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NullEntity() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NonNullEntity() {
    PlatformEntity entity = platformEntities.getFirst();
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(entity, null);

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
  void testGetResponseMultiplePlatforms_EmptyList() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            Collections.emptyList(), Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatforms_NoProfiles() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, platformDtos.size());

    for (PlatformDto platformDto : platformDtos) {
      assertEquals(0, platformDto.getProfileRoles().size());
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_WithProfiles() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, platformDtos.size());

    PlatformDto platformDto1st =
        platformDtos.stream()
            .filter(platformDto -> Objects.equals(platformDto.getId(), ID))
            .findFirst()
            .orElse(null);
    assertNotNull(platformDto1st);
    assertEquals(1, platformDto1st.getProfileRoles().size());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getProfile().getId());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getRoles().size());
    assertEquals(1, platformDto1st.getProfileRoles().getFirst().getRoles().getFirst().getId());

    PlatformDto platformDto4th =
        platformDtos.stream()
            .filter(platformDto -> platformDto.getId() == 4L)
            .findFirst()
            .orElse(null);
    assertNotNull(platformDto4th);
    assertEquals(4L, platformDto4th.getProfileRoles().getFirst().getProfile().getId());
    assertEquals(3, platformDto4th.getProfileRoles().getFirst().getRoles().size());
    assertEquals(4, platformDto4th.getProfileRoles().getFirst().getRoles().getFirst().getId());
    assertEquals(6, platformDto4th.getProfileRoles().getFirst().getRoles().getLast().getId());
  }

  @Test
  void testGetResponseErrorPlatform() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseErrorProfile(
            new JwtInvalidException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().getResponseStatusInfo().getErrMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NullEntity() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NonNullEntity() {
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
  void testGetResponseMultipleProfiles_EmptyList() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            Collections.emptyList(), Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleProfiles_NoRoles() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    for (ProfileDto profileDto : profileDtos) {
      assertEquals(0, profileDto.getPlatformRoles().size());
    }

    assertNotNull(profileDtos.getFirst().getProfileAddress());
  }

  @Test
  void testGetResponseMultipleProfiles_WithRoles() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    assertNotNull(profileDtos.getFirst().getProfileAddress());

    ProfileDto profileDto1st =
        profileDtos.stream()
            .filter(profileDto -> Objects.equals(profileDto.getId(), ID))
            .findFirst()
            .orElse(null);
    assertNotNull(profileDto1st);
    assertEquals(1, profileDto1st.getPlatformRoles().size());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getPlatform().getId());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getRoles().size());
    assertEquals(1, profileDto1st.getPlatformRoles().getFirst().getRoles().getFirst().getId());

    ProfileDto profileDto4th =
        profileDtos.stream()
            .filter(profileDto -> profileDto.getId() == 4L)
            .findFirst()
            .orElse(null);
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
  void testGetResponseValidateProfile_Validated() {
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
  void testGetResponseValidateProfile_NotValidated() {
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
  void testGetResponseValidateProfile_EmptyRedirectUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> entityDtoConvertUtils.getResponseValidateProfile("", true));
  }

  @Test
  void testGetResponseResetProfile_Reset() {
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
  void testGetResponseResetProfile_NotReset() {
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
  void testGetResponseResetProfile_EmptyRedirectUrl() {
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
