package com.aiframework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StockDataService {
    
    @Autowired
    private YahooFinanceService yahooFinanceService;
    
    public Map<String, Object> getStockPrice(String symbol) {
        try {
            return yahooFinanceService.getRealTimePrice(symbol);
        } catch (Exception e) {
            // Fallback to mock data
            Map<String, Object> priceData = new HashMap<>();
            priceData.put("symbol", symbol);
            priceData.put("price", 152.34);
            priceData.put("error", "Failed to fetch real data: " + e.getMessage());
            return priceData;
        }
    }
    
    public List<Map<String, Object>> getHistoricalData(String symbol, String period) {
        try {
            return yahooFinanceService.getHistoricalData(symbol, period);
        } catch (Exception e) {
            // Fallback to mock data
            List<Map<String, Object>> history = new ArrayList<>();
            Map<String, Object> errorDay = new HashMap<>();
            errorDay.put("error", "Failed to fetch historical data: " + e.getMessage());
            history.add(errorDay);
            return history;
        }
    }
    
    public Map<String, Object> getCompanyInfo(String symbol) {
        Map<String, Object> info = new HashMap<>();
        info.put("symbol", symbol);
        info.put("name", "Apple Inc.");
        info.put("sector", "Technology");
        info.put("industry", "Consumer Electronics");
        info.put("employees", 164000);
        info.put("description", "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide.");
        return info;
    }
}