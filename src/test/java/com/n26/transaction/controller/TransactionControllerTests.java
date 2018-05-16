package com.n26.transaction.controller;

import java.nio.charset.Charset;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.transaction.TransactionApplication;
import com.n26.transaction.model.Statistics;
import com.n26.transaction.model.Transaction;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransactionApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionControllerTests {

	private static final Double DEFAULT_AMOUNT = 10.0;

	private static final Double EMPTY_DOUBLE_VALUE = 0.0;
	private static final Double DEFAULT_DOUBLE_VALUE = 10.0;

	private static final Long EMPTY_LONG_VALUE = 0L;
	private static final Long DEFAULT_LONG_VALUE = 1L;

	private static Transaction INVALID_TRANSACTION;
	private static Transaction EXPIRED_TRANSACTION;
	private static Transaction VALID_TRANSACTION;
	private static Transaction NEAR_EXPIRY_TRANSACTION;

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void initTests() {
    	this.mockMvc = webAppContextSetup(webApplicationContext).build();
    	
    	INVALID_TRANSACTION = new Transaction(DEFAULT_AMOUNT, null);
    	EXPIRED_TRANSACTION = new Transaction(DEFAULT_AMOUNT, System.currentTimeMillis() - 61000);
    	VALID_TRANSACTION = new Transaction(DEFAULT_AMOUNT, System.currentTimeMillis());
    	NEAR_EXPIRY_TRANSACTION = new Transaction(DEFAULT_AMOUNT, System.currentTimeMillis() - 55000);
    }

    @Test
    public void testTransactionCreateThrowBadRequest() throws Exception {
        performCreateTransaction(mapper.writeValueAsString(INVALID_TRANSACTION))
        		.andExpect(status().isBadRequest());
    }

    @Test
    public void testTransactionCreateThrowExpired() throws Exception {
        performCreateTransaction(mapper.writeValueAsString(EXPIRED_TRANSACTION))
        		.andExpect(status().isNoContent());
    }

    @Test
    public void testTransactionCreateSuccessful() throws Exception {
    	performCreateTransaction(mapper.writeValueAsString(VALID_TRANSACTION))
                .andExpect(status().isCreated());
    }

    private ResultActions performCreateTransaction(String transaction) throws Exception {
    	return mockMvc.perform(post("/api/transactions")
                .content(transaction)
                .contentType(contentType));
    }

    @Test
    public void testEmptyStatistics() throws Exception {
    	performGetStatistics()
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.sum").value(EMPTY_DOUBLE_VALUE))
                .andExpect(jsonPath("$.avg").value(EMPTY_DOUBLE_VALUE))
                .andExpect(jsonPath("$.max").value(EMPTY_DOUBLE_VALUE))
                .andExpect(jsonPath("$.min").value(EMPTY_DOUBLE_VALUE))
                .andExpect(jsonPath("$.count").value(EMPTY_LONG_VALUE));
    }

    @Test
    public void testStatisticsDefault() throws Exception {
    	performCreateTransaction(mapper.writeValueAsString(VALID_TRANSACTION));
    	
    	performGetStatistics()
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.sum").value(DEFAULT_DOUBLE_VALUE))
                .andExpect(jsonPath("$.avg").value(DEFAULT_DOUBLE_VALUE))
                .andExpect(jsonPath("$.max").value(DEFAULT_DOUBLE_VALUE))
                .andExpect(jsonPath("$.min").value(DEFAULT_DOUBLE_VALUE))
                .andExpect(jsonPath("$.count").value(DEFAULT_LONG_VALUE));
    }

    private ResultActions performGetStatistics() throws Exception {
    	return mockMvc.perform(get("/api/statistics"));
    }

    @Test
    public void testTransactionStatisticsUpdateAfterExpiredTransaction() throws Exception {
    	performCreateTransaction(mapper.writeValueAsString(NEAR_EXPIRY_TRANSACTION));

    	MvcResult mvcResult = performGetStatistics()
                .andExpect(status().isOk())
                .andReturn();
    	Statistics initialResult = mapper.readValue(mvcResult.getResponse().getContentAsString(), Statistics.class);
    	Thread.sleep(6000);

    	mvcResult = performGetStatistics()
                .andExpect(status().isOk())
                .andReturn();
    	Statistics finalResult = mapper.readValue(mvcResult.getResponse().getContentAsString(), Statistics.class);

    	assertTrue(initialResult.getCount() > finalResult.getCount());
    	assertTrue(initialResult.getSum() > finalResult.getSum());
    }
}
