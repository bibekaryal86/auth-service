package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformProfileRoleRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PlatformProfileRoleRepository;
import auth.service.app.util.JpaSpecificationUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    log.debug(
        "Assign Platform Profile Role: PlatformProfileRoleRequest=[{}]",
        platformProfileRoleRequest);
    PlatformProfileRoleEntity platformProfileRoleEntity = new PlatformProfileRoleEntity();
    final PlatformEntity platformEntity =
        circularDependencyService.readPlatform(
            platformProfileRoleRequest.getPlatformId(), Boolean.FALSE);
    final ProfileEntity profileEntity =
        circularDependencyService.readProfile(
            platformProfileRoleRequest.getProfileId(), Boolean.FALSE);
    final RoleEntity roleEntity =
        circularDependencyService.readRole(platformProfileRoleRequest.getRoleId(), Boolean.FALSE);

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
    log.info(
        "Unassign Platform Profile Role: PlatformId=[{}], ProfileId=[{}], RoleId=[{}]",
        platformId,
        profileId,
        roleId);
    final PlatformProfileRoleEntity platformProfileRoleEntity =
        readPlatformProfileRole(platformId, profileId, roleId);
    platformProfileRoleEntity.setUnassignedDate(LocalDateTime.now());
    return platformProfileRoleRepository.save(platformProfileRoleEntity);
  }

  // READ
  private PlatformProfileRoleEntity readPlatformProfileRole(
      final Long platformId, final Long profileId, final Long roleId) {
    log.debug(
        "Read Platform Profile Role: PlatformId=[{}], ProfileId=[{}], RoleId=[{}]",
        platformId,
        profileId,
        roleId);
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
    log.debug("Read Platform Profile Role: PlatformId=[{}], Email=[{}]", platformId, email);

    List<PlatformProfileRoleEntity> pprList =
        platformProfileRoleRepository.findByPlatformIdAndProfileEmail(platformId, email);

    if (pprList.isEmpty()) {
      throw new ElementNotFoundException(
          "Platform Profile Role", String.format("%s,%s", platformId, email));
    }

    List<PlatformProfileRoleEntity> pprListActive =
        pprList.stream()
            .filter(
                ppr ->
                    ppr.getPlatform().getDeletedDate() == null
                        && ppr.getProfile().getDeletedDate() == null
                        && ppr.getRole().getDeletedDate() == null)
            .toList();

    if (pprListActive.isEmpty()) {
      throw new ElementNotActiveException(
          "Platform Profile Role", String.format("%s,%s", platformId, email));
    }

    return pprListActive.getFirst();
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRoles(
      final Long platformId, final Long roleId, final boolean isIncludeDeleted) {
    log.debug(
        "Read Platform Profile Role: PlatformId=[{}], ProfileId=[{}], IsIncludeDeleted=[{}]",
        platformId,
        roleId,
        isIncludeDeleted);
    return platformProfileRoleRepository.findAll(
        JpaSpecificationUtils.pprFilters(platformId, roleId, isIncludeDeleted));
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByPlatformIds(
      final List<Long> platformIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Profile Roles By Platform Ids: PlatformIds=[{}], IsIncludeUnassigned=[{}]",
        platformIds,
        isIncludeUnassigned);
    return platformProfileRoleRepository.findByPlatformIds(platformIds, isIncludeUnassigned);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByProfileIds(
      final List<Long> profileIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Profile Roles By Profile Ids: ProfileIds=[{}], IsIncludeUnassigned=[{}]",
        profileIds,
        isIncludeUnassigned);
    return platformProfileRoleRepository.findByProfileIds(profileIds, isIncludeUnassigned);
  }

  public List<PlatformProfileRoleEntity> readPlatformProfileRolesByRoleIds(
      final List<Long> roleIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Profile Roles By Role Ids: RoleIds=[{}], IsIncludeUnassigned=[{}]",
        roleIds,
        isIncludeUnassigned);
    return platformProfileRoleRepository.findByRoleIds(roleIds, isIncludeUnassigned);
  }

  public void hardDeletePlatformProfileRolesByPlatformIds(final List<Long> platformIds) {
    log.info("Hard Delete Platform Profile Roles By Platform Ids: PlatformIds=[{}]", platformIds);
    platformProfileRoleRepository.deleteByIdPlatformIdIn(platformIds);
  }

  public void hardDeletePlatformProfileRolesByProfileIds(final List<Long> profileIds) {
    log.info("Hard Delete Platform Profile Roles By Profile Ids: ProfileIds=[{}]", profileIds);
    platformProfileRoleRepository.deleteByIdProfileIdIn(profileIds);
  }

  public void hardDeletePlatformProfileRolesByRoleIds(final List<Long> roleIds) {
    log.info("Hard Delete Platform Profile Roles By Role Ids: RoleIds=[{}]", roleIds);
    platformProfileRoleRepository.deleteByIdRoleIdIn(roleIds);
  }

  public void hardDeletePlatformProfileRole(
      final Long platformId, final Long profileId, final Long roleId) {
    log.info(
        "Hard Delete Platform Profile Role: PlatformId=[{}], ProfileId=[{}], RoleId=[{}]",
        platformId,
        profileId,
        roleId);
    // will throw exception if not found
    readPlatformProfileRole(platformId, profileId, roleId);
    platformProfileRoleRepository.deleteById(
        new PlatformProfileRoleId(platformId, profileId, roleId));
  }
}
