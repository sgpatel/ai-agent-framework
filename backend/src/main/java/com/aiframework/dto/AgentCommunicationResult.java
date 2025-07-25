package com.aiframework.dto;

import java.util.Date;
import java.util.Map;

public class AgentCommunicationResult {
    private Map<String, Object> agentResults;
    private String synthesizedResult;
    private Date timestamp;

    // Getters and Setters
    public Map<String, Object> getAgentResults() { return agentResults; }
    public void setAgentResults(Map<String, Object> agentResults) { this.agentResults = agentResults; }

    public String getSynthesizedResult() { return synthesizedResult; }
    public void setSynthesizedResult(String synthesizedResult) { this.synthesizedResult = synthesizedResult; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
