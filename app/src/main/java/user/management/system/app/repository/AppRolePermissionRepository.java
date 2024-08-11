package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppRolePermissionId;

@Repository
public interface AppRolePermissionRepository
    extends JpaRepository<AppRolePermissionEntity, AppRolePermissionId> {}
