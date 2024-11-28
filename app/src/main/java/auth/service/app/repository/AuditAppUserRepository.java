package auth.service.app.repository;

import auth.service.app.model.entity.AuditAppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditAppUserRepository extends JpaRepository<AuditAppUserEntity, Integer> {}
