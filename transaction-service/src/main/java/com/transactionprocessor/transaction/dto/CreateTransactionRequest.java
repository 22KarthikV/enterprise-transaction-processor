package com.transactionprocessor.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotBlank String senderAccount,
    @NotBlank String recipientAccount,
    @NotNull @Positive BigDecimal amount,
    @NotBlank @ValidCurrency String currency) {}
