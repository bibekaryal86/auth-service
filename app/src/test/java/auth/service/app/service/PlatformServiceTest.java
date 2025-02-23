package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PlatformServiceTest extends BaseTest {

  @Mock private SecurityContext securityContext;

  @Autowired private PlatformService platformService;
  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private CircularDependencyService circularDependencyService;

  @Test
  void testReadPlatforms_NoRequestMetadata() {
    Page<PlatformEntity> platformEntityPage = platformService.readPlatforms(null);
    List<PlatformEntity> platformEntities = platformEntityPage.toList();
    assertEquals(9, platformEntities.size());
    assertEquals(1, platformEntityPage.getTotalPages());
    assertEquals(100, platformEntityPage.getSize());
    // check sorted by name
    assertEquals("PLATFORM-01", platformEntities.getFirst().getPlatformName());
    assertEquals("PLATFORM-13", platformEntities.getLast().getPlatformName());
  }

  @Test
  void testReadPlatforms_RequestMetadata() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
    AuthToken authToken = TestData.getAuthToken();
    authToken.setSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    Page<PlatformEntity> platformEntityPage =
        platformService.readPlatforms(RequestMetadata.builder().isIncludeDeleted(true).build());
    List<PlatformEntity> platformEntities = platformEntityPage.toList();
    assertEquals(13, platformEntities.size());
    assertEquals(1, platformEntityPage.getTotalPages());
    assertEquals(100, platformEntityPage.getSize());
    // check sorted by name
    assertEquals("PLATFORM-01", platformEntities.getFirst().getPlatformName());
    assertEquals("PLATFORM-13", platformEntities.getLast().getPlatformName());
  }

  @Test
  void testPlatformService_CRUD() {
    String newName = "PlatformNewName";
    String newDesc = "PlatformNewDesc";
    String updatedDesc = "PlatformUpdatedDesc";
    PlatformRequest request = new PlatformRequest(newName, newDesc);

    // Create
    Long id = assertCreate(request);

    // Update
    request = new PlatformRequest(newName, updatedDesc);
    assertUpdate(id, request);

    // Update fails for deleted Platform
    assertUpdateFailsForDeletedPlatform(ID_DELETED, request);

    // Read
    // readPlatform has private access

    // Soft delete
    assertDeleteSoft(id);

    // Soft delete fails for deleted Platform
    assertDeleteSoftFailsForDeletedPlatform(ID_DELETED);

    // Restore
    assertRestoreSoftDeleted(id);

    // deleteHard
    assertDeleteHard(id);
  }

  private Long assertCreate(PlatformRequest request) {
    PlatformEntity entity = platformService.createPlatform(request);
    assertNotNull(entity);
    assertNotNull(entity.getId());
    return entity.getId();
  }

  private void assertUpdate(Long id, PlatformRequest request) {
    PlatformEntity entity = platformService.updatePlatform(id, request);
    assertNotNull(entity);
  }

  private void assertUpdateFailsForDeletedPlatform(Long id, PlatformRequest request) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> platformService.updatePlatform(id, request),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Platform Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  private void assertDeleteSoft(Long id) {
    PlatformEntity entity = platformService.softDeletePlatform(id);
    assertNotNull(entity);
    assertNotNull(entity.getDeletedDate());
  }

  private void assertDeleteSoftFailsForDeletedPlatform(Long id) {
    ElementNotActiveException exception =
        assertThrows(
            ElementNotActiveException.class,
            () -> platformService.softDeletePlatform(id),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Active Platform Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }

  private void assertRestoreSoftDeleted(Long id) {
    PlatformEntity entity = platformService.restoreSoftDeletedPlatform(id);
    assertNotNull(entity);
    assertNull(entity.getDeletedDate());
  }

  private void assertDeleteHard(Long id) {
    // setup
    PlatformProfileRoleRequest pprRequest = new PlatformProfileRoleRequest(id, ID, ID);
    PlatformProfileRoleEntity pprEntity =
        platformProfileRoleService.assignPlatformProfileRole(pprRequest);
    assertNotNull(pprEntity.getId());
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(id, EMAIL));

    pprRequest = new PlatformProfileRoleRequest(id, 2L, 2L);
    pprEntity = platformProfileRoleService.assignPlatformProfileRole(pprRequest);
    assertNotNull(pprEntity.getId());
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(id, "firstlast@two.com"));

    platformService.hardDeletePlatform(id);

    // assert Platform is Deleted
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> circularDependencyService.readPlatform(id, false),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Platform Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");

    // assert PPRs are Deleted
    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> platformProfileRoleService.readPlatformProfileRole(id, EMAIL),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Platform Profile Role Not Found for [%s,%s]", id, EMAIL),
        exception.getMessage(),
        "Exception message mismatch...");

    exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> platformProfileRoleService.readPlatformProfileRole(id, "firstlast@two.com"),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Platform Profile Role Not Found for [%s,%s]", id, "firstlast@two.com"),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
