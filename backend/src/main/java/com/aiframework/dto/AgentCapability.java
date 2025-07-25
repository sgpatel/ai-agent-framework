package com.aiframework.dto;

import java.util.List;

public class AgentCapability {
    private String agentId;
    private String description;
    private List<String> capabilities;
    private String specialization;
    private double expertise;

    public AgentCapability() {}

    public AgentCapability(String agentId, String description, List<String> capabilities, String specialization) {
        this.agentId = agentId;
        this.description = description;
        this.capabilities = capabilities;
        this.specialization = specialization;
        this.expertise = 1.0;
    }

    // Getters and Setters
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getExpertise() { return expertise; }
    public void setExpertise(double expertise) { this.expertise = expertise; }
}
