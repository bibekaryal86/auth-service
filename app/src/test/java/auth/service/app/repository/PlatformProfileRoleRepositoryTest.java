package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import java.util.List;
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
        List<PlatformProfileRoleEntity> platformProfileRoleEntities = platformProfileRoleRepository.findByPlatformIdAndProfileEmail(4L, "firstlast@four.com");
        assertEquals(3, platformProfileRoleEntities.size());
    }

    @Test
    void testFindByPlatformId() {
        List<PlatformProfileRoleEntity> platformProfileRoleEntities = platformProfileRoleRepository.findAll();
        final Pageable pageable =
                PageRequest.of(0, 100,
                        Sort.by(
                                Sort.Order.asc("platform.platformName"),
                                Sort.Order.asc("profile.email"),
                                Sort.Order.asc("role.roleName")));
        Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage = platformProfileRoleRepository.findByPlatformId(4L, pageable);
        assertEquals(3, platformProfileRoleEntityPage.toList().size());
    }

    @Test
    void testFindByPlatformIdNoDeleted() {
        final Pageable pageable =
                PageRequest.of(0, 100,
                        Sort.by(
                                Sort.Order.asc("platform.platformName"),
                                Sort.Order.asc("profile.email"),
                                Sort.Order.asc("role.roleName")));
        Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage = platformProfileRoleRepository.findByPlatformIdNoDeleted(4L, pageable);
        assertEquals(2, platformProfileRoleEntityPage.toList().size());
    }

    @Test
    void testFindByPlatformIds() {
        List<PlatformProfileRoleEntity> platformProfileRoleEntities = platformProfileRoleRepository.findByPlatformIds(List.of(1L, 2L, 3L, 4L));
        assertEquals(6, platformProfileRoleEntities.size());
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
                                                "ROLE-04", platformProfileRoleEntities.get(3).getRole().getRoleName())),
                () ->
                        assertAll(
                                "Entity 4",
                                () ->
                                        assertEquals(
                                                "PLATFORM-04",
                                                platformProfileRoleEntities.get(4).getPlatform().getPlatformName()),
                                () ->
                                        assertEquals(
                                                "firstlast@four.com",
                                                platformProfileRoleEntities.get(4).getProfile().getEmail()),
                                () ->
                                        assertEquals(
                                                "ROLE-05", platformProfileRoleEntities.get(4).getRole().getRoleName())),
                () ->
                        assertAll(
                                "Entity 5",
                                () ->
                                        assertEquals(
                                                "PLATFORM-04",
                                                platformProfileRoleEntities.get(5).getPlatform().getPlatformName()),
                                () ->
                                        assertEquals(
                                                "firstlast@four.com",
                                                platformProfileRoleEntities.get(5).getProfile().getEmail()),
                                () ->
                                        assertEquals(
                                                "ROLE-06", platformProfileRoleEntities.get(5).getRole().getRoleName())));
    }

    @Test
    void testFindByProfileIds() {
        List<PlatformProfileRoleEntity> platformProfileRoleEntities = platformProfileRoleRepository.findByProfileIds(List.of(1L, 2L, 3L));
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
        List<PlatformProfileRoleEntity> platformProfileRoleEntities = platformProfileRoleRepository.findByRoleIds(List.of(4L, 5L, 6L));
        assertEquals(3, platformProfileRoleEntities.size());
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
                                                "ROLE-04", platformProfileRoleEntities.get(0).getRole().getRoleName())),
                () ->
                        assertAll(
                                "Entity 1",
                                () ->
                                        assertEquals(
                                                "PLATFORM-04",
                                                platformProfileRoleEntities.get(1).getPlatform().getPlatformName()),
                                () ->
                                        assertEquals(
                                                "firstlast@four.com",
                                                platformProfileRoleEntities.get(1).getProfile().getEmail()),
                                () ->
                                        assertEquals(
                                                "ROLE-05", platformProfileRoleEntities.get(1).getRole().getRoleName())),
                () ->
                        assertAll(
                                "Entity 2",
                                () ->
                                        assertEquals(
                                                "PLATFORM-04",
                                                platformProfileRoleEntities.get(2).getPlatform().getPlatformName()),
                                () ->
                                        assertEquals(
                                                "firstlast@four.com",
                                                platformProfileRoleEntities.get(2).getProfile().getEmail()),
                                () ->
                                        assertEquals(
                                                "ROLE-06", platformProfileRoleEntities.get(2).getRole().getRoleName())));
    }
}
