package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entities.UserEntity;

public interface UsersRepository extends JpaRepository<UserEntity, Integer> {}
