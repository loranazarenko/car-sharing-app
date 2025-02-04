package carsharingapp.service;

import carsharingapp.dto.PaymentRequestDto;
import carsharingapp.dto.PaymentResponseDto;
import java.util.List;
import org.springframework.web.util.UriComponentsBuilder;

public interface PaymentService {
    PaymentResponseDto createPayment(PaymentRequestDto requestDto, UriComponentsBuilder uriBuilder);

    List<PaymentResponseDto> getPaymentsByUserId(Long userId);

    void verifySuccessfulPayment(Long paymentId);
}
