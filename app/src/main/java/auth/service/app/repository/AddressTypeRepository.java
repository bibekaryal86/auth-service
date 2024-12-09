package auth.service.app.repository;

import auth.service.app.model.entity.AddressTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressTypeRepository extends JpaRepository<AddressTypeEntity, Long> {
  Optional<AddressTypeEntity> findByTypeName(final String typeName);
}
