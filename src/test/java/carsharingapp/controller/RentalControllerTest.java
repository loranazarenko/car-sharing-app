package carsharingapp.controller;

import static carsharingapp.util.TestUtils.VALID_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharingapp.dto.CarRentedDto;
import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.mapper.RentalMapper;
import carsharingapp.model.Car;
import carsharingapp.model.Rental;
import carsharingapp.model.Role;
import carsharingapp.model.User;
import carsharingapp.repository.RentalRepository;
import carsharingapp.repository.UserRepository;
import carsharingapp.service.impl.TelegramNotificationService;
import carsharingapp.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private WebApplicationContext applicationContext;
    @Mock
    private TelegramNotificationService notificationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private RentalMapper rentalMapper;

    @BeforeAll
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        cleanDatabase();
    }

    @BeforeEach
    public void init() {
        loadDataIntoDatabase();
        doNothing().when(notificationService).sendNotification(anyLong(), anyString());
    }

    @AfterEach
    public void tearDown() {
        cleanDatabase();
    }

    private void loadDataIntoDatabase() {
        executeSqlScript("db/insert-users-to-users_table.sql");
        executeSqlScript("db/insert-cars-to-cars_table.sql");
        executeSqlScript("db/insert-rentals-to-rentals_table.sql");
    }

    private void cleanDatabase() {
        executeSqlScript("db/delete-rentals-from-rentals_table.sql");
        executeSqlScript("db/delete-cars-from-cars_table.sql");
        executeSqlScript("db/delete-users-from-users_table.sql");
    }

    private void executeSqlScript(String scriptPath) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(scriptPath));
        } catch (Exception e) {
            throw new RuntimeException("Error executing SQL script: " + scriptPath, e);
        }
    }

    @Test
    @DisplayName("Verify createRental() method. "
            + "Should create new rental and return RentalResponseDto"
    )
    @WithMockUser(username = "alice@example.com", roles = "MANAGER")
    void createRental_ValidRequest_CreateRental() throws Exception {
        //Given
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.parse("2025-02-09"),
                LocalDate.parse("2025-05-27"),
                1L,
                3L);

        User user = createSecondUser(TestUtils.createRole());
        Car car = createFirstCar();
        Rental rental = createNewRental(requestDto, car, user);
        RentalResponseDto expected = createRentalResponseDto(
                rental, convertCarToRentedCarDto(car, 1L)
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        Message mockedResponse = new Message();
        when(notificationService.execute(any(SendMessage.class))).thenReturn(mockedResponse);

        //When
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        RentalResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalResponseDto.class);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("Verify getRentalById() method. Should return specific rental by it's iD."
    )
    @WithMockUser(username = "bob@example.com", roles = "CUSTOMER")
    void getRentalById_ValidRentalId_ReturnSpecificRental() throws Exception {
        //Given
        User user = createFirstUser(TestUtils.createRole());
        Car car = createSecondCar();
        Rental rental = createSecondRental(car, user);
        RentalResponseDto expected = createRentalResponseDto(
                rental, convertCarToRentedCarDto(car, 3L)
        );
        when(rentalRepository.findById(VALID_ID))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(any(Rental.class)))
                .thenReturn(expected);

        //When
        MvcResult result = mockMvc.perform(
                        get(
                                "/rentals/{rentalId}", expected.getId()
                        )
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalResponseDto.class);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("Verify setActualReturnDate() method. Should set actual return date for rental."
    )
    @WithMockUser(username = "bob@example.com", roles = "MANAGER")
    void setActualReturnDate() throws Exception {
        //Given
        User user = createFirstUser(TestUtils.createRole());
        Car car = createSecondCar();
        Rental rental = createSecondRental(car, user);
        rental.setActualReturnDate(LocalDate.now());
        RentalResponseDto expected =
                createRentalResponseDto(rental, convertCarToRentedCarDto(car, 3L));

        //When
        MvcResult result = mockMvc.perform(
                        put(
                                "/rentals/{rentalId}/return", expected.getId()
                        )
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalResponseDto.class);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }

    private User createFirstUser(Role role) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(role);
        return new User()
                .setId(2L)
                .setEmail("bob@example.com")
                .setPassword("password")
                .setFirstName("Bob")
                .setLastName("Smith")
                .setRoles(roles)
                .setTelegramChatId(Long.valueOf("12345"))
                .setDeleted(false);
    }

    private User createSecondUser(Role role) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(role);
        return new User()
                .setId(3L)
                .setEmail("alice@example.com")
                .setPassword("password")
                .setFirstName("Alice")
                .setLastName("Robson")
                .setRoles(roles)
                .setTelegramChatId(Long.valueOf("12345"))
                .setDeleted(false);
    }

    private static Rental createNewRental(RentalRequestDto requestDto, Car car, User user) {
        return new Rental()
                .setId(4L)
                .setRentalDate(LocalDate.now())
                .setReturnDate(requestDto.getReturnDate())
                .setActualReturnDate(null)
                .setCar(car)
                .setUser(user);
    }

    private CarRentedDto convertCarToRentedCarDto(Car car, Long carRentedDto) {
        return new CarRentedDto(
                carRentedDto,
                car.getBrand(),
                car.getModel(),
                car.getType(),
                car.getDailyFee());
    }

    private static RentalResponseDto createRentalResponseDto(
            Rental rental, CarRentedDto rentedCarDto
    ) {
        return new RentalResponseDto(
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rentedCarDto,
                rental.getUser().getId());
    }

    private static Car createFirstCar() {
        return new Car()
                .setId(1L)
                .setBrand("Tesla")
                .setModel("Model S")
                .setType(Car.Type.SEDAN)
                .setInventory(3)
                .setDailyFee(new BigDecimal("1000.00"))
                .setDeleted(false);
    }

    private static Car createSecondCar() {
        return new Car()
                .setId(3L)
                .setBrand("BMW")
                .setModel("X5")
                .setType(Car.Type.SUV)
                .setInventory(4)
                .setDailyFee(new BigDecimal("1500.00"))
                .setDeleted(false);
    }

    private Rental createSecondRental(Car car, User user) {
        return new Rental()
                .setId(2L)
                .setRentalDate(LocalDate.parse("2024-01-05"))
                .setReturnDate(LocalDate.parse("2024-01-07"))
                .setActualReturnDate(null)
                .setCar(car)
                .setUser(user);
    }
}
