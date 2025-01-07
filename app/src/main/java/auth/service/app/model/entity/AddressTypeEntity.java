package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address_type")
@Getter
@Setter
@NoArgsConstructor
public class AddressTypeEntity extends BaseEntity {
  @Column(name = "type_name", unique = true, nullable = false)
  private String typeName;

  @Column(name = "type_desc", nullable = false)
  private String typeDesc;
}
