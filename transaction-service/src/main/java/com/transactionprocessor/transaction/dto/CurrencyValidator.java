package com.transactionprocessor.transaction.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

  private static final Set<String> ISO_4217_CODES =
      Currency.getAvailableCurrencies().stream()
          .map(Currency::getCurrencyCode)
          .collect(Collectors.toSet());

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || ISO_4217_CODES.contains(value);
  }
}
