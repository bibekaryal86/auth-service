package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.UsersAuditDto;

public interface UsersAuditRepository extends JpaRepository<UsersAuditDto, Integer> {}
