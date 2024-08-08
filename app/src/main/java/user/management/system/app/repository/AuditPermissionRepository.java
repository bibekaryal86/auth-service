package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AuditPermission;

@Repository
public interface AuditPermissionRepository extends JpaRepository<AuditPermission, Integer> {}
