package com.aiframework.dto;

import java.util.Date;
import java.util.Map;

public class AgentMessage {
    private String fromAgent;
    private String toAgent;
    private String task;
    private Map<String, Object> context;
    private Date timestamp;
    private String priority;

    public AgentMessage() {}

    public AgentMessage(String fromAgent, String task, Map<String, Object> context) {
        this.fromAgent = fromAgent;
        this.task = task;
        this.context = context;
        this.timestamp = new Date();
        this.priority = "MEDIUM";
    }

    // Getters and Setters
    public String getFromAgent() { return fromAgent; }
    public void setFromAgent(String fromAgent) { this.fromAgent = fromAgent; }

    public String getToAgent() { return toAgent; }
    public void setToAgent(String toAgent) { this.toAgent = toAgent; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
