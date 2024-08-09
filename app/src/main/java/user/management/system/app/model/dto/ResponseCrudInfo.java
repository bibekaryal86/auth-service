package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCrudInfo {
  private int createdRowsId;
  private int updatedRowsCount;
  private int deletedRowsCount;
  private int restoredRowsCount;
}
