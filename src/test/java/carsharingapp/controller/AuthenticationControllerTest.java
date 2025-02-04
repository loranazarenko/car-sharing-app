package carsharingapp.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharingapp.dto.UserRegistrationRequestDto;
import carsharingapp.dto.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeAll
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        cleanDatabase();
    }

    @AfterEach
    void cleanUpAfterTest() {
        cleanDatabase();
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        setupDatabase(dataSource);
    }

    private void setupDatabase(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/insert-users-to-users_table.sql")
            );
        }
    }

    private void cleanDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/delete-users-from-users_table.sql"));
        } catch (Exception e) {
            throw new RuntimeException("Database cleanup error", e);
        }
    }

    @Test
    @DisplayName("Verify register() method. Should return UserResponseDto")
    void register_ValidUserRegistrationRequestDto_SuccessfulRegistration() throws Exception {
        //Given
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto();
        UserResponseDto expected = createUserResponseDto(requestDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        UserResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                UserResponseDto.class);
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    private static UserRegistrationRequestDto createUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                "elsa@example.com",
                "password",
                "password",
                "Elsa",
                "Collins");
    }

    private static UserResponseDto createUserResponseDto(UserRegistrationRequestDto requestDto) {
        return new UserResponseDto(
                4L,
                requestDto.getEmail(),
                requestDto.getFirstName(),
                requestDto.getLastName(),
                Set.of(2L));
    }
}
