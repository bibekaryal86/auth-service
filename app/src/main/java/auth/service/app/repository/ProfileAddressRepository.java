package auth.service.app.repository;

import auth.service.app.model.entity.ProfileAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileAddressRepository extends JpaRepository<ProfileAddressEntity, Long> {}
