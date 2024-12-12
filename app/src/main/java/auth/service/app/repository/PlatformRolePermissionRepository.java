package auth.service.app.repository;

import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRolePermissionRepository
    extends JpaRepository<PlatformRolePermissionEntity, PlatformRolePermissionId> {
  @Query(
      "SELECT prpe FROM PlatformRolePermissionEntity prpe WHERE prpe.id.platformId = :platformId AND prpe.id.roleId = :roleId")
  List<PlatformRolePermissionEntity> findByPlatformIdAndRoleId(
      @Param("platformId") final Long platformId, @Param("roleId") final Long roleId);

  @Query(
      "SELECT prpe FROM PlatformRolePermissionEntity prpe WHERE prpe.id.platformId = :platformId AND prpe.id.roleId IN (:roleIds)")
  List<PlatformRolePermissionEntity> findByPlatformIdAndRoleIds(
      @Param("platformId") final Long platformId, @Param("roleIds") final List<Long> roleIds);
}
