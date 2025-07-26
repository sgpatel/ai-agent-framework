package com.aiframework.controller;

import com.aiframework.exception.StockDataNotFoundException;
import com.aiframework.service.StockDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for StockController
 */
@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockDataService stockDataService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> mockStockData;

    @BeforeEach
    void setUp() {
        mockStockData = new HashMap<>();
        mockStockData.put("symbol", "AAPL");
        mockStockData.put("price", 150.25);
        mockStockData.put("change", 2.15);
        mockStockData.put("changePercent", 1.45);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getStockPrice_ValidSymbol_ReturnsStockData() throws Exception {
        // Given
        when(stockDataService.getStockPrice("AAPL")).thenReturn(mockStockData);

        // When & Then
        mockMvc.perform(get("/api/stocks/AAPL/price"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.price").value(150.25));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getStockPrice_InvalidSymbol_ReturnsNotFound() throws Exception {
        // Given
        when(stockDataService.getStockPrice(anyString()))
                .thenThrow(new StockDataNotFoundException("INVALID"));

        // When & Then
        mockMvc.perform(get("/api/stocks/INVALID/price"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Stock Data Not Found"));
    }

    @Test
    void getStockPrice_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/stocks/AAPL/price"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getStockPrice_InvalidSymbolFormat_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/stocks/TOOLONGSYMBOL12345/price"))
                .andExpect(status().isBadRequest());
    }
}
