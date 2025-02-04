package carsharingapp.util;

import carsharingapp.dto.CarRentedDto;
import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.dto.PaymentResponseDto;
import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.model.Car;
import carsharingapp.model.Payment;
import carsharingapp.model.Rental;
import carsharingapp.model.Role;
import carsharingapp.model.User;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class TestUtils {
    public static final Long VALID_ID = 1L;
    public static final Long NOT_VALID_ID = -1L;
    public static final String VALID_MODEL = "Valid Model";
    public static final String VALID_BRAND = "Valid Brand";
    public static final String VALID_TYPE = Car.Type.SUV.name();
    public static final int VALID_INVENTORY = 10;
    public static final BigDecimal VALID_DAILY_FEE = BigDecimal.valueOf(1000, 2);
    public static final String VALID_EMAIL = "test@mail.com";
    public static final String VALID_FIRST_NAME = "First Name";
    public static final String VALID_LAST_NAME = "Last Name";
    public static final String VALID_PASSWORD = "Password";
    public static final LocalDate VALID_RENTAL_DATE = LocalDate.now();
    public static final LocalDate VALID_RETURN_DATE = LocalDate.now().plusDays(5);
    public static final LocalDate VALID_ACTUAL_RETURN_DATE = LocalDate.now().plusDays(4);
    public static final URL VALID_SESSION_URL;

    static {
        try {
            VALID_SESSION_URL = new URL("http://payment.url");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int VALID_PAGE_NUMBER = 0;
    public static final int VALID_PAGE_SIZE = 10;
    public static final Long VALID_RENTAL_ID = 1L;
    public static final Long VALID_USER_ID_WHICH_HAS_RENTAL = 2L;
    public static final String UPDATED_MODEL = "updated model";
    public static final String UPDATED_BRAND = "updated brand";

    private TestUtils() {
    }

    public static Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(Car.Type.valueOf(VALID_TYPE));
        car.setInventory(VALID_INVENTORY);
        car.setDailyFee(VALID_DAILY_FEE);
        return car;
    }

    public static CarRequestDto createValidCarRequestDto() {
        return new CarRequestDto(
                VALID_BRAND,
                VALID_MODEL,
                VALID_TYPE,
                VALID_INVENTORY,
                VALID_DAILY_FEE
        );
    }

    public static CarRentedDto createValidCarRentedDto() {
        return new CarRentedDto(
                VALID_ID,
                VALID_BRAND,
                VALID_MODEL,
                Car.Type.valueOf(VALID_TYPE),
                VALID_DAILY_FEE
        );
    }

    public static CarResponseDto createValidCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(VALID_ID);
        carResponseDto.setModel(VALID_MODEL);
        carResponseDto.setBrand(VALID_BRAND);
        carResponseDto.setType(Car.Type.valueOf(VALID_TYPE));
        carResponseDto.setInventory(VALID_INVENTORY);
        carResponseDto.setDailyFee(VALID_DAILY_FEE);
        return carResponseDto;
    }

    public static CarResponseDto createValidCarResponseDto(Long validId) {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(validId);
        carResponseDto.setModel(VALID_MODEL);
        carResponseDto.setBrand(VALID_BRAND);
        carResponseDto.setType(Car.Type.valueOf(VALID_TYPE));
        carResponseDto.setInventory(VALID_INVENTORY);
        carResponseDto.setDailyFee(VALID_DAILY_FEE);
        return carResponseDto;
    }

    public static CarResponseDto createFirstTestCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(1L);
        carResponseDto.setModel("Model S");
        carResponseDto.setBrand("Tesla");
        carResponseDto.setType(Car.Type.SEDAN);
        carResponseDto.setInventory(5);
        carResponseDto.setDailyFee(BigDecimal.valueOf(100000, 2));
        return carResponseDto;
    }

    public static CarResponseDto createSecondTestCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(2L);
        carResponseDto.setModel("Camry");
        carResponseDto.setBrand("Toyota");
        carResponseDto.setType(Car.Type.SEDAN);
        carResponseDto.setInventory(3);
        carResponseDto.setDailyFee(BigDecimal.valueOf(50000, 2));
        return carResponseDto;
    }

    public static CarResponseDto createThirdTestCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(3L);
        carResponseDto.setModel("X5");
        carResponseDto.setBrand("BMW");
        carResponseDto.setType(Car.Type.SUV);
        carResponseDto.setInventory(2);
        carResponseDto.setDailyFee(BigDecimal.valueOf(150000, 2));
        return carResponseDto;
    }

    public static User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setFirstName(VALID_FIRST_NAME);
        user.setLastName(VALID_LAST_NAME);
        user.setPassword(VALID_PASSWORD);
        return user;
    }

    public static Rental createValidRental() {
        Rental rental = new Rental();
        rental.setId(VALID_RENTAL_ID);
        rental.setRentalDate(VALID_RENTAL_DATE);
        rental.setReturnDate(VALID_RETURN_DATE);
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        return rental;
    }

    public static RentalRequestDto createValidRentalRequestDto() {
        return new RentalRequestDto(
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                VALID_ID,
                VALID_ID
        );
    }

    public static RentalResponseDto createValidRentalResponseDto() {
        return new RentalResponseDto(
                VALID_ID,
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                createValidCarRentedDto(),
                VALID_ID
        );
    }

    public static Payment createExpiredPayments() {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setRental(createValidRental());
        try {
            payment.setSessionUrl(new URL("http://localhost:8080/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        payment.setStatus(Payment.Status.PENDING);
        payment.setSessionId("SESSION ID");
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);
        return payment;
    }

    public static RentalResponseDto returnedRentalResponseDto() {
        return new RentalResponseDto(
                VALID_ID,
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                createValidCarRentedDto(),
                VALID_ID
        );
    }

    public static Rental createOverdueRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(LocalDate.now().minusDays(25));
        rental.setReturnDate(LocalDate.now().minusDays(5));
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        return rental;
    }

    public static Pageable createPageable() {
        return PageRequest.of(VALID_PAGE_NUMBER, VALID_PAGE_SIZE);
    }

    public static Car createCar() {
        return new Car()
                .setId(1L)
                .setBrand("Bugatti")
                .setModel("Chiron")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(new BigDecimal("10000"))
                .setDeleted(false);
    }

    public static Role createRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName(Role.RoleName.ROLE_CUSTOMER);
        return role;
    }

    public static User createUser(Role role) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(role);
        return new User()
                .setId(2L)
                .setEmail("rob@example.com")
                .setPassword("111111111111")
                .setFirstName("Rob")
                .setLastName("Johnson")
                .setRoles(roles)
                .setDeleted(false);
    }

    public static Rental createRental(User user, Car car) {
        return new Rental()
                .setId(1L)
                .setUser(user)
                .setCar(car)
                .setRentalDate(LocalDate.parse("2024-01-27"))
                .setReturnDate(LocalDate.parse("2025-05-27"))
                .setActualReturnDate(LocalDate.parse("2025-01-26"));
    }

    public static Payment createPayment(Rental rental) {
        return new Payment()
                .setId(1L)
                .setRental(rental)
                .setStatus(Payment.Status.PENDING)
                .setType(Payment.Type.PAYMENT)
                .setSessionId("1")
                .setAmountToPay(new BigDecimal("30000.00"));
    }

    public static PaymentResponseDto convertPaymentToPaymentResponseDto(Payment payment) {
        return new PaymentResponseDto(
                1L,
                payment.getStatus(),
                payment.getType(),
                payment.getRental().getId(),
                payment.getSessionUrl(),
                payment.getSessionId(),
                payment.getAmountToPay());
    }

    public static RentalResponseDto createRentalResponseDto(Rental rental, Car car, User user) {
        return new RentalResponseDto(
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                convertCarToRentedCarDto(car),
                user.getId()
        );
    }

    private static CarRentedDto convertCarToRentedCarDto(Car car) {
        return new CarRentedDto(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getType(),
                car.getDailyFee()
        );
    }
}

