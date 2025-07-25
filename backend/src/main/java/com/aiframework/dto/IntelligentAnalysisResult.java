package com.aiframework.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class IntelligentAnalysisResult {
    private Map<String, Object> agentResults;
    private String synthesizedAnalysis;
    private Date timestamp;
    private boolean requiresAttention;
    private List<String> recommendedActions;
    private SupportResponse customerSupportResponse;
    private boolean error;
    private String errorMessage;

    // Getters and Setters
    public Map<String, Object> getAgentResults() { return agentResults; }
    public void setAgentResults(Map<String, Object> agentResults) { this.agentResults = agentResults; }

    public String getSynthesizedAnalysis() { return synthesizedAnalysis; }
    public void setSynthesizedAnalysis(String synthesizedAnalysis) { this.synthesizedAnalysis = synthesizedAnalysis; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRequiresAttention() { return requiresAttention; }
    public void setRequiresAttention(boolean requiresAttention) { this.requiresAttention = requiresAttention; }

    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }

    public SupportResponse getCustomerSupportResponse() { return customerSupportResponse; }
    public void setCustomerSupportResponse(SupportResponse customerSupportResponse) { this.customerSupportResponse = customerSupportResponse; }

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
