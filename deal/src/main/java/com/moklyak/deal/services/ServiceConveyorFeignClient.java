package com.moklyak.deal.services;

import com.moklyak.deal.dtos.CreditDTO;
import com.moklyak.deal.dtos.LoanApplicationRequestDTO;
import com.moklyak.deal.dtos.LoanOfferDTO;
import com.moklyak.deal.dtos.ScoringDataDTO;
import com.moklyak.deal.entities.PaymentScheduleElement;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "conveyor-client", url = "${conveyor.url}")
public interface ServiceConveyorFeignClient {
    @PostMapping("/conveyor/offers")
    public List<LoanOfferDTO> getOffers(LoanApplicationRequestDTO dto);

    @PostMapping("/conveyor/calculation")
    public CreditDTO getCalculation(ScoringDataDTO dto);
}
