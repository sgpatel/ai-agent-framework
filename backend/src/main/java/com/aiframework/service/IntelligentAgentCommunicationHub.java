package com.aiframework.service;

import com.aiframework.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IntelligentAgentCommunicationHub {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentAgentCommunicationHub.class);

    @Autowired
    private LocalLLMService localLLMService;

    // Dynamic agent registry - no hardcoded relationships
    private final Map<String, AgentCapability> agentRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<AgentMessage>> conversationHistory = new ConcurrentHashMap<>();

    public void registerAgent(String agentId, AgentCapability capability) {
        agentRegistry.put(agentId, capability);
        logger.info("Registered agent: {} with capabilities: {}", agentId, capability.getCapabilities());
    }

    public CompletableFuture<AgentCommunicationResult> routeMessage(AgentMessage message) {
        return findBestAgentsForTask(message.getTask(), message.getContext())
            .thenCompose(selectedAgents -> coordinateAgentExecution(message, selectedAgents))
            .thenApply(this::synthesizeResults);
    }

    public CompletableFuture<List<String>> findBestAgentsForTask(String task, Map<String, Object> context) {
        String routingPrompt = buildAgentRoutingPrompt(task, context, agentRegistry);

        return localLLMService.generateResponse(routingPrompt, 512, 0.3f)
            .thenApply(this::parseAgentSelection);
    }

    public CompletableFuture<AgentCollaboration> facilitateAgentCollaboration(String initiatingAgent, String task, List<String> involvedAgents) {
        String collaborationPrompt = buildCollaborationPrompt(initiatingAgent, task, involvedAgents);

        return localLLMService.generateResponse(collaborationPrompt, 768, 0.4f)
            .thenApply(response -> createCollaborationPlan(response, involvedAgents));
    }

    private String buildAgentRoutingPrompt(String task, Map<String, Object> context, Map<String, AgentCapability> availableAgents) {
        StringBuilder agentInfo = new StringBuilder();
        for (Map.Entry<String, AgentCapability> entry : availableAgents.entrySet()) {
            agentInfo.append(String.format("- %s: %s (Specialties: %s)\n",
                entry.getKey(),
                entry.getValue().getDescription(),
                String.join(", ", entry.getValue().getCapabilities())));
        }

        return String.format("""
            You are an intelligent agent coordinator. Route this task to the most appropriate agents:
            
            Task: %s
            Context: %s
            
            Available Agents:
            %s
            
            Analyze the task and determine:
            1. Which agents are most relevant for this task
            2. What order should they execute in
            3. What information should be shared between them
            4. Any parallel processing opportunities
            
            Respond with JSON format:
            {
                "primaryAgents": ["agent1", "agent2"],
                "supportingAgents": ["agent3"],
                "executionOrder": "SEQUENTIAL|PARALLEL|HYBRID",
                "coordination": {
                    "step1": {"agents": ["agent1"], "input": "data1"},
                    "step2": {"agents": ["agent2"], "input": "output_from_step1"}
                },
                "reasoning": "Why these agents were selected"
            }
            """, task, formatContext(context), agentInfo.toString());
    }

    private String buildCollaborationPrompt(String initiatingAgent, String task, List<String> involvedAgents) {
        return String.format("""
            Facilitate collaboration between agents for this task:
            
            Initiating Agent: %s
            Task: %s
            Involved Agents: %s
            
            Create a collaboration plan that includes:
            1. Role definition for each agent
            2. Information sharing protocol
            3. Conflict resolution strategy
            4. Quality assurance measures
            5. Timeline and checkpoints
            
            JSON Response:
            {
                "collaborationPlan": {
                    "roles": {"agent1": "specific role", "agent2": "specific role"},
                    "communicationFlow": ["step1", "step2"],
                    "sharedData": ["data_type1", "data_type2"],
                    "checkpoints": ["milestone1", "milestone2"],
                    "conflictResolution": "resolution strategy"
                },
                "successMetrics": ["metric1", "metric2"],
                "fallbackPlan": "backup strategy"
            }
            """, initiatingAgent, task, String.join(", ", involvedAgents));
    }

    private CompletableFuture<Map<String, Object>> coordinateAgentExecution(AgentMessage message, List<String> selectedAgents) {
        Map<String, Object> results = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> agentTasks = new ArrayList<>();

        for (String agentId : selectedAgents) {
            CompletableFuture<Void> agentTask = executeAgentTask(agentId, message)
                .thenAccept(result -> results.put(agentId, result));
            agentTasks.add(agentTask);
        }

        return CompletableFuture.allOf(agentTasks.toArray(new CompletableFuture[0]))
            .thenApply(v -> results);
    }

    private CompletableFuture<Object> executeAgentTask(String agentId, AgentMessage message) {
        // Simulate agent execution - in real implementation, this would delegate to actual agents
        return CompletableFuture.supplyAsync(() -> {
            AgentCapability capability = agentRegistry.get(agentId);
            if (capability != null) {
                return executeAgentWithCapability(agentId, capability, message);
            }
            return "Agent not found: " + agentId;
        });
    }

    private Object executeAgentWithCapability(String agentId, AgentCapability capability, AgentMessage message) {
        // This is where you'd integrate with your actual agent implementations
        Map<String, Object> result = new HashMap<>();
        result.put("agentId", agentId);
        result.put("capability", capability.getCapabilities());
        result.put("output", "Processed: " + message.getTask());
        result.put("timestamp", new Date());

        // Store conversation history
        storeConversationHistory(agentId, message);

        return result;
    }

    private void storeConversationHistory(String agentId, AgentMessage message) {
        conversationHistory.computeIfAbsent(agentId, k -> new ArrayList<>()).add(message);

        // Keep only last 100 messages per agent
        List<AgentMessage> history = conversationHistory.get(agentId);
        if (history.size() > 100) {
            history.subList(0, history.size() - 100).clear();
        }
    }

    private List<String> parseAgentSelection(String llmResponse) {
        List<String> selectedAgents = new ArrayList<>();

        // Simple parsing - in production, use proper JSON parsing
        if (llmResponse.contains("\"primaryAgents\":")) {
            // Extract agent names from the LLM response
            for (String agentId : agentRegistry.keySet()) {
                if (llmResponse.toLowerCase().contains(agentId.toLowerCase())) {
                    selectedAgents.add(agentId);
                }
            }
        }

        // Fallback: if no agents found, return all available agents
        if (selectedAgents.isEmpty()) {
            selectedAgents.addAll(agentRegistry.keySet());
        }

        return selectedAgents;
    }

    private AgentCollaboration createCollaborationPlan(String response, List<String> involvedAgents) {
        AgentCollaboration collaboration = new AgentCollaboration();
        collaboration.setInvolvedAgents(involvedAgents);
        collaboration.setPlan(response);
        collaboration.setCreatedAt(new Date());
        collaboration.setStatus("ACTIVE");

        return collaboration;
    }

    private AgentCommunicationResult synthesizeResults(Map<String, Object> agentResults) {
        // Use LLM to synthesize results from multiple agents
        String synthesisPrompt = buildSynthesisPrompt(agentResults);

        AgentCommunicationResult result = new AgentCommunicationResult();
        result.setAgentResults(agentResults);
        result.setTimestamp(new Date());

        try {
            // Synthesize results synchronously to ensure the result is populated
            String synthesizedResponse = localLLMService.generateResponse(synthesisPrompt, 768, 0.3f)
                .get(); // Wait for completion
            result.setSynthesizedResult(synthesizedResponse);
        } catch (Exception e) {
            logger.error("Error synthesizing agent results: {}", e.getMessage());
            // Create a fallback synthesis based on agent outputs
            String fallbackSynthesis = createFallbackSynthesis(agentResults);
            result.setSynthesizedResult(fallbackSynthesis);
        }

        return result;
    }

    private String buildSynthesisPrompt(Map<String, Object> agentResults) {
        return String.format("""
            Synthesize the results from multiple AI agents into a coherent response:
            
            Agent Results: %s
            
            Create a unified response that:
            1. Combines insights from all agents
            2. Resolves any conflicts or contradictions
            3. Provides clear, actionable recommendations
            4. Highlights areas of agreement and disagreement
            5. Suggests next steps based on collective intelligence
            
            Format as a comprehensive summary that leverages the strengths of each agent's contribution.
            """, formatAgentResults(agentResults));
    }

    public List<AgentCapability> getAvailableAgents() {
        return new ArrayList<>(agentRegistry.values());
    }

    public Map<String, List<AgentMessage>> getConversationHistory(String agentId) {
        if (agentId != null) {
            return Map.of(agentId, conversationHistory.getOrDefault(agentId, new ArrayList<>()));
        }
        return new HashMap<>(conversationHistory);
    }

    private String formatContext(Map<String, Object> context) {
        return context.toString();
    }

    private String formatAgentResults(Map<String, Object> results) {
        return results.toString();
    }

    private String createFallbackSynthesis(Map<String, Object> agentResults) {
        StringBuilder synthesis = new StringBuilder();
        synthesis.append("## Agent Collaboration Summary\n\n");

        // Extract insights from each agent
        for (Map.Entry<String, Object> entry : agentResults.entrySet()) {
            String agentId = entry.getKey();
            Map<String, Object> agentResult = (Map<String, Object>) entry.getValue();

            synthesis.append(String.format("**%s Agent Analysis:**\n",
                agentId.replace("-", " ").toUpperCase()));

            if (agentResult.containsKey("capability")) {
                List<String> capabilities = (List<String>) agentResult.get("capability");
                synthesis.append("- Specialized in: ").append(String.join(", ", capabilities)).append("\n");
            }

            if (agentResult.containsKey("output")) {
                synthesis.append("- Analysis: ").append(agentResult.get("output")).append("\n");
            }

            synthesis.append("\n");
        }

        // Add unified recommendations
        synthesis.append("## Unified Recommendations:\n");
        synthesis.append("Based on the collective analysis from ").append(agentResults.size())
                .append(" specialized agents, here are the key insights:\n\n");

        if (agentResults.containsKey("risk-assessor")) {
            synthesis.append("- **Risk Management**: Comprehensive risk analysis completed\n");
        }
        if (agentResults.containsKey("pattern-recognizer")) {
            synthesis.append("- **Technical Patterns**: Chart patterns and formations analyzed\n");
        }
        if (agentResults.containsKey("strategy-generator")) {
            synthesis.append("- **Trading Strategy**: Optimized strategy recommendations provided\n");
        }
        if (agentResults.containsKey("stock-analyzer")) {
            synthesis.append("- **Technical Analysis**: Complete technical indicator analysis performed\n");
        }

        synthesis.append("\n**Next Steps**: Review individual agent recommendations and implement suggested strategies with appropriate risk management.");

        return synthesis.toString();
    }
}
