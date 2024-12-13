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
      "SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId = :platformId AND ppre.id.profileId = :profileId")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileId(
      @Param("platformId") final Long platformId, @Param("profileId") final Long profileId);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId = :platformId AND ppre.id.profileId IN (:profileIds)")
  List<PlatformProfileRoleEntity> findByPlatformIdAndProfileIds(
      @Param("platformId") final Long platformId, @Param("profileIds") final List<Long> profileIds);

  @Query(
          "SELECT ppre FROM PlatformProfileRoleEntity ppre WHERE ppre.id.platformId = :platformId")
  List<PlatformProfileRoleEntity> findByPlatformId(
          @Param("platformId") final Long platformId);
}
