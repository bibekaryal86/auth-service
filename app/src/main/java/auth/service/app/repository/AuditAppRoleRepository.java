package auth.service.app.repository;

import auth.service.app.model.entity.AuditAppRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditAppRoleRepository extends JpaRepository<AuditAppRoleEntity, Integer> {}
