package com.aiframework.orchestrator;

import com.aiframework.core.*;
import com.aiframework.manager.AgentManager;
import com.aiframework.communication.AgentCommunicationProtocol;
import com.aiframework.context.ContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of OrchestratorService
 */
@Service
public class OrchestratorServiceImpl implements OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceImpl.class);

    private final AgentManager agentManager;
    private final AgentCommunicationProtocol communicationProtocol;
    private final ContextStore contextStore;

    public OrchestratorServiceImpl(AgentManager agentManager, 
                                 AgentCommunicationProtocol communicationProtocol,
                                 ContextStore contextStore) {
        this.agentManager = agentManager;
        this.communicationProtocol = communicationProtocol;
        this.contextStore = contextStore;
    }

    @Override
    public AgentResult processTask(Task task, AgentContext context) {
        logger.info("Processing task: {} of type: {}", task.getId(), task.getType());

        long startTime = System.currentTimeMillis();

        try {
            // Store context using the new ContextStore interface
            contextStore.storeContext(task.getId(), "taskContext", context);
            contextStore.storeContext(task.getId(), "taskType", task.getType());
            contextStore.storeContext(task.getId(), "startTime", startTime);

            // Find capable agents
            List<Agent> capableAgents = agentManager.getCapableAgents(task.getType());

            if (capableAgents.isEmpty()) {
                logger.warn("No agents found capable of handling task type: {}", task.getType());
                return AgentResult.failure(task.getId(), "orchestrator", 
                    "No agents found capable of handling task type: " + task.getType());
            }

            // Select the best agent (for now, just pick the first one)
            Agent selectedAgent = selectBestAgent(capableAgents, task);

            // Execute the task
            AgentResult result = selectedAgent.execute(task, context);

            // Update execution time
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            // Store result using the new ContextStore interface
            contextStore.storeContext(task.getId(), "result", result);
            contextStore.storeContext(task.getId(), "executedBy", selectedAgent.getName());
            contextStore.storeContext(task.getId(), "status", "completed");

            // Send communication protocol message
            communicationProtocol.notifyTaskCompleted(task, result);

            logger.info("Task {} completed by agent {} in {}ms", 
                task.getId(), selectedAgent.getName(), result.getExecutionTimeMs());

            return result;

        } catch (Exception e) {
            logger.error("Error processing task: {}", task.getId(), e);
            AgentResult errorResult = AgentResult.failure(task.getId(), "orchestrator", 
                "Error processing task: " + e.getMessage());
            errorResult.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            // Store error result
            contextStore.storeContext(task.getId(), "result", errorResult);
            contextStore.storeContext(task.getId(), "status", "failed");
            contextStore.storeContext(task.getId(), "error", e.getMessage());

            return errorResult;
        }
    }

    @Override
    @Async
    public CompletableFuture<AgentResult> processTaskAsync(Task task, AgentContext context) {
        return CompletableFuture.completedFuture(processTask(task, context));
    }

    @Override
    public List<AgentResult> processTasks(List<Task> tasks, AgentContext context) {
        logger.info("Processing {} tasks in parallel", tasks.size());

        List<CompletableFuture<AgentResult>> futures = tasks.stream()
            .map(task -> processTaskAsync(task, context))
            .collect(Collectors.toList());

        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }

    @Override
    @Async
    public CompletableFuture<List<AgentResult>> processTasksAsync(List<Task> tasks, AgentContext context) {
        return CompletableFuture.completedFuture(processTasks(tasks, context));
    }

    @Override
    public List<Task> decomposeTask(Task complexTask, AgentContext context) {
        // TODO: Integrate with LLM for intelligent task decomposition
        // For now, return the original task as a single-item list
        logger.info("Task decomposition not yet implemented, returning original task");
        return Collections.singletonList(complexTask);
    }

    private Agent selectBestAgent(List<Agent> capableAgents, Task task) {
        // Simple selection strategy - could be enhanced with:
        // - Agent load balancing
        // - Agent performance metrics
        // - Task priority matching
        // - Agent specialization scoring

        return capableAgents.stream()
            .filter(agent -> agent.getStatus() == AgentStatus.READY)
            .findFirst()
            .orElse(capableAgents.get(0));
    }
}
