package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.RolesAuditDto;

public interface RolesAuditRepository extends JpaRepository<RolesAuditDto, Integer> {}
