package com.aiframework.controller;

import com.aiframework.core.*;
import com.aiframework.orchestrator.OrchestratorService;
import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Search Controller with Perplexity-like capabilities
 * Handles intelligent search with automatic map integration
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private OrchestratorService orchestrator;

    @Autowired(required = false)
    private ContextStore contextStore;

    /**
     * Perform intelligent search with automatic map integration
     */
    @PostMapping("/intelligent")
    public ResponseEntity<?> performIntelligentSearch(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            String searchType = (String) request.getOrDefault("searchType", "comprehensive");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Query parameter is required",
                    "code", "MISSING_QUERY"
                ));
            }

            // Create search task
            Task searchTask = new Task();
            searchTask.setId("search-" + System.currentTimeMillis());
            searchTask.setType("SEARCH");
            searchTask.setDescription(query);
            searchTask.setParameters(Map.of(
                "query", query,
                "searchType", searchType,
                "enableMapIntegration", true
            ));

            // Execute search
            AgentContext agentContext = new AgentContext();
            AgentResult searchResult = orchestrator.processTask(searchTask, agentContext);

            // Check for map visualization
            Map<String, Object> mapVisualization = null;
            if (contextStore != null) {
                var mapData = contextStore.getSharedData("activeMapVisualization");
                if (mapData.isPresent()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mapResult = (Map<String, Object>) mapData.get().getData();
                    mapVisualization = mapResult;
                }
            }

            // Compile comprehensive response
            Map<String, Object> response = new HashMap<>();
            response.put("searchResults", searchResult.getData());
            response.put("mapVisualization", mapVisualization);
            response.put("hasMap", mapVisualization != null);
            response.put("query", query);
            response.put("searchType", searchType);
            response.put("timestamp", System.currentTimeMillis());

            if (searchResult.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", searchResult.getMessage());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Search failed: " + e.getMessage(),
                "code", "SEARCH_ERROR"
            ));
        }
    }

    /**
     * Get map visualization for a specific query
     */
    @PostMapping("/map")
    public ResponseEntity<?> getMapVisualization(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            String location = (String) request.get("location");
            String visualizationType = (String) request.getOrDefault("visualizationType", "roadmap");

            if (query == null && location == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Either query or location parameter is required"
                ));
            }

            // Create map task
            Task mapTask = new Task();
            mapTask.setId("map-" + System.currentTimeMillis());
            mapTask.setType("MAP_VISUALIZATION");
            mapTask.setDescription(query != null ? query : "Show map for: " + location);
            mapTask.setParameters(Map.of(
                "query", query != null ? query : location,
                "location", location != null ? location : "",
                "visualizationType", visualizationType
            ));

            // Execute map visualization
            AgentContext agentContext = new AgentContext();
            AgentResult mapResult = orchestrator.processTask(mapTask, agentContext);

            if (mapResult.isSuccess()) {
                return ResponseEntity.ok(mapResult.getData());
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "error", mapResult.getMessage(),
                    "code", "MAP_ERROR"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Map visualization failed: " + e.getMessage(),
                "code", "MAP_ERROR"
            ));
        }
    }

    /**
     * Search with specific focus (news, academic, location)
     */
    @PostMapping("/focused")
    public ResponseEntity<?> performFocusedSearch(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            String focus = (String) request.getOrDefault("focus", "general");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Query parameter is required"
                ));
            }

            // Determine task type based on focus
            String taskType = switch (focus.toLowerCase()) {
                case "news" -> "WEB_SEARCH";
                case "academic" -> "RESEARCH";
                case "location" -> "SEARCH";
                default -> "SEARCH";
            };

            Task searchTask = new Task();
            searchTask.setId("focused-search-" + System.currentTimeMillis());
            searchTask.setType(taskType);
            searchTask.setDescription(query);
            searchTask.setParameters(Map.of(
                "query", query,
                "searchType", focus,
                "enableMapIntegration", "location".equals(focus)
            ));

            AgentContext agentContext = new AgentContext();
            AgentResult result = orchestrator.processTask(searchTask, agentContext);

            Map<String, Object> response = new HashMap<>();
            response.put("results", result.getData());
            response.put("focus", focus);
            response.put("query", query);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Focused search failed: " + e.getMessage(),
                "code", "FOCUSED_SEARCH_ERROR"
            ));
        }
    }

    /**
     * Get search suggestions based on partial query
     */
    @GetMapping("/suggestions")
    public ResponseEntity<?> getSearchSuggestions(@RequestParam String query) {
        try {
            List<String> suggestions = generateSearchSuggestions(query);

            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "query", query
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get suggestions: " + e.getMessage()
            ));
        }
    }

    /**
     * Get current search context and active visualizations
     */
    @GetMapping("/context")
    public ResponseEntity<?> getSearchContext() {
        try {
            Map<String, Object> context = new HashMap<>();

            if (contextStore != null) {
                // Get active search results
                var searchResults = contextStore.getSharedData("searchResults");
                if (searchResults.isPresent()) {
                    context.put("activeSearch", searchResults.get().getData());
                }

                // Get active map visualization
                var mapVisualization = contextStore.getSharedData("activeMapVisualization");
                if (mapVisualization.isPresent()) {
                    context.put("activeMap", mapVisualization.get().getData());
                }

                // Get search agent context
                var searchAgent = contextStore.getContext("IntelligentSearchAgent", "currentQuery");
                if (searchAgent.isPresent()) {
                    context.put("currentQuery", searchAgent.get());
                }
            }

            return ResponseEntity.ok(context);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get search context: " + e.getMessage()
            ));
        }
    }

    private List<String> generateSearchSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return Arrays.asList(
                "What is artificial intelligence?",
                "Best restaurants in New York",
                "Latest technology news",
                "Weather in London",
                "How to learn programming"
            );
        }

        String lowerQuery = query.toLowerCase();

        // Location-based suggestions
        if (lowerQuery.contains("where") || lowerQuery.contains("location") || lowerQuery.contains("map")) {
            suggestions.addAll(Arrays.asList(
                query + " - show on map",
                "Best places in " + query,
                "Directions to " + query,
                "Weather in " + query
            ));
        }

        // News suggestions
        if (lowerQuery.contains("news") || lowerQuery.contains("latest") || lowerQuery.contains("recent")) {
            suggestions.addAll(Arrays.asList(
                "Latest " + query,
                "Breaking news about " + query,
                "Recent developments in " + query
            ));
        }

        // General suggestions
        suggestions.addAll(Arrays.asList(
            "What is " + query + "?",
            "How does " + query + " work?",
            "Best " + query + " options",
            query + " comparison",
            query + " reviews"
        ));

        return suggestions.stream().limit(10).toList();
    }
}
