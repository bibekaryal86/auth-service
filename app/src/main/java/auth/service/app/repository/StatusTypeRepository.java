package auth.service.app.repository;

import auth.service.app.model.entity.StatusTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusTypeEntity, Long> {}
