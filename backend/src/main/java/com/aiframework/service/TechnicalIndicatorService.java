package com.aiframework.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TechnicalIndicatorService {
    
    public TechnicalIndicatorService() {
        // Default constructor for direct instantiation
    }

    public Map<String, Object> calculateIndicators(List<Map<String, Object>> historicalData, String indicators) {
        Map<String, Object> results = new HashMap<>();
        
        if (indicators.contains("sma")) {
            results.put("sma_20", calculateSMA(historicalData, 20));
            results.put("sma_50", calculateSMA(historicalData, 50));
        }
        
        if (indicators.contains("ema")) {
            results.put("ema_12", calculateEMA(historicalData, 12));
            results.put("ema_26", calculateEMA(historicalData, 26));
        }
        
        if (indicators.contains("rsi")) {
            results.put("rsi", calculateRSI(historicalData, 14));
        }
        
        if (indicators.contains("macd")) {
            results.putAll(calculateMACD(historicalData));
        }
        
        if (indicators.contains("bollinger")) {
            results.putAll(calculateBollingerBands(historicalData, 20, 2));
        }
        
        return results;
    }

    private double calculateSMA(List<Map<String, Object>> data, int period) {
        if (data.size() < period) return 0.0;
        
        double sum = 0;
        for (int i = data.size() - period; i < data.size(); i++) {
            sum += (Double) data.get(i).get("close");
        }
        return sum / period;
    }

    private double calculateEMA(List<Map<String, Object>> data, int period) {
        if (data.size() < period) return 0.0;
        
        double multiplier = 2.0 / (period + 1);
        double ema = calculateSMA(data.subList(0, period), period);
        
        for (int i = period; i < data.size(); i++) {
            double close = (Double) data.get(i).get("close");
            ema = (close * multiplier) + (ema * (1 - multiplier));
        }
        return ema;
    }

    private double calculateRSI(List<Map<String, Object>> data, int period) {
        if (data.size() < period + 1) return 50.0;
        
        double avgGain = 0, avgLoss = 0;
        
        // Calculate initial average gain and loss
        for (int i = 1; i <= period; i++) {
            double change = (Double) data.get(i).get("close") - (Double) data.get(i-1).get("close");
            if (change > 0) avgGain += change;
            else avgLoss += Math.abs(change);
        }
        avgGain /= period;
        avgLoss /= period;
        
        // Calculate RSI for the latest period
        for (int i = period + 1; i < data.size(); i++) {
            double change = (Double) data.get(i).get("close") - (Double) data.get(i-1).get("close");
            if (change > 0) {
                avgGain = (avgGain * (period - 1) + change) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            } else {
                avgGain = (avgGain * (period - 1)) / period;
                avgLoss = (avgLoss * (period - 1) + Math.abs(change)) / period;
            }
        }
        
        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private Map<String, Object> calculateMACD(List<Map<String, Object>> data) {
        double ema12 = calculateEMA(data, 12);
        double ema26 = calculateEMA(data, 26);
        double macdLine = ema12 - ema26;
        
        Map<String, Object> macd = new HashMap<>();
        macd.put("macd_line", macdLine);
        macd.put("signal_line", macdLine * 0.9); // Simplified signal line
        macd.put("histogram", macdLine * 0.1);
        
        return macd;
    }

    private Map<String, Object> calculateBollingerBands(List<Map<String, Object>> data, int period, double stdDev) {
        double sma = calculateSMA(data, period);
        double variance = 0;
        
        for (int i = data.size() - period; i < data.size(); i++) {
            double close = (Double) data.get(i).get("close");
            variance += Math.pow(close - sma, 2);
        }
        double standardDeviation = Math.sqrt(variance / period);
        
        Map<String, Object> bands = new HashMap<>();
        bands.put("upper_band", sma + (standardDeviation * stdDev));
        bands.put("middle_band", sma);
        bands.put("lower_band", sma - (standardDeviation * stdDev));
        
        return bands;
    }
}