package com.moklyak.deal.controllers;

import com.moklyak.deal.dtos.*;
import com.moklyak.deal.entities.Application;
import com.moklyak.deal.entities.Client;
import com.moklyak.deal.services.DealService;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/deal")
public class DealController {

    private final static Logger logger = LoggerFactory.getLogger(DealController.class);
    private final DealService dealService;



    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/application")
    public ResponseEntity<List<LoanOfferDTO>> application(@RequestBody LoanApplicationRequestDTO dto){
        logger.info("start processing /application with dto:{}", dto);
        logger.info("creating client entity");
        Client client = dealService.clientFromDto(dto);
        client = dealService.saveClient(client);
        logger.info("client entity created and saved: {}", client);
        logger.info("creating application entity");
        Application app = dealService.applicationFromDtoAndClient(dto, client);
        app = dealService.saveApplication(app);
        logger.info("application entity created and saved: {}", app);
        List<LoanOfferDTO> result;
        try {
            logger.info("requesting CC MS /offers with dto: {}", dto);
            result = dealService.getOffers(dto);
        } catch (FeignException.UnprocessableEntity ex){
            logger.info("MS returned unprocessable entry: {}", ex);
            return  ResponseEntity.badRequest().header("error", ex.responseHeaders().get("error").toArray(String[]::new)).build();
        }
        logger.info("requesting CC MS return a result: {}", result);
        for (LoanOfferDTO t : result){
            t.setApplicationId(app.getId());
        }
        logger.info("successful end of processing {} with result: {}", dto, result);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/offer")
    public ResponseEntity<Void> offer(@RequestBody LoanOfferDTO dto){
        logger.info("start processing /offer with dto:{}", dto);
        Application app = dealService.getApplication(dto.getApplicationId());
        logger.info("setting chosen loan offer to application:{}", app);
        app = dealService.setLoanOfferAndSave(app, dto);
        logger.info("successfully set loan offer to application:{}", app);
        logger.info("successful end of processing {} ", dto);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/calculate/{applicationId}")
    public ResponseEntity<Void> calculate(@RequestBody FinishRegistrationRequestDTO dto, @PathVariable Long applicationId){
        logger.info("start processing /calculate with dto:{}", dto);
        Application app = dealService.getApplication(applicationId);
        try {
            CreditDTO cDto = dealService.scoringRequest(app, dto);
        } catch (FeignException.UnprocessableEntity ex){
            logger.info("MS returned unprocessable entry: {}", ex);
                return ResponseEntity.badRequest().header("error", ex.responseHeaders().get("error").toArray(String[]::new)).build();
        }
        logger.info("successful end of processing {} ", dto);
        return ResponseEntity.ok(null);
    }


}
