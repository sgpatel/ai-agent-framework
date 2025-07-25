package com.aiframework.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StockSymbolValidator implements ConstraintValidator<ValidStockSymbol, String> {
    
    @Override
    public boolean isValid(String symbol, ConstraintValidatorContext context) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation: 1-5 uppercase letters
        return symbol.matches("^[A-Z]{1,5}$");
    }
}