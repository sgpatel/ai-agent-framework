package com.aiframework.context;

import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Enhanced Context Store interface for agent collaboration and data sharing
 */
public interface ContextStore {

    // Basic context operations
    void storeContext(String agentId, String key, Object value);
    Optional<Object> getContext(String agentId, String key);
    Map<String, Object> getAllContext(String agentId);
    void clearContext(String agentId);
    void clearAllContext();

    // Enhanced collaborative features
    void storeSharedData(String dataKey, Object data, String sourceAgent, Map<String, Object> metadata);
    Optional<SharedDataEntry> getSharedData(String dataKey);
    Map<String, SharedDataEntry> getAllSharedData();
    void clearSharedData(String dataKey);

    // Agent subscriptions and notifications
    void subscribeToContext(String subscriberAgent, String contextKey);
    void unsubscribeFromContext(String subscriberAgent, String contextKey);
    List<String> getSubscribers(String contextKey);
    void notifySubscribers(String contextKey, Object data);

    // Workflow management
    void createWorkflow(String workflowId, WorkflowDefinition workflow);
    Optional<WorkflowDefinition> getWorkflow(String workflowId);
    List<WorkflowDefinition> getActiveWorkflows();
    void updateWorkflowStatus(String workflowId, WorkflowStatus status);

    // Agent recommendations
    void setAgentRecommendations(String agentId, List<AgentRecommendation> recommendations);
    List<AgentRecommendation> getAgentRecommendations(String agentId);
    void clearAgentRecommendations(String agentId);

    // Context querying and analysis
    List<String> findAgentsWithDataType(String dataType);
    List<String> findCompatibleAgents(String sourceAgent, String dataType);
    Map<String, Object> generateCollaborationSuggestions(String agentId);

    // Data sharing classes
    class SharedDataEntry {
        private Object data;
        private String sourceAgent;
        private String timestamp;
        private Map<String, Object> metadata;
        private String dataType;

        public SharedDataEntry(Object data, String sourceAgent, String timestamp,
                             Map<String, Object> metadata, String dataType) {
            this.data = data;
            this.sourceAgent = sourceAgent;
            this.timestamp = timestamp;
            this.metadata = metadata;
            this.dataType = dataType;
        }

        // Getters and setters
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }

        public String getSourceAgent() { return sourceAgent; }
        public void setSourceAgent(String sourceAgent) { this.sourceAgent = sourceAgent; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }

    // Workflow definition
    class WorkflowDefinition {
        private String id;
        private String name;
        private String description;
        private List<String> participatingAgents;
        private Map<String, Object> dataFlow;
        private WorkflowStatus status;
        private String createdAt;
        private String updatedAt;

        public WorkflowDefinition(String id, String name, String description,
                                List<String> participatingAgents, Map<String, Object> dataFlow) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.participatingAgents = participatingAgents;
            this.dataFlow = dataFlow;
            this.status = WorkflowStatus.ACTIVE;
            this.createdAt = java.time.Instant.now().toString();
            this.updatedAt = this.createdAt;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getParticipatingAgents() { return participatingAgents; }
        public void setParticipatingAgents(List<String> participatingAgents) { this.participatingAgents = participatingAgents; }

        public Map<String, Object> getDataFlow() { return dataFlow; }
        public void setDataFlow(Map<String, Object> dataFlow) { this.dataFlow = dataFlow; }

        public WorkflowStatus getStatus() { return status; }
        public void setStatus(WorkflowStatus status) { this.status = status; this.updatedAt = java.time.Instant.now().toString(); }

        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }

    // Agent recommendation
    class AgentRecommendation {
        private String id;
        private String type;
        private String title;
        private String description;
        private String suggestedAgent;
        private String action;
        private String priority;
        private List<String> dataKeys;
        private Map<String, Object> parameters;

        public AgentRecommendation(String id, String type, String title, String description,
                                 String suggestedAgent, String action, String priority,
                                 List<String> dataKeys, Map<String, Object> parameters) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.description = description;
            this.suggestedAgent = suggestedAgent;
            this.action = action;
            this.priority = priority;
            this.dataKeys = dataKeys;
            this.parameters = parameters;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSuggestedAgent() { return suggestedAgent; }
        public void setSuggestedAgent(String suggestedAgent) { this.suggestedAgent = suggestedAgent; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public List<String> getDataKeys() { return dataKeys; }
        public void setDataKeys(List<String> dataKeys) { this.dataKeys = dataKeys; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    // Workflow status enum
    enum WorkflowStatus {
        ACTIVE, PAUSED, COMPLETED, FAILED, CANCELLED
    }
}
