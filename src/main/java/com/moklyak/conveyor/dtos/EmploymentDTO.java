package com.moklyak.conveyor.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moklyak.conveyor.enums.EmploymentPosition;
import com.moklyak.conveyor.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDTO implements Serializable {
    EmploymentStatus employmentStatus;
    String employerINN;
    BigDecimal salary;
    EmploymentPosition position;
    Integer workExperienceTotal;
    Integer workExperienceCurrent;
}
