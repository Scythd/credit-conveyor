package com.moklyak.deal.controllers;

import com.moklyak.deal.dtos.*;
import com.moklyak.deal.entities.Application;
import com.moklyak.deal.entities.Client;
import com.moklyak.deal.services.DealService;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/deal")
public class DealController {

    private final DealService dealService;



    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/application")
    public ResponseEntity<List<LoanOfferDTO>> application(@RequestBody LoanApplicationRequestDTO dto){
        Client client = dealService.clientFromDto(dto);
        client = dealService.saveClient(client);
        Application app = dealService.applicationFromDtoAndClient(dto, client);
        app = dealService.saveApplication(app);
        List<LoanOfferDTO> result;
        try {
            result = dealService.getOffers(dto);
        } catch (FeignException.UnprocessableEntity ex){
            return  ResponseEntity.badRequest().header("error", ex.responseHeaders().get("error").toArray(String[]::new)).build();
        }
        for (LoanOfferDTO t : result){
            t.setApplicationId(app.getId());
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/offer")
    public ResponseEntity<Void> offer(@RequestBody LoanOfferDTO dto){
        Application app = dealService.getApplication(dto.getApplicationId());
        app = dealService.setLoanOfferAndSave(app, dto);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/calculate/{applicationId}")
    public ResponseEntity<Void> calculate(@RequestBody FinishRegistrationRequestDTO dto, @PathVariable Long applicationId){
        Application app = dealService.getApplication(applicationId);
        try {
            CreditDTO cDto = dealService.scoringRequest(app, dto);
        } catch (FeignException.UnprocessableEntity ex){
                return ResponseEntity.badRequest().header("error", ex.responseHeaders().get("error").toArray(String[]::new)).build();
        }
        return ResponseEntity.ok(null);
    }


}
