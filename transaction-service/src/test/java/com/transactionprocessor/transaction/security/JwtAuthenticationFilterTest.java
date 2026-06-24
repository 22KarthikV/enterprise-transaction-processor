package com.transactionprocessor.transaction.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.transactionprocessor.transaction.domain.RiskLevel;
import com.transactionprocessor.transaction.domain.TransactionStatus;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TransactionService transactionService;

  @Test
  void requestWithoutTokenReturns401() throws Exception {
    mockMvc.perform(get("/api/v1/transactions/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
  }

  @Test
  void requestWithExpiredTokenReturns401() throws Exception {
    String token = JwtTestHelper.expiredToken("test-user");

    mockMvc
        .perform(get("/api/v1/transactions/{id}", UUID.randomUUID()).header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void requestWithValidTokenReachesController() throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    when(transactionService.getTransaction(id))
        .thenReturn(
            new TransactionResponse(
                id,
                "SENDER",
                "RECIPIENT",
                BigDecimal.TEN,
                "GBP",
                BigDecimal.TEN,
                TransactionStatus.COMPLETED,
                RiskLevel.LOW,
                now,
                now));
    String token = JwtTestHelper.validToken("test-user");

    mockMvc
        .perform(get("/api/v1/transactions/{id}", id).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}
