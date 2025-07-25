package com.aiframework.dto;

import java.util.List;

/**
 * DTO for batch task requests
 */
public class BatchTaskRequest {
    private List<TaskRequest> tasks;
    private String userId;
    private String sessionId;

    public BatchTaskRequest() {}

    // Getters and Setters
    public List<TaskRequest> getTasks() { return tasks; }
    public void setTasks(List<TaskRequest> tasks) { this.tasks = tasks; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
