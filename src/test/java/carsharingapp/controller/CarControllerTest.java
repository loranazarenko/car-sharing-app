package carsharingapp.controller;

import static carsharingapp.util.TestUtils.UPDATED_BRAND;
import static carsharingapp.util.TestUtils.UPDATED_MODEL;
import static carsharingapp.util.TestUtils.VALID_DAILY_FEE;
import static carsharingapp.util.TestUtils.VALID_ID;
import static carsharingapp.util.TestUtils.VALID_INVENTORY;
import static carsharingapp.util.TestUtils.createFirstTestCarResponseDto;
import static carsharingapp.util.TestUtils.createSecondTestCarResponseDto;
import static carsharingapp.util.TestUtils.createThirdTestCarResponseDto;
import static carsharingapp.util.TestUtils.createValidCarRequestDto;
import static carsharingapp.util.TestUtils.createValidCarResponseDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.model.Car;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeAll
    void beforeAll() throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown();
    }

    @AfterAll
    void afterAll() throws SQLException {
        setupDatabase(dataSource);
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        setupDatabase(dataSource);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        teardown();
    }

    private void teardown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/delete-cars-from-cars_table.sql")
            );
        }
    }

    private void setupDatabase(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/insert-cars-to-cars_table.sql")
            );
        }
    }

    @Test
    @DisplayName("create() method works")
    @WithMockUser(username = "carina@mail.com", roles = {"MANAGER"})
    public void create_WithValidCreateCarRequestDto_ReturnValidCarResponseDto() throws Exception {
        CarRequestDto createCarRequestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        String jsonRequest = objectMapper.writeValueAsString(createCarRequestDto);

        MvcResult mvcResult = mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                )
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), CarResponseDto.class);

        assertThat(actual.getModel()).isNotNull();
        assertThat(actual.getModel()).isEqualTo(expected.getModel());
    }

    @Test
    @WithMockUser(username = "carina@mail.com", roles = {"MANAGER"})
    @DisplayName("deleteById() method works")
    public void deleteById_WithValidId_ReturnNoContentStatus() throws Exception {
        mockMvc.perform(
                        delete("/cars/{id}", VALID_ID)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "joan@mail.com", roles = {"CUSTOMER"})
    @DisplayName("getAll() method returns all cars")
    public void getAll_WithValidParam_ReturnListWithAllCars() throws Exception {
        CarResponseDto firstCar = createFirstTestCarResponseDto();
        CarResponseDto secondCar = createSecondTestCarResponseDto();
        CarResponseDto thirdCar = createThirdTestCarResponseDto();
        List<CarResponseDto> expected = List.of(firstCar, secondCar, thirdCar);

        MvcResult mvcResult = mockMvc.perform(
                        get("/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<CarResponseDto> actual = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "joan@mail.com", roles = {"CUSTOMER"})
    @DisplayName("getById() method returns car with specified id")
    public void getById_WithValidId_ReturnValidCarResponseDto() throws Exception {
        CarResponseDto expected = createFirstTestCarResponseDto();

        MvcResult mvcResult = mockMvc.perform(
                        get("/cars/{id}", VALID_ID)
                                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        CarResponseDto actual = objectMapper
                .readValue(jsonResponse, CarResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "carina@mail.com", roles = {"MANAGER"})
    @DisplayName("updateById() method updates car by specified id")
    public void updateById_WithValidIdAndRequestDto_ReturnValidCarResponseDto() throws Exception {
        CarRequestDto updateCarRequestDto = new CarRequestDto(
                UPDATED_BRAND,
                UPDATED_MODEL,
                Car.Type.SEDAN.name(),
                VALID_INVENTORY,
                VALID_DAILY_FEE
        );
        CarResponseDto expected = new CarResponseDto();
        expected.setId(VALID_ID);
        expected.setBrand(UPDATED_BRAND);
        expected.setModel(UPDATED_MODEL);
        expected.setType(Car.Type.SEDAN);
        expected.setInventory(VALID_INVENTORY);
        expected.setDailyFee(VALID_DAILY_FEE);

        String jsonRequest = objectMapper.writeValueAsString(updateCarRequestDto);
        MvcResult mvcResult = mockMvc.perform(
                        put("/cars/{id}", VALID_ID)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), CarResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }
}
