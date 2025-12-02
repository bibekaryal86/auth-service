package auth.service.app.repository;

import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformProfileRoleRepository
    extends JpaRepository<PlatformProfileRoleEntity, PlatformProfileRoleId>,
        JpaSpecificationExecutor<PlatformProfileRoleEntity> {
  @Query(
      "SELECT p FROM PlatformProfileRoleEntity p WHERE p.platform.id = :platformId AND p.profile.email = :email")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileEmail(
      @Param("platformId") final Long platformId, @Param("email") final String email);

  @Query(
      "SELECT p FROM PlatformProfileRoleEntity p WHERE p.id.platformId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformProfileRoleEntity> findByPlatformIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  @Query(
      "SELECT p FROM PlatformProfileRoleEntity p WHERE p.id.profileId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformProfileRoleEntity> findByProfileIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  @Query(
      "SELECT p FROM PlatformProfileRoleEntity p WHERE p.id.roleId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformProfileRoleEntity> findByRoleIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  void deleteByIdPlatformIdIn(final List<Long> platformIds);

  void deleteByIdProfileIdIn(final List<Long> profileIds);

  void deleteByIdRoleIdIn(final List<Long> roleIds);
}
