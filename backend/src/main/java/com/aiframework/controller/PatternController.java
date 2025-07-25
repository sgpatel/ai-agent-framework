package com.aiframework.controller;

import com.aiframework.service.PatternRecognitionService;
import com.aiframework.service.YahooFinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patterns")
public class PatternController {
    
    private final PatternRecognitionService patternService;
    private final YahooFinanceService yahooFinanceService;
    
    public PatternController(PatternRecognitionService patternService, YahooFinanceService yahooFinanceService) {
        this.patternService = patternService;
        this.yahooFinanceService = yahooFinanceService;
    }
    
    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getPatterns(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "3mo") String period) {
        
        try {
            List<Map<String, Object>> historicalData = yahooFinanceService.getHistoricalData(symbol, period);
            Map<String, Object> patterns = patternService.detectPatterns(historicalData);
            
            patterns.put("symbol", symbol);
            patterns.put("period", period);
            patterns.put("data_points", historicalData.size());
            
            return ResponseEntity.ok(patterns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}