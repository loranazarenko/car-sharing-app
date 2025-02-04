package carsharingapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class RentalRequestDto {
    @NotNull
    private LocalDate rentalDate;
    @NotNull
    private LocalDate returnDate;
    @Positive
    private Long carId;
    @NotNull
    private Long userId;
}
