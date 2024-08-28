package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppUserAddressEntity;

@Repository
public interface AppUserAddressRepository extends JpaRepository<AppUserAddressEntity, Integer> {}
