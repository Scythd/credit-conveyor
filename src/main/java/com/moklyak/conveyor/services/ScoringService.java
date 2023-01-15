package com.moklyak.conveyor.services;

import com.moklyak.conveyor.dtos.LoanApplicationRequestDTO;
import com.moklyak.conveyor.dtos.LoanOfferDTO;
import com.moklyak.conveyor.dtos.PaymentScheduleElement;
import com.moklyak.conveyor.dtos.ScoringDataDTO;
import com.moklyak.conveyor.exceptions.ScoreDenyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
public class ScoringService {

    Logger logger = LoggerFactory.getLogger(ScoringService.class);
    @Value("${conveyor.baseRate}")
    BigDecimal baseRate;
    final Predicate<String> NAME_REGEXP = Pattern.compile("[a-zA-Z]{2,30}").asMatchPredicate();
    final Predicate<String> EMAIL_REGEXP = Pattern.compile("[\\w\\.]{2,50}@[\\w\\.]{2,20}").asMatchPredicate();
    final Predicate<String> PASSPORT_SERIAL_REGEXP = Pattern.compile("\\d{4}").asMatchPredicate();
    final Predicate<String> PASSPORT_NUMBER_REGEXP = Pattern.compile("\\d{6}").asMatchPredicate();
    final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(10000);
    final Integer MIN_TERM = 6;
    final Long MIN_AGE = 18l;
    final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    final Integer BASIC_PERIOD_COUNT = 12;
    final BigDecimal BASIC_PERIOD_DAYS = BigDecimal.valueOf(365. / BASIC_PERIOD_COUNT);


    final  BigDecimal INSURANCE_PERCENT = BigDecimal.valueOf(.05);
    final  BigDecimal INSURANCE_BASE = BigDecimal.valueOf(50000);
    public void preScore(LoanApplicationRequestDTO dto) {
        logger.info("preScoring started");
        try {
            if (!NAME_REGEXP.test(dto.getFirstName())) {
                throw new IllegalArgumentException("First name is illegal, should contain 2-30 latin letters, current: %s"
                        .formatted(dto.getFirstName()));
            }
            if (!NAME_REGEXP.test(dto.getLastName())) {
                throw new IllegalArgumentException("Last name is illegal, should contain 2-30 latin letters, current: %s"
                        .formatted(dto.getLastName()));
            }
            if (dto.getMiddleName() != null && !NAME_REGEXP.test(dto.getMiddleName())) {
                throw new IllegalArgumentException("Middle name is illegal, should contain 2-30 latin letters, current: %s"
                        .formatted(dto.getMiddleName()));
            }
            if (dto.getAmount().compareTo(MIN_AMOUNT) == -1) {
                throw new IllegalArgumentException("Amount is illegal, should be greater or equals 10000, current: %s"
                        .formatted(dto.getAmount()));
            }
            if (dto.getTerm().compareTo(MIN_TERM) == -1) {
                throw new IllegalArgumentException("Term is illegal, should be greater or equals 6, current: %s"
                        .formatted(dto.getTerm()));
            }
            if (dto.getBirthDate().isAfter(LocalDate.now().minusYears(MIN_AGE))) {
                throw new IllegalArgumentException("Birth date is illegal, should be not later then 18 years before, current: %s"
                        .formatted(dto.getBirthDate().format(DATE_FORMATTER)));
            }
            if (!EMAIL_REGEXP.test(dto.getEmail())) {
                throw new IllegalArgumentException("Email is illegal, should be [\\w\\.]{2,50}@[\\w\\.]{2,20}, current: %s"
                        .formatted(dto.getBirthDate().format(DATE_FORMATTER)));
            }
            if (!PASSPORT_SERIAL_REGEXP.test(dto.getPassportSeries())) {
                throw new IllegalArgumentException("Passport serial is illegal, should be 4 length, current: %s"
                        .formatted(dto.getPassportSeries()));
            }
            if (!PASSPORT_NUMBER_REGEXP.test(dto.getPassportNumber())) {
                throw new IllegalArgumentException("Passport number is illegal, should be 6 length, current: %s"
                        .formatted(dto.getPassportNumber()));
            }
        } catch (IllegalArgumentException ex){
            logger.info("preScoring interrupted");
            logger.info(ex.getMessage());
            throw ex;
        }
        logger.info("preScoring finished successfully");
    }

