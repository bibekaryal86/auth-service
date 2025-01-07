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
      "SELECT prpe FROM PlatformRolePermissionEntity prpe "
          + "WHERE prpe.id.roleId = :roleId "
          + "ORDER BY prpe.platform.platformName, prpe.role.roleName, prpe.permission.permissionName")
  List<PlatformRolePermissionEntity> findByRoleId(@Param("roleId") final Long roleId);

  @Query(
      "SELECT prpe FROM PlatformRolePermissionEntity prpe "
          + "WHERE prpe.id.roleId in (:roleIds) "
          + "ORDER BY prpe.platform.platformName, prpe.role.roleName, prpe.permission.permissionName")
  List<PlatformRolePermissionEntity> findByRoleIds(@Param("roleIds") final List<Long> roleIds);
}
