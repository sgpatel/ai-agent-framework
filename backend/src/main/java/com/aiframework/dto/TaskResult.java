package com.aiframework.dto;

import com.aiframework.core.AgentResult;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for task results
 */
public class TaskResult {
    private String taskId;
    private String agentName;
    private boolean success;
    private Object data;
    private String message;
    private Map<String, Object> metadata;
    private LocalDateTime completedAt;
    private long executionTimeMs;

    public TaskResult() {}

    public TaskResult(AgentResult agentResult) {
        this.taskId = agentResult.getTaskId();
        this.agentName = agentResult.getAgentName();
        this.success = agentResult.isSuccess();
        this.data = agentResult.getData();
        this.message = agentResult.getMessage();
        this.metadata = agentResult.getMetadata();
        this.completedAt = agentResult.getCompletedAt();
        this.executionTimeMs = agentResult.getExecutionTimeMs();
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
