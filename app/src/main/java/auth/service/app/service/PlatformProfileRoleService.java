package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PlatformProfileRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformProfileRoleService {

  private final PlatformProfileRoleRepository platformProfileRoleRepository;
  private final CircularDependencyService circularDependencyService;

  // CREATE
  public PlatformProfileRoleEntity createPlatformProfileRole(
      final PlatformProfileRoleRequest platformProfileRoleRequest) {
    log.debug("Create Platform Profile Role: [{}]", platformProfileRoleRequest);
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    final PlatformEntity platformEntity =
        circularDependencyService.readPlatform(platformProfileRoleRequest.getPlatformId());
    final ProfileEntity profileEntity =
        circularDependencyService.readProfile(platformProfileRoleRequest.getProfileId());
    final RoleEntity roleEntity =
        circularDependencyService.readRole(platformProfileRoleRequest.getRoleId());
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    platformProfileRoleEntity.setId(
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId()));
    return platformProfileRoleRepository.save(platformProfileRoleEntity);
  }

  // READ
  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByProfileId() {
    log.debug("Read Platform Profile Role...");
    return platformProfileRoleRepository.findAll(
        Sort.by(
            Sort.Order.asc("platform.platformName"),
            Sort.Order.asc("profile.email"),
            Sort.Order.asc("role.roleName")));
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByProfileId(final Long profileId) {
    log.debug("Read Platform Profile Roles: [{}]", profileId);
    return platformProfileRoleRepository.findByProfileId(profileId);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByProfileIds(
      final List<Long> profileIds) {
    log.debug("Read Platform Profile Roles: [{}]", profileIds);
    return platformProfileRoleRepository.findByProfileIds(profileIds);
  }

  public PlatformProfileRoleEntity readPlatformProfileRole(
      final Long platformId, final Long profileId, final Long roleId) {
    log.debug("Read Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId);
    return platformProfileRoleRepository
        .findById(new PlatformProfileRoleId(platformId, profileId, roleId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "Platform Profile Role",
                    String.format("%s,%s,%s", platformId, profileId, roleId)));
  }

  public PlatformProfileRoleEntity readPlatformProfileRole(
      final Long platformId, final String email) {
    log.debug("Read Platform Profile Role: [{}], [{}]", platformId, email);
    return platformProfileRoleRepository.findByPlatformIdAndProfileEmail(platformId, email).stream()
        .findFirst()
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "Platform Profile Role", String.format("%s,%s", platformId, email)));
  }

  // UPDATE
  // not provided

  // DELETE
  public void deletePlatformProfileRole(
      final Long platformId, final Long profileId, final Long roleId) {
    log.info("Delete Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId);
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        readPlatformProfileRole(platformId, profileId, roleId);
    platformProfileRoleRepository.delete(platformProfileRoleEntity);
  }
}
