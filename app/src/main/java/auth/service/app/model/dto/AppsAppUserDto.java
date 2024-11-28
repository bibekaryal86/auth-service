package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppsAppUserDto {
  private AppsDto app;
  private AppUserDto user;
  private LocalDateTime assignedDate;
}
