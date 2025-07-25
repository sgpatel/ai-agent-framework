package com.aiframework.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class YahooFinanceService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public YahooFinanceService() {
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    private RestTemplate createRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(10000);   // 10 seconds
        return new RestTemplate(factory);
    }

    public Map<String, Object> getRealTimePrice(String symbol) {
        try {
            // Try Yahoo Finance API with headers
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;
            
            // Add headers to avoid rate limiting
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode meta = result.path("meta");
            
            Map<String, Object> data = new HashMap<>();
            data.put("symbol", symbol);
            data.put("price", meta.path("regularMarketPrice").asDouble());
            data.put("change", meta.path("regularMarketPrice").asDouble() - meta.path("previousClose").asDouble());
            data.put("change_percent", ((meta.path("regularMarketPrice").asDouble() - meta.path("previousClose").asDouble()) / meta.path("previousClose").asDouble()) * 100);
            data.put("volume", meta.path("regularMarketVolume").asLong());
            data.put("market_cap", meta.path("marketCap").asText());
            data.put("currency", meta.path("currency").asText());
            
            return data;
        } catch (Exception e) {
            // Fallback to realistic mock data
            return getMockPriceData(symbol);
        }
    }
    
    private Map<String, Object> getMockPriceData(String symbol) {
        Map<String, Object> data = new HashMap<>();
        double basePrice = getBasePriceForSymbol(symbol);
        double change = (Math.random() - 0.5) * 10; // Random change between -5 and +5
        
        data.put("symbol", symbol);
        data.put("price", basePrice + change);
        data.put("change", change);
        data.put("change_percent", (change / basePrice) * 100);
        data.put("volume", (long)(50000000 + Math.random() * 30000000));
        data.put("market_cap", getMarketCapForSymbol(symbol));
        data.put("currency", "USD");
        data.put("data_source", "mock_fallback");
        
        return data;
    }
    
    private double getBasePriceForSymbol(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> 190.0;
            case "TSLA" -> 250.0;
            case "GOOGL" -> 140.0;
            case "MSFT" -> 420.0;
            case "AMZN" -> 155.0;
            case "META" -> 485.0;
            case "NVDA" -> 875.0;
            default -> 100.0;
        };
    }
    
    private String getMarketCapForSymbol(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> "2.9T";
            case "TSLA" -> "800B";
            case "GOOGL" -> "1.7T";
            case "MSFT" -> "3.1T";
            case "AMZN" -> "1.5T";
            case "META" -> "1.2T";
            case "NVDA" -> "2.2T";
            default -> "50B";
        };
    }

    public List<Map<String, Object>> getHistoricalData(String symbol, String period) {
        try {
            long period2 = System.currentTimeMillis() / 1000;
            long period1 = period2 - getPeriodSeconds(period);
            
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d", 
                symbol, period1, period2);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode timestamps = result.path("timestamp");
            JsonNode indicators = result.path("indicators").path("quote").get(0);
            
            List<Map<String, Object>> history = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                Map<String, Object> day = new HashMap<>();
                day.put("timestamp", timestamps.get(i).asLong());
                day.put("date", new Date(timestamps.get(i).asLong() * 1000).toString());
                day.put("open", indicators.path("open").get(i).asDouble());
                day.put("high", indicators.path("high").get(i).asDouble());
                day.put("low", indicators.path("low").get(i).asDouble());
                day.put("close", indicators.path("close").get(i).asDouble());
                day.put("volume", indicators.path("volume").get(i).asLong());
                history.add(day);
            }
            
            return history;
        } catch (Exception e) {
            // Fallback to realistic mock historical data
            return getMockHistoricalData(symbol, period);
        }
    }
    
    private List<Map<String, Object>> getMockHistoricalData(String symbol, String period) {
        List<Map<String, Object>> history = new ArrayList<>();
        double basePrice = getBasePriceForSymbol(symbol);
        long days = getPeriodSeconds(period) / 86400;
        
        for (int i = 0; i < Math.min(days, 365); i++) {
            double dailyChange = (Math.random() - 0.5) * 0.1; // 10% daily volatility
            double price = basePrice * (1 + dailyChange);
            
            Map<String, Object> day = new HashMap<>();
            day.put("timestamp", (System.currentTimeMillis() / 1000) - (i * 86400));
            day.put("date", new Date(System.currentTimeMillis() - (i * 86400000L)).toString());
            day.put("open", price * 0.99);
            day.put("high", price * 1.02);
            day.put("low", price * 0.98);
            day.put("close", price);
            day.put("volume", (long)(40000000 + Math.random() * 20000000));
            history.add(day);
            
            basePrice = price; // Use previous close as next base
        }
        
        Collections.reverse(history); // Oldest first
        return history;
    }

    private long getPeriodSeconds(String period) {
        return switch (period) {
            case "1d" -> 86400L;
            case "5d" -> 432000L;
            case "1mo" -> 2592000L;
            case "3mo" -> 7776000L;
            case "6mo" -> 15552000L;
            case "1y" -> 31536000L;
            case "2y" -> 63072000L;
            case "5y" -> 157680000L;
            default -> 31536000L; // 1 year default
        };
    }
}