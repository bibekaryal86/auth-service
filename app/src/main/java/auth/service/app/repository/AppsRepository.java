package auth.service.app.repository;

import auth.service.app.model.entity.AppsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppsRepository extends JpaRepository<AppsEntity, String> {}
