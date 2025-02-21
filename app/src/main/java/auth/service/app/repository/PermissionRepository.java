package auth.service.app.repository;

import auth.service.app.model.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PermissionRepository
    extends JpaRepository<PermissionEntity, Long>, JpaSpecificationExecutor<PermissionEntity> {
  @Modifying
  @Transactional
  @Query("DELETE FROM PermissionEntity pe WHERE pe.role.id = :roleId")
  void deleteByRoleId(@Param("roleId") Long roleId);
}
