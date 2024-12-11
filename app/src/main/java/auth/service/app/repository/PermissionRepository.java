package auth.service.app.repository;

import auth.service.app.model.entity.PermissionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
  Optional<PermissionEntity> findByPermissionName(final String permissionName);
}