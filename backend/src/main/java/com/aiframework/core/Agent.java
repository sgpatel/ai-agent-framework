package com.aiframework.core;

/**
 * Base interface for all AI agents in the framework
 */
public interface Agent {
    /**
     * Get the unique name of this agent
     */
    String getName();

    /**
     * Get the description of what this agent does
     */
    String getDescription();

    /**
     * Check if this agent can handle the given task
     */
    boolean canHandle(Task task);

    /**
     * Execute the task with the given context
     */
    AgentResult execute(Task task, AgentContext context);

    /**
     * Get the agent's current status
     */
    AgentStatus getStatus();

    /**
     * Initialize the agent with configuration
     */
    void initialize(AgentConfig config);

    /**
     * Cleanup resources when agent is unloaded
     */
    void shutdown();

}
