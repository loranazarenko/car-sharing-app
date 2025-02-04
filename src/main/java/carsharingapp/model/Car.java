package carsharingapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Setter
@Getter
@SQLDelete(sql = "UPDATE cars SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Table(name = "cars")
@Accessors(chain = true)
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String model;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(nullable = false)
    private int inventory;
    @Column(nullable = false)
    private BigDecimal dailyFee;
    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    public enum Type {
        SEDAN,
        SUV,
        HATCHBACK,
        UNIVERSAL;
    }
}


