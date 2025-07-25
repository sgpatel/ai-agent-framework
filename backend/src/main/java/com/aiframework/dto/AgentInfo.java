package com.aiframework.dto;

import com.aiframework.core.Agent;
import com.aiframework.core.AgentStatus;

/**
 * DTO for agent information
 */
public class AgentInfo {
    private String name;
    private String description;
    private AgentStatus status;
    private String[] supportedTaskTypes;

    public AgentInfo() {}

    public AgentInfo(Agent agent) {
        this.name = agent.getName();
        this.description = agent.getDescription();
        this.status = agent.getStatus();
        // TODO: Extract supported task types from agent
        this.supportedTaskTypes = new String[]{}; // Placeholder
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AgentStatus getStatus() { return status; }
    public void setStatus(AgentStatus status) { this.status = status; }

    public String[] getSupportedTaskTypes() { return supportedTaskTypes; }
    public void setSupportedTaskTypes(String[] supportedTaskTypes) { this.supportedTaskTypes = supportedTaskTypes; }
}
