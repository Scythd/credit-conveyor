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

    private static final Logger logger = LoggerFactory.getLogger(ScoringService.class);
    @Value("${conveyor.baseRate}")
    private BigDecimal baseRate;
    public final Predicate<String> NAME_REGEXP = Pattern.compile("[a-zA-Z]{2,30}").asMatchPredicate();
    public final Predicate<String> EMAIL_REGEXP = Pattern.compile("[\\w\\.]{2,50}@[\\w\\.]{2,20}").asMatchPredicate();
    public final Predicate<String> PASSPORT_SERIAL_REGEXP = Pattern.compile("\\d{4}").asMatchPredicate();
    public final Predicate<String> PASSPORT_NUMBER_REGEXP = Pattern.compile("\\d{6}").asMatchPredicate();
    public final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(10000);
    public final Integer MIN_TERM = 6;
    public final Long MIN_AGE = 18l;
    public final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public final  BigDecimal INSURANCE_PERCENT = BigDecimal.valueOf(.05);
    public final  BigDecimal INSURANCE_BASE = BigDecimal.valueOf(50000);
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
            if (dto.getEmployment().getSalary().multiply(BigDecimal.valueOf(20)).compareTo(dto.getAmount()) != 1) {
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

    public BigDecimal calcPsk(BigDecimal amount,
                              Integer term,
                              BigDecimal rate,
                              BigDecimal monthlyPayment,
                              List<PaymentScheduleElement> paymentSchedule) {

        logger.info("calculation psk");
        return monthlyPayment.multiply(BigDecimal.valueOf(term))
                .divide(amount, MathContext.DECIMAL64)
                .subtract(BigDecimal.ONE)
                .divide(BigDecimal.valueOf(term).divide(BigDecimal.valueOf(12), MathContext.DECIMAL64))
                .multiply(BigDecimal.valueOf(100));

    }

    public BigDecimal calcMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate) {
        logger.info("calculating monthly payment");
        rate = rate.divide(BigDecimal.valueOf(1200), MathContext.DECIMAL64);
        return rate.add(BigDecimal.ONE)
                .pow(term)
                .multiply(rate)
                .divide(rate.add(BigDecimal.ONE).pow(term).subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                .multiply(amount);

    }

    public List<PaymentScheduleElement> calcPaymentSchedule(BigDecimal loanAmount,
                                                            Integer term,
                                                            BigDecimal rate,
                                                            BigDecimal monthlyPayment) {
        logger.info("calculating payment schedule");
        BigDecimal monthRate = rate.divide(BigDecimal.valueOf(1200));
        BigDecimal currLoanAmount = loanAmount;
        List<PaymentScheduleElement> result = new ArrayList<>();
        LocalDate dateBegin = LocalDate.now();

        PaymentScheduleElement pse = new PaymentScheduleElement();
        pse.setDate(dateBegin);
        pse.setNumber(0);
        pse.setTotalPayment(BigDecimal.ZERO);
        pse.setInterestPayment(BigDecimal.ZERO);
        pse.setDebtPayment(BigDecimal.ZERO);
        pse.setRemainDebt(currLoanAmount.negate());
        result.add(pse);
        for (int i = 0; i < term; i++) {
            BigDecimal temp;
            pse = new PaymentScheduleElement();
            pse.setDate(dateBegin.plusMonths(i + 1));
            pse.setNumber(i + 1);
            pse.setTotalPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP));
            pse.setInterestPayment(currLoanAmount.multiply(monthRate).setScale(2, RoundingMode.HALF_UP));
            temp = monthlyPayment.subtract(pse.getInterestPayment()).setScale(2, RoundingMode.HALF_UP);
            pse.setDebtPayment(temp);
            currLoanAmount = currLoanAmount.subtract(pse.getDebtPayment());
            pse.setRemainDebt(currLoanAmount.setScale(2, RoundingMode.HALF_UP));
            result.add(pse);
        }

        if (currLoanAmount.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) != 0) {
            logger.info("Payment schedule calculation malfunction. Remain loan amount is not zero: %s"
                    .formatted(currLoanAmount.setScale(2, RoundingMode.HALF_UP).toString()));
        }
        return result;
    }

    public List<LoanOfferDTO> createOffers(LoanApplicationRequestDTO dto) {
        BigDecimal tempMonthlyPayment, tempTotalAmount, tempRate;
        LoanOfferDTO curr;
        List<LoanOfferDTO> result = new ArrayList<>(4);
        for (int i = 0 ; i < 4; i++){

            curr = new LoanOfferDTO();
            curr.setTerm(dto.getTerm());
            curr.setRequestedAmount(dto.getAmount());
            curr.setApplicationId((long) i);
            result.add(curr);
        }

        tempMonthlyPayment = this.calcMonthlyPayment(dto.getAmount(), dto.getTerm(), baseRate);
        tempTotalAmount = tempMonthlyPayment.multiply(BigDecimal.valueOf(dto.getTerm()));
        fillLoanOffer(result.get(0), false, false, baseRate, tempMonthlyPayment, tempTotalAmount);

        tempRate = baseRate.subtract(BigDecimal.ONE);
        tempMonthlyPayment = this.calcMonthlyPayment(dto.getAmount(), dto.getTerm(), tempRate);
        tempTotalAmount = tempMonthlyPayment.multiply(BigDecimal.valueOf(dto.getTerm()));
        fillLoanOffer(result.get(1), true, false, tempRate, tempMonthlyPayment, tempTotalAmount);

        tempRate = baseRate.subtract(BigDecimal.valueOf(2));
        tempMonthlyPayment = INSURANCE_BASE.add(INSURANCE_PERCENT.multiply(dto.getAmount()));
        tempMonthlyPayment = this.calcMonthlyPayment(dto.getAmount().add(tempMonthlyPayment), dto.getTerm(), tempRate);
        tempTotalAmount = tempMonthlyPayment.multiply(BigDecimal.valueOf(dto.getTerm()));
        fillLoanOffer(result.get(2), false, true, tempRate, tempMonthlyPayment, tempTotalAmount);

        tempRate = baseRate.subtract(BigDecimal.valueOf(3));
        tempMonthlyPayment = INSURANCE_BASE.add(INSURANCE_PERCENT.multiply(dto.getAmount()));
        tempMonthlyPayment = this.calcMonthlyPayment(dto.getAmount().add(tempMonthlyPayment), dto.getTerm(), tempRate);
        tempTotalAmount = tempMonthlyPayment.multiply(BigDecimal.valueOf(dto.getTerm()));
        fillLoanOffer(result.get(3), true, true, tempRate, tempMonthlyPayment, tempTotalAmount);

        return result;
    }

    private void fillLoanOffer(LoanOfferDTO curr,
                               boolean isSalary,
                               boolean isInsurance,
                               BigDecimal rate,
                               BigDecimal monthlyPayment,
                               BigDecimal totalAmount ){
        curr.setIsSalaryClient(isSalary);
        curr.setIsInsuranceEnabled(isInsurance);
        curr.setRate(rate);
        curr.setMonthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP));
        curr.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
    }
}
