package com.aiframework.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Context passed to agents during task execution
 */
public class AgentContext {
    private String sessionId;
    private String userId;
    private Map<String, Object> sharedData;
    private Map<String, Object> configuration;

    public AgentContext() {
        this.sharedData = new HashMap<>();
        this.configuration = new HashMap<>();
    }

    public AgentContext(String sessionId, String userId) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public void put(String key, Object value) {
        sharedData.put(key, value);
    }

    public Object get(String key) {
        return sharedData.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = sharedData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Map<String, Object> getSharedData() { return sharedData; }
    public void setSharedData(Map<String, Object> sharedData) { this.sharedData = sharedData; }

    public Map<String, Object> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
}
