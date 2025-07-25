package com.aiframework.core;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result returned by an agent after processing a task
 */
public class AgentResult {
    private String taskId;
    private String agentName;
    private boolean success;
    private Object data;
    private String message;
    private Map<String, Object> metadata;
    private LocalDateTime completedAt;
    private long executionTimeMs;

    public AgentResult() {
        this.completedAt = LocalDateTime.now();
    }

    public AgentResult(String taskId, String agentName, boolean success, Object data) {
        this();
        this.taskId = taskId;
        this.agentName = agentName;
        this.success = success;
        this.data = data;
    }

    // Static factory methods
    public static AgentResult success(String taskId, String agentName, Object data) {
        return new AgentResult(taskId, agentName, true, data);
    }

    public static AgentResult failure(String taskId, String agentName, String message) {
        AgentResult result = new AgentResult(taskId, agentName, false, null);
        result.setMessage(message);
        return result;
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
