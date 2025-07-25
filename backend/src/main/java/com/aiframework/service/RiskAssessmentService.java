package com.aiframework.service;

import com.aiframework.dto.StockQuote;
import com.aiframework.dto.TechnicalIndicators;
import com.aiframework.dto.TradingSignal;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RiskAssessmentService {

    public static class RiskMetrics {
        private String symbol;
        private double volatilityRisk;
        private double liquidityRisk;
        private double marketRisk;
        private double technicalRisk;
        private double overallRisk;
        private String riskLevel; // LOW, MEDIUM, HIGH, EXTREME
        private List<String> riskFactors;
        private Map<String, Double> riskBreakdown;
        private LocalDateTime timestamp;

        public RiskMetrics(String symbol) {
            this.symbol = symbol;
            this.riskFactors = new ArrayList<>();
            this.riskBreakdown = new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public double getVolatilityRisk() { return volatilityRisk; }
        public void setVolatilityRisk(double volatilityRisk) { this.volatilityRisk = volatilityRisk; }

        public double getLiquidityRisk() { return liquidityRisk; }
        public void setLiquidityRisk(double liquidityRisk) { this.liquidityRisk = liquidityRisk; }

        public double getMarketRisk() { return marketRisk; }
        public void setMarketRisk(double marketRisk) { this.marketRisk = marketRisk; }

        public double getTechnicalRisk() { return technicalRisk; }
        public void setTechnicalRisk(double technicalRisk) { this.technicalRisk = technicalRisk; }

        public double getOverallRisk() { return overallRisk; }
        public void setOverallRisk(double overallRisk) { this.overallRisk = overallRisk; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

        public Map<String, Double> getRiskBreakdown() { return riskBreakdown; }
        public void setRiskBreakdown(Map<String, Double> riskBreakdown) { this.riskBreakdown = riskBreakdown; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class PortfolioMetrics {
        private double totalValue;
        private double expectedReturn;
        private double portfolioVolatility;
        private double sharpeRatio;
        private double maxDrawdown;
        private double beta;
        private double alpha;
        private Map<String, Double> assetWeights;
        private Map<String, Double> sectorWeights;
        private String riskProfile; // CONSERVATIVE, MODERATE, AGGRESSIVE
        private List<String> recommendations;

        public PortfolioMetrics() {
            this.assetWeights = new HashMap<>();
            this.sectorWeights = new HashMap<>();
            this.recommendations = new ArrayList<>();
        }

        // Getters and setters
        public double getTotalValue() { return totalValue; }
        public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

        public double getExpectedReturn() { return expectedReturn; }
        public void setExpectedReturn(double expectedReturn) { this.expectedReturn = expectedReturn; }

        public double getPortfolioVolatility() { return portfolioVolatility; }
        public void setPortfolioVolatility(double portfolioVolatility) { this.portfolioVolatility = portfolioVolatility; }

        public double getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(double sharpeRatio) { this.sharpeRatio = sharpeRatio; }

        public double getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

        public double getBeta() { return beta; }
        public void setBeta(double beta) { this.beta = beta; }

        public double getAlpha() { return alpha; }
        public void setAlpha(double alpha) { this.alpha = alpha; }

        public Map<String, Double> getAssetWeights() { return assetWeights; }
        public void setAssetWeights(Map<String, Double> assetWeights) { this.assetWeights = assetWeights; }

        public Map<String, Double> getSectorWeights() { return sectorWeights; }
        public void setSectorWeights(Map<String, Double> sectorWeights) { this.sectorWeights = sectorWeights; }

        public String getRiskProfile() { return riskProfile; }
        public void setRiskProfile(String riskProfile) { this.riskProfile = riskProfile; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public RiskMetrics assessRisk(String symbol, StockQuote quote, TechnicalIndicators indicators,
                                  List<Map<String, Object>> historicalData, TradingSignal signal) {
        RiskMetrics risk = new RiskMetrics(symbol);

        // Calculate volatility risk
        risk.setVolatilityRisk(calculateVolatilityRisk(historicalData));

        // Calculate liquidity risk
        risk.setLiquidityRisk(calculateLiquidityRisk(quote));

        // Calculate market risk
        risk.setMarketRisk(calculateMarketRisk(quote, indicators));

        // Calculate technical risk
        risk.setTechnicalRisk(calculateTechnicalRisk(indicators, signal));

        // Calculate overall risk
        risk.setOverallRisk(calculateOverallRisk(risk));

        // Determine risk level
        risk.setRiskLevel(determineRiskLevel(risk.getOverallRisk()));

        // Populate risk breakdown
        risk.getRiskBreakdown().put("Volatility", risk.getVolatilityRisk());
        risk.getRiskBreakdown().put("Liquidity", risk.getLiquidityRisk());
        risk.getRiskBreakdown().put("Market", risk.getMarketRisk());
        risk.getRiskBreakdown().put("Technical", risk.getTechnicalRisk());

        // Add risk factors
        populateRiskFactors(risk, quote, indicators, signal);

        return risk;
    }

    private double calculateVolatilityRisk(List<Map<String, Object>> historicalData) {
        if (historicalData == null || historicalData.size() < 20) return 0.5;

        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < historicalData.size(); i++) {
            double currentPrice = Double.parseDouble(historicalData.get(i).get("close").toString());
            double previousPrice = Double.parseDouble(historicalData.get(i-1).get("close").toString());
            double dailyReturn = (currentPrice - previousPrice) / previousPrice;
            returns.add(dailyReturn);
        }

        double volatility = calculateStandardDeviation(returns) * Math.sqrt(252); // Annualized

        // Normalize volatility to 0-1 scale (0.3 = high volatility)
        return Math.min(volatility / 0.3, 1.0);
    }

    private double calculateLiquidityRisk(StockQuote quote) {
        if (quote == null || quote.getVolume() <= 0) return 0.7;

        // Higher volume = lower liquidity risk
        double avgVolume = 1000000; // Baseline average volume
        double volumeRatio = avgVolume / quote.getVolume();

        return Math.min(volumeRatio * 0.5, 1.0);
    }

    private double calculateMarketRisk(StockQuote quote, TechnicalIndicators indicators) {
        double risk = 0.0;

        // Price change risk
        if (quote != null) {
            double changePercent = Math.abs(quote.getChangePercent());
            risk += Math.min(changePercent / 10.0, 0.3); // Large moves = higher risk
        }

        // Technical indicator risk
        if (indicators != null && indicators.getRsi() != null) {
            if (indicators.getRsi() > 80 || indicators.getRsi() < 20) {
                risk += 0.2; // Extreme RSI levels
            }
        }

        return Math.min(risk, 1.0);
    }

    private double calculateTechnicalRisk(TechnicalIndicators indicators, TradingSignal signal) {
        double risk = 0.0;

        if (signal != null) {
            // Lower confidence = higher risk
            risk += (1.0 - signal.getConfidence()) * 0.4;

            // Strong signals in either direction = lower risk
            if (signal.getSignal() == TradingSignal.SignalType.STRONG_BUY ||
                signal.getSignal() == TradingSignal.SignalType.STRONG_SELL) {
                risk -= 0.1;
            }
        }

        if (indicators != null && indicators.getAtr() != null) {
            // Higher ATR = higher technical risk
            risk += Math.min(indicators.getAtr() / 5.0, 0.3);
        }

        return Math.max(0.0, Math.min(risk, 1.0));
    }

    private double calculateOverallRisk(RiskMetrics risk) {
        return (risk.getVolatilityRisk() * 0.35 +
                risk.getLiquidityRisk() * 0.25 +
                risk.getMarketRisk() * 0.25 +
                risk.getTechnicalRisk() * 0.15);
    }

    private String determineRiskLevel(double overallRisk) {
        if (overallRisk < 0.25) return "LOW";
        else if (overallRisk < 0.5) return "MEDIUM";
        else if (overallRisk < 0.75) return "HIGH";
        else return "EXTREME";
    }

    private void populateRiskFactors(RiskMetrics risk, StockQuote quote,
                                   TechnicalIndicators indicators, TradingSignal signal) {
        if (risk.getVolatilityRisk() > 0.6) {
            risk.getRiskFactors().add("High price volatility detected");
        }

        if (risk.getLiquidityRisk() > 0.6) {
            risk.getRiskFactors().add("Low trading volume - liquidity concerns");
        }

        if (quote != null && Math.abs(quote.getChangePercent()) > 5) {
            risk.getRiskFactors().add("Significant price movement in current session");
        }

        if (indicators != null && indicators.getRsi() != null) {
            if (indicators.getRsi() > 80) {
                risk.getRiskFactors().add("Extremely overbought conditions");
            } else if (indicators.getRsi() < 20) {
                risk.getRiskFactors().add("Extremely oversold conditions");
            }
        }

        if (signal != null && signal.getConfidence() < 0.5) {
            risk.getRiskFactors().add("Low confidence in technical signals");
        }
    }

    public PortfolioMetrics analyzePortfolio(Map<String, Double> holdings,
                                           Map<String, RiskMetrics> stockRisks) {
        PortfolioMetrics portfolio = new PortfolioMetrics();

        double totalValue = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
        portfolio.setTotalValue(totalValue);

        // Calculate weights
        for (Map.Entry<String, Double> holding : holdings.entrySet()) {
            double weight = holding.getValue() / totalValue;
            portfolio.getAssetWeights().put(holding.getKey(), weight);
        }

        // Calculate portfolio risk metrics
        portfolio.setPortfolioVolatility(calculatePortfolioVolatility(holdings, stockRisks));
        portfolio.setExpectedReturn(calculateExpectedReturn(holdings, stockRisks));
        portfolio.setSharpeRatio(calculateSharpeRatio(portfolio));
        portfolio.setMaxDrawdown(calculateMaxDrawdown(holdings));
        portfolio.setBeta(calculatePortfolioBeta(holdings));

        // Determine risk profile
        portfolio.setRiskProfile(determinePortfolioRiskProfile(portfolio.getPortfolioVolatility()));

        // Generate recommendations
        generatePortfolioRecommendations(portfolio, stockRisks);

        return portfolio;
    }

    private double calculatePortfolioVolatility(Map<String, Double> holdings,
                                              Map<String, RiskMetrics> stockRisks) {
        double totalValue = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
        double weightedVolatility = 0.0;

        for (Map.Entry<String, Double> holding : holdings.entrySet()) {
            String symbol = holding.getKey();
            double weight = holding.getValue() / totalValue;
            RiskMetrics risk = stockRisks.get(symbol);

            if (risk != null) {
                weightedVolatility += weight * risk.getVolatilityRisk();
            }
        }

        return weightedVolatility;
    }

    private double calculateExpectedReturn(Map<String, Double> holdings,
                                         Map<String, RiskMetrics> stockRisks) {
        // Simplified expected return calculation
        double totalValue = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
        double weightedReturn = 0.0;

        for (Map.Entry<String, Double> holding : holdings.entrySet()) {
            double weight = holding.getValue() / totalValue;
            // Assume base return of 8% adjusted by risk
            RiskMetrics risk = stockRisks.get(holding.getKey());
            double expectedReturn = 0.08;

            if (risk != null) {
                expectedReturn = 0.08 + (risk.getOverallRisk() * 0.05); // Higher risk = higher expected return
            }

            weightedReturn += weight * expectedReturn;
        }

        return weightedReturn;
    }

    private double calculateSharpeRatio(PortfolioMetrics portfolio) {
        double riskFreeRate = 0.02; // Assume 2% risk-free rate
        return (portfolio.getExpectedReturn() - riskFreeRate) / portfolio.getPortfolioVolatility();
    }

    private double calculateMaxDrawdown(Map<String, Double> holdings) {
        // Simplified max drawdown calculation
        return 0.15; // Assume 15% max drawdown
    }

    private double calculatePortfolioBeta(Map<String, Double> holdings) {
        // Simplified beta calculation
        return 1.0 + (holdings.size() * 0.1); // More holdings = slightly higher beta
    }

    private String determinePortfolioRiskProfile(double volatility) {
        if (volatility < 0.3) return "CONSERVATIVE";
        else if (volatility < 0.6) return "MODERATE";
        else return "AGGRESSIVE";
    }

    private void generatePortfolioRecommendations(PortfolioMetrics portfolio,
                                                Map<String, RiskMetrics> stockRisks) {
        if (portfolio.getPortfolioVolatility() > 0.7) {
            portfolio.getRecommendations().add("Portfolio risk is high - consider diversification");
        }

        if (portfolio.getSharpeRatio() < 0.5) {
            portfolio.getRecommendations().add("Poor risk-adjusted returns - review asset allocation");
        }

        if (portfolio.getAssetWeights().size() < 5) {
            portfolio.getRecommendations().add("Portfolio lacks diversification - consider adding more assets");
        }

        // Check for concentration risk
        for (Map.Entry<String, Double> weight : portfolio.getAssetWeights().entrySet()) {
            if (weight.getValue() > 0.3) {
                portfolio.getRecommendations().add("High concentration in " + weight.getKey() + " - consider reducing position");
            }
        }
    }

    private double calculateStandardDeviation(List<Double> values) {
        if (values.isEmpty()) return 0.0;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0.0);

        return Math.sqrt(variance);
    }
}
