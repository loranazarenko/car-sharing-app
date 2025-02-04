package carsharingapp.dto;

import carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class CarRentedDto {
    private Long id;
    private String brand;
    private String model;
    private Car.Type type;
    private BigDecimal dailyFee;
}
