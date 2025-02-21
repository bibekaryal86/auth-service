package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.repository.PlatformProfileRoleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import helper.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformProfileRoleServiceTest extends BaseTest {

  @Autowired private PlatformProfileRoleService platformProfileRoleService;
  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;

  @Test
  void testReadPlatformProfileRolesByPlatformIds() {
    assertEquals(
        4,
        platformProfileRoleService.readPlatformProfileRolesByPlatformIds(List.of(1L, 4L)).size());
  }

  @Test
  void testReadPlatformProfileRolesByProfileIds() {
    assertEquals(
        3,
        platformProfileRoleService
            .readPlatformProfileRolesByProfileIds(List.of(1L, 2L, 3L))
            .size());
  }

  @Test
  void testReadPlatformProfileRolesByRoleIds() {
    assertEquals(
        3,
        platformProfileRoleService.readPlatformProfileRolesByRoleIds(List.of(4L, 5L, 6L)).size());
  }

  @Test
  void testReadPlatformProfileRole_ByPlatformIdProfileEmail() {
    assertNotNull(platformProfileRoleService.readPlatformProfileRole(3L, "firstlast@three.com"));
  }

  @Test
  void testReadPlatformProfileRole_ByPlatformIdProfileEmail_NotFound() {
    assertThrows(
        ElementNotFoundException.class,
        () -> platformProfileRoleService.readPlatformProfileRole(3L, "firstlast-1@nine.com"));
  }

  @Test
  void testPlatformProfileRoleService_assignUnassign() {
    Long platformId = 7L;
    Long profileId = 7L;
    Long roleId = 7L;
    PlatformProfileRoleRequest platformProfileRoleRequest =
        new PlatformProfileRoleRequest(platformId, profileId, roleId);

    // assign
    PlatformProfileRoleEntity platformProfileRoleEntity =
        platformProfileRoleService.assignPlatformProfileRole(platformProfileRoleRequest);
    assertNotNull(platformProfileRoleEntity);
    assertNotNull(platformProfileRoleEntity.getId());
    assertNotNull(platformProfileRoleEntity.getAssignedDate());

    // unassign
    platformProfileRoleEntity =
        platformProfileRoleService.unassignPlatformProfileRole(platformId, profileId, roleId);
    assertNotNull(platformProfileRoleEntity);
    assertNotNull(platformProfileRoleEntity.getId());
    assertNotNull(platformProfileRoleEntity.getAssignedDate());
    assertNotNull(platformProfileRoleEntity.getUnassignedDate());

    // cleanup
    platformProfileRoleRepository.delete(platformProfileRoleEntity);
  }

  @Test
  void testAssignPlatformProfileRole_platformErrors() {
    ElementNotFoundException elementNotFoundException =
        assertThrows(
            ElementNotFoundException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID_NOT_FOUND, ID, ID)));
    assertEquals("Platform Not Found for [99]", elementNotFoundException.getMessage());

    ElementNotActiveException elementNotActiveException =
        assertThrows(
            ElementNotActiveException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID_DELETED, ID, ID)));
    assertEquals(
        String.format("Active Platform Not Found for [%s]", ID_DELETED),
        elementNotActiveException.getMessage());
  }

  @Test
  void testAssignPlatformProfileRole_profileErrors() {
    ElementNotFoundException elementNotFoundException =
        assertThrows(
            ElementNotFoundException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID, ID_NOT_FOUND, ID)));
    assertEquals("Profile Not Found for [99]", elementNotFoundException.getMessage());

    ElementNotActiveException elementNotActiveException =
        assertThrows(
            ElementNotActiveException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID, ID_DELETED, ID)));
    assertEquals(
        String.format("Active Profile Not Found for [%s]", ID_DELETED),
        elementNotActiveException.getMessage());
  }

  @Test
  void testAssignPlatformProfileRole_roleErrors() {
    ElementNotFoundException elementNotFoundException =
        assertThrows(
            ElementNotFoundException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID, ID, ID_NOT_FOUND)));
    assertEquals("Role Not Found for [99]", elementNotFoundException.getMessage());

    ElementNotActiveException elementNotActiveException =
        assertThrows(
            ElementNotActiveException.class,
            () ->
                platformProfileRoleService.assignPlatformProfileRole(
                    new PlatformProfileRoleRequest(ID, ID, ID_DELETED)));
    assertEquals(
        String.format("Active Role Not Found for [%s]", ID_DELETED),
        elementNotActiveException.getMessage());
  }

  @Test
  void testDeletedByPlatformIds() {
    for (int i=10; i<13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleService.deletedPlatformProfileRolesByPlatformIds(List.of(11L, 12L, 13L));

    for (int i=10; i<13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional = platformProfileRoleRepository.findById(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      assertTrue(pprOptional.isEmpty());
    }
  }

  @Test
  void testDeletedByProfileIds() {
    // setup
    for (int i=10; i<13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleService.deletedPlatformProfileRolesByProfileIds(List.of(11L, 12L, 13L));

    for (int i=10; i<13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional = platformProfileRoleRepository.findById(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      assertTrue(pprOptional.isEmpty());
    }
  }

  @Test
  void testDeletedByRoleIds() {
    for (int i=10; i<13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleService.deletedPlatformProfileRolesByRoleIds(List.of(11L, 12L, 13L));

    for (int i=10; i<13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional = platformProfileRoleRepository.findById(new PlatformProfileRoleId((long) (i+1), (long) i+1, (long) i+1));
      assertTrue(pprOptional.isEmpty());
    }
  }
}
