package carsharingapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.net.URL;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Setter
@Getter
@Table(name = "payments")
@Accessors(chain = true)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private Type type;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Rental rental;
    @Column(nullable = false, columnDefinition = "text")
    private URL sessionUrl;
    @Column(nullable = false)
    private String sessionId;
    @Column(nullable = false)
    private BigDecimal amountToPay;

    public enum Status {
        PENDING,
        PAID;
    }

    public enum Type {
        PAYMENT,
        FINE;
    }
}
