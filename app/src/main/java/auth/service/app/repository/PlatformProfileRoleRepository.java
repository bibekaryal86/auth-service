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
      "SELECT ppre FROM PlatformProfileRoleEntity ppre JOIN ppre.role r WHERE ppre.id.platformId = :platformId AND ppre.id.profileId = :profileId ORDER BY r.roleName ASC")
  List<PlatformProfileRoleEntity> findByIdPlatformIdAndProfileIdOrderByRoleNameAsc(
      @Param("platformId") final long platformId, @Param("profileId") final long profileId);

  @Query(
      "SELECT ppre FROM PlatformProfileRoleEntity ppre JOIN ppre.role r WHERE ppre.id.platformId = :platformId AND ppre.id.profileId in (:profileIds) ORDER BY r.roleName ASC")
  List<PlatformProfileRoleEntity> findByIdPlatformIdAndProfileIdInOrderByRoleNameAsc(
      @Param("platformId") final long platformId, @Param("profileIds") List<Long> profileIds);
}
