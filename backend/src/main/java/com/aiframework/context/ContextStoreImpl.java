package com.aiframework.context;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.Instant;

/**
 * Enhanced Context Store implementation with agent collaboration features
 */
@Service
public class ContextStoreImpl implements ContextStore {

    // Core storage maps
    private final Map<String, Map<String, Object>> agentContexts = new ConcurrentHashMap<>();
    private final Map<String, SharedDataEntry> sharedData = new ConcurrentHashMap<>();
    private final Map<String, List<String>> contextSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();
    private final Map<String, List<AgentRecommendation>> agentRecommendations = new ConcurrentHashMap<>();

    // Agent type mappings for intelligent recommendations
    private final Map<String, String> agentTypes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> agentCapabilities = new ConcurrentHashMap<>();

    public ContextStoreImpl() {
        // Initialize known agent types and capabilities
        initializeAgentCapabilities();
    }

    private void initializeAgentCapabilities() {
        // Stock analysis agents
        agentCapabilities.put("stock-analyzer", Arrays.asList(
            "financial-data", "time-series", "market-analysis", "technical-indicators"
        ));

        // Chart visualization agents
        agentCapabilities.put("chart-visualizer", Arrays.asList(
            "data-visualization", "interactive-charts", "technical-charts", "time-series-viz"
        ));

        // Risk assessment agents
        agentCapabilities.put("risk-assessor", Arrays.asList(
            "risk-analysis", "financial-metrics", "volatility-analysis", "portfolio-risk"
        ));

        // Technical analysis agents
        agentCapabilities.put("technical-analyzer", Arrays.asList(
            "pattern-recognition", "trend-analysis", "signal-generation", "market-indicators"
        ));

        // GPT4All chat agents
        agentCapabilities.put("gpt4all-chat", Arrays.asList(
            "natural-language", "conversation", "analysis-explanation", "decision-support"
        ));
    }

    // Basic context operations
    @Override
    public void storeContext(String agentId, String key, Object value) {
        agentContexts.computeIfAbsent(agentId, k -> new ConcurrentHashMap<>()).put(key, value);

        // Auto-generate recommendations when context changes
        generateContextBasedRecommendations(agentId);
    }

    @Override
    public Optional<Object> getContext(String agentId, String key) {
        Map<String, Object> context = agentContexts.get(agentId);
        return context != null ? Optional.ofNullable(context.get(key)) : Optional.empty();
    }

    @Override
    public Map<String, Object> getAllContext(String agentId) {
        return new HashMap<>(agentContexts.getOrDefault(agentId, Collections.emptyMap()));
    }

    @Override
    public void clearContext(String agentId) {
        agentContexts.remove(agentId);
        agentRecommendations.remove(agentId);
    }

    @Override
    public void clearAllContext() {
        agentContexts.clear();
        agentRecommendations.clear();
    }

    // Enhanced shared data operations
    @Override
    public void storeSharedData(String dataKey, Object data, String sourceAgent, Map<String, Object> metadata) {
        String dataType = extractDataType(metadata);
        SharedDataEntry entry = new SharedDataEntry(
            data, sourceAgent, Instant.now().toString(), metadata, dataType
        );
        sharedData.put(dataKey, entry);

        // Notify subscribers
        notifySubscribers(dataKey, data);

        // Generate collaborative recommendations
        generateCollaborativeRecommendations(dataKey, data, sourceAgent, dataType);
    }

    private String extractDataType(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.getOrDefault("dataType", "unknown") : "unknown";
    }

    @Override
    public Optional<SharedDataEntry> getSharedData(String dataKey) {
        return Optional.ofNullable(sharedData.get(dataKey));
    }

    @Override
    public Map<String, SharedDataEntry> getAllSharedData() {
        return new HashMap<>(sharedData);
    }

    @Override
    public void clearSharedData(String dataKey) {
        sharedData.remove(dataKey);
    }

    // Subscription management
    @Override
    public void subscribeToContext(String subscriberAgent, String contextKey) {
        contextSubscriptions.computeIfAbsent(contextKey, k -> new ArrayList<>()).add(subscriberAgent);
    }

    @Override
    public void unsubscribeFromContext(String subscriberAgent, String contextKey) {
        List<String> subscribers = contextSubscriptions.get(contextKey);
        if (subscribers != null) {
            subscribers.remove(subscriberAgent);
            if (subscribers.isEmpty()) {
                contextSubscriptions.remove(contextKey);
            }
        }
    }

