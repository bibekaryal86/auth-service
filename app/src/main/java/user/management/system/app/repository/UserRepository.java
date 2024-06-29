package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {}
