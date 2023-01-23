package com.moklyak.conveyor.enums;

import com.moklyak.conveyor.exceptions.ScoreDenyException;

import java.math.BigDecimal;

public enum EmploymentStatus {
    UNEMPLOYED(null), SELF_EMPLOYED(BigDecimal.ONE), EMPLOYED(BigDecimal.ZERO), BUSINESS_OWNER(BigDecimal.valueOf(3));

    EmploymentStatus(BigDecimal rateChanger) {
        this.rateChanger = rateChanger;
    }

    private BigDecimal rateChanger;

    public BigDecimal changeRate(BigDecimal rate) throws ScoreDenyException{
        if (rateChanger == null){
            throw new ScoreDenyException("Denied by employment status");
        }
        return rate.add(rateChanger);
    }
}
