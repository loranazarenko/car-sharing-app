package carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import carsharingapp.dto.PaymentRequestDto;
import carsharingapp.dto.PaymentResponseDto;
import carsharingapp.exception.CustomerAccessException;
import carsharingapp.mapper.PaymentMapper;
import carsharingapp.model.Car;
import carsharingapp.model.Payment;
import carsharingapp.model.Rental;
import carsharingapp.model.User;
import carsharingapp.repository.PaymentRepository;
import carsharingapp.repository.RentalRepository;
import carsharingapp.service.impl.PaymentServiceImpl;
import carsharingapp.service.impl.TelegramNotificationService;
import carsharingapp.util.TestUtils;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private UserService userService;
    @Mock
    private TelegramNotificationService notificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;
    private Car car;
    private User user;
    private Rental rental;
    private Authentication authentication;
    private Payment payment;
    private PaymentResponseDto expected;

    @BeforeEach
    void setUp() {
        car = TestUtils.createCar();
        user = TestUtils.createUser(TestUtils.createRole());
        rental = TestUtils.createRental(user, car);
        payment = TestUtils.createPayment(rental);
        expected = TestUtils.convertPaymentToPaymentResponseDto(payment);
    }

    @Test
    @DisplayName("Verify createPayment() method. "
            + "Should create new payment and return PaymentResponseDto"
    )
    void createPayment_ValidPaymentRequestDto_ReturnPaymentResponseDto() {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(rental.getId());

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("mockSessionId");
        when(mockSession.getUrl()).thenReturn("http://mock.url");

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(userService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.findByRentalId(rental.getId())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        doNothing().when(notificationService).sendNotification(any(), anyString());
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expected);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            // When

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");
            PaymentResponseDto actual = paymentService.createPayment(requestDto, uriBuilder);

            // Then
            assertEquals(expected, actual);
            verify(rentalRepository, times(1))
                    .findById(rental.getId());
            verify(userService, times(2))
                    .getCurrentUser();
            verify(paymentRepository, times(1))
                    .findByRentalId(rental.getId());
            verify(paymentRepository, times(1))
                    .save(any(Payment.class));
            verify(notificationService, times(1))
                    .sendNotification(any(), anyString());
            verify(paymentMapper, times(1)).toDto(any(Payment.class));

            verifyNoMoreInteractions(rentalRepository, userDetailsService,
                    paymentRepository, notificationService, paymentMapper
            );
        }
    }

    @Test
    @DisplayName(
            "Verify getAllPaymentsByUserId() method. Should return list of all user's payments"
    )
    void getAllPaymentsByUserId_ValidUserId_ReturnAllUserPayments() {
        //Given
        List<Payment> payments = List.of(payment);
        when(userService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.findAllByUserId(user.getId())).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(expected);
        List<PaymentResponseDto> expectedList = List.of(expected);
        //When
        List<PaymentResponseDto> actualList =
                paymentService.getPaymentsByUserId(user.getId());

        //Then
        assertEquals(expectedList, actualList);
        verify(userService, times(1))
                .getCurrentUser();
        verify(paymentRepository, times(1)).findAllByUserId(user.getId());
        verify(paymentMapper, times(1)).toDto(payment);

        verifyNoMoreInteractions(userDetailsService, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify getAllPaymentsByUserId() method. Should throw an Exception"
    )
    void getAllPaymentsByUserId_InvalidUserId_ThrowException() {
        //Given
        Long invalidUserId = 10L;
        when(userService.getCurrentUser()).thenReturn(user);

        //When
        CustomerAccessException exception = assertThrows(CustomerAccessException.class,
                () -> paymentService.getPaymentsByUserId(invalidUserId)
        );

        //Then
        assertEquals("Access denied for user ID: " + invalidUserId,
                exception.getMessage());
        assertEquals(CustomerAccessException.class, exception.getClass());
    }

    @Test
    @DisplayName("Verify verifySuccessfulPayment() method.")
    void verifySuccessfulPayment_ValidPaymentId_Success() {
        //Given
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(userService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.save(payment)).thenReturn(payment);

        //When
        paymentService.verifySuccessfulPayment(payment.getId());

        //Then
        verify(paymentRepository, times(1)).findById(payment.getId());
        verify(userService, times(1))
                .getCurrentUser();
        verify(paymentRepository, times(1)).save(payment);

        verifyNoMoreInteractions(paymentRepository, userDetailsService);
    }

    @Test
    @DisplayName("Verify verifySuccessfulPayment() method. Should throw an Exception.")
    void verifySuccessfulPayment_InvalidPaymentId_ThrowException() {
        //Given
        Long invalidPaymentId = 10L;

        when(paymentRepository.findById(invalidPaymentId)).thenThrow(
                new EntityNotFoundException("Payment with id " + invalidPaymentId + " not found")
        );

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.verifySuccessfulPayment(invalidPaymentId)
        );

        //Then
        assertEquals("Payment with id " + invalidPaymentId + " not found",
                exception.getMessage());
        assertEquals(EntityNotFoundException.class, exception.getClass());
    }
}
