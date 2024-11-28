package auth.service.app.repository;

import auth.service.app.model.entity.AppUserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserAddressRepository extends JpaRepository<AppUserAddressEntity, Integer> {}
