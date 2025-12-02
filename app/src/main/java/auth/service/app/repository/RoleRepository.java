package auth.service.app.repository;

import auth.service.app.model.entity.RoleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

  @Query("SELECT p FROM RoleEntity p WHERE :isIncludeDeleted = true OR p.deletedDate IS NULL")
  List<RoleEntity> findAllRoles(@Param("isIncludeDeleted") final boolean isIncludeDeleted);
}
