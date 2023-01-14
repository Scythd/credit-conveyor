package com.moklyak.conveyor.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanOfferDTO {
    Long applicationId;
    BigDecimal requestedAmount;
    BigDecimal totalAmount;
    Integer term;
    BigDecimal monthlyPayment;
    BigDecimal rate;
    Boolean isInsuranceEnabled;
    Boolean isSalaryClient;
}
