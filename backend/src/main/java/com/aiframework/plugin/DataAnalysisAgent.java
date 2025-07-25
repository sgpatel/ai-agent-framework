package com.aiframework.plugin;

import com.aiframework.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example agent for data analysis tasks
 */
public class DataAnalysisAgent implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisAgent.class);

    private AgentStatus status = AgentStatus.INITIALIZING;
    private AgentConfig config;

    @Override
    public String getName() {
        return "DataAnalysisAgent";
    }

    @Override
    public String getDescription() {
        return "Analyzes data and provides insights, statistics, and visualizations";
    }

    @Override
    public boolean canHandle(Task task) {
        return "data_analysis".equalsIgnoreCase(task.getType()) ||
               "statistics".equalsIgnoreCase(task.getType()) ||
               "visualization".equalsIgnoreCase(task.getType());
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        logger.info("DataAnalysisAgent executing task: {}", task.getId());

        long startTime = System.currentTimeMillis();

        try {
            // Simulate data analysis work
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));

            Map<String, Object> analysisResult = new HashMap<>();
            analysisResult.put("mean", 42.5);
            analysisResult.put("median", 41.0);
            analysisResult.put("standardDeviation", 5.2);
            analysisResult.put("totalRecords", 1000);
            analysisResult.put("analysisType", task.getType());
            analysisResult.put("insights", "Data shows normal distribution with slight positive skew");

            AgentResult result = AgentResult.success(task.getId(), getName(), analysisResult);
            result.setMessage("Data analysis completed successfully");
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AgentResult.failure(task.getId(), getName(), "Task interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error in DataAnalysisAgent", e);
            return AgentResult.failure(task.getId(), getName(), "Analysis failed: " + e.getMessage());
        }
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        this.status = AgentStatus.READY;
        logger.info("DataAnalysisAgent initialized");
    }

    @Override
    public void shutdown() {
        this.status = AgentStatus.SHUTDOWN;
        logger.info("DataAnalysisAgent shutdown");
    }

    @Override
    public AgentConfig getConfig() {
        return config;
    }
}

