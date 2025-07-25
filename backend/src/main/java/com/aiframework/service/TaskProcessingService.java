package com.aiframework.service;

import com.aiframework.core.*;
import com.aiframework.orchestrator.OrchestratorService;
import com.aiframework.communication.ModelContextProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * High-level service for processing tasks
 */
@Service
public class TaskProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(TaskProcessingService.class);

    private final OrchestratorService orchestratorService;
    private final ModelContextProtocol modelContextProtocol;

    public TaskProcessingService(OrchestratorService orchestratorService,
                               ModelContextProtocol modelContextProtocol) {
        this.orchestratorService = orchestratorService;
        this.modelContextProtocol = modelContextProtocol;
    }

    /**
     * Process a single task with automatic context creation
     */
    public AgentResult processTask(Task task, String userId) {
        String sessionId = UUID.randomUUID().toString();
        AgentContext context = modelContextProtocol.createContext(sessionId, userId);

        logger.info("Processing task {} for user {} in session {}", 
            task.getId(), userId, sessionId);

        return orchestratorService.processTask(task, context);
    }

    /**
     * Process a task with existing context
     */
    public AgentResult processTask(Task task, String sessionId, String userId) {
        AgentContext context = modelContextProtocol.createContext(sessionId, userId);
        return orchestratorService.processTask(task, context);
    }

    /**
     * Process multiple tasks
     */
    public List<AgentResult> processTasks(List<Task> tasks, String userId) {
        String sessionId = UUID.randomUUID().toString();
        AgentContext context = modelContextProtocol.createContext(sessionId, userId);

        logger.info("Processing {} tasks for user {} in session {}", 
            tasks.size(), userId, sessionId);

        return orchestratorService.processTasks(tasks, context);
    }

    /**
     * Process a task asynchronously
     */
    public CompletableFuture<AgentResult> processTaskAsync(Task task, String userId) {
        String sessionId = UUID.randomUUID().toString();
        AgentContext context = modelContextProtocol.createContext(sessionId, userId);

        logger.info("Processing task {} asynchronously for user {} in session {}", 
            task.getId(), userId, sessionId);

        return orchestratorService.processTaskAsync(task, context);
    }

    /**
     * Process multiple tasks asynchronously
     */
    public CompletableFuture<List<AgentResult>> processTasksAsync(List<Task> tasks, String userId) {
        String sessionId = UUID.randomUUID().toString();
        AgentContext context = modelContextProtocol.createContext(sessionId, userId);

        logger.info("Processing {} tasks asynchronously for user {} in session {}", 
            tasks.size(), userId, sessionId);

        return orchestratorService.processTasksAsync(tasks, context);
    }
}
