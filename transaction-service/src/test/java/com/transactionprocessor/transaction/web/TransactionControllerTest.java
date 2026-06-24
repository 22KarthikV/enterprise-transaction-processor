package com.transactionprocessor.transaction.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.transactionprocessor.transaction.domain.TransactionStatus;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.service.TransactionNotFoundException;
import com.transactionprocessor.transaction.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TransactionService transactionService;

  @Test
  void postValidTransactionReturns202WithId() throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    when(transactionService.createTransaction(any()))
        .thenReturn(
            new TransactionResponse(
                id,
                "SENDER",
                "RECIPIENT",
                new BigDecimal("50.00"),
                "GBP",
                null,
                TransactionStatus.PENDING,
                null,
                now,
                now));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"senderAccount\":\"SENDER\",\"recipientAccount\":\"RECIPIENT\",\"amount\":50.00,\"currency\":\"GBP\"}"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void postNegativeAmountReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"senderAccount\":\"SENDER\",\"recipientAccount\":\"RECIPIENT\",\"amount\":-50.00,\"currency\":\"GBP\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postInvalidCurrencyReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"senderAccount\":\"SENDER\",\"recipientAccount\":\"RECIPIENT\",\"amount\":50.00,\"currency\":\"XYZ\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getUnknownIdReturns404() throws Exception {
    UUID id = UUID.randomUUID();
    when(transactionService.getTransaction(eq(id))).thenThrow(new TransactionNotFoundException(id));

    mockMvc.perform(get("/api/v1/transactions/{id}", id)).andExpect(status().isNotFound());
  }
}
