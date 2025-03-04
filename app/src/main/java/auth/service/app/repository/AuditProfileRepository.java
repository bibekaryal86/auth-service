package auth.service.app.repository;

import auth.service.app.model.entity.AuditProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditProfileRepository
    extends JpaRepository<AuditProfileEntity, Long>, JpaSpecificationExecutor<AuditProfileEntity> {
  Page<AuditProfileEntity> findByProfileId(Long profileId, Pageable pageable);
}
