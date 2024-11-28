package auth.service.app.repository;

import auth.service.app.model.entity.AuditAppsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditAppsRepository extends JpaRepository<AuditAppsEntity, Integer> {}
