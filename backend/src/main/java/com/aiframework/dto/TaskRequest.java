package com.aiframework.dto;

import com.aiframework.core.Task;
import java.util.Map;

/**
 * DTO for task requests from the frontend
 */
public class TaskRequest {
    private String type;
    private String description;
    private Map<String, Object> parameters;
    private String priority;
    private String userId;
    private String sessionId;

    public TaskRequest() {}

    public Task toTask() {
        Task task = new Task(type, description, parameters);
        if (priority != null) {
            task.setPriority(Task.Priority.valueOf(priority.toUpperCase()));
        }
        task.setUserId(userId);
        return task;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
