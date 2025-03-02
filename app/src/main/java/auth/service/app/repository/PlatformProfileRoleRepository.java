package auth.service.app.repository;

import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PlatformProfileRoleRepository
    extends JpaRepository<PlatformProfileRoleEntity, PlatformProfileRoleId> {
  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.platform.id = :platformId AND ppre.profile.email = :email")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileEmail(
      @Param("platformId") final Long platformId, @Param("email") final String email);

  @Query("SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId = :platformId")
  Page<PlatformProfileRoleEntity> findByPlatformId(
      @Param("platformId") final Long platformId, final Pageable pageable);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId = :platformId "
          + "AND ppre.platform.deletedDate IS NULL AND ppre.profile.deletedDate IS NULL and ppre.role.deletedDate IS NULL")
  Page<PlatformProfileRoleEntity> findByPlatformIdNoDeleted(
      @Param("platformId") final Long platformId, final Pageable pageable);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre "
          + "WHERE ppre.id.platformId = :platformId "
          + "AND ppre.id.profileId = :profileId ")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileId(
      @Param("platformId") final Long platformId, @Param("profileId") final Long profileId);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre "
          + "WHERE ppre.id.platformId in (:platformIds) "
          + "ORDER BY ppre.platform.platformName, ppre.profile.lastName, ppre.role.roleName")
  List<PlatformProfileRoleEntity> findByPlatformIds(
      @Param("platformIds") final List<Long> platformIds);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre "
          + "WHERE ppre.id.profileId in (:profileIds) "
          + "ORDER BY ppre.platform.platformName, ppre.profile.lastName, ppre.role.roleName")
  List<PlatformProfileRoleEntity> findByProfileIds(
      @Param("profileIds") final List<Long> profileIds);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre "
          + "WHERE ppre.id.roleId in (:roleIds) "
          + "ORDER BY ppre.platform.platformName, ppre.profile.lastName, ppre.role.roleName")
  List<PlatformProfileRoleEntity> findByRoleIds(@Param("roleIds") final List<Long> roleIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId IN (:platformIds)")
  void deleteByPlatformIds(@Param("platformIds") List<Long> platformIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlatformProfileRoleEntity ppre WHERE ppre.id.profileId IN (:profileIds)")
  void deleteByProfileIds(@Param("profileIds") List<Long> profileIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlatformProfileRoleEntity ppre WHERE ppre.id.roleId IN (:roleIds)")
  void deleteByRoleIds(@Param("roleIds") List<Long> roleIds);
}
