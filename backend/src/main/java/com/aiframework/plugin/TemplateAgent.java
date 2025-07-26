package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Template for creating new AI Agent plugins
 *
 * To create a new plugin:
 * 1. Copy this template
 * 2. Rename the class
 * 3. Implement your specific logic
 * 4. Register in META-INF/services/com.aiframework.core.Agent
 */
@Component
public class TemplateAgent implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;

    // Plugin metadata
    private static final String PLUGIN_NAME = "Template Agent";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_AUTHOR = "Your Name";
    private static final String PLUGIN_CATEGORY = "General";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return "Template agent demonstrating plugin creation structure";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;

        // Define what types of tasks this plugin can handle
        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

        return "TEMPLATE_TASK".equals(task.getType()) ||
               taskType.contains("template") ||
               taskType.contains("example") ||
               description.contains("template");
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            // Set status to running
            status = AgentStatus.RUNNING;

            // Store context for collaboration
            if (contextStore != null) {
                contextStore.storeContext(getName(), "currentTask", task.getId());
                contextStore.storeContext(getName(), "dataType", "template-data");
                contextStore.storeContext(getName(), "startTime", LocalDateTime.now().toString());
            }

            // Main plugin logic goes here
            Map<String, Object> results = processTask(task, context);

            // Share results with other agents
            if (contextStore != null) {
                contextStore.storeSharedData(
                    "templateResults",
                    results,
                    getName(),
                    Map.of("dataType", "template-output", "taskId", task.getId())
                );
            }

            // Set status to ready
            status = AgentStatus.READY;

            return AgentResult.success(
                task.getId(),
                getName(),
                results
            );

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(
                task.getId(),
                getName(),
                "Template task failed: " + e.getMessage()
            );
        }
    }

    /**
     * Main processing logic - customize this for your specific plugin
     */
    private Map<String, Object> processTask(Task task, AgentContext context) {
        Map<String, Object> results = new HashMap<>();

        // Example processing logic
        results.put("processedAt", LocalDateTime.now().toString());
        results.put("taskType", task.getType());
        results.put("parameters", task.getParameters());
        results.put("pluginVersion", PLUGIN_VERSION);
        results.put("pluginAuthor", PLUGIN_AUTHOR);

        // Add your specific processing logic here
        // For example:
        // - Data analysis
        // - API calls
        // - Machine learning inference
        // - File processing
        // - Database operations

        return results;
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        this.status = AgentStatus.READY;

        // Initialize any resources your plugin needs
        // For example:
        // - Database connections
        // - API clients
        // - ML models
        // - Configuration validation
    }

    @Override
    public void shutdown() {
        this.status = AgentStatus.SHUTDOWN;
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public AgentConfig getConfig() {
        return config;
    }
}
