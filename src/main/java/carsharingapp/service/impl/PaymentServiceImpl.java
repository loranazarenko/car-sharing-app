package carsharingapp.service.impl;

import carsharingapp.dto.PaymentRequestDto;
import carsharingapp.dto.PaymentResponseDto;
import carsharingapp.exception.CreateSessionException;
import carsharingapp.exception.CustomerAccessException;
import carsharingapp.exception.PaidPaymentException;
import carsharingapp.mapper.PaymentMapper;
import carsharingapp.model.Payment;
import carsharingapp.model.Rental;
import carsharingapp.model.Role;
import carsharingapp.model.User;
import carsharingapp.repository.PaymentRepository;
import carsharingapp.repository.RentalRepository;
import carsharingapp.security.JwtUtil;
import carsharingapp.service.PaymentService;
import carsharingapp.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private static final String CURRENCY = "usd";

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private final TelegramNotificationService notificationService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto requestDto,
                                            UriComponentsBuilder uriBuilder) {
        Rental rental = validateAndRetrieveRental(requestDto.getRentalId());
        Payment payment = initializePayment(rental);

        BigDecimal amountToPay = calculateAmountToPay(rental);
        Session stripeSession = createStripeSession(payment, amountToPay, uriBuilder);

        updatePayment(payment, stripeSession, amountToPay);
        paymentRepository.save(payment);

        User user = userService.getCurrentUser();
        notificationService.sendNotification(user.getTelegramChatId(),
                "Rental with id " + rental.getId() + " was successfully paid");
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        validateUserAccess(userId);
        return paymentRepository.findAllByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void verifySuccessfulPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        validateUserAccess(payment.getRental().getUser().getId());
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
    }

    private Rental validateAndRetrieveRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find rental with ID: " + rentalId));
        checkUserIsRentalOwner(rental);
        checkRentalIsClosed(rental);
        checkPaymentIsDone(rental);
        return rental;
    }

    private void checkUserIsRentalOwner(Rental rental) {
        User user = userService.getCurrentUser();
        if (!rental.getUser().getId().equals(user.getId())) {
            throw new PaidPaymentException("You can't pay for other users' rentals");
        }
    }

    private static void checkRentalIsClosed(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            throw new RuntimeException("This rental hasn't been closed. "
                    + "Close the rental before payment.");
        }
    }

    private void checkPaymentIsDone(Rental rental) {
        Optional<Payment> existingPayment = paymentRepository.findByRentalId(rental.getId())
                .filter(payment -> payment.getStatus() == Payment.Status.PAID);
        if (existingPayment.isPresent()) {
            throw new PaidPaymentException("Rental with ID " + rental.getId()
                    + " has already been paid.");
        }
    }

    private BigDecimal calculateAmountToPay(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDuration = ChronoUnit.DAYS.between(rental.getRentalDate(),
                rental.getActualReturnDate());
        if (isRentalClosedLate(rental)) {
            long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(),
                    rental.getActualReturnDate());
            return dailyFee.multiply(BigDecimal.valueOf(rentalDuration))
                    .add(dailyFee.multiply(FINE_MULTIPLIER)
                            .multiply(BigDecimal.valueOf(overdueDays)));
        } else {
            return dailyFee.multiply(BigDecimal.valueOf(Math.max(1, rentalDuration)));
        }
    }

    private static boolean isRentalClosedLate(Rental rental) {
        return rental.getActualReturnDate().isAfter(rental.getReturnDate());
    }

    private Payment initializePayment(Rental rental) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.ZERO);
        return payment;
    }

    private Session createStripeSession(Payment payment, BigDecimal amountToPay,
                                        UriComponentsBuilder uriBuilder) {
        Stripe.apiKey = stripeSecretKey;

        String successUrl = buildUri(uriBuilder, "/payments/success/", payment.getId());
        String cancelUrl = buildUri(uriBuilder, "/payments/cancel/", payment.getId());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(createLineItemParams(amountToPay))
                .setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond())
                .build();

        try {
            return Session.create(params);
        } catch (StripeException e) {
            throw new CreateSessionException("Can't create payment session");
        }
    }

    private String buildUri(UriComponentsBuilder uriBuilder, String path, Long id) {
        return uriBuilder.path(path).path(String.valueOf(id)).toUriString();
    }

    private SessionCreateParams.LineItem createLineItemParams(BigDecimal amountToPay) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(CURRENCY)
                        .setUnitAmount(amountToPay.multiply(BigDecimal.valueOf(100)).longValue())
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Rental Payment")
                                .build())
                        .build())
                .build();
    }

    private void updatePayment(Payment payment, Session session, BigDecimal amountToPay) {
        payment.setStatus(Payment.Status.PENDING);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(toUrl(session.getUrl()));
        payment.setAmountToPay(amountToPay);
    }

    private URL toUrl(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: "
                    + urlString, e);
        }
    }

    private void validateUserAccess(Long userId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRoles().stream().anyMatch(role ->
                role.getName().equals(Role.RoleName.ROLE_CUSTOMER))
                && !currentUser.getId().equals(userId)) {
            throw new CustomerAccessException("Access denied for user ID: " + userId);
        }
    }

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with ID "
                        + paymentId + " not found"));
    }
}
