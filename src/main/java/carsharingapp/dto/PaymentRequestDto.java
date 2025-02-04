package carsharingapp.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @Positive
    private Long rentalId;
}
