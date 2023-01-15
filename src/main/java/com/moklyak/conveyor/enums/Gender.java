package com.moklyak.conveyor.enums;

import java.math.BigDecimal;
import java.util.function.Predicate;

public enum Gender {
    MALE(BigDecimal.valueOf(-3), x -> x >= 30 && x < 55),
    FEMALE(BigDecimal.valueOf(-3), x -> x >= 35 && x < 60),
    NON_BINARY(BigDecimal.valueOf(3), x -> true);

    BigDecimal rateChanger;
    Predicate<Integer> ageChecker;

    Gender(BigDecimal rateChanger, Predicate<Integer> ageChecker) {
        this.rateChanger = rateChanger;
        this.ageChecker = ageChecker;
    }

    public BigDecimal changeRate(BigDecimal rate, int age) {
        if(ageChecker.test(age)){
            return rate.add(rateChanger);
        } else {
            return rate;
        }
    }
}
