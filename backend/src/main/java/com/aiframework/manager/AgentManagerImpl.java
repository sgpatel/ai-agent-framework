package com.aiframework.manager;

import com.aiframework.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of AgentManager
 */
@Service
public class AgentManagerImpl implements AgentManager {
    private static final Logger logger = LoggerFactory.getLogger(AgentManagerImpl.class);

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, AgentConfig> agentConfigs = new ConcurrentHashMap<>();
    private final PluginManager pluginManager;

    public AgentManagerImpl(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        loadPluginAgents();
    }

    @Override
    public void registerAgent(Agent agent) {
        logger.info("Registering agent: {}", agent.getName());
        agents.put(agent.getName(), agent);

        // Initialize with default config if not already configured
        if (!agentConfigs.containsKey(agent.getName())) {
            AgentConfig config = new AgentConfig(agent.getName());
            agentConfigs.put(agent.getName(), config);
            agent.initialize(config);
        }
    }

    @Override
    public void unregisterAgent(String agentName) {
        logger.info("Unregistering agent: {}", agentName);
        Agent agent = agents.remove(agentName);
        if (agent != null) {
            agent.shutdown();
        }
        agentConfigs.remove(agentName);
    }

    @Override
    public Optional<Agent> getAgent(String agentName) {
        return Optional.ofNullable(agents.get(agentName));
    }

    @Override
    public List<Agent> getAllAgents() {
        return new ArrayList<>(agents.values());
    }

    @Override
    public List<Agent> getCapableAgents(String taskType) {
        Task dummyTask = new Task();
        dummyTask.setType(taskType);

        return agents.values().stream()
                .filter(agent -> agent.canHandle(dummyTask))
                .collect(Collectors.toList());
    }

    @Override
    public void reloadAgents() {
        logger.info("Reloading all agents");

        // Shutdown existing agents
        agents.values().forEach(Agent::shutdown);
        agents.clear();

        // Reload plugins
        pluginManager.reloadPlugins();
        loadPluginAgents();
    }

    @Override
    public void configureAgent(String agentName, AgentConfig config) {
        agentConfigs.put(agentName, config);
        Agent agent = agents.get(agentName);
        if (agent != null) {
            agent.initialize(config);
        }
    }

    @Override
    public void shutdownAll() {
        logger.info("Shutting down all agents");
        agents.values().forEach(Agent::shutdown);
        agents.clear();
        agentConfigs.clear();
    }

    private void loadPluginAgents() {
        List<Agent> pluginAgents = pluginManager.loadAgents();
        for (Agent agent : pluginAgents) {
            registerAgent(agent);
        }
        logger.info("Loaded {} plugin agents", pluginAgents.size());
    }
}
