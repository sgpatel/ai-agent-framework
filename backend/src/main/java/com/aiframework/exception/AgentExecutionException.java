package com.aiframework.exception;

/**
 * Exception thrown when agent execution fails
 */
public class AgentExecutionException extends RuntimeException {
    
    private final String agentName;
    private final String taskId;

    public AgentExecutionException(String message, String agentName, String taskId) {
        super(message);
        this.agentName = agentName;
        this.taskId = taskId;
    }

    public AgentExecutionException(String message, Throwable cause, String agentName, String taskId) {
        super(message, cause);
        this.agentName = agentName;
        this.taskId = taskId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getTaskId() {
        return taskId;
    }
}