    @Override
    public List<String> getSubscribers(String contextKey) {
        return new ArrayList<>(contextSubscriptions.getOrDefault(contextKey, Collections.emptyList()));
    }

    @Override
    public void notifySubscribers(String contextKey, Object data) {
        List<String> subscribers = getSubscribers(contextKey);
        for (String subscriber : subscribers) {
            // Generate notifications for each subscriber
            generateContextUpdateNotification(subscriber, contextKey, data);
        }
    }

    // Workflow management
    @Override
    public void createWorkflow(String workflowId, WorkflowDefinition workflow) {
        workflows.put(workflowId, workflow);
    }

    @Override
    public Optional<WorkflowDefinition> getWorkflow(String workflowId) {
        return Optional.ofNullable(workflows.get(workflowId));
    }

    @Override
    public List<WorkflowDefinition> getActiveWorkflows() {
        return workflows.values().stream()
            .filter(w -> w.getStatus() == WorkflowStatus.ACTIVE)
            .collect(Collectors.toList());
    }

    @Override
    public void updateWorkflowStatus(String workflowId, WorkflowStatus status) {
        WorkflowDefinition workflow = workflows.get(workflowId);
        if (workflow != null) {
            workflow.setStatus(status);
        }
    }

    // Agent recommendations
    @Override
    public void setAgentRecommendations(String agentId, List<AgentRecommendation> recommendations) {
        agentRecommendations.put(agentId, new ArrayList<>(recommendations));
    }

    @Override
    public List<AgentRecommendation> getAgentRecommendations(String agentId) {
        return new ArrayList<>(agentRecommendations.getOrDefault(agentId, Collections.emptyList()));
    }

    @Override
    public void clearAgentRecommendations(String agentId) {
        agentRecommendations.remove(agentId);
    }

