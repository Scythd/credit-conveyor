package com.moklyak.conveyor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moklyak.conveyor.controllers.ConveyorController;
import com.moklyak.conveyor.dtos.LoanApplicationRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(classes = ConveyorController.class)
class ConveyorApplicationTests {

    @Autowired
    ConveyorController controller;
    protected MockMvc mvc;
    @Autowired
    WebApplicationContext webApplicationContext;

    protected void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
    protected <T> T mapFromJson(String json, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    @Test
    void conveyorOffersPreScoringTesting() throws Exception{
        String uri = "/conveyor/offers";
        LoanApplicationRequestDTO object = new LoanApplicationRequestDTO();
        object.setFirstName("Nick");
        object.setLastName("Taylor");
        object.setEmail("nicktaylor@mail.com");
        object.setMiddleName(null);
        object.setBirthDate(LocalDate.now().minusYears(18));
        object.setAmount(BigDecimal.valueOf(100000));
        object.setTerm(6);
        object.setPassportSeries("1234");
        object.setPassportNumber("123456");
        String inputJson = mapToJson(object);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        Assertions.assertEquals(200, status);

    }
}
