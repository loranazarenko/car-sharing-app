package carsharingapp.repository;

import carsharingapp.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("FROM Payment p LEFT JOIN FETCH p.rental r LEFT JOIN FETCH r.user u "
            + "WHERE u.id = :userId")
    List<Payment> findAllByUserId(long userId);

    Optional<Payment> findByRentalId(Long rentalId);
}
