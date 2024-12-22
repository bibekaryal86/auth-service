package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformProfileRoleRepositoryTest extends BaseTest {

  @Autowired private PlatformProfileRoleRepository platformProfileRoleRepository;

  @Test
  void testFindByPlatformIdAndProfileEmail() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByPlatformIdAndProfileEmail(4L, "firstlast-1@one.com");
    assertEquals(3, platformProfileRoleEntities.size());
  }

  @Test
  void testFindByProfileId() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByProfileId(4L);
    assertEquals(3, platformProfileRoleEntities.size());
  }

  @Test
  void testFindByProfileIds() {
    List<PlatformProfileRoleEntity> platformProfileRoleEntities =
        platformProfileRoleRepository.findByProfileIds(List.of(1L, 2L, 3L, 4L));
    assertEquals(6, platformProfileRoleEntities.size());
    // test order by platform name, profile email and role name
    assertAll(
        "Platform Profile Role Entities Find By Profile Ids",
        () ->
            assertAll(
                "Entity 0",
                () ->
                    assertEquals(
                        "Auth Service",
                        platformProfileRoleEntities.get(0).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@one.com",
                        platformProfileRoleEntities.get(0).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "SUPERUSER", platformProfileRoleEntities.get(0).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 1",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformProfileRoleEntities.get(1).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast-1@one.com",
                        platformProfileRoleEntities.get(1).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "GUEST-1", platformProfileRoleEntities.get(1).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 2",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformProfileRoleEntities.get(2).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast-1@one.com",
                        platformProfileRoleEntities.get(2).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "POWERUSER-1", platformProfileRoleEntities.get(2).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 3",
                () ->
                    assertEquals(
                        "Auth Service-1",
                        platformProfileRoleEntities.get(3).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast-1@one.com",
                        platformProfileRoleEntities.get(3).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "SUPERUSER-1", platformProfileRoleEntities.get(3).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 4",
                () ->
                    assertEquals(
                        "Env Service",
                        platformProfileRoleEntities.get(4).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@two.com",
                        platformProfileRoleEntities.get(4).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "POWERUSER", platformProfileRoleEntities.get(4).getRole().getRoleName())),
        () ->
            assertAll(
                "Entity 5",
                () ->
                    assertEquals(
                        "Personal Expenses Tracking System",
                        platformProfileRoleEntities.get(5).getPlatform().getPlatformName()),
                () ->
                    assertEquals(
                        "firstlast@three.com",
                        platformProfileRoleEntities.get(5).getProfile().getEmail()),
                () ->
                    assertEquals(
                        "GUEST", platformProfileRoleEntities.get(5).getRole().getRoleName())));
  }
}
