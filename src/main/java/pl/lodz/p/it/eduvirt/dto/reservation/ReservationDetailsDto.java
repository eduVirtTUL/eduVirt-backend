package pl.lodz.p.it.eduvirt.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailsDto {

    private UUID resourceGroupId;
    private UUID teamId;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean automaticStartup;
}
