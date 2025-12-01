package auth.service.app.repository;

import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRolePermissionRepository
    extends JpaRepository<PlatformRolePermissionEntity, PlatformRolePermissionId> {}
