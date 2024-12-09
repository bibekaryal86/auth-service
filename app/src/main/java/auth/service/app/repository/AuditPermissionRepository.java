package auth.service.app.repository;

import auth.service.app.model.entity.AuditPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditPermissionRepository extends JpaRepository<AuditPermissionEntity, Long> {}
