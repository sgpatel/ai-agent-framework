package com.aiframework.manager;

import com.aiframework.core.Agent;
import com.aiframework.core.AgentConfig;
import java.util.List;
import java.util.Optional;

/**
 * Interface for managing agents in the framework
 */
public interface AgentManager {
    /**
     * Register a new agent
     */
    void registerAgent(Agent agent);

    /**
     * Unregister an agent by name
     */
    void unregisterAgent(String agentName);

    /**
     * Get an agent by name
     */
    Optional<Agent> getAgent(String agentName);

    /**
     * Get all registered agents
     */
    List<Agent> getAllAgents();

    /**
     * Get agents that can handle a specific task type
     */
    List<Agent> getCapableAgents(String taskType);

    /**
     * Reload all agents (useful for plugin updates)
     */
    void reloadAgents();

    /**
     * Initialize agent with configuration
     */
    void configureAgent(String agentName, AgentConfig config);

    /**
     * Shutdown all agents
     */
    void shutdownAll();
}
