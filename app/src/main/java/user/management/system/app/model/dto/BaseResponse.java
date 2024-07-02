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
  private Integer createdRowsId;
  private Integer updatedRowsCount;
  private Integer deletedRowsCount;
  private Integer restoredRowsCount;
  private String error;
}
