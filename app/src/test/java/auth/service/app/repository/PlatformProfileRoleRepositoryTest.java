package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import helper.TestData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PlatformProfileRoleRepositoryTest extends BaseTest {

  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;

  @Test
  void testFindByPlatformIdAndProfileEmail() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByPlatformIdAndProfileEmail(4L, "firstlast@four.com");
    assertEquals(1, platformProfileRoleEntities.size());
  }

  @Test
  void testFindByPlatformIdAndProfileId() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByPlatformIdAndProfileId(4L, 4L);
    assertEquals(1, platformProfileRoleEntities.size());
  }

  @Test
  void testFindByPlatformId() {
    final Pageable pageable =
        PageRequest.of(
            0,
            100,
            Sort.by(
                Sort.Order.asc("platform.platformName"),
                Sort.Order.asc("profile.email"),
                Sort.Order.asc("role.roleName")));
    Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage =
        platformProfileRoleRepository.findByPlatformId(4L, pageable);
    assertEquals(1, platformProfileRoleEntityPage.toList().size());
  }

  @Test
  void testFindByPlatformIdNoDeleted() {
    final Pageable pageable =
        PageRequest.of(
            0,
            100,
            Sort.by(
                Sort.Order.asc("platform.platformName"),
                Sort.Order.asc("profile.email"),
                Sort.Order.asc("role.roleName")));
    Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage =
        platformProfileRoleRepository.findByPlatformIdNoDeleted(4L, pageable);
    assertEquals(1, platformProfileRoleEntityPage.toList().size());
  }

  @Test
  void testFindByPlatformIds() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByPlatformIds(List.of(ID, 2L, ID_DELETED, 4L));
    assertEquals(4, platformProfileRoleEntities.size());
    // test order by platform name, profile email and role name
    assertAll(
        "Platform Profile Role Entities Find By Profile Ids",
        () ->
            assertAll(
                "Entity 0",
                () ->
                    assertEquals(
                        "PLATFORM-01",
                        platformProfileRoleEntities.get(0).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@one.com",
                        platformProfileRoleEntities.get(0).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-01", platformProfileRoleEntities.get(0).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 1",
                () ->
                    assertEquals(
                        "PLATFORM-02",
                        platformProfileRoleEntities.get(1).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@two.com",
                        platformProfileRoleEntities.get(1).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-02", platformProfileRoleEntities.get(1).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 2",
                () ->
                    assertEquals(
                        "PLATFORM-03",
                        platformProfileRoleEntities.get(2).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@three.com",
                        platformProfileRoleEntities.get(2).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-03", platformProfileRoleEntities.get(2).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 3",
                () ->
                    assertEquals(
                        "PLATFORM-04",
                        platformProfileRoleEntities.get(3).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@four.com",
                        platformProfileRoleEntities.get(3).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-04", platformProfileRoleEntities.get(3).getRole().getRoleName())));
  }

  @Test
  void testFindByProfileIds() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByProfileIds(List.of(ID, 2L, ID_DELETED));
    assertEquals(3, platformProfileRoleEntities.size());
    // test order by platform name, profile email and role name
    assertAll(
        "Platform Profile Role Entities Find By Profile Ids",
        () ->
            assertAll(
                "Entity 0",
                () ->
                    assertEquals(
                        "PLATFORM-01",
                        platformProfileRoleEntities.get(0).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@one.com",
                        platformProfileRoleEntities.get(0).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-01", platformProfileRoleEntities.get(0).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 1",
                () ->
                    assertEquals(
                        "PLATFORM-02",
                        platformProfileRoleEntities.get(1).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@two.com",
                        platformProfileRoleEntities.get(1).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-02", platformProfileRoleEntities.get(1).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 2",
                () ->
                    assertEquals(
                        "PLATFORM-03",
                        platformProfileRoleEntities.get(2).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@three.com",
                        platformProfileRoleEntities.get(2).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-03", platformProfileRoleEntities.get(2).getRole().getRoleName())));
  }

  @Test
  void testFindByRoleIds() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByRoleIds(List.of(4L, 5L, 6L));
    assertEquals(1, platformProfileRoleEntities.size());
    // test order by platform name, profile email and role name
    assertAll(
        "Platform Profile Role Entities Find By Profile Ids",
        () ->
            assertAll(
                "Entity 0",
                () ->
                    assertEquals(
                        "PLATFORM-04",
                        platformProfileRoleEntities.get(0).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@four.com",
                        platformProfileRoleEntities.get(0).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "ROLE-04", platformProfileRoleEntities.get(0).getRole().getRoleName())));
  }

  @Test
  void testDeletedByPlatformIds() {
    for (int i = 10; i < 13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(
          new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleRepository.deleteByPlatformIds(List.of(11L, 12L, 13L));

    for (int i = 10; i < 13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional =
          platformProfileRoleRepository.findById(
              new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      assertTrue(pprOptional.isEmpty());
    }
  }

  @Test
  void testDeletedByProfileIds() {
    // setup
    for (int i = 10; i < 13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(
          new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleRepository.deleteByProfileIds(List.of(11L, 12L, 13L));

    for (int i = 10; i < 13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional =
          platformProfileRoleRepository.findById(
              new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      assertTrue(pprOptional.isEmpty());
    }
  }

  @Test
  void testDeletedByRoleIds() {
    for (int i = 10; i < 13; i++) {
      PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
      platformProfileRoleEntity.setPlatform(TestData.getPlatformEntities().get(i));
      platformProfileRoleEntity.setProfile(TestData.getProfileEntities().get(i));
      platformProfileRoleEntity.setRole(TestData.getRoleEntities().get(i));
      platformProfileRoleEntity.setId(
          new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
      platformProfileRoleRepository.save(platformProfileRoleEntity);
    }

    platformProfileRoleRepository.deleteByProfileIds(List.of(11L, 12L, 13L));

    for (int i = 10; i < 13; i++) {
      Optional<PlatformProfileRoleEntity> pprOptional =
          platformProfileRoleRepository.findById(
              new PlatformProfileRoleId((long) (i + 1), (long) i + 1, (long) i + 1));
      assertTrue(pprOptional.isEmpty());
    }
  }
}
