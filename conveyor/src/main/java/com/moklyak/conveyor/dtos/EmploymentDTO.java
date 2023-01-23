package com.moklyak.conveyor.dtos;

import com.moklyak.conveyor.enums.EmploymentPosition;
import com.moklyak.conveyor.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema
@Getter
@Setter
public class EmploymentDTO implements Serializable {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private EmploymentPosition position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
