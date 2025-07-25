package com.aiframework.controller;

import com.aiframework.core.Agent;
import com.aiframework.dto.AgentInfo;
import com.aiframework.manager.AgentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for agent management
 */
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    private final AgentManager agentManager;

    public AgentController(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    /**
     * Get all agents
     */
    @GetMapping
    public ResponseEntity<List<AgentInfo>> getAllAgents() {
        logger.info("Fetching all agents");

        List<Agent> agents = agentManager.getAllAgents();
        List<AgentInfo> agentInfos = agents.stream()
            .map(AgentInfo::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(agentInfos);
    }

    /**
     * Get a specific agent by name
     */
    @GetMapping("/{name}")
    public ResponseEntity<AgentInfo> getAgent(@PathVariable String name) {
        logger.info("Fetching agent: {}", name);

        Optional<Agent> agent = agentManager.getAgent(name);
        if (agent.isPresent()) {
            return ResponseEntity.ok(new AgentInfo(agent.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get agents capable of handling a specific task type
     */
    @GetMapping("/capable/{taskType}")
    public ResponseEntity<List<AgentInfo>> getCapableAgents(@PathVariable String taskType) {
        logger.info("Fetching agents capable of handling task type: {}", taskType);

        List<Agent> agents = agentManager.getCapableAgents(taskType);
        List<AgentInfo> agentInfos = agents.stream()
            .map(AgentInfo::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(agentInfos);
    }

    /**
     * Reload all agents (useful for plugin updates)
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadAgents() {
        logger.info("Reloading all agents");

        try {
            agentManager.reloadAgents();
            return ResponseEntity.ok("Agents reloaded successfully");
        } catch (Exception e) {
            logger.error("Error reloading agents", e);
            return ResponseEntity.internalServerError().body("Error reloading agents: " + e.getMessage());
        }
    }
}
