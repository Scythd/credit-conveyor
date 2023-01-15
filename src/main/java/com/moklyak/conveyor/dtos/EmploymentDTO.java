package com.moklyak.conveyor.dtos;

import com.moklyak.conveyor.enums.EmploymentPosition;
import com.moklyak.conveyor.enums.EmploymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDTO {
    EmploymentStatus employmentStatus;
    String employerINN;
    BigDecimal salary;
    EmploymentPosition position;
    Integer workExperienceTotal;
    Integer workExperienceCurrent;
}
