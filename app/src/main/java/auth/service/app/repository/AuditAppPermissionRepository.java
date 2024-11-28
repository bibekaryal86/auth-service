package auth.service.app.repository;

import auth.service.app.model.entity.AuditAppPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditAppPermissionRepository
    extends JpaRepository<AuditAppPermissionEntity, Integer> {}
