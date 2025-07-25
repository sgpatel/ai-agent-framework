package com.aiframework.dto;

import java.util.Date;
import java.util.List;

public class AgentCollaboration {
    private List<String> involvedAgents;
    private String plan;
    private Date createdAt;
    private String status;

    // Getters and Setters
    public List<String> getInvolvedAgents() { return involvedAgents; }
    public void setInvolvedAgents(List<String> involvedAgents) { this.involvedAgents = involvedAgents; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
