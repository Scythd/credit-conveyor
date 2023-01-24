package com.moklyak.deal.services;

import com.moklyak.deal.dtos.*;
import com.moklyak.deal.entities.*;
import com.moklyak.deal.enums.ApplicationStatus;
import com.moklyak.deal.repositories.ApplicationRepository;
import com.moklyak.deal.repositories.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DealService {

    private final static Logger logger = LoggerFactory.getLogger(DealService.class);
    private final ClientRepository clientRepository;
    private final ApplicationRepository applicationRepository;
    private final ServiceConveyorFeignClient conveyorFeignClient;

    public DealService(ClientRepository clientRepository, ApplicationRepository applicationRepository, ServiceConveyorFeignClient conveyorFeignClient) {
        this.clientRepository = clientRepository;
        this.applicationRepository = applicationRepository;
        this.conveyorFeignClient = conveyorFeignClient;
    }

    public List<LoanOfferDTO> getOffers(LoanApplicationRequestDTO dto){
        return conveyorFeignClient.getOffers(dto);
    }

    public Client clientFromDto(LoanApplicationRequestDTO dto){
        Client client = new Client();
        client.setEmail(dto.getEmail());
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setMiddleName(dto.getMiddleName());
        client.setBirthDate(Date.valueOf(dto.getBirthDate()));
        Passport passport = new Passport();
        passport.setNumber(dto.getPassportNumber());
        passport.setSeries(dto.getPassportSeries());
        client.setPassport(passport);
        return client;
    }

    public Client saveClient(Client client){
        return clientRepository.save(client);
    }

    public Application applicationFromDtoAndClient(LoanApplicationRequestDTO dto, Client client){
        Application app = new Application();
        app.setClient(client);
        app.setStatusHistory(new ArrayList<>());
        app.getStatusHistory().add(ApplicationStatus.PREAPPROVAL.asStatusHistory());
        app.setStatus(ApplicationStatus.PREAPPROVAL);
        app.setCreationDate(Timestamp.valueOf(LocalDateTime.now()));
        return app;
    }

    public Application saveApplication(Application app){
        return applicationRepository.save(app);
    }
    public Application getApplication(Long id){
        return applicationRepository.findById(id).orElse(null);
    }

    public Application setLoanOfferAndSave(Application app, LoanOfferDTO dto) {
        app.getStatusHistory().add(ApplicationStatus.APPROVED.asStatusHistory());
        app.setStatus(ApplicationStatus.APPROVED);
        LoanOffer lo = new LoanOffer();
        lo.setTerm(dto.getTerm());
        lo.setRate(dto.getRate());
        lo.setMonthlyPayment(dto.getMonthlyPayment());
        lo.setRequestedAmount(dto.getRequestedAmount());
        lo.setTotalAmount(dto.getTotalAmount());
        lo.setIsInsuranceEnabled(dto.getIsInsuranceEnabled());
        lo.setIsSalaryClient(dto.getIsSalaryClient());
        app.setAppliedOffer(lo);
        return applicationRepository.save(app);
    }

    public CreditDTO scoringRequest(Application app, FinishRegistrationRequestDTO dto) {
        ScoringDataDTO sdDto = new ScoringDataDTO();

        app.getClient().getPassport().setIssueBranch(dto.getPassportIssueBranch());
        app.getClient().getPassport().setDate(Date.valueOf(dto.getPassportIssueDate()));
        app.getClient().setGender(dto.getGender());
        app.getClient().setMaritalStatus(dto.getMaritalStatus());
        app.getClient().setDependentAmount(dto.getDependentAmount());
        app.getClient().setEmployment(getEmploymentFromDto(dto.getEmployment()));
        app.getClient().setAccount(dto.getAccount());

        sdDto.setAccount(dto.getAccount());
        sdDto.setEmployment(dto.getEmployment());
        sdDto.setAmount(app.getAppliedOffer().getRequestedAmount());
        sdDto.setGender(app.getClient().getGender());
        sdDto.setDependentAmount(dto.getDependentAmount());
        sdDto.setMaritalStatus(dto.getMaritalStatus());
        sdDto.setPassportNumber(app.getClient().getPassport().getNumber());
        sdDto.setPassportSeries(app.getClient().getPassport().getSeries());
        sdDto.setPassportIssueBranch(dto.getPassportIssueBranch());
        sdDto.setPassportIssueDate(dto.getPassportIssueDate());
        sdDto.setTerm(app.getAppliedOffer().getTerm());
        sdDto.setBirthDate(app.getClient().getBirthDate().toLocalDate());
        sdDto.setFirstName(app.getClient().getFirstName());
        sdDto.setLastName(app.getClient().getLastName());
        sdDto.setMiddleName(app.getClient().getLastName());
        sdDto.setIsInsuranceEnabled(app.getAppliedOffer().getIsInsuranceEnabled());
        sdDto.setIsSalaryClient(app.getAppliedOffer().getIsSalaryClient());
        logger.info("adding changes to client {} in app {}", app.getClient(), app);
        applicationRepository.save(app);
        clientRepository.save(app.getClient());
        logger.info("start sending request to MS CC /calculation with dto: {}", sdDto);
        return conveyorFeignClient.getCalculation(sdDto);
    }

    private Employment getEmploymentFromDto(EmploymentDTO dto){
        Employment e = new Employment();
        e.setPosition(dto.getPosition());
        e.setEmployerInn(dto.getEmployerINN());
        e.setSalary(dto.getSalary());
        e.setStatus(dto.getEmploymentStatus());
        e.setWorkExperienceCurrent(dto.getWorkExperienceCurrent());
        e.setWorkExperienceTotal(dto.getWorkExperienceTotal());
        return e;
    }
}
