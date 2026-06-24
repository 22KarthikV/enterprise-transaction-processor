package com.transactionprocessor.transaction.dto;

import com.transactionprocessor.transaction.domain.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  TransactionResponse toResponse(Transaction transaction);
}
