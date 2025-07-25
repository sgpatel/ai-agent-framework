package com.aiframework.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aiframework.dto.*;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class AgentRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AgentRegistryService.class);

    @Autowired
    private IntelligentAgentCommunicationHub communicationHub;

    @PostConstruct
    public void initializeAgents() {
        registerBuiltInAgents();
        logger.info("Agent registry initialized with built-in agents");
    }

    private void registerBuiltInAgents() {
        // Stock Analysis Agent
        communicationHub.registerAgent("stock-analyzer",
            new AgentCapability("stock-analyzer",
                "Advanced stock analysis with technical and fundamental indicators",
                Arrays.asList("technical-analysis", "price-prediction", "trend-analysis", "volume-analysis"),
                "Stock Market Analysis"));

        // Risk Assessment Agent
        communicationHub.registerAgent("risk-assessor",
            new AgentCapability("risk-assessor",
                "Comprehensive risk analysis and portfolio risk management",
                Arrays.asList("risk-calculation", "volatility-analysis", "correlation-analysis", "var-calculation"),
                "Risk Management"));

        // Trading Strategy Agent
        communicationHub.registerAgent("strategy-generator",
            new AgentCapability("strategy-generator",
                "Creates and optimizes trading strategies based on market conditions",
                Arrays.asList("strategy-creation", "backtesting", "optimization", "signal-generation"),
                "Trading Strategies"));

        // Pattern Recognition Agent
        communicationHub.registerAgent("pattern-recognizer",
            new AgentCapability("pattern-recognizer",
                "Identifies chart patterns and technical formations",
                Arrays.asList("pattern-detection", "chart-analysis", "formation-recognition", "breakout-prediction"),
                "Pattern Recognition"));

        // Market Sentiment Agent
        communicationHub.registerAgent("sentiment-analyzer",
            new AgentCapability("sentiment-analyzer",
                "Analyzes market sentiment from multiple data sources",
                Arrays.asList("sentiment-analysis", "news-analysis", "social-media-monitoring", "market-mood"),
                "Market Sentiment"));

        // Customer Support Agent
        communicationHub.registerAgent("customer-support",
            new AgentCapability("customer-support",
                "Provides intelligent customer support and education",
                Arrays.asList("customer-assistance", "education", "explanation", "guidance"),
                "Customer Support"));

        // Portfolio Manager Agent
        communicationHub.registerAgent("portfolio-manager",
            new AgentCapability("portfolio-manager",
                "Manages portfolio allocation and rebalancing strategies",
                Arrays.asList("portfolio-optimization", "asset-allocation", "rebalancing", "diversification"),
                "Portfolio Management"));

        // Decision Coordinator Agent
        communicationHub.registerAgent("decision-coordinator",
            new AgentCapability("decision-coordinator",
                "Coordinates complex decisions across multiple agents",
                Arrays.asList("decision-synthesis", "conflict-resolution", "priority-management", "coordination"),
                "Decision Coordination"));
    }

    public void registerCustomAgent(String agentId, String description, List<String> capabilities, String specialization) {
        AgentCapability capability = new AgentCapability(agentId, description, capabilities, specialization);
        communicationHub.registerAgent(agentId, capability);
        logger.info("Registered custom agent: {} with specialization: {}", agentId, specialization);
    }

    public List<AgentCapability> getAgentsByCapability(String capability) {
        return communicationHub.getAvailableAgents().stream()
            .filter(agent -> agent.getCapabilities().contains(capability))
            .toList();
    }

    public List<AgentCapability> getAgentsBySpecialization(String specialization) {
        return communicationHub.getAvailableAgents().stream()
            .filter(agent -> agent.getSpecialization().equalsIgnoreCase(specialization))
            .toList();
    }
}
