package com.transactionprocessor.transaction.service;

import com.transactionprocessor.transaction.domain.Transaction;
import com.transactionprocessor.transaction.dto.CreateTransactionRequest;
import com.transactionprocessor.transaction.dto.TransactionMapper;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.repository.TransactionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper;

  public TransactionService(
      TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
    this.transactionRepository = transactionRepository;
    this.transactionMapper = transactionMapper;
  }

  @Transactional
  public TransactionResponse createTransaction(CreateTransactionRequest request) {
    Transaction transaction =
        new Transaction(
            request.senderAccount(),
            request.recipientAccount(),
            request.amount(),
            request.currency());
    Transaction saved = transactionRepository.save(transaction);
    return transactionMapper.toResponse(saved);
  }

  public TransactionResponse getTransaction(UUID id) {
    Transaction transaction =
        transactionRepository
            .findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));
    return transactionMapper.toResponse(transaction);
  }
}
