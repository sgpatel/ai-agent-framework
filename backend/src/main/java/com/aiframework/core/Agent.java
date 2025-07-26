package com.aiframework.core;

/**
 * Base interface for all AI agents in the framework
 *
 * All agents must implement this interface to be discoverable and manageable
 * by the agent orchestrator. Agents should be stateless and thread-safe.
 *
 * @author AI Agent Framework Team
 * @since 1.0.0
 */
public interface Agent {

    /**
     * Get the unique name of this agent
     *
     * @return A unique identifier for this agent (must be unique across all agents)
     */
    String getName();

    /**
     * Get the description of what this agent does
     *
     * @return A human-readable description of the agent's capabilities
     */
    String getDescription();

    /**
     * Check if this agent can handle the given task
     *
     * @param task The task to evaluate
     * @return true if this agent can handle the task, false otherwise
     * @throws IllegalArgumentException if task is null
     */
    boolean canHandle(Task task);

    /**
     * Execute the task with the given context
     *
     * @param task The task to execute (must not be null)
     * @param context The execution context (must not be null)
     * @return The result of the task execution
     * @throws AgentExecutionException if task execution fails
     * @throws IllegalArgumentException if task or context is null
     */
    AgentResult execute(Task task, AgentContext context);

    /**
     * Get the agent's current status
     *
     * @return The current status of the agent
     */
    AgentStatus getStatus();

    /**
     * Initialize the agent with configuration
     *
     * @param config The configuration for this agent (must not be null)
     * @throws IllegalArgumentException if config is null
     */
    void initialize(AgentConfig config);

    /**
     * Cleanup resources when agent is unloaded
     *
     * This method should be called when the agent is being shut down
     * to ensure proper cleanup of resources.
     */
    void shutdown();

    /**
     * Get the agent's configuration
     *
     * @return The current configuration of the agent
     */
    AgentConfig getConfig();
}
