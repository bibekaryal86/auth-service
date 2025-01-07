package auth.service.app.repository;

import auth.service.app.model.entity.AuditProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditProfileRepository extends JpaRepository<AuditProfileEntity, Long> {}
