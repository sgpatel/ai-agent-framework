package com.aiframework.dto;

import java.util.Date;
import java.util.List;

public class SupportResponse {
    private String responseType;
    private String symbol;
    private String content;
    private Date timestamp;
    private String urgencyLevel;
    private boolean followUpRequired;
    private List<String> actionItems;
    private String educationalContent;

    // Getters and Setters
    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public boolean isFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(boolean followUpRequired) { this.followUpRequired = followUpRequired; }

    public List<String> getActionItems() { return actionItems; }
    public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }

    public String getEducationalContent() { return educationalContent; }
    public void setEducationalContent(String educationalContent) { this.educationalContent = educationalContent; }
}
