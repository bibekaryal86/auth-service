package auth.service.app.repository;

import auth.service.app.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository
    extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {}
