package com.aiframework.controller;

import com.aiframework.dto.StockQuote;
import com.aiframework.service.AdvancedStockAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class StockWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(StockWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final AdvancedStockAnalysisService analysisService;
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();

    public StockWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   AdvancedStockAnalysisService analysisService) {
        this.messagingTemplate = messagingTemplate;
        this.analysisService = analysisService;
    }

    @MessageMapping("/subscribe")
    @SendTo("/topic/stock-updates")
    public String subscribeToStock(String symbol) {
        subscribedSymbols.add(symbol.toUpperCase());
        logger.info("Client subscribed to stock: {}", symbol);
        return "Subscribed to " + symbol;
    }

    @MessageMapping("/unsubscribe")
    @SendTo("/topic/stock-updates")
    public String unsubscribeFromStock(String symbol) {
        subscribedSymbols.remove(symbol.toUpperCase());
        logger.info("Client unsubscribed from stock: {}", symbol);
        return "Unsubscribed from " + symbol;
    }

    @Scheduled(fixedRate = 5000) // Update every 5 seconds
    public void broadcastStockUpdates() {
        for (String symbol : subscribedSymbols) {
            try {
                analysisService.getRealTimeQuote(symbol)
                    .thenAccept(quote -> {
                        messagingTemplate.convertAndSend("/topic/quotes/" + symbol, quote);
                    })
                    .exceptionally(throwable -> {
                        logger.error("Failed to get quote for {}: {}", symbol, throwable.getMessage());
                        return null;
                    });
            } catch (Exception e) {
                logger.error("Error broadcasting update for {}: {}", symbol, e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 30000) // Update indicators every 30 seconds
    public void broadcastIndicatorUpdates() {
        for (String symbol : subscribedSymbols) {
            try {
                var indicators = analysisService.calculateTechnicalIndicators(symbol);
                var signal = analysisService.generateTradingSignal(symbol);

                messagingTemplate.convertAndSend("/topic/indicators/" + symbol, indicators);
                messagingTemplate.convertAndSend("/topic/signals/" + symbol, signal);
            } catch (Exception e) {
                logger.error("Error broadcasting indicators for {}: {}", symbol, e.getMessage());
            }
        }
    }
}
