package com.transactionprocessor.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.transactionprocessor.transaction.domain.Transaction;
import com.transactionprocessor.transaction.dto.CreateTransactionRequest;
import com.transactionprocessor.transaction.dto.TransactionMapperImpl;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {

  private TransactionRepository transactionRepository;
  private TransactionService transactionService;

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    transactionService = new TransactionService(transactionRepository, new TransactionMapperImpl());
  }

  @Test
  void createTransactionPersistsAndReturnsResponse() {
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TransactionResponse response =
        transactionService.createTransaction(
            new CreateTransactionRequest("SENDER", "RECIPIENT", new BigDecimal("50.00"), "GBP"));

    assertThat(response.senderAccount()).isEqualTo("SENDER");
    assertThat(response.recipientAccount()).isEqualTo("RECIPIENT");
    assertThat(response.amount()).isEqualByComparingTo("50.00");
    assertThat(response.currency()).isEqualTo("GBP");
  }

  @Test
  void getTransactionReturnsResponseWhenFound() {
    Transaction transaction = new Transaction("SENDER", "RECIPIENT", new BigDecimal("10.00"), "USD");
    when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

    TransactionResponse response = transactionService.getTransaction(transaction.getId());

    assertThat(response.id()).isEqualTo(transaction.getId());
    assertThat(response.senderAccount()).isEqualTo("SENDER");
  }

  @Test
  void getTransactionThrowsWhenNotFound() {
    UUID id = UUID.randomUUID();
    when(transactionRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionService.getTransaction(id))
        .isInstanceOf(TransactionNotFoundException.class);
  }
}
