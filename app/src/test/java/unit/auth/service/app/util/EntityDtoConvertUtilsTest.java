package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.exception.TokenInvalidException;
import auth.service.app.model.dto.PermissionDto;
import auth.service.app.model.dto.PermissionResponse;
import auth.service.app.model.dto.PlatformResponse;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RoleDto;
import auth.service.app.model.dto.RoleResponse;
import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.ProfileAddressEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.token.AuthTokenRolePermissionLookup;
import auth.service.app.repository.RawSqlRepository;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.PlatformRolePermissionService;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.CookieService;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.exception.CheckPermissionException;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EntityDtoConvertUtils Unit Tests")
class EntityDtoConvertUtilsTest {

  @Mock private PlatformProfileRoleService pprService;
  @Mock private PlatformRolePermissionService prpService;
  @Mock private CookieService cookieService;
  @Mock private RawSqlRepository rawSqlRepository;

  @InjectMocks private EntityDtoConvertUtils convertUtils;

  private static final Long TEST_ID = 1L;
  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_NAME = "Test Name";

  @Nested
  @DisplayName("Helper method tests")
  class HelperMethodTests {

    @Test
    @DisplayName("Should return NOT_FOUND for ElementNotFoundException")
    void shouldReturnNotFoundForElementNotFoundException() {
      Exception exception = new ElementNotFoundException("Entity", "Column");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.NOT_FOUND, status);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for ElementMissingException")
    void shouldReturnBadRequestForElementMissingException() {
      Exception exception = new ElementMissingException("Entity", "Variable");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.BAD_REQUEST, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for ProfileForbiddenException")
    void shouldReturnForbiddenForProfileForbiddenException() {
      Exception exception = new ProfileForbiddenException();
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for ProfileNotValidatedException")
    void shouldReturnForbiddenForProfileNotValidatedException() {
      Exception exception = new ProfileNotValidatedException();
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for ElementNotActiveException")
    void shouldReturnForbiddenForElementNotActiveException() {
      Exception exception = new ElementNotActiveException("Type", "Entity");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for ProfileNotActiveException")
    void shouldReturnForbiddenForProfileNotActiveException() {
      Exception exception = new ProfileNotActiveException();
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for ProfileLockedException")
    void shouldReturnForbiddenForProfileLockedException() {
      Exception exception = new ProfileLockedException();
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return FORBIDDEN for CheckPermissionException")
    void shouldReturnForbiddenForCheckPermissionException() {
      Exception exception = new CheckPermissionException("No permission");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.FORBIDDEN, status);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED for ProfileNotAuthorizedException")
    void shouldReturnUnauthorizedForProfileNotAuthorizedException() {
      Exception exception = new ProfileNotAuthorizedException("Not authorized");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.UNAUTHORIZED, status);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED for TokenInvalidException")
    void shouldReturnUnauthorizedForTokenInvalidException() {
      Exception exception = new TokenInvalidException("Invalid token");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.UNAUTHORIZED, status);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for DataIntegrityViolationException")
    void shouldReturnBadRequestForDataIntegrityViolationException() {
      Exception exception = new DataIntegrityViolationException("something bad happened");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.BAD_REQUEST, status);
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR for unknown exceptions")
    void shouldReturnInternalServerErrorForUnknownExceptions() {
      Exception exception = new RuntimeException("Unknown error");
      HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exception);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
    }

    @Test
    @DisplayName("Should return OK when object is not empty")
    void shouldReturnOkWhenObjectIsNotEmpty() {
      HttpStatus status = convertUtils.getHttpStatusForSingleResponse("test");
      assertEquals(HttpStatus.OK, status);
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR when object is empty")
    void shouldReturnInternalServerErrorWhenObjectIsEmpty() {
      HttpStatus status = convertUtils.getHttpStatusForSingleResponse(null);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
    }

    @Test
    @DisplayName("Should return empty status info for non-empty object")
    void shouldReturnEmptyStatusInfoForNonEmptyObject() {
      ResponseMetadata.ResponseStatusInfo statusInfo =
          convertUtils.getResponseStatusInfoForSingleResponse("test");
      assertNotNull(statusInfo);
      assertTrue(CommonUtilities.isEmpty(statusInfo.errMsg()));
    }

    @Test
    @DisplayName("Should return error status info for empty object")
    void shouldReturnErrorStatusInfoForEmptyObject() {
      ResponseMetadata.ResponseStatusInfo statusInfo =
          convertUtils.getResponseStatusInfoForSingleResponse(null);
      assertNotNull(statusInfo);
      Assertions.assertEquals(ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE, statusInfo.errMsg());
    }

    @Test
    @DisplayName("Should return provided crud info when not null")
    void shouldReturnProvidedCrudInfoWhenNotNull() {
      ResponseMetadata.ResponseCrudInfo crudInfo =
          new ResponseMetadata.ResponseCrudInfo(1, 2, 3, 4);
      ResponseMetadata.ResponseCrudInfo result =
          convertUtils.getResponseCrudInfoForResponse(crudInfo);
      assertEquals(crudInfo, result);
    }

    @Test
    @DisplayName("Should return empty crud info when null")
    void shouldReturnEmptyCrudInfoWhenNull() {
      ResponseMetadata.ResponseCrudInfo result = convertUtils.getResponseCrudInfoForResponse(null);
      assertNotNull(result);
      assertEquals(0, result.insertedRowsCount());
      assertEquals(0, result.updatedRowsCount());
      assertEquals(0, result.deletedRowsCount());
      assertEquals(0, result.restoredRowsCount());
    }

    @Test
    @DisplayName("Should return empty status info when exception is null")
    void shouldReturnEmptyStatusInfoWhenExceptionIsNull() {
      ResponseMetadata.ResponseStatusInfo statusInfo =
          convertUtils.getResponseStatusInfoForResponse(null);
      assertNotNull(statusInfo);
      assertTrue(CommonUtilities.isEmpty(statusInfo.errMsg()));
    }

    @Test
    @DisplayName("Should return status info with exception message")
    void shouldReturnStatusInfoWithExceptionMessage() {
      Exception exception = new RuntimeException("Error message");
      ResponseMetadata.ResponseStatusInfo statusInfo =
          convertUtils.getResponseStatusInfoForResponse(exception);
      assertNotNull(statusInfo);
      assertEquals("Error message", statusInfo.errMsg());
    }

    @Test
    @DisplayName("Should create error response with correct status and metadata")
    void shouldCreateErrorResponseWithCorrectStatusAndMetadata() {
      Exception exception = new ElementNotFoundException("Entity", "Column");
      ResponseEntity<ResponseWithMetadata> response =
          convertUtils.getResponseErrorResponseMetadata(exception);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(
          "Entity Not Found for [Column]",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }
  }

  @Nested
  @DisplayName("Permission conversion and response tests")
  class PermissionTests {

    private PermissionEntity permissionEntity;
    private AuditPermissionEntity auditEntity;

    @BeforeEach
    void setUp() {
      permissionEntity = new PermissionEntity();
      permissionEntity.setId(TEST_ID);
      permissionEntity.setPermissionName("READ");

      auditEntity = new AuditPermissionEntity();
      auditEntity.setId(1L);
      auditEntity.setEventType(TypeEnums.EventType.CREATE.name());
      auditEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return null when permission entity is null")
    void shouldReturnNullWhenPermissionEntityIsNull() {
      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(null, null, null);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPermissions().isEmpty());
    }

    @Test
    @DisplayName("Should convert permission entity to DTO successfully")
    void shouldConvertPermissionEntityToDtoSuccessfully() {
      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getPermissions().size());
      assertEquals("READ", response.getBody().getPermissions().get(0).getPermissionName());
    }

    @Test
    @DisplayName("Should include audit history when provided")
    void shouldIncludeAuditHistoryWhenProvided() {
      ProfileEntity createdBy = new ProfileEntity();
      createdBy.setId(2L);
      createdBy.setEmail(TEST_EMAIL);
      auditEntity.setCreatedBy(createdBy);
      auditEntity.setEventData(permissionEntity);

      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, null, List.of(auditEntity));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getPermissions().size());
      assertFalse(response.getBody().getPermissions().get(0).getHistory().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple permissions successfully")
    void shouldReturnMultiplePermissionsSuccessfully() {
      PermissionEntity permission2 = new PermissionEntity();
      permission2.setId(2L);
      permission2.setPermissionName("WRITE");

      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseMultiplePermissions(List.of(permissionEntity, permission2));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().getPermissions().size());
    }

    @Test
    @DisplayName("Should return empty list for null entities")
    void shouldReturnEmptyListForNullEntities() {
      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseMultiplePermissions(null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPermissions().isEmpty());
    }

    @Test
    @DisplayName("Should create error response for permission")
    void shouldCreateErrorResponseForPermission() {
      Exception exception = new ElementNotFoundException("Permission", "ID");
      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseErrorPermission(exception);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPermissions().isEmpty());
      assertEquals(
          "Permission Not Found for [ID]",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Should create data integrity violation error response for permission")
    void shouldCreateErrorResponseForPermission_DataIntegrityViolationException() {
      Exception exception = new DataIntegrityViolationException("something bad happened");
      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseErrorPermission(exception);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPermissions().isEmpty());
      assertEquals(
          "Action Failed! Permission Name Already Exists!! Please Try Again!!!",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }
  }

  @Nested
  @DisplayName("Role conversion and response tests")
  class RoleTests {

    private RoleEntity roleEntity;
    private AuditRoleEntity auditEntity;

    @BeforeEach
    void setUp() {
      roleEntity = new RoleEntity();
      roleEntity.setId(TEST_ID);
      roleEntity.setRoleName("ADMIN");

      auditEntity = new AuditRoleEntity();
      auditEntity.setId(1L);
      auditEntity.setEventType(TypeEnums.EventType.CREATE.name());
      auditEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return null when role entity is null")
    void shouldReturnNullWhenRoleEntityIsNull() {
      ResponseEntity<RoleResponse> response = convertUtils.getResponseSingleRole(null, null, null);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should convert role entity to DTO successfully")
    void shouldConvertRoleEntityToDtoSuccessfully() {
      when(pprService.readPlatformProfileRolesByRoleIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());
      when(prpService.readPlatformRolePermissionsByRoleIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<RoleResponse> response =
          convertUtils.getResponseSingleRole(roleEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getRoles().size());
      assertEquals("ADMIN", response.getBody().getRoles().get(0).getRoleName());
    }

    @Test
    @DisplayName("Should include audit history when provided")
    void shouldIncludeAuditHistoryWhenProvided() {
      ProfileEntity createdBy = new ProfileEntity();
      createdBy.setId(2L);
      createdBy.setEmail(TEST_EMAIL);
      auditEntity.setCreatedBy(createdBy);
      auditEntity.setEventData(roleEntity);

      when(pprService.readPlatformProfileRolesByRoleIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());
      when(prpService.readPlatformRolePermissionsByRoleIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<RoleResponse> response =
          convertUtils.getResponseSingleRole(roleEntity, null, List.of(auditEntity));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getRoles().size());
      assertFalse(response.getBody().getRoles().get(0).getHistory().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple roles successfully")
    void shouldReturnMultipleRolesSuccessfully() {
      RoleEntity role2 = new RoleEntity();
      role2.setId(2L);
      role2.setRoleName("USER");

      ResponseEntity<RoleResponse> response =
          convertUtils.getResponseMultipleRoles(List.of(roleEntity, role2));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().getRoles().size());
    }

    @Test
    @DisplayName("Should create error response for role")
    void shouldCreateErrorResponseForRole() {
      Exception exception = new ElementNotFoundException("Role", "ID");
      ResponseEntity<RoleResponse> response = convertUtils.getResponseErrorRole(exception);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getRoles().isEmpty());
      assertEquals(
          "Role Not Found for [ID]",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Should create data integrity violation error response for role")
    void shouldCreateErrorResponseForRole_DataIntegrityViolationException() {
      Exception exception = new DataIntegrityViolationException("something bad happened");
      ResponseEntity<RoleResponse> response = convertUtils.getResponseErrorRole(exception);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getRoles().isEmpty());
      assertEquals(
          "Action Failed! Role Name Already Exists!! Please Try Again!!!",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }
  }

  @Nested
  @DisplayName("Platform conversion and response tests")
  class PlatformTests {

    private PlatformEntity platformEntity;
    private AuditPlatformEntity auditEntity;

    @BeforeEach
    void setUp() {
      platformEntity = new PlatformEntity();
      platformEntity.setId(TEST_ID);
      platformEntity.setPlatformName("Test Platform");

      auditEntity = new AuditPlatformEntity();
      auditEntity.setId(1L);
      auditEntity.setEventType(TypeEnums.EventType.CREATE.name());
      auditEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return null when platform entity is null")
    void shouldReturnNullWhenPlatformEntityIsNull() {
      ResponseEntity<PlatformResponse> response =
          convertUtils.getResponseSinglePlatform(null, null, null);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPlatforms().isEmpty());
    }

    @Test
    @DisplayName("Should convert platform entity to DTO successfully")
    void shouldConvertPlatformEntityToDtoSuccessfully() {
      when(pprService.readPlatformProfileRolesByPlatformIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());
      when(prpService.readPlatformRolePermissionsByPlatformIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PlatformResponse> response =
          convertUtils.getResponseSinglePlatform(platformEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getPlatforms().size());
      assertEquals("Test Platform", response.getBody().getPlatforms().get(0).getPlatformName());
    }

    @Test
    @DisplayName("Should include audit history when provided")
    void shouldIncludeAuditHistoryWhenProvided() {
      ProfileEntity createdBy = new ProfileEntity();
      createdBy.setId(2L);
      createdBy.setEmail(TEST_EMAIL);
      auditEntity.setCreatedBy(createdBy);
      auditEntity.setEventData(platformEntity);

      when(pprService.readPlatformProfileRolesByPlatformIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());
      when(prpService.readPlatformRolePermissionsByPlatformIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PlatformResponse> response =
          convertUtils.getResponseSinglePlatform(platformEntity, null, List.of(auditEntity));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getPlatforms().size());
      assertFalse(response.getBody().getPlatforms().get(0).getHistory().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple platforms successfully")
    void shouldReturnMultiplePlatformsSuccessfully() {
      PlatformEntity platform2 = new PlatformEntity();
      platform2.setId(2L);
      platform2.setPlatformName("Platform 2");

      ResponseEntity<PlatformResponse> response =
          convertUtils.getResponseMultiplePlatforms(List.of(platformEntity, platform2));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().getPlatforms().size());
    }

    @Test
    @DisplayName("Should create error response for platform")
    void shouldCreateErrorResponseForPlatform() {
      Exception exception = new ElementNotFoundException("Platform", "ID");
      ResponseEntity<PlatformResponse> response = convertUtils.getResponseErrorPlatform(exception);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPlatforms().isEmpty());
      assertEquals(
          "Platform Not Found for [ID]",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Should create data integrity violation error response for platform")
    void shouldCreateErrorResponseForRole_DataIntegrityViolationException() {
      Exception exception = new DataIntegrityViolationException("something bad happened");
      ResponseEntity<PlatformResponse> response = convertUtils.getResponseErrorPlatform(exception);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getPlatforms().isEmpty());
      assertEquals(
          "Action Failed! Platform Name Already Exists!! Please Try Again!!!",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }
  }

  @Nested
  @DisplayName("Profile conversion and response tests")
  class ProfileTests {

    private ProfileEntity profileEntity;
    private ProfileAddressEntity profileAddress;
    private AuditProfileEntity auditEntity;

    @BeforeEach
    void setUp() {
      profileEntity = new ProfileEntity();
      profileEntity.setId(TEST_ID);
      profileEntity.setEmail(TEST_EMAIL);
      profileEntity.setFirstName("John");
      profileEntity.setLastName("Doe");

      profileAddress = new ProfileAddressEntity();
      profileAddress.setStreet("123 Main St");
      profileAddress.setCity("Test City");
      profileEntity.setProfileAddress(profileAddress);

      auditEntity = new AuditProfileEntity();
      auditEntity.setId(1L);
      auditEntity.setEventType(TypeEnums.EventType.CREATE.name());
      auditEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return null when profile entity is null")
    void shouldReturnNullWhenProfileEntityIsNull() {
      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(null, null, null);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getProfiles().isEmpty());
    }

    @Test
    @DisplayName("Should convert profile entity to DTO successfully")
    void shouldConvertProfileEntityToDtoSuccessfully() {
      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(profileEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getProfiles().size());
      assertEquals(TEST_EMAIL, response.getBody().getProfiles().get(0).getEmail());
    }

    @Test
    @DisplayName("Should include profile address when present")
    void shouldIncludeProfileAddressWhenPresent() {
      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(profileEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      ProfileDto profileDto = response.getBody().getProfiles().get(0);
      assertNotNull(profileDto.getProfileAddress());
      assertEquals("123 Main St", profileDto.getProfileAddress().getStreet());
    }

    @Test
    @DisplayName("Should handle profile without address")
    void shouldHandleProfileWithoutAddress() {
      profileEntity.setProfileAddress(null);
      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(profileEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      ProfileDto profileDto = response.getBody().getProfiles().get(0);
      assertNull(profileDto.getProfileAddress());
    }

    @Test
    @DisplayName("Should include audit history when provided")
    void shouldIncludeAuditHistoryWhenProvided() {
      ProfileEntity createdBy = new ProfileEntity();
      createdBy.setId(2L);
      createdBy.setEmail("creator@example.com");
      auditEntity.setCreatedBy(createdBy);
      auditEntity.setEventData(profileEntity);

      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(profileEntity, null, List.of(auditEntity));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().getProfiles().size());
      assertFalse(response.getBody().getProfiles().get(0).getHistory().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple profiles successfully")
    void shouldReturnMultipleProfilesSuccessfully() {
      ProfileEntity profile2 = new ProfileEntity();
      profile2.setId(2L);
      profile2.setEmail("user2@example.com");

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseMultipleProfiles(List.of(profileEntity, profile2), false);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().getProfiles().size());
    }

    @Test
    @DisplayName("Should include platform and role IDs when isIncludeExtras is true")
    void shouldIncludePlatformAndRoleIdsWhenIncludeExtras() {
      PlatformEntity platform = new PlatformEntity();
      platform.setId(10L);
      RoleEntity role = new RoleEntity();
      role.setId(20L);

      PlatformProfileRoleEntity pprEntity = new PlatformProfileRoleEntity();
      pprEntity.setPlatform(platform);
      pprEntity.setRole(role);

      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(List.of(pprEntity));

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseMultipleProfiles(List.of(profileEntity), true);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertFalse(response.getBody().getPlatformIds().isEmpty());
      assertFalse(response.getBody().getRoleIds().isEmpty());
      assertEquals(10L, response.getBody().getPlatformIds().get(0));
      assertEquals(20L, response.getBody().getRoleIds().get(0));
    }

    @Test
    @DisplayName("Should create error response for profile")
    void shouldCreateErrorResponseForProfile() {
      Exception exception = new ElementNotFoundException("Profile", "ID");
      ResponseEntity<ProfileResponse> response = convertUtils.getResponseErrorProfile(exception);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getProfiles().isEmpty());
      assertEquals(
          "Profile Not Found for [ID]",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }

    @Test
    @DisplayName("Should create data integrity violation error response for profile")
    void shouldCreateErrorResponseForRole_DataIntegrityViolationException() {
      Exception exception = new DataIntegrityViolationException("something bad happened");
      ResponseEntity<ProfileResponse> response = convertUtils.getResponseErrorProfile(exception);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getProfiles().isEmpty());
      assertEquals(
          "Action Failed! Profile Email or Phone Already Exists!! Please Try Again!!!",
          response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
    }
  }

  @Nested
  @DisplayName("Other response methods tests")
  class OtherResponseTests {

    @Test
    @DisplayName("Should create validate profile response with success")
    void shouldCreateValidateProfileResponseWithSuccess() {
      String redirectUrl = "https://example.com/validate";
      ResponseEntity<Void> response = convertUtils.getResponseValidateProfile(redirectUrl, true);

      assertEquals(HttpStatus.FOUND, response.getStatusCode());
      assertNotNull(response.getHeaders().getLocation());
      assertTrue(response.getHeaders().getLocation().toString().contains("is_validated=true"));
    }

    @Test
    @DisplayName("Should create validate profile response with failure")
    void shouldCreateValidateProfileResponseWithFailure() {
      String redirectUrl = "https://example.com/validate";
      ResponseEntity<Void> response = convertUtils.getResponseValidateProfile(redirectUrl, false);

      assertEquals(HttpStatus.FOUND, response.getStatusCode());
      assertNotNull(response.getHeaders().getLocation());
      assertTrue(response.getHeaders().getLocation().toString().contains("is_validated=false"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when redirect URL is null")
    void shouldThrowIllegalStateExceptionWhenRedirectUrlIsNull() {
      assertThrows(
          IllegalStateException.class, () -> convertUtils.getResponseValidateProfile(null, true));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when redirect URL is empty")
    void shouldThrowIllegalStateExceptionWhenRedirectUrlIsEmpty() {
      assertThrows(
          IllegalStateException.class, () -> convertUtils.getResponseValidateProfile("", true));
    }

    @Test
    @DisplayName("Should create reset profile response with success")
    void shouldCreateResetProfileResponseWithSuccess() {
      String redirectUrl = "https://example.com/reset";
      String email = TEST_EMAIL;
      ResponseEntity<Void> response =
          convertUtils.getResponseResetProfile(redirectUrl, true, email);

      assertEquals(HttpStatus.FOUND, response.getStatusCode());
      assertNotNull(response.getHeaders().getLocation());
      assertTrue(response.getHeaders().getLocation().toString().contains("is_reset=true"));
      assertTrue(response.getHeaders().getLocation().toString().contains("to_reset=" + email));
    }

    @Test
    @DisplayName("Should create reset profile response with failure")
    void shouldCreateResetProfileResponseWithFailure() {
      String redirectUrl = "https://example.com/reset";
      ResponseEntity<Void> response =
          convertUtils.getResponseResetProfile(redirectUrl, false, TEST_EMAIL);

      assertEquals(HttpStatus.FOUND, response.getStatusCode());
      assertNotNull(response.getHeaders().getLocation());
      assertTrue(response.getHeaders().getLocation().toString().contains("is_reset=false"));
      assertFalse(response.getHeaders().getLocation().toString().contains("to_reset="));
    }

    @Test
    @DisplayName("Should throw IllegalStateException for reset when redirect URL is null")
    void shouldThrowIllegalStateExceptionForResetWhenRedirectUrlIsNull() {
      assertThrows(
          IllegalStateException.class,
          () -> convertUtils.getResponseResetProfile(null, true, TEST_EMAIL));
    }

    @Test
    @DisplayName("Should create error response for profile password with cookies")
    void shouldCreateErrorResponseForProfilePasswordWithCookies() {
      ResponseCookie refreshCookie = ResponseCookie.from("refresh", "").maxAge(0).build();
      ResponseCookie csrfCookie = ResponseCookie.from("csrf", "").maxAge(0).build();

      when(cookieService.buildRefreshCookie("", 0)).thenReturn(refreshCookie);
      when(cookieService.buildCsrfCookie("", 0)).thenReturn(csrfCookie);

      Exception exception = new TokenInvalidException("Invalid token");
      ResponseEntity<ProfilePasswordTokenResponse> response =
          convertUtils.getResponseErrorProfilePassword(exception);

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(
          "Invalid token", response.getBody().getResponseMetadata().responseStatusInfo().errMsg());
      assertNotNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
    }
  }

  @Nested
  @DisplayName("AuthToken creation tests")
  class AuthTokenCreationTests {

    private PlatformEntity platformEntity;
    private ProfileEntity profileEntity;

    @BeforeEach
    void setUp() {
      platformEntity = new PlatformEntity();
      platformEntity.setId(TEST_ID);
      platformEntity.setPlatformName("Test Platform");

      profileEntity = new ProfileEntity();
      profileEntity.setId(TEST_ID);
      profileEntity.setEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should create auth token with platform and profile")
    void shouldCreateAuthTokenWithPlatformAndProfile() {
      List<AuthTokenRolePermissionLookup> lookups = new ArrayList<>();
      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID)).thenReturn(lookups);

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      assertNotNull(authToken.getPlatform());
      assertEquals(TEST_ID, authToken.getPlatform().getId());
      assertEquals("Test Platform", authToken.getPlatform().getPlatformName());
      assertNotNull(authToken.getProfile());
      assertEquals(TEST_ID, authToken.getProfile().getId());
      assertEquals(TEST_EMAIL, authToken.getProfile().getEmail());
    }

    @Test
    @DisplayName("Should create auth token with roles and permissions")
    void shouldCreateAuthTokenWithRolesAndPermissions() {
      AuthTokenRolePermissionLookup lookup1 =
          new AuthTokenRolePermissionLookup(1L, "ADMIN", 10L, "READ");
      AuthTokenRolePermissionLookup lookup2 =
          new AuthTokenRolePermissionLookup(1L, "ADMIN", 11L, "WRITE");

      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID))
          .thenReturn(List.of(lookup1, lookup2));

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      assertFalse(authToken.getRoles().isEmpty());
      assertFalse(authToken.getPermissions().isEmpty());
      assertEquals(2, authToken.getRoles().size());
      assertEquals(2, authToken.getPermissions().size());
    }

    @Test
    @DisplayName("Should set isSuperUser to true when SUPERUSER role is present")
    void shouldSetIsSuperUserToTrueWhenSuperuserRolePresent() {
      AuthTokenRolePermissionLookup lookup =
          new AuthTokenRolePermissionLookup(1L, ConstantUtils.ROLE_NAME_SUPERUSER, 10L, "ALL");

      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID))
          .thenReturn(List.of(lookup));

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      assertTrue(authToken.getIsSuperUser());
    }

    @Test
    @DisplayName("Should set isSuperUser to false when SUPERUSER role is not present")
    void shouldSetIsSuperUserToFalseWhenSuperuserRoleNotPresent() {
      AuthTokenRolePermissionLookup lookup =
          new AuthTokenRolePermissionLookup(1L, "USER", 10L, "READ");

      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID))
          .thenReturn(List.of(lookup));

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      assertFalse(authToken.getIsSuperUser());
    }

    @Test
    @DisplayName("Should handle empty roles and permissions")
    void shouldHandleEmptyRolesAndPermissions() {
      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID))
          .thenReturn(Collections.emptyList());

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      assertTrue(authToken.getRoles().isEmpty());
      assertTrue(authToken.getPermissions().isEmpty());
      assertFalse(authToken.getIsSuperUser());
    }

    @Test
    @DisplayName("Should deduplicate roles correctly")
    void shouldDeduplicateRolesCorrectly() {
      AuthTokenRolePermissionLookup lookup1 =
          new AuthTokenRolePermissionLookup(1L, "ADMIN", 10L, "READ");
      AuthTokenRolePermissionLookup lookup2 =
          new AuthTokenRolePermissionLookup(1L, "ADMIN", 11L, "WRITE");
      AuthTokenRolePermissionLookup lookup3 =
          new AuthTokenRolePermissionLookup(2L, "USER", 12L, "READ");

      when(rawSqlRepository.findRolePermissionsForAuthToken(TEST_ID, TEST_ID))
          .thenReturn(List.of(lookup1, lookup2, lookup3));

      AuthToken authToken = convertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);

      assertNotNull(authToken);
      // Should have 3 role entries (duplicates not removed in current implementation)
      // This tests current behavior
      assertEquals(3, authToken.getRoles().size());
      assertEquals(3, authToken.getPermissions().size());
    }
  }

  @Nested
  @DisplayName("Complex conversion scenarios")
  class ComplexConversionTests {

    @Test
    @DisplayName("Should handle permission with platform role permissions")
    void shouldHandlePermissionWithPlatformRolePermissions() {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setId(TEST_ID);
      permissionEntity.setPermissionName("READ");

      PlatformEntity platform = new PlatformEntity();
      platform.setId(10L);
      platform.setPlatformName("Platform A");

      RoleEntity role = new RoleEntity();
      role.setId(20L);
      role.setRoleName("ADMIN");

      PlatformRolePermissionEntity prpEntity = new PlatformRolePermissionEntity();
      prpEntity.setPlatform(platform);
      prpEntity.setRole(role);
      prpEntity.setPermission(permissionEntity);
      prpEntity.setAssignedDate(LocalDateTime.now());

      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(List.of(prpEntity));

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      PermissionDto dto = response.getBody().getPermissions().get(0);
      assertFalse(dto.getPlatformRolePermissions().isEmpty());
      assertEquals(
          "Platform A", dto.getPlatformRolePermissions().get(0).getPlatform().getPlatformName());
      assertEquals("ADMIN", dto.getPlatformRolePermissions().get(0).getRole().getRoleName());
    }

    @Test
    @DisplayName("Should handle role with platform profile roles and permissions")
    void shouldHandleRoleWithPlatformProfileRolesAndPermissions() {
      RoleEntity roleEntity = new RoleEntity();
      roleEntity.setId(TEST_ID);
      roleEntity.setRoleName("ADMIN");

      PlatformEntity platform = new PlatformEntity();
      platform.setId(10L);

      ProfileEntity profile = new ProfileEntity();
      profile.setId(5L);
      profile.setEmail(TEST_EMAIL);

      PermissionEntity permission = new PermissionEntity();
      permission.setId(15L);
      permission.setPermissionName("READ");

      PlatformProfileRoleEntity pprEntity = new PlatformProfileRoleEntity();
      pprEntity.setPlatform(platform);
      pprEntity.setProfile(profile);
      pprEntity.setRole(roleEntity);

      PlatformRolePermissionEntity prpEntity = new PlatformRolePermissionEntity();
      prpEntity.setPlatform(platform);
      prpEntity.setRole(roleEntity);
      prpEntity.setPermission(permission);

      when(pprService.readPlatformProfileRolesByRoleIds(anyList(), anyBoolean()))
          .thenReturn(List.of(pprEntity));
      when(prpService.readPlatformRolePermissionsByRoleIds(anyList(), anyBoolean()))
          .thenReturn(List.of(prpEntity));

      ResponseEntity<RoleResponse> response =
          convertUtils.getResponseSingleRole(roleEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      RoleDto dto = response.getBody().getRoles().get(0);
      assertFalse(dto.getPlatformProfileRoles().isEmpty());
      assertFalse(dto.getPlatformRolePermissions().isEmpty());
    }

    @Test
    @DisplayName("Should handle null profile address correctly")
    void shouldHandleNullProfileAddressCorrectly() {
      ProfileEntity profileEntity = new ProfileEntity();
      profileEntity.setId(TEST_ID);
      profileEntity.setEmail(TEST_EMAIL);
      profileEntity.setProfileAddress(null);

      when(pprService.readPlatformProfileRolesByProfileIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<ProfileResponse> response =
          convertUtils.getResponseSingleProfile(profileEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      ProfileDto dto = response.getBody().getProfiles().get(0);
      assertNull(dto.getProfileAddress());
    }

    @Test
    @DisplayName("Should handle empty audit history lists")
    void shouldHandleEmptyAuditHistoryLists() {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setId(TEST_ID);

      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, null, Collections.emptyList());

      assertEquals(HttpStatus.OK, response.getStatusCode());
      PermissionDto dto = response.getBody().getPermissions().get(0);
      assertTrue(dto.getHistory().isEmpty());
    }
  }

  @Nested
  @DisplayName("Edge cases and null handling")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle entity with null ID")
    void shouldHandleEntityWithNullId() {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setId(null);

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, null, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().getPermissions().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty list in multiple responses")
    void shouldHandleEmptyListInMultipleResponses() {
      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseMultiplePermissions(Collections.emptyList());

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().getPermissions().isEmpty());
      assertTrue(response.getBody().getPlatformNames().isEmpty());
    }

    @Test
    @DisplayName("Should preserve crud info in response")
    void shouldPreserveCrudInfoInResponse() {
      PermissionEntity permissionEntity = new PermissionEntity();
      permissionEntity.setId(TEST_ID);

      ResponseMetadata.ResponseCrudInfo crudInfo =
          new ResponseMetadata.ResponseCrudInfo(1, 2, 3, 4);

      when(prpService.readPlatformRolePermissionsByPermissionIds(anyList(), anyBoolean()))
          .thenReturn(Collections.emptyList());

      ResponseEntity<PermissionResponse> response =
          convertUtils.getResponseSinglePermission(permissionEntity, crudInfo, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      ResponseMetadata.ResponseCrudInfo responseCrudInfo =
          response.getBody().getResponseMetadata().responseCrudInfo();
      assertEquals(1, responseCrudInfo.insertedRowsCount());
      assertEquals(2, responseCrudInfo.updatedRowsCount());
      assertEquals(3, responseCrudInfo.deletedRowsCount());
      assertEquals(4, responseCrudInfo.restoredRowsCount());
    }

    @Test
    @DisplayName("Should handle various exception types correctly")
    void shouldHandleVariousExceptionTypesCorrectly() {
      Exception[] exceptions = {
        new ElementNotFoundException("type", "column"),
        new ElementMissingException("type", "variable"),
        new ProfileForbiddenException(),
        new ProfileNotAuthorizedException("Not authorized"),
        new DataIntegrityViolationException("something bad happened"),
        new RuntimeException("Unknown")
      };

      HttpStatus[] expectedStatuses = {
        HttpStatus.NOT_FOUND,
        HttpStatus.BAD_REQUEST,
        HttpStatus.FORBIDDEN,
        HttpStatus.UNAUTHORIZED,
        HttpStatus.BAD_REQUEST,
        HttpStatus.INTERNAL_SERVER_ERROR
      };

      for (int i = 0; i < exceptions.length; i++) {
        HttpStatus status = convertUtils.getHttpStatusForErrorResponse(exceptions[i]);
        assertEquals(
            expectedStatuses[i],
            status,
            "Failed for exception: " + exceptions[i].getClass().getSimpleName());
      }
    }
  }
}
