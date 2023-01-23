package com.moklyak.deal.entities;


import com.moklyak.deal.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import com.moklyak.deal.enums.Gender;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "client_id", nullable = false)
    private Long id;

    @Column
    private String lastName;
    @Column
    private String firstName;
    @Column
    private String middleName;
    @Column
    private Date birthDate;
    @Column
    private String email;
    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    @Column
    private Integer dependentAmount;
    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private Passport passport;
    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private Employment employment;
    @Column
    private String account;
}
