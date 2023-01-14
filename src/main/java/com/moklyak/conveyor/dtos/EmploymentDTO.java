package com.moklyak.conveyor.dtos;

import com.moklyak.conveyor.enums.EmploymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDTO {
    Enum<EmploymentStatus> employmentStatus;
    String employerINN;
    BigDecimal salary;
    Enum<EmploymentStatus> position;
    Integer workExperienceTotal;
    Integer workExperienceCurrent;
}
