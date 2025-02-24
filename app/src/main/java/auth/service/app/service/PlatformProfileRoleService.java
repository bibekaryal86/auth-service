package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.util.JpaDataUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformProfileRoleService {

  private final PlatformProfileRoleRepository platformProfileRoleRepository;
  private final CircularDependencyService circularDependencyService;

  // ASSIGN
  public PlatformProfileRoleEntity assignPlatformProfileRole(
      final PlatformProfileRoleRequest platformProfileRoleRequest) {
    log.debug("Create Platform Profile Role: [{}]", platformProfileRoleRequest);
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    final PlatformEntity platformEntity =
        circularDependencyService.readPlatform(platformProfileRoleRequest.getPlatformId(), false);
    final ProfileEntity profileEntity =
        circularDependencyService.readProfile(platformProfileRoleRequest.getProfileId(), false);
    final RoleEntity roleEntity =
        circularDependencyService.readRole(platformProfileRoleRequest.getRoleId(), false);
    platformProfileRoleEntity.setPlatform(platformEntity);
    platformProfileRoleEntity.setProfile(profileEntity);
    platformProfileRoleEntity.setRole(roleEntity);
    platformProfileRoleEntity.setAssignedDate(LocalDateTime.now());
    platformProfileRoleEntity.setUnassignedDate(null);
    platformProfileRoleEntity.setId(
        new PlatformProfileRoleId(
            platformEntity.getId(), profileEntity.getId(), roleEntity.getId()));
    return platformProfileRoleRepository.save(platformProfileRoleEntity);
  }

  // UNASSIGN
  public PlatformProfileRoleEntity unassignPlatformProfileRole(
      final Long platformId, final Long profileId, final Long roleId) {
    log.info("Delete Platform Profile Role: [{}], [{}], [{}]", platformId, profileId, roleId);
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        readPlatformProfileRole(platformId, profileId, roleId);
    platformProfileRoleEntity.setUnassignedDate(LocalDateTime.now());
    return platformProfileRoleRepository.save(platformProfileRoleEntity);
  }

  // READ
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

  public Page<PlatformProfileRoleEntity> readPlatformProfileRolesByPlatformId(
      final Long platformId, final RequestMetadata requestMetadata) {
    log.debug(
        "Read Platform Profile Roles By Platform Id: [{}] | [{}]", platformId, requestMetadata);

    final Pageable pageable =
        PageRequest.of(
            Math.max(requestMetadata.getPageNumber(), 0),
            (requestMetadata.getPerPage() < 10 || requestMetadata.getPerPage() > 1000)
                ? 100
                : requestMetadata.getPerPage(),
            Sort.by(
                Sort.Order.asc("platform.platformName"),
                Sort.Order.asc("profile.email"),
                Sort.Order.asc("role.roleName")));

    if (JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata)) {
      return platformProfileRoleRepository.findByPlatformId(platformId, pageable);
    }
    return platformProfileRoleRepository.findByPlatformIdNoDeleted(platformId, pageable);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByPlatformIds(
      final List<Long> platformIds) {
    log.debug("Read Platform Profile Roles By Platform Ids: [{}]", platformIds);
    return platformProfileRoleRepository.findByPlatformIds(platformIds);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByProfileIds(
      final List<Long> profileIds) {
    log.debug("Read Platform Profile Roles By Profile Ids: [{}]", profileIds);
    return platformProfileRoleRepository.findByProfileIds(profileIds);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByRoleIds(
      final List<Long> roleIds) {
    log.debug("Read Platform Profile Roles By Role Ids: [{}]", roleIds);
    return platformProfileRoleRepository.findByRoleIds(roleIds);
  }

  public void hardDeletePlatformProfileRolesByPlatformIds(final List<Long> platformIds) {
    log.info("Hard Delete Platform Profile Roles By Platform Ids: [{}]", platformIds);
    platformProfileRoleRepository.deleteByPlatformIds(platformIds);
  }

  public void hardDeletePlatformProfileRolesByProfileIds(final List<Long> profileIds) {
    log.info("Hard Delete Platform Profile Roles By Profile Ids: [{}]", profileIds);
    platformProfileRoleRepository.deleteByProfileIds(profileIds);
  }

  public void hardDeletePlatformProfileRolesByRoleIds(final List<Long> roleIds) {
    log.info("Hard Delete Platform Profile Roles By Role Ids: [{}]", roleIds);
    platformProfileRoleRepository.deleteByRoleIds(roleIds);
  }

  private PlatformProfileRoleEntity readPlatformProfileRole(
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
}
