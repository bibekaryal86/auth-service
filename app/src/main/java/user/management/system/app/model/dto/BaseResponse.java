package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
  private int createdRowsId;
  private int updatedRowsCount;
  private int deletedRowsCount;
  private int restoredRowsCount;
  private String error;
}
