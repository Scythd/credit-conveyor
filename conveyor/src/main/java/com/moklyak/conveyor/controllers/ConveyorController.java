package com.moklyak.conveyor.controllers;

import com.moklyak.conveyor.dtos.CreditDTO;
import com.moklyak.conveyor.dtos.LoanApplicationRequestDTO;
import com.moklyak.conveyor.dtos.LoanOfferDTO;
import com.moklyak.conveyor.dtos.ScoringDataDTO;
import com.moklyak.conveyor.exceptions.ScoreDenyException;
import com.moklyak.conveyor.services.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conveyor")
public class ConveyorController {

    private final static Logger logger = LoggerFactory.getLogger(ScoringService.class);
    private final ScoringService scoringService;

    public ConveyorController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }


    @PostMapping(value = "/offers")
    ResponseEntity<List<LoanOfferDTO>> offers(@RequestBody LoanApplicationRequestDTO dto) {
        logger.info("generating offers by request body: {}.", dto);
        scoringService.preScore(dto);
        List<LoanOfferDTO> result = scoringService.createOffers(dto);
        result.sort((x, y) -> y.getRate().compareTo(x.getRate()));
        logger.info("generated offers: {}, by request body: {}.", result, dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/calculation")
    ResponseEntity<CreditDTO> calculation(@RequestBody ScoringDataDTO dto) {
        logger.info("calculation for request body: {}.", dto);
        CreditDTO creditDTO = new CreditDTO();
        try {
            creditDTO.setRate(scoringService.score(dto));
            creditDTO.setMonthlyPayment(
                    scoringService.calcMonthlyPayment(
                            dto.getAmount(),
                            dto.getTerm(),
                            creditDTO.getRate()
                    )
            );
            creditDTO.setPaymentSchedule(
                    scoringService.calcPaymentSchedule(
                            dto.getAmount(),
                            dto.getTerm(),
                            creditDTO.getRate(),
                            creditDTO.getMonthlyPayment()
                    )
            );
            creditDTO.setPsk(
                    scoringService.calcPsk(
                            dto.getAmount(),
                            dto.getTerm(),
                            creditDTO.getRate(),
                            creditDTO.getMonthlyPayment(),
                            creditDTO.getPaymentSchedule()
                    )
            );

        } catch (ScoreDenyException ex) {
            logger.info("calculation for body: {} interrupted via scoring violation", dto);
            return ResponseEntity.unprocessableEntity().header("error", ex.getMessage()).build();
        }

        creditDTO.setAmount(dto.getAmount());
        creditDTO.setTerm(dto.getTerm());
        creditDTO.setIsSalaryClient(dto.getIsSalaryClient());
        creditDTO.setIsInsuranceEnabled(dto.getIsInsuranceEnabled());
        creditDTO.getPaymentSchedule().remove(0);

        logger.info("calculated result: {}, for request body: {}.",creditDTO, dto);
        return ResponseEntity.ok(creditDTO);
    }

}
