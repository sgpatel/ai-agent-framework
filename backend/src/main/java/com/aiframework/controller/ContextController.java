package com.aiframework.controller;

import com.aiframework.context.ContextStore;
import com.aiframework.context.ContextStore.SharedDataEntry;
import com.aiframework.context.ContextStore.WorkflowDefinition;
import com.aiframework.context.ContextStore.AgentRecommendation;
import com.aiframework.context.ContextStore.WorkflowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Agent Context Management and Collaboration
 */
@RestController
@RequestMapping("/api/context")
@CrossOrigin(origins = "*")
public class ContextController {

    @Autowired
    private ContextStore contextStore;

    // Basic Context Operations
    @PostMapping("/agent/{agentId}/context")
    public ResponseEntity<String> setAgentContext(@PathVariable String agentId,
                                                  @RequestBody Map<String, Object> context) {
        context.forEach((key, value) -> contextStore.storeContext(agentId, key, value));
        return ResponseEntity.ok("Context updated for agent: " + agentId);
    }

    @GetMapping("/agent/{agentId}/context")
    public ResponseEntity<Map<String, Object>> getAgentContext(@PathVariable String agentId) {
        Map<String, Object> context = contextStore.getAllContext(agentId);
        return ResponseEntity.ok(context);
    }

    @GetMapping("/agent/{agentId}/context/{key}")
    public ResponseEntity<Object> getAgentContextValue(@PathVariable String agentId,
                                                       @PathVariable String key) {
        Optional<Object> value = contextStore.getContext(agentId, key);
        return value.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/agent/{agentId}/context")
    public ResponseEntity<String> clearAgentContext(@PathVariable String agentId) {
        contextStore.clearContext(agentId);
        return ResponseEntity.ok("Context cleared for agent: " + agentId);
    }

    // Shared Data Operations
    @PostMapping("/shared-data/{dataKey}")
    public ResponseEntity<String> setSharedData(@PathVariable String dataKey,
                                               @RequestBody Map<String, Object> request) {
        Object data = request.get("data");
        String sourceAgent = (String) request.get("sourceAgent");
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());

        contextStore.storeSharedData(dataKey, data, sourceAgent, metadata);
        return ResponseEntity.ok("Shared data stored: " + dataKey);
    }

