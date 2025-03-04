package auth.service.app.repository;

import auth.service.app.model.entity.AuditRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRoleRepository
    extends JpaRepository<AuditRoleEntity, Long>, JpaSpecificationExecutor<AuditRoleEntity> {
  Page<AuditRoleEntity> findByRoleId(Long roleId, Pageable pageable);
}
