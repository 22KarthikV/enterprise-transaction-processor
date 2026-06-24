package com.transactionprocessor.transaction.dto;

import com.transactionprocessor.transaction.domain.RiskLevel;
import com.transactionprocessor.transaction.domain.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    String senderAccount,
    String recipientAccount,
    BigDecimal amount,
    String currency,
    BigDecimal amountGbp,
    TransactionStatus status,
    RiskLevel riskLevel,
    Instant createdAt,
    Instant updatedAt) {}