    @GetMapping("/shared-data/{dataKey}")
    public ResponseEntity<SharedDataEntry> getSharedData(@PathVariable String dataKey) {
        Optional<SharedDataEntry> data = contextStore.getSharedData(dataKey);
        return data.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/shared-data")
    public ResponseEntity<Map<String, SharedDataEntry>> getAllSharedData() {
        Map<String, SharedDataEntry> allData = contextStore.getAllSharedData();
        return ResponseEntity.ok(allData);
    }

    @DeleteMapping("/shared-data/{dataKey}")
    public ResponseEntity<String> clearSharedData(@PathVariable String dataKey) {
        contextStore.clearSharedData(dataKey);
        return ResponseEntity.ok("Shared data cleared: " + dataKey);
    }

    // Subscription Management
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribeToContext(@RequestBody Map<String, String> request) {
        String subscriberAgent = request.get("subscriberAgent");
        String contextKey = request.get("contextKey");

        contextStore.subscribeToContext(subscriberAgent, contextKey);
        return ResponseEntity.ok(subscriberAgent + " subscribed to " + contextKey);
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<String> unsubscribeFromContext(@RequestBody Map<String, String> request) {
        String subscriberAgent = request.get("subscriberAgent");
        String contextKey = request.get("contextKey");

        contextStore.unsubscribeFromContext(subscriberAgent, contextKey);
        return ResponseEntity.ok(subscriberAgent + " unsubscribed from " + contextKey);
    }

    @GetMapping("/subscribers/{contextKey}")
    public ResponseEntity<List<String>> getSubscribers(@PathVariable String contextKey) {
        List<String> subscribers = contextStore.getSubscribers(contextKey);
        return ResponseEntity.ok(subscribers);
    }

    // Workflow Management
    @PostMapping("/workflows")
    public ResponseEntity<String> createWorkflow(@RequestBody Map<String, Object> request) {
        String workflowId = (String) request.get("id");
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        @SuppressWarnings("unchecked")
        List<String> agents = (List<String>) request.get("participatingAgents");
        @SuppressWarnings("unchecked")
        Map<String, Object> dataFlow = (Map<String, Object>) request.get("dataFlow");

        WorkflowDefinition workflow = new WorkflowDefinition(workflowId, name, description, agents, dataFlow);
        contextStore.createWorkflow(workflowId, workflow);

        return ResponseEntity.ok("Workflow created: " + workflowId);
    }

    @GetMapping("/workflows/{workflowId}")
    public ResponseEntity<WorkflowDefinition> getWorkflow(@PathVariable String workflowId) {
        Optional<WorkflowDefinition> workflow = contextStore.getWorkflow(workflowId);
        return workflow.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/workflows")
    public ResponseEntity<List<WorkflowDefinition>> getActiveWorkflows() {
        List<WorkflowDefinition> workflows = contextStore.getActiveWorkflows();
        return ResponseEntity.ok(workflows);
    }

    @PutMapping("/workflows/{workflowId}/status")
    public ResponseEntity<String> updateWorkflowStatus(@PathVariable String workflowId,
                                                       @RequestBody Map<String, String> request) {
        String status = request.get("status");
        WorkflowStatus workflowStatus = WorkflowStatus.valueOf(status.toUpperCase());

        contextStore.updateWorkflowStatus(workflowId, workflowStatus);
        return ResponseEntity.ok("Workflow status updated: " + workflowId);
    }

    // Agent Recommendations
    @GetMapping("/agent/{agentId}/recommendations")
    public ResponseEntity<List<AgentRecommendation>> getAgentRecommendations(@PathVariable String agentId) {
        List<AgentRecommendation> recommendations = contextStore.getAgentRecommendations(agentId);
        return ResponseEntity.ok(recommendations);
    }

    @DeleteMapping("/agent/{agentId}/recommendations")
    public ResponseEntity<String> clearAgentRecommendations(@PathVariable String agentId) {
        contextStore.clearAgentRecommendations(agentId);
        return ResponseEntity.ok("Recommendations cleared for agent: " + agentId);
    }

    // Advanced Queries
    @GetMapping("/agents/with-data-type/{dataType}")
    public ResponseEntity<List<String>> findAgentsWithDataType(@PathVariable String dataType) {
        List<String> agents = contextStore.findAgentsWithDataType(dataType);
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/agents/{sourceAgent}/compatible/{dataType}")
    public ResponseEntity<List<String>> findCompatibleAgents(@PathVariable String sourceAgent,
                                                            @PathVariable String dataType) {
        List<String> compatibleAgents = contextStore.findCompatibleAgents(sourceAgent, dataType);
        return ResponseEntity.ok(compatibleAgents);
    }

    @GetMapping("/agent/{agentId}/collaboration-suggestions")
    public ResponseEntity<Map<String, Object>> getCollaborationSuggestions(@PathVariable String agentId) {
        Map<String, Object> suggestions = contextStore.generateCollaborationSuggestions(agentId);
        return ResponseEntity.ok(suggestions);
    }

    // Chart Type Suggestions (for frontend integration)
    @PostMapping("/chart-suggestions")
    public ResponseEntity<List<Map<String, Object>>> getChartSuggestions(@RequestBody Map<String, Object> request) {
        String dataType = (String) request.get("dataType");
        Object dataStructure = request.get("dataStructure");

        List<Map<String, Object>> suggestions = generateChartTypeSuggestions(dataType);
        return ResponseEntity.ok(suggestions);
    }

    // Execute Agent Recommendation
    @PostMapping("/agent/{agentId}/execute-recommendation")
    public ResponseEntity<String> executeRecommendation(@PathVariable String agentId,
                                                        @RequestBody Map<String, Object> request) {
        String recommendationId = (String) request.get("recommendationId");
        String action = (String) request.get("action");

        // Find and execute the recommendation
        List<AgentRecommendation> recommendations = contextStore.getAgentRecommendations(agentId);
        Optional<AgentRecommendation> targetRec = recommendations.stream()
            .filter(rec -> rec.getId().equals(recommendationId))
            .findFirst();

        if (targetRec.isPresent()) {
            executeAgentAction(targetRec.get(), agentId);
            return ResponseEntity.ok("Recommendation executed: " + recommendationId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Trigger Agent Collaboration
    @PostMapping("/trigger-collaboration")
    public ResponseEntity<String> triggerCollaboration(@RequestBody Map<String, Object> request) {
        String sourceAgent = (String) request.get("sourceAgent");
        String targetAgent = (String) request.get("targetAgent");
        String dataKey = (String) request.get("dataKey");
        String action = (String) request.get("action");

        // Create collaboration context
        Map<String, Object> collaborationContext = Map.of(
            "sourceAgent", sourceAgent,
            "dataKey", dataKey,
            "action", action,
            "timestamp", java.time.Instant.now().toString()
        );

        contextStore.storeContext(targetAgent, "collaboration-request", collaborationContext);

        return ResponseEntity.ok("Collaboration triggered between " + sourceAgent + " and " + targetAgent);
    }

    // Helper methods
    private List<Map<String, Object>> generateChartTypeSuggestions(String dataType) {
        switch (dataType) {
            case "time-series":
                return List.of(
                    Map.of("type", "line", "suitable", true, "description", "Best for trend analysis"),
                    Map.of("type", "candlestick", "suitable", true, "description", "Ideal for OHLC data"),
                    Map.of("type", "area", "suitable", true, "description", "Good for volume visualization")
                );
            case "comparative":
                return List.of(
                    Map.of("type", "bar", "suitable", true, "description", "Compare multiple stocks"),
                    Map.of("type", "radar", "suitable", true, "description", "Multi-dimensional comparison")
                );
            case "correlation":
                return List.of(
                    Map.of("type", "scatter", "suitable", true, "description", "Show correlation patterns"),
                    Map.of("type", "heatmap", "suitable", true, "description", "Correlation matrix")
                );
            default:
                return List.of(
                    Map.of("type", "line", "suitable", true, "description", "General purpose chart")
                );
        }
    }

    private void executeAgentAction(AgentRecommendation recommendation, String agentId) {
        switch (recommendation.getAction()) {
            case "create-chart":
                // Set context for chart creation
                Map<String, Object> chartContext = Map.of(
                    "action", "create-chart",
                    "dataKeys", recommendation.getDataKeys(),
                    "parameters", recommendation.getParameters(),
                    "requestedBy", agentId
                );
                contextStore.storeContext(recommendation.getSuggestedAgent(), "chart-request", chartContext);
                break;

            case "assess-risk":
                // Set context for risk assessment
                Map<String, Object> riskContext = Map.of(
                    "action", "assess-risk",
                    "dataKeys", recommendation.getDataKeys(),
                    "parameters", recommendation.getParameters(),
                    "requestedBy", agentId
                );
                contextStore.storeContext(recommendation.getSuggestedAgent(), "risk-request", riskContext);
                break;

            case "detect-patterns":
                // Set context for pattern detection
                Map<String, Object> patternContext = Map.of(
                    "action", "detect-patterns",
                    "dataKeys", recommendation.getDataKeys(),
                    "parameters", recommendation.getParameters(),
                    "requestedBy", agentId
                );
                contextStore.storeContext(recommendation.getSuggestedAgent(), "pattern-request", patternContext);
                break;

            default:
                // Generic action execution
                Map<String, Object> genericContext = Map.of(
                    "action", recommendation.getAction(),
                    "dataKeys", recommendation.getDataKeys(),
                    "parameters", recommendation.getParameters(),
                    "requestedBy", agentId
                );
                contextStore.storeContext(recommendation.getSuggestedAgent(), "action-request", genericContext);
        }
    }
}
