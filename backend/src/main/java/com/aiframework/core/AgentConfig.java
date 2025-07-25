package com.aiframework.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for an agent
 */
public class AgentConfig {
    private String name;
    private Map<String, Object> properties;
    private boolean enabled;

    public AgentConfig() {
        this.properties = new HashMap<>();
        this.enabled = true;
    }

    public AgentConfig(String name) {
        this();
        this.name = name;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
