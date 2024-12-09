package auth.service.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_address")
@Getter
@Setter
@NoArgsConstructor
public class ProfileAddressEntity extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ProfileEntity profile;

  @ManyToOne
  @JoinColumn(name = "type_id", nullable = false)
  private AddressTypeEntity type;

  @Column(name = "street", nullable = false)
  private String street;

  @Column(name = "city", nullable = false)
  private String city;

  @Column(name = "state", nullable = false)
  private String state;

  @Column(name = "country", nullable = false)
  private String country;

  @Column(name = "postal_code", nullable = false)
  private String postalCode;
}