    // Advanced querying and analysis
    @Override
    public List<String> findAgentsWithDataType(String dataType) {
        return agentContexts.entrySet().stream()
            .filter(entry -> {
                Object contextDataType = entry.getValue().get("dataType");
                return dataType.equals(contextDataType);
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> findCompatibleAgents(String sourceAgent, String dataType) {
        return agentCapabilities.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(sourceAgent))
            .filter(entry -> entry.getValue().contains(dataType) ||
                           entry.getValue().stream().anyMatch(cap -> cap.contains(dataType)))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> generateCollaborationSuggestions(String agentId) {
        Map<String, Object> suggestions = new HashMap<>();

        // Get agent's current context
        Map<String, Object> context = getAllContext(agentId);
        String dataType = (String) context.get("dataType");

        if (dataType != null) {
            // Find compatible agents
            List<String> compatibleAgents = findCompatibleAgents(agentId, dataType);
            suggestions.put("compatibleAgents", compatibleAgents);

            // Find relevant shared data
            List<String> relevantData = sharedData.entrySet().stream()
                .filter(entry -> dataType.equals(entry.getValue().getDataType()) ||
                               isDataTypeCompatible(dataType, entry.getValue().getDataType()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            suggestions.put("relevantSharedData", relevantData);

            // Generate workflow suggestions
            suggestions.put("workflowSuggestions", generateWorkflowSuggestions(agentId, dataType));
        }

        return suggestions;
    }

    // Helper methods for intelligent recommendations
    private void generateContextBasedRecommendations(String agentId) {
        Map<String, Object> context = getAllContext(agentId);
        String dataType = (String) context.get("dataType");

        if (dataType != null) {
            List<AgentRecommendation> recommendations = createRecommendationsForDataType(agentId, dataType);
            if (!recommendations.isEmpty()) {
                setAgentRecommendations(agentId, recommendations);
            }
        }
    }

    private void generateCollaborativeRecommendations(String dataKey, Object data, String sourceAgent, String dataType) {
        List<String> compatibleAgents = findCompatibleAgents(sourceAgent, dataType);

        for (String targetAgent : compatibleAgents) {
            List<AgentRecommendation> recommendations = createCollaborativeRecommendations(
                sourceAgent, targetAgent, dataKey, dataType
            );

            if (!recommendations.isEmpty()) {
                List<AgentRecommendation> existing = getAgentRecommendations(targetAgent);
                existing.addAll(recommendations);
                setAgentRecommendations(targetAgent, existing);
            }
        }
    }

    private List<AgentRecommendation> createRecommendationsForDataType(String agentId, String dataType) {
        List<AgentRecommendation> recommendations = new ArrayList<>();

        switch (dataType) {
            case "stock-analysis":
                recommendations.add(new AgentRecommendation(
                    "chart-viz-" + System.currentTimeMillis(),
                    "visualization",
                    "Create Interactive Stock Chart",
                    "Visualize the stock analysis data with interactive charts",
                    "chart-visualizer",
                    "create-chart",
                    "high",
                    Arrays.asList("stockData"),
                    Map.of("chartTypes", Arrays.asList("candlestick", "line", "volume"))
                ));
                break;

            case "time-series":
                recommendations.add(new AgentRecommendation(
                    "pattern-analysis-" + System.currentTimeMillis(),
                    "analysis",
                    "Detect Chart Patterns",
                    "Analyze time series data for technical patterns",
                    "technical-analyzer",
                    "detect-patterns",
                    "medium",
                    Arrays.asList("historicalData"),
                    Map.of("patterns", Arrays.asList("head-shoulders", "triangles", "support-resistance"))
                ));
                break;

            case "financial-data":
                recommendations.add(new AgentRecommendation(
                    "risk-assessment-" + System.currentTimeMillis(),
                    "analysis",
                    "Perform Risk Assessment",
                    "Analyze financial data for risk factors and portfolio implications",
                    "risk-assessor",
                    "assess-risk",
                    "high",
                    Arrays.asList("financialMetrics"),
                    Map.of("riskTypes", Arrays.asList("volatility", "correlation", "var"))
                ));
                break;
        }

        return recommendations;
    }

    private List<AgentRecommendation> createCollaborativeRecommendations(String sourceAgent, String targetAgent,
                                                                        String dataKey, String dataType) {
        List<AgentRecommendation> recommendations = new ArrayList<>();

        if ("chart-visualizer".equals(targetAgent) && "stock-analysis".equals(dataType)) {
            recommendations.add(new AgentRecommendation(
                "collab-chart-" + System.currentTimeMillis(),
                "collaboration",
                "Visualize Stock Analysis",
                String.format("Create charts from %s's stock analysis data", sourceAgent),
                targetAgent,
                "create-collaborative-chart",
                "high",
                Arrays.asList(dataKey),
                Map.of("sourceAgent", sourceAgent, "dataKey", dataKey)
            ));
        }

        return recommendations;
    }

    private void generateContextUpdateNotification(String subscriberAgent, String contextKey, Object data) {
        // Create notification recommendation
        AgentRecommendation notification = new AgentRecommendation(
            "context-update-" + System.currentTimeMillis(),
            "notification",
            "Context Data Updated",
            String.format("New data available for %s", contextKey),
            subscriberAgent,
            "process-context-update",
            "low",
            Arrays.asList(contextKey),
            Map.of("updateType", "context-change", "contextKey", contextKey)
        );

        List<AgentRecommendation> existing = getAgentRecommendations(subscriberAgent);
        existing.add(notification);
        setAgentRecommendations(subscriberAgent, existing);
    }

    private List<Map<String, Object>> generateWorkflowSuggestions(String agentId, String dataType) {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        if ("stock-analysis".equals(dataType)) {
            suggestions.add(Map.of(
                "name", "Stock Analysis to Visualization Workflow",
                "description", "Automatically create charts when stock analysis completes",
                "agents", Arrays.asList(agentId, "chart-visualizer"),
                "trigger", "stock-analysis-complete"
            ));

            suggestions.add(Map.of(
                "name", "Comprehensive Market Analysis Workflow",
                "description", "Chain stock analysis, risk assessment, and visualization",
                "agents", Arrays.asList(agentId, "risk-assessor", "chart-visualizer"),
                "trigger", "market-data-available"
            ));
        }

        return suggestions;
    }

    private boolean isDataTypeCompatible(String dataType1, String dataType2) {
        // Define compatibility matrix
        Map<String, List<String>> compatibility = Map.of(
            "stock-analysis", Arrays.asList("financial-data", "time-series", "market-data"),
            "financial-data", Arrays.asList("stock-analysis", "risk-data", "portfolio-data"),
            "time-series", Arrays.asList("stock-analysis", "chart-data", "trend-data")
        );

        return compatibility.getOrDefault(dataType1, Collections.emptyList()).contains(dataType2);
    }
}
