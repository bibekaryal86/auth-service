package auth.service.app.repository;

import auth.service.app.model.entity.AppRolePermissionEntity;
import auth.service.app.model.entity.AppRolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRolePermissionRepository
    extends JpaRepository<AppRolePermissionEntity, AppRolePermissionId> {
  List<AppRolePermissionEntity> findByAppRoleIdOrderByAppPermissionNameAsc(
      @Param("roleId") final int roleId);

  List<AppRolePermissionEntity> findByAppRoleIdInOrderByAppPermissionNameAsc(
      @Param("roleIds") final List<Integer> roleIds);

  List<AppRolePermissionEntity> findByAppPermissionAppIdAndAppRoleIdInOrderByAppPermissionNameAsc(
      @Param("appId") final String appId, @Param("roleIds") final List<Integer> roleIds);
}
