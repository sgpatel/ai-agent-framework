package com.aiframework.controller;

import com.aiframework.core.AgentResult;
import com.aiframework.core.Task;
import com.aiframework.dto.*;
import com.aiframework.service.TaskProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for task operations
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskProcessingService taskProcessingService;

    public TaskController(TaskProcessingService taskProcessingService) {
        this.taskProcessingService = taskProcessingService;
    }

    /**
     * Process a single task
     */
    @PostMapping
    public ResponseEntity<TaskResult> processTask(@RequestBody TaskRequest request) {
        logger.info("Received task request: type={}, user={}", request.getType(), request.getUserId());

        try {
            Task task = request.toTask();
            AgentResult result;

            if (request.getSessionId() != null) {
                result = taskProcessingService.processTask(task, request.getSessionId(), request.getUserId());
            } else {
                result = taskProcessingService.processTask(task, request.getUserId());
            }

            return ResponseEntity.ok(new TaskResult(result));

        } catch (Exception e) {
            logger.error("Error processing task", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Process multiple tasks in batch
     */
    @PostMapping("/batch")
    public ResponseEntity<List<TaskResult>> processBatchTasks(@RequestBody BatchTaskRequest request) {
        logger.info("Received batch task request: {} tasks, user={}", 
            request.getTasks().size(), request.getUserId());

        try {
            List<Task> tasks = request.getTasks().stream()
                .map(TaskRequest::toTask)
                .collect(Collectors.toList());

            List<AgentResult> results = taskProcessingService.processTasks(tasks, request.getUserId());

            List<TaskResult> taskResults = results.stream()
                .map(TaskResult::new)
                .collect(Collectors.toList());

            return ResponseEntity.ok(taskResults);

        } catch (Exception e) {
            logger.error("Error processing batch tasks", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Process a task asynchronously
     */
    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<TaskResult>> processTaskAsync(@RequestBody TaskRequest request) {
        logger.info("Received async task request: type={}, user={}", request.getType(), request.getUserId());

        Task task = request.toTask();

        return taskProcessingService.processTaskAsync(task, request.getUserId())
            .thenApply(result -> ResponseEntity.ok(new TaskResult(result)))
            .exceptionally(throwable -> {
                logger.error("Error processing async task", throwable);
                return ResponseEntity.internalServerError().build();
            });
    }

    /**
     * Get system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Object> getMetrics() {
        logger.info("Fetching system metrics");
        
        // Basic metrics object
        var metrics = new java.util.HashMap<String, Object>();
        metrics.put("totalTasks", 0);
        metrics.put("completedTasks", 0);
        metrics.put("failedTasks", 0);
        metrics.put("averageProcessingTime", 0.0);
        metrics.put("activeAgents", 2);
        metrics.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(metrics);
    }
}
