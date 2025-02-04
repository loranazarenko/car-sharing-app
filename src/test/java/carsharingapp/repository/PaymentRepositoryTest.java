package carsharingapp.repository;

import static carsharingapp.util.TestUtils.NOT_VALID_ID;
import static carsharingapp.util.TestUtils.createValidRental;
import static org.assertj.core.api.Assertions.assertThat;

import carsharingapp.model.Payment;
import carsharingapp.model.Rental;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {
        "classpath:db/insert-users-to-users_table.sql",
        "classpath:db/insert-cars-to-cars_table.sql",
        "classpath:db/insert-rentals-to-rentals_table.sql",
        "classpath:db/insert-payments-to-payments_table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:db/delete-payments-from-payments_table.sql",
        "classpath:db/delete-rentals-from-rentals_table.sql",
        "classpath:db/delete-cars-from-cars_table.sql",
        "classpath:db/delete-users-from-users_table.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("findBySessionId() method finds payment by sessionId")
    void getSumByRentalAndPaymentStatus_WithValidParams_ReturnSumOfPayments() {
        Rental rental = createValidRental();
        BigDecimal sum =
                paymentRepository.findByRentalId(rental.getId())
                        .orElseThrow().getAmountToPay();
        assertThat(sum.floatValue()).isEqualTo(10000.0f);
    }

    @Test
    @DisplayName("getAllByUserId() method finds all payments for specified "
            + "user")
    void getAllByUserIdAndPaymentStatus_WithValidUserIdAndStatus_ReturnPayments() {
        List<Payment> actual =
                paymentRepository.findAllByUserId(2L);
        List<Payment> emptyActual = paymentRepository.findAllByUserId(NOT_VALID_ID);

        int expectedPaymentsCount = 1;
        assertThat(actual.size()).isEqualTo(expectedPaymentsCount);
        assertThat(emptyActual.size()).isEqualTo(0);
    }
}
