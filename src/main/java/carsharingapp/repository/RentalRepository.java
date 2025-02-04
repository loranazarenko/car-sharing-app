package carsharingapp.repository;

import carsharingapp.model.Rental;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("FROM Rental r LEFT JOIN FETCH r.user u "
            + "WHERE u.id = :userId AND r.actualReturnDate IS NULL")
    List<Rental> findOpenRental(Long userId);
}
