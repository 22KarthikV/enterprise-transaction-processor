package com.transactionprocessor.transaction.web;

import com.transactionprocessor.transaction.dto.CreateTransactionRequest;
import com.transactionprocessor.transaction.dto.TransactionResponse;
import com.transactionprocessor.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Operation(summary = "Submit a transaction for asynchronous processing")
  @ApiResponse(responseCode = "202", description = "Transaction accepted")
  @ApiResponse(responseCode = "400", description = "Validation failed")
  @PostMapping
  public ResponseEntity<TransactionResponse> createTransaction(
      @Valid @RequestBody CreateTransactionRequest request) {
    TransactionResponse response = transactionService.createTransaction(request);
    return ResponseEntity.accepted()
        .location(URI.create("/api/v1/transactions/" + response.id()))
        .body(response);
  }

  @Operation(summary = "Fetch a transaction by id")
  @ApiResponse(responseCode = "200", description = "Transaction found")
  @ApiResponse(responseCode = "404", description = "Transaction not found")
  @GetMapping("/{id}")
  public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID id) {
    return ResponseEntity.ok(transactionService.getTransaction(id));
  }
}
