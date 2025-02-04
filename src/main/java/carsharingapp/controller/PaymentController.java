package carsharingapp.controller;

import carsharingapp.dto.PaymentRequestDto;
import carsharingapp.dto.PaymentResponseDto;
import carsharingapp.service.impl.PaymentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment management", description = "Endpoints for managing payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentServiceImpl paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Create a new payment session",
            description = "Create a new payment session"
    )
    public PaymentResponseDto createPayment(
            @RequestBody @Valid PaymentRequestDto requestDto, UriComponentsBuilder uriBuilder
    ) {
        return paymentService.createPayment(requestDto, uriBuilder);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's payments",
            description = "Get user's payments by user Id"
    )
    public List<PaymentResponseDto> getPaymentsByUserId(
            @PathVariable Long userId
    ) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @GetMapping("/success/{paymentId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Check successful Stripe payments",
            description = "Check successful Stripe payments")
    public String paymentSuccessRedirect(
            @PathVariable Long paymentId
    ) {
        paymentService.verifySuccessfulPayment(paymentId);
        return "Success Stripe payments";
    }

    @GetMapping("/cancel/{paymentId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Return payment paused message",
            description = "Return payment paused message")
    public String paymentCancelRedirect(
            @PathVariable Long paymentId
    ) {
        return "Payment with id " + paymentId
                + " was paused. ";
    }
}
