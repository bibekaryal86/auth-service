package auth.service.app.repository;

import auth.service.app.model.entity.AuditPermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditPermissionRepository
    extends JpaRepository<AuditPermissionEntity, Long>,
        JpaSpecificationExecutor<AuditPermissionEntity> {
  Page<AuditPermissionEntity> findByPermissionId(Long permissionId, Pageable pageable);
}
