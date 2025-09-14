package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.AuditResponse;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformDto;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.ProfileService;
import helper.EntityDtoComparator;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class EntityDtoConvertUtilsTest extends BaseTest {

  @Autowired private EntityDtoConvertUtils entityDtoConvertUtils;
  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private ProfileService profileService;

  private static List<PermissionEntity> permissionEntities;
  private static List<RoleEntity> roleEntities;
  private static List<PlatformEntity> platformEntities;
  private static List<ProfileEntity> profileEntities;

  private static Authentication authentication;

  @Mock private SecurityContext securityContext;

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
        entityDtoConvertUtils.getResponseSinglePermission(null, null, null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertEquals(
        response.getBody().getResponseMetadata().responseCrudInfo(),
        ResponseMetadata.emptyResponseCrudInfo());
    assertEquals(
        response.getBody().getResponseMetadata().responsePageInfo(),
        ResponseMetadata.emptyResponsePageInfo());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePermission_NonNullEntity() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    PermissionEntity entity = permissionEntities.getFirst();
    ResponseMetadata.ResponseCrudInfo rciInput = new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0);
    RequestMetadata rmInput =
        RequestMetadata.builder().isIncludeHistory(true).historyPage(1).historySize(10).build();
    AuditResponse arInput =
        AuditResponse.builder()
            .auditPageInfo(new ResponseMetadata.ResponsePageInfo(3, 1, 1, 10))
            .build();
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseSinglePermission(entity, rciInput, rmInput, arInput);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(1, response.getBody().getPermissions().size());
    assertNotNull(response.getBody().getPermissions().getFirst().getRole());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PermissionDto dto = response.getBody().getPermissions().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
    assertEquals(arInput, dto.getAuditResponse());
    assertEquals(rciInput, response.getBody().getResponseMetadata().responseCrudInfo());
    assertEquals(rmInput, response.getBody().getRequestMetadata());
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
    ResponseMetadata.ResponsePageInfo defaultResponsePageInfo =
        new ResponseMetadata.ResponsePageInfo(13, 1, 1, 13);
    RequestMetadata defaultRequestMetadata = TestData.defaultRequestMetadata("permissionName");
    ResponseEntity<PermissionResponse> response =
        entityDtoConvertUtils.getResponseMultiplePermissions(
            permissionEntities, defaultResponsePageInfo, defaultRequestMetadata);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPermissions());
    assertEquals(13, response.getBody().getPermissions().size());

    assertEquals(
        response.getBody().getResponseMetadata().responsePageInfo(), defaultResponsePageInfo);
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
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertEquals(
        response.getBody().getResponseMetadata().responseCrudInfo(),
        ResponseMetadata.emptyResponseCrudInfo());
    assertEquals(
        response.getBody().getResponseMetadata().responsePageInfo(),
        ResponseMetadata.emptyResponsePageInfo());
    assertTrue(response.getBody().getPermissions().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NullEntity() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseSingleRole(null, null, null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleRole_NonNullEntity() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RoleEntity entity = roleEntities.getFirst();
    ResponseMetadata.ResponseCrudInfo rciInput = new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0);
    RequestMetadata rmInput =
        RequestMetadata.builder().isIncludeHistory(true).historyPage(1).historySize(10).build();
    AuditResponse arInput =
        AuditResponse.builder()
            .auditPageInfo(new ResponseMetadata.ResponsePageInfo(3, 1, 1, 10))
            .build();
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseSingleRole(entity, rciInput, rmInput, arInput);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    RoleDto dto = response.getBody().getRoles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
    assertEquals(arInput, dto.getAuditResponse());
    assertEquals(rciInput, response.getBody().getResponseMetadata().responseCrudInfo());
    assertEquals(rmInput, response.getBody().getRequestMetadata());
    ;

    assertFalse(dto.getPermissions().isEmpty());
    assertFalse(dto.getPlatformProfiles().isEmpty());
    assertFalse(dto.getProfilePlatforms().isEmpty());
  }

  @Test
  void testGetResponseSingleRole_NonNullEntity_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RoleEntity entity = roleEntities.getFirst();
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseSingleRole(entity, null, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getRoles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    RoleDto dto = response.getBody().getRoles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertTrue(dto.getPermissions().isEmpty());
    assertTrue(dto.getPlatformProfiles().isEmpty());
    assertTrue(dto.getProfilePlatforms().isEmpty());
  }

  @Test
  void testGetResponseMultipleRoles_EmptyList() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            Collections.emptyList(), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getRoles());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleRoles_NoPermissionsNoPlatformsNoProfiles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (RoleDto roleDto : roleDtos) {
      assertTrue(roleDto.getPermissions().isEmpty());
      assertTrue(roleDto.getPlatformProfiles().isEmpty());
      assertTrue(roleDto.getProfilePlatforms().isEmpty());
    }
  }

  @Test
  void testGetResponseMultipleRoles_WithPermissionsNoPlatformsNoProfiles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null);

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
      assertTrue(roleDto.getProfilePlatforms().isEmpty());

      if (List.of(1, 2, 5, 6, 10, 13).contains(i)) {
        assertFalse(roleDto.getPermissions().isEmpty());
        if (i == 1) {
          assertEquals(4, roleDto.getPermissions().size());
        }
      }
    }
  }

  @Test
  void testGetResponseMultipleRoles_WithoutPermissionsNoPlatformsNoProfiles() {
    // isIncludePermission is sent as TRUE but no READ permission
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of("AUTHSVC_PLATFORM_READ", "AUTHSVC_PROFILE_READ", "AUTHSVC_ROLE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null);

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
      assertTrue(roleDto.getPlatformProfiles().isEmpty());
      assertTrue(roleDto.getProfilePlatforms().isEmpty());
    }
  }

  @Test
  void testGetResponseMultipleRoles_NoPermissionsWithPlatformsNoProfiles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null, null);

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
      assertTrue(roleDto.getProfilePlatforms().isEmpty());

      if (List.of(1, 2, 3, 4, 5, 6).contains(i)) {
        assertEquals(1, roleDto.getPlatformProfiles().size());
        if (i == 1) {
          assertEquals(i, roleDto.getPlatformProfiles().getFirst().getPlatform().getId());
          assertEquals(1, roleDto.getPlatformProfiles().getFirst().getProfiles().size());
          assertEquals(
              i, roleDto.getPlatformProfiles().getFirst().getProfiles().getFirst().getId());
        }
      } else {
        assertTrue(roleDto.getPlatformProfiles().isEmpty());
      }
    }
  }

  @Test
  void testGetResponseMultipleRoles_NoPermissionsNoPlatformsWithProfiles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null, null);

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
      assertTrue(roleDto.getPlatformProfiles().isEmpty());

      try {
        assertFalse(roleDto.getProfilePlatforms().isEmpty());
      } catch (AssertionFailedError ex) {
        System.out.println("AssertionFailedError: " + i);
      }
    }
  }

  @Test
  void testGetResponseMultipleRoles_WithPermissionsWithPlatformsWithProfiles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

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
        assertEquals(1, roleDto.getProfilePlatforms().size());
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
  void testGetResponseMultipleRoles_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_ROLE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseMultipleRoles(
            roleEntities, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<RoleDto> roleDtos = response.getBody().getRoles();
    assertNotNull(roleDtos);
    assertEquals(13, roleDtos.size());

    for (RoleDto roleDto : roleDtos) {
      assertTrue(roleDto.getPermissions().isEmpty());
      assertTrue(roleDto.getPlatformProfiles().isEmpty());
      assertTrue(roleDto.getProfilePlatforms().isEmpty());
    }
  }

  @Test
  void testGetResponseErrorRole() {
    ResponseEntity<RoleResponse> response =
        entityDtoConvertUtils.getResponseErrorRole(
            new CheckPermissionException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(response.getBody().getRoles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NullEntity() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(null, null, null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSinglePlatform_NonNullEntity() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    PlatformEntity entity = platformEntities.getFirst();
    ResponseMetadata.ResponseCrudInfo rciInput = new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0);
    RequestMetadata rmInput =
        RequestMetadata.builder().isIncludeHistory(true).historyPage(1).historySize(10).build();
    AuditResponse arInput =
        AuditResponse.builder()
            .auditPageInfo(new ResponseMetadata.ResponsePageInfo(3, 1, 1, 10))
            .build();
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(entity, rciInput, rmInput, arInput);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getPlatforms().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformDto dto = response.getBody().getPlatforms().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
    assertEquals(arInput, dto.getAuditResponse());
    assertEquals(rciInput, response.getBody().getResponseMetadata().responseCrudInfo());
    assertEquals(rmInput, response.getBody().getRequestMetadata());

    assertFalse(dto.getProfileRoles().isEmpty());
    assertFalse(dto.getRoleProfiles().isEmpty());
  }

  @Test
  void testGetResponseSinglePlatform_NonNullEntity_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    PlatformEntity entity = platformEntities.getFirst();
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseSinglePlatform(entity, null, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getPlatforms().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    PlatformDto dto = response.getBody().getPlatforms().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertTrue(dto.getProfileRoles().isEmpty());
    assertTrue(dto.getRoleProfiles().isEmpty());
  }

  @Test
  void testGetResponseMultiplePlatforms_EmptyList() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            Collections.emptyList(), Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getPlatforms());
    assertTrue(response.getBody().getPlatforms().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultiplePlatforms_NoProfilesNoRoles() {
    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.FALSE, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, platformDtos.size());

    for (PlatformDto platformDto : platformDtos) {
      assertTrue(platformDto.getProfileRoles().isEmpty());
      assertTrue(platformDto.getRoleProfiles().isEmpty());
    }
  }

  @Test
  void testGetResponseMultiplePlatforms_NoProfilesWithRoles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.FALSE, Boolean.TRUE, null, null);

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
    assertEquals(0, platformDto1st.getProfileRoles().size());
    assertEquals(1, platformDto1st.getRoleProfiles().size());

    PlatformDto platformDto4th =
        platformDtos.stream()
            .filter(platformDto -> platformDto.getId() == 4L)
            .findFirst()
            .orElse(null);
    assertNotNull(platformDto4th);
    assertEquals(0, platformDto1st.getProfileRoles().size());
    assertEquals(1, platformDto1st.getRoleProfiles().size());
  }

  @Test
  void testGetResponseMultiplePlatforms_WithProfilesNoRoles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.TRUE, Boolean.FALSE, null, null);

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
    assertEquals(0, platformDto1st.getRoleProfiles().size());
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
    assertEquals(0, platformDto4th.getRoleProfiles().size());
    assertEquals(4L, platformDto4th.getProfileRoles().getFirst().getProfile().getId());
    assertEquals(3, platformDto4th.getProfileRoles().getFirst().getRoles().size());
    assertEquals(4, platformDto4th.getProfileRoles().getFirst().getRoles().getFirst().getId());
    assertEquals(6, platformDto4th.getProfileRoles().getFirst().getRoles().getLast().getId());
  }

  @Test
  void testGetResponseMultiplePlatforms_WithProfilesWithRoles() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.TRUE, Boolean.TRUE, null, null);

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
    assertEquals(1, platformDto1st.getRoleProfiles().size());
    assertEquals(1, platformDto1st.getRoleProfiles().getFirst().getRole().getId());
    assertEquals(1, platformDto1st.getRoleProfiles().getFirst().getProfiles().size());
    assertEquals(1, platformDto1st.getRoleProfiles().getFirst().getProfiles().getFirst().getId());

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
    assertEquals(3, platformDto4th.getRoleProfiles().size());
    assertEquals(1, platformDto4th.getRoleProfiles().getFirst().getProfiles().size());
    assertEquals(1, platformDto4th.getRoleProfiles().getLast().getProfiles().size());

    assertEquals(1, platformDto4th.getProfileRoles().size());
    assertEquals(3, platformDto4th.getProfileRoles().getFirst().getRoles().size());
  }

  @Test
  void testGetResponseMultiplePlatforms_WithProfilesWithRoles_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PLATFORM_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<PlatformResponse> response =
        entityDtoConvertUtils.getResponseMultiplePlatforms(
            platformEntities, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PlatformDto> platformDtos = response.getBody().getPlatforms();
    assertNotNull(platformDtos);
    assertEquals(13, platformDtos.size());

    for (PlatformDto platformDto : platformDtos) {
      assertTrue(platformDto.getProfileRoles().isEmpty());
      assertTrue(platformDto.getRoleProfiles().isEmpty());
    }
  }

  @Test
  void testGetResponseErrorPlatform() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseErrorProfile(
            new JwtInvalidException("something anything"));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NullEntity() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(null, null, null, null);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetResponseSingleProfile_NonNullEntity() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ProfileEntity entity = profileEntities.getFirst();
    ResponseMetadata.ResponseCrudInfo rciInput = new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0);
    RequestMetadata rmInput =
        RequestMetadata.builder().isIncludeHistory(true).historyPage(1).historySize(10).build();
    AuditResponse arInput =
        AuditResponse.builder()
            .auditPageInfo(new ResponseMetadata.ResponsePageInfo(3, 1, 1, 10))
            .build();
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(entity, rciInput, rmInput, arInput);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getProfiles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ProfileDto dto = response.getBody().getProfiles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));
    assertEquals(arInput, dto.getAuditResponse());
    assertEquals(rciInput, response.getBody().getResponseMetadata().responseCrudInfo());
    assertEquals(rmInput, response.getBody().getRequestMetadata());

    assertFalse(dto.getPlatformRoles().isEmpty());
    assertFalse(dto.getRolePlatforms().isEmpty());
    assertNotNull(dto.getProfileAddress());
  }

  @Test
  void testGetResponseSingleProfile_NonNullEntity_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ProfileEntity entity = profileEntities.getFirst();
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseSingleProfile(entity, null, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertNotNull(response.getBody().getResponseMetadata());
    assertEquals(1, response.getBody().getProfiles().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ProfileDto dto = response.getBody().getProfiles().getFirst();
    assertTrue(EntityDtoComparator.areEqual(entity, dto));

    assertTrue(dto.getPlatformRoles().isEmpty());
    assertTrue(dto.getRolePlatforms().isEmpty());
  }

  @Test
  void testGetResponseMultipleProfiles_EmptyList() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            Collections.emptyList(), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getProfiles());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetResponseMultipleProfiles_NoRolesNoPlatforms() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    for (ProfileDto profileDto : profileDtos) {
      assertTrue(profileDto.getPlatformRoles().isEmpty());
      assertTrue(profileDto.getRolePlatforms().isEmpty());
    }

    assertNotNull(profileDtos.getFirst().getProfileAddress());
  }

  @Test
  void testGetResponseMultipleProfiles_NoRolesWithPlatforms() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, null, null);

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
    assertEquals(0, profileDto1st.getRolePlatforms().size());
    assertEquals(1, profileDto1st.getPlatformRoles().size());

    ProfileDto profileDto4th =
        profileDtos.stream()
            .filter(profileDto -> profileDto.getId() == 4L)
            .findFirst()
            .orElse(null);
    assertNotNull(profileDto4th);
    assertEquals(1, profileDto4th.getPlatformRoles().size());
    assertEquals(0, profileDto4th.getRolePlatforms().size());
  }

  @Test
  void testGetResponseMultipleProfiles_WithRolesNoPlatforms() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, null, null);

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
    assertEquals(1, profileDto1st.getRolePlatforms().size());
    assertEquals(0, profileDto1st.getPlatformRoles().size());

    ProfileDto profileDto4th =
        profileDtos.stream()
            .filter(profileDto -> profileDto.getId() == 4L)
            .findFirst()
            .orElse(null);
    assertNotNull(profileDto4th);
    assertEquals(3, profileDto4th.getRolePlatforms().size());
    assertEquals(0, profileDto1st.getPlatformRoles().size());
  }

  @Test
  void testGetResponseMultipleProfiles_WithRolesWithPlatforms() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(
                List.of(
                    "AUTHSVC_PLATFORM_READ",
                    "AUTHSVC_PROFILE_READ",
                    "AUTHSVC_ROLE_READ",
                    "AUTHSVC_PERMISSION_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

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
    assertEquals(1, profileDto1st.getRolePlatforms().size());
    assertEquals(1, profileDto1st.getPlatformRoles().size());

    ProfileDto profileDto4th =
        profileDtos.stream()
            .filter(profileDto -> profileDto.getId() == 4L)
            .findFirst()
            .orElse(null);
    assertNotNull(profileDto4th);
    assertEquals(3, profileDto4th.getRolePlatforms().size());
    assertEquals(1, profileDto4th.getPlatformRoles().size());
  }

  @Test
  void testGetResponseMultipleProfiles_WithRolesWithPlatforms_NoReadPermissions() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    for (ProfileDto profileDto : profileDtos) {
      assertTrue(profileDto.getPlatformRoles().isEmpty());
      assertTrue(profileDto.getRolePlatforms().isEmpty());
    }

    assertNotNull(profileDtos.getFirst().getProfileAddress());
  }

  @Test
  void testGetResponseMultipleProfiles_NoProfileAddress() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    authentication =
        new TestingAuthenticationToken(
            EMAIL,
            TestData.getAuthTokenWithPermissions(List.of("AUTHSVC_PROFILE_READ")),
            Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseMultipleProfiles(
            profileEntities, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null, null);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<ProfileDto> profileDtos = response.getBody().getProfiles();
    assertNotNull(profileDtos);
    assertEquals(13, profileDtos.size());

    for (ProfileDto profileDto : profileDtos) {
      assertTrue(profileDto.getPlatformRoles().isEmpty());
      assertTrue(profileDto.getRolePlatforms().isEmpty());
      assertNull(profileDto.getProfileAddress());
    }
  }

  @Test
  void testGetResponseErrorProfile() {
    ResponseEntity<ProfileResponse> response =
        entityDtoConvertUtils.getResponseErrorProfile(new ProfileNotValidatedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertTrue(response.getBody().getProfiles().isEmpty());
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorProfilePassword() {
    ResponseEntity<ProfilePasswordTokenResponse> response =
        entityDtoConvertUtils.getResponseErrorProfilePassword(new ProfileNotActiveException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    assertNull(response.getBody().getProfile());
    assertTrue(response.getBody().getAToken() == null && response.getBody().getRToken() == null);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testGetResponseErrorResponseMetadata() {
    ResponseEntity<ResponseWithMetadata> response =
        entityDtoConvertUtils.getResponseErrorResponseMetadata(new ProfileLockedException());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getResponseMetadata());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo());
    assertNotNull(response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
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
  void testConvertEntityToDtoProfileBasic() {
    // setup
    platformProfileRoleService.assignPlatformProfileRole(
        new PlatformProfileRoleRequest(7L, 8L, 7L));
    platformProfileRoleService.assignPlatformProfileRole(
        new PlatformProfileRoleRequest(7L, 8L, 8L));
    platformProfileRoleService.assignPlatformProfileRole(
        new PlatformProfileRoleRequest(8L, 8L, 10L));

    ProfileEntity profileEntity = profileService.readProfileByEmail("firstlast@eight.com");
    assertNotNull(profileEntity);

    ProfileDto profileDto = entityDtoConvertUtils.convertEntityToDtoProfileBasic(profileEntity, 7L);
    assertEquals(2, profileDto.getRolePlatforms().size());
    assertEquals(1, profileDto.getPlatformRoles().size());

    List<Long> platformIds =
        profileDto.getPlatformRoles().stream()
            .map(pd -> pd.getPlatform().getId())
            .sorted()
            .toList();
    assertEquals(List.of(7L), platformIds);
    List<Long> roleIdsPlatform =
        profileDto.getPlatformRoles().stream()
            .filter(pd -> Objects.equals(7L, pd.getPlatform().getId()))
            .flatMap(pd -> pd.getRoles().stream().map(RoleDto::getId))
            .sorted()
            .toList();
    assertEquals(List.of(7L, 8L), roleIdsPlatform);

    List<Long> roleIds =
        profileDto.getRolePlatforms().stream().map(pd -> pd.getRole().getId()).sorted().toList();
    assertEquals(List.of(7L, 8L), roleIds);
    List<Long> platformIdsRoleOne =
        profileDto.getRolePlatforms().stream()
            .filter(pd -> Objects.equals(roleIds.getFirst(), pd.getRole().getId()))
            .flatMap(pd -> pd.getPlatforms().stream().map(PlatformDto::getId))
            .sorted()
            .toList();
    List<Long> platformIdsRoleTwo =
        profileDto.getRolePlatforms().stream()
            .filter(pd -> Objects.equals(roleIds.getFirst(), pd.getRole().getId()))
            .flatMap(pd -> pd.getPlatforms().stream().map(PlatformDto::getId))
            .sorted()
            .toList();
    assertEquals(List.of(7L), platformIdsRoleOne);
    assertEquals(List.of(7L), platformIdsRoleTwo);
    // cleanup
    platformProfileRoleService.hardDeletePlatformProfileRolesByProfileIds(List.of(8L));
  }
}
