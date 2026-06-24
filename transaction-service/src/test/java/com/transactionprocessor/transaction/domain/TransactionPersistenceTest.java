package com.transactionprocessor.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class TransactionPersistenceTest {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private EntityManager entityManager;

  @Test
  void savesAndReloadsAllFields() {
    Transaction transaction =
        new Transaction("GB-SENDER-001", "GB-RECIPIENT-002", new BigDecimal("150.00"), "GBP");
    transaction.setMetadata(Map.of("channel", "mobile"));

    entityManager.persist(transaction);
    entityManager.flush();
    entityManager.clear();

    Transaction reloaded = entityManager.find(Transaction.class, transaction.getId());

    assertThat(reloaded).isNotNull();
    assertThat(reloaded.getSenderAccount()).isEqualTo("GB-SENDER-001");
    assertThat(reloaded.getRecipientAccount()).isEqualTo("GB-RECIPIENT-002");
    assertThat(reloaded.getAmount()).isEqualByComparingTo("150.00");
    assertThat(reloaded.getCurrency()).isEqualTo("GBP");
    assertThat(reloaded.getStatus()).isEqualTo(TransactionStatus.PENDING);
    assertThat(reloaded.getMetadata()).containsEntry("channel", "mobile");
    assertThat(reloaded.getCreatedAt()).isNotNull();
    assertThat(reloaded.getUpdatedAt()).isNotNull();
  }

  @Test
  void roundTripsEachTransactionStatus() {
    for (TransactionStatus status : TransactionStatus.values()) {
      Transaction transaction = new Transaction("SENDER", "RECIPIENT", BigDecimal.TEN, "USD");
      transaction.setStatus(status);

      entityManager.persist(transaction);
      entityManager.flush();
      entityManager.clear();

      Transaction reloaded = entityManager.find(Transaction.class, transaction.getId());
      assertThat(reloaded.getStatus()).isEqualTo(status);
    }
  }
}
