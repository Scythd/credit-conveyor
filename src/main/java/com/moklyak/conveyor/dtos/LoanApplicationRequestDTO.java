package com.moklyak.conveyor.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequestDTO {
    BigDecimal amount;
    Integer term;
    String firstName;
    String lastName;
    String middleName;
    String email;
    LocalDate birthDate;
    String passportSeries;
    String passportNumber;
}
