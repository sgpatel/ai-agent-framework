package com.aiframework.manager;

import com.aiframework.core.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Manages plugin loading for agents
 */
@Component
public class PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final String pluginDirectory = "plugins";
    private URLClassLoader pluginClassLoader;

    /**
     * Load all agents from plugins
     */
    public List<Agent> loadAgents() {
        List<Agent> agents = new ArrayList<>();

        // Load from classpath using ServiceLoader (built-in agents)
        ServiceLoader<Agent> serviceLoader = ServiceLoader.load(Agent.class);
        for (Agent agent : serviceLoader) {
            agents.add(agent);
            logger.info("Loaded built-in agent: {}", agent.getName());
        }

        // Load from plugin directory
        agents.addAll(loadPluginAgents());

        return agents;
    }

    /**
     * Load agents from plugin JAR files
     */
    private List<Agent> loadPluginAgents() {
        List<Agent> agents = new ArrayList<>();

        File pluginDir = new File(pluginDirectory);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            logger.warn("Plugin directory does not exist: {}", pluginDirectory);
            return agents;
        }

        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.info("No plugin JAR files found in: {}", pluginDirectory);
            return agents;
        }

        try {
            // Create URLClassLoader for plugin JARs
            URL[] urls = new URL[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++) {
                urls[i] = jarFiles[i].toURI().toURL();
            }

            if (pluginClassLoader != null) {
                pluginClassLoader.close();
            }

            pluginClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            // Load agents from plugins
            ServiceLoader<Agent> pluginServiceLoader = ServiceLoader.load(Agent.class, pluginClassLoader);
            for (Agent agent : pluginServiceLoader) {
                agents.add(agent);
                logger.info("Loaded plugin agent: {}", agent.getName());
            }

        } catch (Exception e) {
            logger.error("Error loading plugin agents", e);
        }

        return agents;
    }

    /**
     * Reload all plugins
     */
    public void reloadPlugins() {
        if (pluginClassLoader != null) {
            try {
                pluginClassLoader.close();
            } catch (Exception e) {
                logger.error("Error closing plugin class loader", e);
            }
        }
        pluginClassLoader = null;
    }
}
