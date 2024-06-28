package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entity.UsersAuditEntity;

public interface UsersAuditRepository extends JpaRepository<UsersAuditEntity, Integer> {}
