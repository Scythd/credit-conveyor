package com.moklyak.conveyor.controllers;

import com.moklyak.conveyor.dtos.CreditDTO;
import com.moklyak.conveyor.dtos.LoanApplicationRequestDTO;
import com.moklyak.conveyor.dtos.LoanOfferDTO;
import com.moklyak.conveyor.dtos.ScoringDataDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conveyor")
public class ConveyorController {

    @PostMapping("/offers")
    List<LoanOfferDTO> offers(LoanApplicationRequestDTO dto){

        return null;
    }

    @PostMapping("/calculation")
    CreditDTO calculation(ScoringDataDTO dto){

        return null;
    }

}
