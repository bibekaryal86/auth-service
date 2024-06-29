package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.UsersAuditEntity;

public interface UsersAuditRepository extends CrudRepository<UsersAuditEntity, Integer> {}