    public BigDecimal score(ScoringDataDTO dto) throws ScoreDenyException {
        logger.info("scoring started");
        try {
            BigDecimal tempRate = baseRate;
            tempRate = dto.getEmployment().getEmploymentStatus().changeRate(tempRate);
            tempRate = dto.getEmployment().getPosition().changeRate(tempRate);
            if (dto.getEmployment().getSalary().multiply(BigDecimal.valueOf(20)).compareTo(dto.getAmount()) == 1) {
                throw new ScoreDenyException("Denied by salary amount");
            }
            tempRate = dto.getMaritalStatus().changeRate(tempRate);
            if (dto.getDependentAmount() > 1) {
                tempRate = tempRate.add(BigDecimal.ONE);
            }
            int age = Period.between(dto.getBirthDate(), LocalDate.now()).getYears();
            if (age < 35 || age >= 60) {
                throw new ScoreDenyException("Denied by age");
            }
            tempRate = dto.getGender().changeRate(tempRate, age);

            if (dto.getEmployment().getWorkExperienceTotal() < 12) {
                throw new ScoreDenyException("Denied by total experience");
            }
            if (dto.getEmployment().getWorkExperienceCurrent() < 3) {
                throw new ScoreDenyException("Denied by current experience");
            }
            logger.info("scoring finished successfully");
            return tempRate;
        } catch (ScoreDenyException ex){
            logger.info("scoring interrupted");
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    public BigDecimal calcPsk(BigDecimal amount, Integer term, BigDecimal rate, BigDecimal monthlyPayment, List<PaymentScheduleElement> paymentSchedule) {
        logger.info("calculation psk");
        BigDecimal[] q = new BigDecimal[term + 1], e = new BigDecimal[term + 1];
        LocalDate dateNow = LocalDate.now();
        q[0] = BigDecimal.ONE;
        e[0] = BigDecimal.ZERO;

        for (int i = 0; i < term - 1; i++) {
            PaymentScheduleElement payment = paymentSchedule.get(i);
            int moneyFlowDateDiff = Period.between(payment.getDate(), dateNow).getDays();
            BigDecimal temp = BigDecimal.valueOf(moneyFlowDateDiff).divide(BASIC_PERIOD_DAYS);
            q[i + 1] = temp.setScale(0, RoundingMode.FLOOR);
            e[i + 1] = temp.subtract(q[i + 1]).divide(BASIC_PERIOD_DAYS);
        }
        BigDecimal i = BigDecimal.ZERO;
        BigDecimal x = BigDecimal.ONE;
        BigDecimal x_m = BigDecimal.ZERO;
        BigDecimal s = BigDecimal.valueOf(0.000001);
        while (x.compareTo(BigDecimal.ZERO) == 1) {
            x_m = x;
            x = BigDecimal.ZERO;
            x = x.add(amount.negate().divide(BigDecimal.ONE));
            for (int k = 0; k < paymentSchedule.size(); k++) {
                int value;
                try {
                    value = q[k + 1].intValueExact();
                } catch (ArithmeticException ex) {
                    logger.error(ex.getMessage());
                    logger.info("possibly loses info in conversation from BigDecimal to exact int");
                    value = q[k + 1].intValue();
                }
                x = x.add(paymentSchedule.get(k).getTotalPayment()
                        .divide(BigDecimal.ONE.add(e[k + 1].multiply(i))
                                .multiply(BigDecimal.ONE.add(i).pow(value))));
            }
            i = i.add(s);
        }
        if (x.compareTo(x_m) == 1) {
            i = i.subtract(s);
        }

        ///is floor really needed?
        BigDecimal psk = i.multiply(BigDecimal.valueOf(BASIC_PERIOD_COUNT)
                        .multiply(BigDecimal.valueOf(100000)))
                .setScale(0, RoundingMode.FLOOR)
                .divide(BigDecimal.valueOf(1000));


        return psk;
    }

    public BigDecimal calcMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate) {
        logger.info("calculating monthly payment");
        // amount * (rate + (rate / (rate + 1) * term - 1))
        return amount.multiply(rate.add(rate.divide(rate.add(BigDecimal.ONE), MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(term))
                .subtract(BigDecimal.ONE))
        );
    }

    public List<PaymentScheduleElement> calcPaymentSchedule(BigDecimal loanAmount, Integer term, BigDecimal rate, BigDecimal monthlyPayment) {
        logger.info("calculating payment schedule");
        BigDecimal monthRate = rate.divide(BigDecimal.valueOf(12));
        BigDecimal currLoanAmount = loanAmount;
        List<PaymentScheduleElement> result = new ArrayList<>();
        LocalDate dateBegin = LocalDate.now();
        for (int i = 0; i < term; i++) {
            PaymentScheduleElement pse = new PaymentScheduleElement();
            pse.setDate(dateBegin.plusMonths(i + 1));
            pse.setNumber(i + 1);
            pse.setTotalPayment(monthlyPayment);
            pse.setInterestPayment(currLoanAmount.multiply(monthRate));
            pse.setDebtPayment(monthlyPayment.subtract(pse.getInterestPayment()));
            currLoanAmount = currLoanAmount.subtract(pse.getDebtPayment());
            pse.setRemainDebt(currLoanAmount);
            result.add(pse);
        }
        if (currLoanAmount.setScale(2).compareTo(BigDecimal.ZERO) != 0) {
            logger.info("Payment schedule calculation malfunction. Remain loan amount is not zero: %s"
                    .formatted(currLoanAmount.setScale(2).toString()));
        }
        return result;
    }

    public List<LoanOfferDTO> createOffers(LoanApplicationRequestDTO dto) {
        LoanOfferDTO curr;
        List<LoanOfferDTO> result = new ArrayList<>(4);
        for (int i = 0 ; i < 4; i++){

            curr = new LoanOfferDTO();
            curr.setTerm(dto.getTerm());
            curr.setRequestedAmount(dto.getAmount());
            // where to pick up id?
            // placeholder
            curr.setApplicationId((long) i);
            result.add(curr);
        }
        curr = result.get(0);
        curr.setIsSalaryClient(false);
        curr.setIsInsuranceEnabled(false);
        curr.setRate(baseRate);
        curr.setMonthlyPayment(this.calcMonthlyPayment(dto.getAmount(), dto.getTerm(), curr.getRate()).setScale(2, RoundingMode.HALF_UP));
        curr.setTotalAmount(curr.getMonthlyPayment().multiply(BigDecimal.valueOf(curr.getTerm())).setScale(2, RoundingMode.HALF_UP));

        curr = result.get(1);
        curr.setIsSalaryClient(true);
        curr.setIsInsuranceEnabled(false);
        curr.setRate(baseRate.subtract(BigDecimal.ONE));
        curr.setMonthlyPayment(this.calcMonthlyPayment(dto.getAmount(), dto.getTerm(), curr.getRate()).setScale(2, RoundingMode.HALF_UP));
        curr.setTotalAmount(curr.getMonthlyPayment().multiply(BigDecimal.valueOf(curr.getTerm())).setScale(2, RoundingMode.HALF_UP));

        curr = result.get(2);
        curr.setIsSalaryClient(false);
        curr.setIsInsuranceEnabled(true);
        curr.setRate(baseRate.subtract(BigDecimal.valueOf(2)));
        curr.setMonthlyPayment(this.calcMonthlyPayment(dto.getAmount().add(INSURANCE_BASE.add(INSURANCE_PERCENT.multiply(dto.getAmount()))), dto.getTerm(), curr.getRate()).setScale(2, RoundingMode.HALF_UP));
        curr.setTotalAmount(curr.getMonthlyPayment().multiply(BigDecimal.valueOf(curr.getTerm())).setScale(2, RoundingMode.HALF_UP));

        curr = result.get(3);
        curr.setIsSalaryClient(true);
        curr.setIsInsuranceEnabled(true);
        curr.setRate(baseRate.subtract(BigDecimal.valueOf(3)));
        curr.setMonthlyPayment(this.calcMonthlyPayment(dto.getAmount().add(INSURANCE_BASE.add(INSURANCE_PERCENT.multiply(dto.getAmount()))), dto.getTerm(), curr.getRate()).setScale(2, RoundingMode.HALF_UP));
        curr.setTotalAmount(curr.getMonthlyPayment().multiply(BigDecimal.valueOf(curr.getTerm())).setScale(2, RoundingMode.HALF_UP));

        return result;
    }
}
