package carsharingapp.dto;

import carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CarResponseDto {
    private Long id;
    private String brand;
    private String model;
    private Car.Type type;
    private int inventory;
    private BigDecimal dailyFee;
}
