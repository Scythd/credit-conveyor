package com.moklyak.conveyor.enums;

import java.math.BigDecimal;

public enum MaritalStatus {
    MARRIED(BigDecimal.valueOf(-3)), DIVORCED(BigDecimal.ONE), SINGLE(BigDecimal.ZERO), WIDOW_WIDOWER(BigDecimal.ZERO);

    MaritalStatus(BigDecimal rateChanger) {
        this.rateChanger = rateChanger;
    }

    private BigDecimal rateChanger;
    public BigDecimal changeRate(BigDecimal rate) {
        return rate.add(rateChanger);
    }
}
