package com.transactionprocessor.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.transactionprocessor.transaction.domain.TransactionStatus;
import com.transactionprocessor.transaction.dto.CreateTransactionRequest;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.security.JwtTestHelper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class TransactionPhase1IntegrationTest {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private TestRestTemplate restTemplate;

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(JwtTestHelper.validToken("integration-test-user"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  void postThenGetReturnsConsistentTransaction() {
    CreateTransactionRequest request =
        new CreateTransactionRequest("SENDER-IT", "RECIPIENT-IT", new BigDecimal("75.50"), "GBP");

    ResponseEntity<TransactionResponse> postResponse =
        restTemplate.exchange(
            "/api/v1/transactions",
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders()),
            TransactionResponse.class);

    assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(postResponse.getBody()).isNotNull();
    UUID id = postResponse.getBody().id();
    assertThat(id).isNotNull();

    ResponseEntity<TransactionResponse> getResponse =
        restTemplate.exchange(
            "/api/v1/transactions/{id}",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            TransactionResponse.class,
            id);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    TransactionResponse body = getResponse.getBody();
    assertThat(body).isNotNull();
    assertThat(body.id()).isEqualTo(id);
    assertThat(body.senderAccount()).isEqualTo("SENDER-IT");
    assertThat(body.recipientAccount()).isEqualTo("RECIPIENT-IT");
    assertThat(body.amount()).isEqualByComparingTo("75.50");
    assertThat(body.currency()).isEqualTo("GBP");
    assertThat(body.status()).isEqualTo(TransactionStatus.PENDING);
  }

  @Test
  void getUnknownIdReturns404() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/transactions/{id}",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class,
            UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
