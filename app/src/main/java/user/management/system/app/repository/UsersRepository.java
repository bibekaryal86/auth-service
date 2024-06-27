package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.UsersDto;

public interface UsersRepository extends JpaRepository<UsersDto, Integer> {}
