package auth.service.app.repository;

import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRolePermissionRepository
    extends JpaRepository<PlatformRolePermissionEntity, PlatformRolePermissionId> {
  List<PlatformRolePermissionEntity> findByIdPlatformIdAndIdRoleIdOrderByPermissionNameAsc(
      @Param("platformId") final long platformId, @Param("roleId") final long roleId);

  List<PlatformRolePermissionEntity> findByIdPlatformIdAndIdRoleIdInOrderByPermissionNameAsc(
      @Param("platformId") final long platformId, @Param("roleIds") final List<Integer> roleIds);
}
