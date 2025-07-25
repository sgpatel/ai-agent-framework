package com.aiframework.core;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a task to be processed by an agent
 */
public class Task {
    private String id;
    private String type;
    private String description;
    private Map<String, Object> parameters;
    private Priority priority;
    private LocalDateTime createdAt;
    private String userId;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
    }

    public Task(String type, String description, Map<String, Object> parameters) {
        this();
        this.type = type;
        this.description = description;
        this.parameters = parameters;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
