package carsharingapp.dto;

import carsharingapp.model.Payment;
import java.math.BigDecimal;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class PaymentResponseDto {
    private Long id;
    private Payment.Status status;
    private Payment.Type type;
    private Long rentalId;
    private URL sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
