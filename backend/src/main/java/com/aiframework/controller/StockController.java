package com.aiframework.controller;

import com.aiframework.dto.StockQuote;
import com.aiframework.dto.TechnicalIndicators;
import com.aiframework.dto.TradingSignal;
import com.aiframework.exception.StockDataNotFoundException;
import com.aiframework.service.AdvancedStockAnalysisService;
import com.aiframework.service.StockDataService;
import com.aiframework.service.TechnicalPatternService;
import com.aiframework.service.RiskAssessmentService;
import com.aiframework.service.PricePredictionService;
import com.aiframework.validation.ValidStockSymbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/stocks")
@Validated
@Slf4j
public class StockController {
    
    private final StockDataService stockDataService;
    private final AdvancedStockAnalysisService advancedAnalysisService;
    private final TechnicalPatternService patternService;
    private final RiskAssessmentService riskService;
    private final PricePredictionService predictionService;

    public StockController(StockDataService stockDataService,
                          AdvancedStockAnalysisService advancedAnalysisService,
                          TechnicalPatternService patternService,
                          RiskAssessmentService riskService,
                          PricePredictionService predictionService) {
        this.stockDataService = stockDataService;
        this.advancedAnalysisService = advancedAnalysisService;
        this.patternService = patternService;
        this.riskService = riskService;
        this.predictionService = predictionService;
    }

    @GetMapping("/{symbol}/quote")
    // @PreAuthorize("hasRole('USER')") // Commented out for development
    public CompletableFuture<ResponseEntity<StockQuote>> getRealTimeQuote(
            @PathVariable @ValidStockSymbol @Size(min = 1, max = 10) String symbol) {

        log.info("Fetching real-time quote for symbol: {}", symbol);

        return advancedAnalysisService.getRealTimeQuote(symbol.toUpperCase())
                .thenApply(quote -> {
                    log.debug("Successfully retrieved quote for {}: {}", symbol, quote.getPrice());
                    return ResponseEntity.ok(quote);
                })
                .exceptionally(ex -> {
                    log.error("Failed to retrieve quote for symbol: {}", symbol, ex);
                    throw new StockDataNotFoundException(symbol);
                });
    }
    
    @GetMapping("/{symbol}/price")
    // @PreAuthorize("hasRole('USER')") // Commented out for development
    public ResponseEntity<Map<String, Object>> getStockPrice(
            @PathVariable @ValidStockSymbol @Size(min = 1, max = 10) String symbol) {

        log.info("Fetching stock price for symbol: {}", symbol);

        try {
            Map<String, Object> price = stockDataService.getStockPrice(symbol.toUpperCase());
            return ResponseEntity.ok(price);
        } catch (Exception ex) {
            log.error("Failed to fetch stock price for symbol: {}", symbol, ex);
            throw new StockDataNotFoundException(symbol);
        }
    }
    
    @GetMapping("/{symbol}/history")
    // @PreAuthorize("hasRole('USER')") // Commented out for development
    public ResponseEntity<List<Map<String, Object>>> getHistoricalData(
            @PathVariable @ValidStockSymbol @Size(min = 1, max = 10) String symbol,
            @RequestParam(defaultValue = "1y") @Pattern(regexp = "^(1d|5d|1mo|3mo|6mo|1y|2y|5y|10y|ytd|max)$") String period) {

        log.info("Fetching historical data for symbol: {}, period: {}", symbol, period);

        try {
            List<Map<String, Object>> history = stockDataService.getHistoricalData(symbol.toUpperCase(), period);
            return ResponseEntity.ok(history);
        } catch (Exception ex) {
            log.error("Failed to fetch historical data for symbol: {}, period: {}", symbol, period, ex);
            throw new StockDataNotFoundException(symbol);
        }
    }
    
