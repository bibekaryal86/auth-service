package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AddressTypeRequest;
import auth.service.app.model.entity.AddressTypeEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AddressTypeServiceTest extends BaseTest {

  @Autowired private AddressTypeService addressTypeService;

  @Test
  void testReadAddressTypes() {
    List<AddressTypeEntity> addressTypeEntities = addressTypeService.readAddressTypes();
    assertEquals(6, addressTypeEntities.size());
    // check sorted by name
    assertEquals("Billing", addressTypeEntities.getFirst().getTypeName());
    assertEquals("Shipping-1", addressTypeEntities.getLast().getTypeName());
  }

  void testAddressTypeService_CRUD() {
    String newName = "AddressTypeNewName";
    String newDesc = "AddressTypeNewDesc";
    String updatedDesc = "AddressTypeUpdatedDesc";
    AddressTypeRequest request = new AddressTypeRequest(newName, newDesc);

    // Create
    Long id = assertCreate(request);

    // Update
    request = new AddressTypeRequest(newName, updatedDesc);
    assertUpdate(id, request);

    // Read
    assertRead(id, newName, updatedDesc);

    // Soft delete
    assertDeleteSoft(id);

    // Restore
    assertRestoreSoftDeleted(id);

    // deleteHard
    assertDeleteHard(id);
  }

  private Long assertCreate(AddressTypeRequest request) {
    AddressTypeEntity entity = addressTypeService.createAddressType(request);
    assertNotNull(entity);
    assertNotNull(entity.getId());
    return entity.getId();
  }

  private void assertUpdate(Long id, AddressTypeRequest request) {
    AddressTypeEntity entity = addressTypeService.updateAddressType(id, request);
    assertNotNull(entity);
  }

  private void assertRead(Long id, String expectedName, String expectedDescription) {
    AddressTypeEntity entity = addressTypeService.readAddressType(id);
    assertNotNull(entity);
    assertEquals(expectedName, entity.getTypeName());
    assertEquals(expectedDescription, entity.getTypeDesc());
    assertNull(entity.getDeletedDate());
  }

  private void assertDeleteSoft(Long id) {
    AddressTypeEntity entity = addressTypeService.softDeleteAddressType(id);
    assertNotNull(entity);
    assertNotNull(entity.getDeletedDate());
  }

  private void assertRestoreSoftDeleted(Long id) {
    AddressTypeEntity entity = addressTypeService.restoreSoftDeletedAddressType(id);
    assertNotNull(entity);
    assertNull(entity.getDeletedDate());
  }

  private void assertDeleteHard(Long id) {
    addressTypeService.hardDeleteAddressType(id);
    ElementNotFoundException exception =
        assertThrows(
            ElementNotFoundException.class,
            () -> addressTypeService.readAddressType(id),
            "Expected ElementNotFoundException after hard delete...");
    assertEquals(
        String.format("Address Type Not Found for [%s]", id),
        exception.getMessage(),
        "Exception message mismatch...");
  }
}
