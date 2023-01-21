package com.moklyak.conveyor.enums;

import java.math.BigDecimal;

public enum EmploymentPosition {
    WORKER(BigDecimal.ZERO), MID_MANAGER(BigDecimal.valueOf(-2)), TOP_MANAGER(BigDecimal.valueOf(-4)), OWNER(BigDecimal.ZERO);

    private BigDecimal rateChanger;

    EmploymentPosition(BigDecimal rateChanger) {
        this.rateChanger = rateChanger;
    }

    public BigDecimal changeRate(BigDecimal rate) {
        return rate.add(rateChanger);
    }
}
