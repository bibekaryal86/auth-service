package auth.service.app.repository;

import auth.service.app.model.entity.AuditProfileEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditProfileRepository extends JpaRepository<AuditProfileEntity, Long> {
  List<AuditProfileEntity> findByProfileId(Long profileId);

  @Modifying
  @Query("DELETE FROM AuditProfileEntity a WHERE a.createdAt < :paramDate")
  int deleteByCreatedAtBefore(@Param("paramDate") LocalDateTime paramDate);
}
