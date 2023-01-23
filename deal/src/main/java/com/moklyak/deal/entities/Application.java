package com.moklyak.deal.entities;

import com.moklyak.deal.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "application_id", nullable = false)
    private Long id;

    @OneToOne(targetEntity = Client.class)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private Client client;

    @OneToOne(targetEntity = Credit.class)
    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    private Credit credit;

    @Column
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    @Column
    private Timestamp creationDate;
    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private LoanOffer appliedOffer;
    @Column
    private Timestamp signDate;
    @Column
    private String sesCode;
    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private List<StatusHistory> statusHistory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
