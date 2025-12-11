package auth.service.app.repository;

import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRolePermissionRepository
    extends JpaRepository<PlatformRolePermissionEntity, PlatformRolePermissionId>,
        JpaSpecificationExecutor<PlatformRolePermissionEntity> {

  @Query(
      "SELECT p FROM PlatformRolePermissionEntity p WHERE p.id.platformId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformRolePermissionEntity> findByPlatformIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  @Query(
      "SELECT p FROM PlatformRolePermissionEntity p WHERE p.id.roleId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformRolePermissionEntity> findByRoleIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  @Query(
      "SELECT p FROM PlatformRolePermissionEntity p WHERE p.id.permissionId IN (:ids) AND (:isIncludeUnassigned = true OR p.unassignedDate IS NULL)")
  List<PlatformRolePermissionEntity> findByPermissionIds(
      @Param("ids") final List<Long> ids,
      @Param("isIncludeUnassigned") final boolean isIncludeUnassigned);

  void deleteByIdPlatformIdIn(final List<Long> platformIds);

  void deleteByIdRoleIdIn(final List<Long> roleIds);

  void deleteByIdPermissionIdIn(final List<Long> permissionIds);
}
