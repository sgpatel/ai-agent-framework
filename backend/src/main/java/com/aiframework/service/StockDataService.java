package com.aiframework.service;

import com.aiframework.exception.StockDataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for fetching stock data from various sources
 */
@Service
@Slf4j
public class StockDataService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get current stock price
     */
    public Map<String, Object> getStockPrice(String symbol) throws StockDataNotFoundException {
        try {
            log.info("Fetching stock price for symbol: {}", symbol);
            
            // Mock implementation for now - replace with actual API calls
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("symbol", symbol);
            stockData.put("price", generateMockPrice());
            stockData.put("change", generateMockChange());
            stockData.put("changePercent", generateMockChangePercent());
            stockData.put("volume", generateMockVolume());
            stockData.put("timestamp", System.currentTimeMillis());
            
            return stockData;
            
        } catch (Exception ex) {
            log.error("Failed to fetch stock price for symbol: {}", symbol, ex);
            throw new StockDataNotFoundException("Unable to fetch stock price for " + symbol);
        }
    }

    /**
     * Get historical stock data
     */
    public List<Map<String, Object>> getHistoricalData(String symbol, String period) throws StockDataNotFoundException {
        try {
            log.info("Fetching historical data for symbol: {}, period: {}", symbol, period);
            
            // Mock implementation - replace with actual API calls
            List<Map<String, Object>> historicalData = new ArrayList<>();
            
            int dataPoints = getPeriodDataPoints(period);
            long currentTime = System.currentTimeMillis();
            double basePrice = 100.0 + (Math.random() * 500); // Random base price
            
            for (int i = dataPoints; i >= 0; i--) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", new Date(currentTime - (i * 24 * 60 * 60 * 1000L))); // Daily intervals
                dataPoint.put("open", basePrice + (Math.random() * 10 - 5));
                dataPoint.put("high", basePrice + (Math.random() * 15));
                dataPoint.put("low", basePrice - (Math.random() * 10));
                dataPoint.put("close", basePrice + (Math.random() * 10 - 5));
                dataPoint.put("volume", (long)(Math.random() * 10000000));
                
                historicalData.add(dataPoint);
                basePrice += (Math.random() * 4 - 2); // Slight drift
            }
            
            return historicalData;
            
        } catch (Exception ex) {
            log.error("Failed to fetch historical data for symbol: {}, period: {}", symbol, period, ex);
            throw new StockDataNotFoundException("Unable to fetch historical data for " + symbol);
        }
    }

    /**
     * Get company information
     */
    public Map<String, Object> getCompanyInfo(String symbol) {
        try {
            log.info("Fetching company info for symbol: {}", symbol);
            
            // Mock implementation
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("symbol", symbol);
            companyInfo.put("name", getCompanyName(symbol));
            companyInfo.put("sector", "Technology");
            companyInfo.put("industry", "Software");
            companyInfo.put("marketCap", (long)(Math.random() * 1000000000000L));
            companyInfo.put("employees", (int)(Math.random() * 100000));
            companyInfo.put("description", "A leading technology company");
            
            return companyInfo;
            
        } catch (Exception ex) {
            log.error("Failed to fetch company info for symbol: {}", symbol, ex);
            return Map.of("symbol", symbol, "error", "Company information not available");
        }
    }

    private int getPeriodDataPoints(String period) {
        return switch (period.toLowerCase()) {
            case "1d" -> 1;
            case "5d" -> 5;
            case "1mo" -> 30;
            case "3mo" -> 90;
            case "6mo" -> 180;
            case "1y" -> 365;
            case "2y" -> 730;
            case "5y" -> 1825;
            case "10y" -> 3650;
            default -> 365;
        };
    }

    private String getCompanyName(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> "Apple Inc.";
            case "GOOGL" -> "Alphabet Inc.";
            case "MSFT" -> "Microsoft Corporation";
            case "AMZN" -> "Amazon.com Inc.";
            case "TSLA" -> "Tesla Inc.";
            case "NVDA" -> "NVIDIA Corporation";
            case "META" -> "Meta Platforms Inc.";
            case "NFLX" -> "Netflix Inc.";
            case "TCS" -> "Tata Consultancy Services";
            case "INFY" -> "Infosys Limited";
            case "RIL" -> "Reliance Industries Limited";
            default -> symbol + " Corporation";
        };
    }

    private double generateMockPrice() {
        return 100.0 + (Math.random() * 500);
    }

    private double generateMockChange() {
        return (Math.random() * 20) - 10; // -10 to +10
    }

    private double generateMockChangePercent() {
        return (Math.random() * 10) - 5; // -5% to +5%
    }

    private long generateMockVolume() {
        return (long)(Math.random() * 50000000); // Up to 50M volume
    }
}
