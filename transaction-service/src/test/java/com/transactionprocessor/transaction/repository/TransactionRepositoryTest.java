package com.transactionprocessor.transaction.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.transactionprocessor.transaction.domain.Transaction;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class TransactionRepositoryTest {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private TransactionRepository transactionRepository;
  @Autowired private EntityManager entityManager;

  @Test
  void findBySenderAccountReturnsOnlyMatchingTransactions() {
    entityManager.persist(new Transaction("SENDER-A", "RECIPIENT-1", BigDecimal.TEN, "GBP"));
    entityManager.persist(new Transaction("SENDER-A", "RECIPIENT-2", BigDecimal.ONE, "GBP"));
    entityManager.persist(new Transaction("SENDER-B", "RECIPIENT-3", BigDecimal.TEN, "GBP"));
    entityManager.flush();

    Page<Transaction> page =
        transactionRepository.findBySenderAccount("SENDER-A", PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent()).extracting(Transaction::getSenderAccount).containsOnly("SENDER-A");
  }
}
