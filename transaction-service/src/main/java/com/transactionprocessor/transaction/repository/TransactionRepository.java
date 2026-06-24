package com.transactionprocessor.transaction.repository;

import com.transactionprocessor.transaction.domain.Transaction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

  Page<Transaction> findBySenderAccount(String senderAccount, Pageable pageable);
}