    @GetMapping("/{symbol}/history/advanced")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getAdvancedHistoricalData(
            @PathVariable @ValidStockSymbol String symbol,
            @RequestParam(defaultValue = "daily") String interval,
            @RequestParam(defaultValue = "compact") String outputSize) {
        return advancedAnalysisService.getHistoricalData(symbol.toUpperCase(), interval, outputSize)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{symbol}/indicators")
    public ResponseEntity<TechnicalIndicators> getTechnicalIndicators(
            @PathVariable @ValidStockSymbol String symbol) {
        TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(symbol.toUpperCase());
        return ResponseEntity.ok(indicators);
    }

    @GetMapping("/{symbol}/signal")
    public ResponseEntity<TradingSignal> getTradingSignal(
            @PathVariable @ValidStockSymbol String symbol) {
        TradingSignal signal = advancedAnalysisService.generateTradingSignal(symbol.toUpperCase());
        return ResponseEntity.ok(signal);
    }

    // NEW ADVANCED ENDPOINTS

    @GetMapping("/{symbol}/patterns")
    public ResponseEntity<List<TechnicalPatternService.Pattern>> getTechnicalPatterns(
            @PathVariable @ValidStockSymbol String symbol) {
        String upperSymbol = symbol.toUpperCase();
        TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(upperSymbol);

        return advancedAnalysisService.getHistoricalData(upperSymbol, "daily", "compact")
                .thenApply(historicalData -> {
                    List<TechnicalPatternService.Pattern> patterns = patternService.detectPatterns(historicalData, indicators);
                    return ResponseEntity.ok(patterns);
                }).join();
    }

    @GetMapping("/{symbol}/risk")
    public CompletableFuture<ResponseEntity<RiskAssessmentService.RiskMetrics>> getRiskAssessment(
            @PathVariable @ValidStockSymbol String symbol) {
        String upperSymbol = symbol.toUpperCase();

        return advancedAnalysisService.getRealTimeQuote(upperSymbol)
                .thenCompose(quote -> {
                    TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(upperSymbol);
                    TradingSignal signal = advancedAnalysisService.generateTradingSignal(upperSymbol);

                    return advancedAnalysisService.getHistoricalData(upperSymbol, "daily", "compact")
                            .thenApply(historicalData -> {
                                RiskAssessmentService.RiskMetrics risk = riskService.assessRisk(
                                    upperSymbol, quote, indicators, historicalData, signal);
                                return ResponseEntity.ok(risk);
                            });
                });
    }

    @GetMapping("/{symbol}/prediction")
    public CompletableFuture<ResponseEntity<PricePredictionService.PricePrediction>> getPricePrediction(
            @PathVariable @ValidStockSymbol String symbol) {
        String upperSymbol = symbol.toUpperCase();

        return advancedAnalysisService.getRealTimeQuote(upperSymbol)
                .thenCompose(quote -> {
                    TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(upperSymbol);

                    return advancedAnalysisService.getHistoricalData(upperSymbol, "daily", "compact")
                            .thenApply(historicalData -> {
                                PricePredictionService.PricePrediction prediction = predictionService.predictPrice(
                                    upperSymbol, quote, indicators, historicalData);
                                return ResponseEntity.ok(prediction);
                            });
                });
    }

    @GetMapping("/{symbol}/analysis/complete")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getCompleteAnalysis(
            @PathVariable @ValidStockSymbol String symbol) {
        String upperSymbol = symbol.toUpperCase();

        return advancedAnalysisService.getRealTimeQuote(upperSymbol)
                .thenCompose(quote -> {
                    TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(upperSymbol);
                    TradingSignal signal = advancedAnalysisService.generateTradingSignal(upperSymbol);

                    return advancedAnalysisService.getHistoricalData(upperSymbol, "daily", "compact")
                            .thenApply(historicalData -> {
                                Map<String, Object> analysis = new HashMap<>();

                                // Core data
                                analysis.put("quote", quote);
                                analysis.put("technicalIndicators", indicators);
                                analysis.put("tradingSignal", signal);

                                // Advanced analysis
                                analysis.put("patterns", patternService.detectPatterns(historicalData, indicators));
                                analysis.put("riskMetrics", riskService.assessRisk(upperSymbol, quote, indicators, historicalData, signal));
                                analysis.put("pricePrediction", predictionService.predictPrice(upperSymbol, quote, indicators, historicalData));
                                analysis.put("alerts", predictionService.checkAlerts(upperSymbol, quote, indicators));

                                analysis.put("timestamp", System.currentTimeMillis());
                                return ResponseEntity.ok(analysis);
                            });
                });
    }

    @PostMapping("/{symbol}/alerts")
    public ResponseEntity<Map<String, Object>> createAlert(
            @PathVariable String symbol,
            @RequestBody Map<String, Object> alertRequest) {

        String type = (String) alertRequest.get("type");
        Double threshold = ((Number) alertRequest.get("threshold")).doubleValue();
        String message = (String) alertRequest.get("message");

        String alertId = predictionService.createAlert(symbol.toUpperCase(), type, threshold, message);

        Map<String, Object> response = new HashMap<>();
        response.put("alertId", alertId);
        response.put("message", "Alert created successfully");
        response.put("symbol", symbol.toUpperCase());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{symbol}/alerts")
    public ResponseEntity<List<PricePredictionService.AlertRule>> getActiveAlerts(@PathVariable String symbol) {
        List<PricePredictionService.AlertRule> alerts = predictionService.getActiveAlerts(symbol.toUpperCase());
        return ResponseEntity.ok(alerts);
    }

    @DeleteMapping("/alerts/{alertId}")
    public ResponseEntity<Map<String, Object>> removeAlert(@PathVariable String alertId) {
        boolean removed = predictionService.removeAlert(alertId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", removed);
        response.put("message", removed ? "Alert removed successfully" : "Alert not found");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/portfolio/analyze")
    public ResponseEntity<RiskAssessmentService.PortfolioMetrics> analyzePortfolio(
            @RequestBody Map<String, Object> portfolioRequest) {

        @SuppressWarnings("unchecked")
        Map<String, Double> holdings = (Map<String, Double>) portfolioRequest.get("holdings");

        // Calculate risk metrics for each holding
        Map<String, RiskAssessmentService.RiskMetrics> stockRisks = new HashMap<>();

        for (String symbol : holdings.keySet()) {
            try {
                StockQuote quote = advancedAnalysisService.getRealTimeQuote(symbol).join();
                TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(symbol);
                TradingSignal signal = advancedAnalysisService.generateTradingSignal(symbol);
                List<Map<String, Object>> historicalData = advancedAnalysisService.getHistoricalData(symbol, "daily", "compact").join();

                RiskAssessmentService.RiskMetrics risk = riskService.assessRisk(symbol, quote, indicators, historicalData, signal);
                stockRisks.put(symbol, risk);
            } catch (Exception e) {
                // Skip stocks that fail to load
                continue;
            }
        }

        RiskAssessmentService.PortfolioMetrics portfolio = riskService.analyzePortfolio(holdings, stockRisks);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/{symbol}/comparison")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> compareWithMarket(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "SPY") String benchmarkSymbol) {

        String upperSymbol = symbol.toUpperCase();
        String upperBenchmark = benchmarkSymbol.toUpperCase();

        CompletableFuture<Map<String, Object>> symbolDataFuture = getSymbolComparisonData(upperSymbol);
        CompletableFuture<Map<String, Object>> benchmarkDataFuture = getSymbolComparisonData(upperBenchmark);

        return CompletableFuture.allOf(symbolDataFuture, benchmarkDataFuture)
                .thenApply(v -> {
                    Map<String, Object> comparison = new HashMap<>();
                    comparison.put("symbol", symbolDataFuture.join());
                    comparison.put("benchmark", benchmarkDataFuture.join());
                    comparison.put("benchmarkSymbol", upperBenchmark);

                    // Calculate relative performance
                    Map<String, Object> symbolData = symbolDataFuture.join();
                    Map<String, Object> benchmarkData = benchmarkDataFuture.join();

                    if (symbolData.get("quote") != null && benchmarkData.get("quote") != null) {
                        StockQuote symbolQuote = (StockQuote) symbolData.get("quote");
                        StockQuote benchmarkQuote = (StockQuote) benchmarkData.get("quote");

                        double relativePerformance = symbolQuote.getChangePercent() - benchmarkQuote.getChangePercent();
                        comparison.put("relativePerformance", relativePerformance);
                        comparison.put("outperforming", relativePerformance > 0);
                    }

                    return ResponseEntity.ok(comparison);
                });
    }

    private CompletableFuture<Map<String, Object>> getSymbolComparisonData(String symbol) {
        return advancedAnalysisService.getRealTimeQuote(symbol)
                .thenCompose(quote -> {
                    TechnicalIndicators indicators = advancedAnalysisService.calculateTechnicalIndicators(symbol);
                    TradingSignal signal = advancedAnalysisService.generateTradingSignal(symbol);

                    return advancedAnalysisService.getHistoricalData(symbol, "daily", "compact")
                            .thenApply(historicalData -> {
                                Map<String, Object> data = new HashMap<>();
                                data.put("quote", quote);
                                data.put("indicators", indicators);
                                data.put("signal", signal);
                                data.put("risk", riskService.assessRisk(symbol, quote, indicators, historicalData, signal));
                                return data;
                            });
                });
    }

    @GetMapping("/{symbol}/info")
    public ResponseEntity<Map<String, Object>> getCompanyInfo(@PathVariable String symbol) {
        return ResponseEntity.ok(stockDataService.getCompanyInfo(symbol));
    }

    @PostMapping("/watchlist")
    public ResponseEntity<Map<String, Object>> addToWatchlist(@RequestBody Map<String, String> request) {
        String symbol = request.get("symbol");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added " + symbol + " to watchlist");
        response.put("symbol", symbol.toUpperCase());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/market/status")
    public ResponseEntity<Map<String, Object>> getMarketStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isOpen", true);
        status.put("nextOpen", "9:30 AM EST");
        status.put("nextClose", "4:00 PM EST");
        status.put("timezone", "America/New_York");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}
