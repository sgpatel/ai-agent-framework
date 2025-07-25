package com.aiframework.dto;

import java.time.LocalDateTime;

public class StockQuote {
    private String symbol;
    private double price;
    private double change;
    private double changePercent;
    private double volume;
    private double open;
    private double high;
    private double low;
    private double previousClose;
    private LocalDateTime timestamp;
    private String marketStatus;

    public StockQuote() {}

    public StockQuote(String symbol, double price, double change, double changePercent) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public double getOpen() { return open; }
    public void setOpen(double open) { this.open = open; }

    public double getHigh() { return high; }
    public void setHigh(double high) { this.high = high; }

    public double getLow() { return low; }
    public void setLow(double low) { this.low = low; }

    public double getPreviousClose() { return previousClose; }
    public void setPreviousClose(double previousClose) { this.previousClose = previousClose; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMarketStatus() { return marketStatus; }
    public void setMarketStatus(String marketStatus) { this.marketStatus = marketStatus; }
}
