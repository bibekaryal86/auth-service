package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCrudInfo {
  private int insertedRowsCount;
  private int updatedRowsCount;
  private int deletedRowsCount;
  private int restoredRowsCount;
}
