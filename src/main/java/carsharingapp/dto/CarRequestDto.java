package carsharingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class CarRequestDto {
    @NotBlank
    private String brand;
    @NotBlank
    private String model;
    @NotBlank
    private String type;
    @Positive
    private int inventory;
    @Positive
    private BigDecimal dailyFee;
}
