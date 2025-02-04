package carsharingapp.service;

import static carsharingapp.util.TestUtils.VALID_ACTUAL_RETURN_DATE;
import static carsharingapp.util.TestUtils.VALID_ID;
import static carsharingapp.util.TestUtils.createExpiredPayments;
import static carsharingapp.util.TestUtils.createOverdueRental;
import static carsharingapp.util.TestUtils.createValidCar;
import static carsharingapp.util.TestUtils.createValidRental;
import static carsharingapp.util.TestUtils.createValidRentalRequestDto;
import static carsharingapp.util.TestUtils.createValidRentalResponseDto;
import static carsharingapp.util.TestUtils.returnedRentalResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.exception.CarAvailableException;
import carsharingapp.exception.RentalException;
import carsharingapp.mapper.RentalMapper;
import carsharingapp.model.Car;
import carsharingapp.model.Payment;
import carsharingapp.model.Rental;
import carsharingapp.model.User;
import carsharingapp.repository.CarRepository;
import carsharingapp.repository.RentalRepository;
import carsharingapp.repository.UserRepository;
import carsharingapp.service.impl.RentalServiceImpl;
import carsharingapp.service.impl.TelegramNotificationService;
import carsharingapp.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private TelegramNotificationService notificationService;
    private final ObjectMapper notMockedMapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private RentalServiceImpl rentalService;

    {
        notMockedMapper.findAndRegisterModules();
    }

    private Car car;
    private User user;
    private Rental rental;
    private RentalResponseDto expected;

    @BeforeEach
    void setUp() {
        car = TestUtils.createCar();
        user = TestUtils.createUser(TestUtils.createRole());
        rental = TestUtils.createRental(user, car);
        expected = TestUtils.createRentalResponseDto(rental, car, user);
    }

    @Test
    @DisplayName("save() method works")
    public void save_WithValidCreateRentalRequestDto_ReturnRental() {
        //Given
        RentalRequestDto requestDto = createValidRentalRequestDto();
        Rental rental = createValidRental();
        RentalResponseDto expected = createValidRentalResponseDto();
        when(carRepository.findById(requestDto.getCarId()))
                .thenReturn(Optional.of(car));
        when(userRepository.getReferenceById(requestDto.getUserId()))
                 .thenReturn(user);
        lenient().doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());
        lenient().doNothing().when(rentalService).checkUserHasOpenRentals(any(User.class));
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(rental);
        when(rentalMapper.toDto(any(Rental.class)))
                .thenReturn(expected);
        //When
        RentalResponseDto actualResponse = rentalService.save(requestDto);
        //Then
        assertEquals(expected, actualResponse);
    }

    @Test
    @DisplayName("save() method throws RentalException when user has expired payments")
    public void save_WithExpiredPayments_ThrowsRentalException() {
        //Given
        RentalRequestDto requestDto = createValidRentalRequestDto();
        List<Payment> expiredPayments = List.of(createExpiredPayments());
        when(userRepository.getReferenceById(requestDto.getUserId()))
                .thenReturn(user);
        //Then
        Assertions.assertThrows(RentalException.class, () -> rentalService.save(requestDto));
    }

    @Test
    @DisplayName("save() method throws RentalException when there is no available car")
    public void save_WhenThereIsNoAvailableCar_ThrowsRentalException() {
        //Given
        Car notAvailableCar = createValidCar();
        notAvailableCar.setInventory(0);
        when(carRepository.findById(anyLong()))
                .thenReturn(Optional.of(notAvailableCar));
        RentalRequestDto requestDto = createValidRentalRequestDto();
        when(userRepository.getReferenceById(requestDto.getUserId()))
                .thenReturn(user);
        //Then
        Assertions.assertThrows(CarAvailableException.class, () -> rentalService.save(requestDto));
    }

    @Test
    @DisplayName("returnRental() method returns rental with specified id")
    public void returnRental_WithValidRentalId_ReturnRental() {
        //Given
        Rental rentalFromDb = createValidRental();
        Rental returnedRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DATE);
        RentalResponseDto expected = returnedRentalResponseDto();
        when(rentalRepository.findById(VALID_ID))
                .thenReturn(Optional.of(rentalFromDb));
        when(rentalMapper.toDto(any(Rental.class)))
                .thenReturn(expected);
        // When
        RentalResponseDto actual = rentalService.getRentalById(VALID_ID);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("checkOverdueRentals() method sends notifications about every overdue rental")
    public void checkOverdueRentals_WithOverdueRentals() {
        //Given
        List<Rental> overdueRentals = List.of(createOverdueRental(), createOverdueRental());
        Long chatId = 12345L;
        rentalService.setChatId(String.valueOf(chatId));
        when(rentalRepository.findAll()).thenReturn(overdueRentals);
        lenient().doNothing().when(notificationService).sendNotification(anyLong(), anyString());
        // When
        rentalService.checkOverdueRentals();
        //Then
        verify(notificationService, times(overdueRentals.size()))
                .sendNotification(anyLong(), anyString());
    }

    @Test
    @DisplayName("checkOverdueRentals() method sends global notification "
            + "when no rentals are overdue")
    public void checkOverdueRentals_WithNoOverdueRentals() {
        //Given
        Long chatId = 12345L;
        rentalService.setChatId(String.valueOf(chatId));
        when(rentalRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(notificationService).sendNotification(anyLong(), anyString());

        // When
        rentalService.checkOverdueRentals();
        //Then
        verify(notificationService, times(1))
                .sendNotification(eq(chatId), anyString());
    }
}
