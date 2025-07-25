package com.aiframework.orchestrator;

import com.aiframework.core.AgentContext;
import com.aiframework.core.AgentResult;
import com.aiframework.core.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for orchestrating tasks across multiple agents
 */
public interface OrchestratorService {
    /**
     * Process a single task
     */
    AgentResult processTask(Task task, AgentContext context);

    /**
     * Process a single task asynchronously
     */
    CompletableFuture<AgentResult> processTaskAsync(Task task, AgentContext context);

    /**
     * Process multiple tasks in parallel
     */
    List<AgentResult> processTasks(List<Task> tasks, AgentContext context);

    /**
     * Process multiple tasks asynchronously
     */
    CompletableFuture<List<AgentResult>> processTasksAsync(List<Task> tasks, AgentContext context);

    /**
     * Decompose a complex task into subtasks (LLM integration point)
     */
    List<Task> decomposeTask(Task complexTask, AgentContext context);
}
