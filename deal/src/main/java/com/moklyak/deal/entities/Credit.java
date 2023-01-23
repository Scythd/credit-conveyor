package com.moklyak.deal.entities;

import com.moklyak.deal.enums.CreditStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "credit_id", nullable = false)
    private Long id;

    @Column
    private BigDecimal amount;
    @Column
    private Integer term;
    @Column
    private BigDecimal monthlyPayment;
    @Column
    private BigDecimal rate;
    @Column
    private BigDecimal psk;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private List<PaymentScheduleElement> paymentSchedule;

    @Column
    private Boolean insuranceEnable;
    @Column
    private Boolean salaryClient;
    @Column
    @Enumerated(EnumType.STRING)
    private CreditStatus creditStatus;
}
