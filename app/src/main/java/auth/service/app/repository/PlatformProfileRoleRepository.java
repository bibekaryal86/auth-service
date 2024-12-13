package auth.service.app.repository;

import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformProfileRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformProfileRoleRepository
    extends JpaRepository<PlatformProfileRoleEntity, PlatformProfileRoleId> {
  @Query(
      "SELECT p FROM PlatformProfileRoleEntity p WHERE p.platform.id = :platformId AND p.profile.email = :email")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileEmail(
      @Param("platformId") final Long platformId, @Param("email") final String email);

  @Query(
          "SELECT ppre FROM PlatformProfileRoleEntity prpe " +
                  "WHERE ppre.id.profileId = :profileId " +
                  "ORDER BY ppre.platform.platformName, prpe.profile.email, prpe.role.roleName")
    List<PlatformProfileRoleEntity> findByProfileId(@Param("profileId") final Long profileId);

  @Query(
          "SELECT ppre FROM PlatformProfileRoleEntity prpe " +
                  "WHERE ppre.id.profileId in (:profileIds) " +
                  "ORDER BY ppre.platform.platformName, prpe.profile.email, prpe.role.roleName")
  List<PlatformProfileRoleEntity> findByProfileIds(@Param("profileIds") final List<Long> profileIds);
}
