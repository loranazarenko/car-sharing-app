package carsharingapp.repository;

import static carsharingapp.util.TestUtils.VALID_USER_ID_WHICH_HAS_RENTAL;
import static org.assertj.core.api.Assertions.assertThat;

import carsharingapp.model.Rental;
import java.util.Collections;
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
        "classpath:db/insert-rentals-to-rentals_table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:db/delete-rentals-from-rentals_table.sql",
        "classpath:db/delete-cars-from-cars_table.sql",
        "classpath:db/delete-users-from-users_table.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("findAll() method finds all rentals  for specified users ids and rental state")
    void findAll_WithSpecifiedUserIdsAndRentalState_ReturnRentals() {

        Long expectedRentalsCount = 1L;
        List<Rental> actual = rentalRepository
                .findAllById(Collections.singleton(VALID_USER_ID_WHICH_HAS_RENTAL));
        assertThat((long) actual.size()).isEqualTo(expectedRentalsCount);
    }

    @Test
    @DisplayName("findAllByReturnDateBeforeAndActualReturnDateIsNull() method finds all rentals  "
            + "which are not returned in time")
    void findAllByReturnDateBeforeAndActualReturnDateIsNull_WithReturnDate_ReturnRentals() {

        List<Rental> actual = rentalRepository
                .findOpenRental(VALID_USER_ID_WHICH_HAS_RENTAL);
        assertThat(actual.size()).isEqualTo(1L);
    }
}
