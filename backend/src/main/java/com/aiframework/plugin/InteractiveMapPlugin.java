package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Interactive Map Plugin with Perplexity-like capabilities
 * Automatically displays maps when sentiment analysis indicates location relevance
 */
@Component
public class InteractiveMapPlugin implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;

    // Map configuration
    private final boolean enableRealTimeTraffic = true;
    private final boolean enablePointsOfInterest = true;
    private String defaultMapProvider = "google";

    @Override
    public String getName() {
        return "InteractiveMapPlugin";
    }

    @Override
    public String getDescription() {
        return "Interactive map visualization plugin that automatically displays maps based on search context and sentiment analysis";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;

        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

        return "MAP_VISUALIZATION".equals(task.getType()) ||
               "LOCATION_DISPLAY".equals(task.getType()) ||
               "CHART_CREATION".equals(task.getType()) ||
               taskType.contains("map") ||
               taskType.contains("location") ||
               taskType.contains("directions") ||
               description.contains("map") ||
               description.contains("location") ||
               description.contains("where") ||
               hasLocationContext();
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            status = AgentStatus.RUNNING;

            Map<String, Object> parameters = task.getParameters();
            String visualizationType = (String) parameters.getOrDefault("visualizationType", "roadmap");

            // Check for shared context from search agent
            Map<String, Object> mapData = checkForMapContext(task, parameters);

            if (mapData == null) {
                // Create map from task parameters
                mapData = createMapFromTask(task, parameters);
            }

            // Generate map visualization
            Map<String, Object> mapVisualization = generateMapVisualization(mapData);

            // Share map data with frontend
            shareMapVisualization(mapVisualization);

            status = AgentStatus.READY;

            return AgentResult.success(
                task.getId(),
                getName(),
                mapVisualization
            );

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(
                task.getId(),
                getName(),
                "Map visualization failed: " + e.getMessage()
            );
        }
    }

    /**
     * Check if there's location context from other agents (like search agent)
     */
    private boolean hasLocationContext() {
        if (contextStore == null) return false;

        try {
            // Check for map visualization requests
            var mapContext = contextStore.getContext("interactive-map", "mapRequest");
            if (mapContext.isPresent()) {
                return true;
            }

            // Check for shared map data
            var sharedMapData = contextStore.getSharedData("mapVisualization");
            if (sharedMapData.isPresent()) {
                return true;
            }

            // Check for location-related search results
            var searchResults = contextStore.getSharedData("searchResults");
            if (searchResults.isPresent()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) searchResults.get().getMetadata();
                Boolean hasLocationData = (Boolean) metadata.get("hasLocationData");
                return hasLocationData != null && hasLocationData;
            }

        } catch (Exception e) {
            System.err.println("Error checking location context: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check for map context from other agents
     */
    private Map<String, Object> checkForMapContext(Task task, Map<String, Object> parameters) {
        if (contextStore == null) return null;

        try {
            // First check for direct map request from search agent
            var mapContext = contextStore.getContext("interactive-map", "mapRequest");
            if (mapContext.isPresent()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapRequest = (Map<String, Object>) mapContext.get();

                // Clear the context after reading
                contextStore.clearContext("interactive-map");
                contextStore.clearContext("mapRequest");

                return processMapRequest(mapRequest);
            }

            // Check for shared map visualization data
            var sharedMapData = contextStore.getSharedData("mapVisualization");
            if (sharedMapData.isPresent()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) sharedMapData.get().getData();
                return mapData;
            }

            // Check for search results with location data
            var searchResults = contextStore.getSharedData("searchResults");
            if (searchResults.isPresent()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> results = (Map<String, Object>) searchResults.get().getData();
                @SuppressWarnings("unchecked")
                Map<String, Object> locationResults = (Map<String, Object>) results.get("locationResults");

                if (locationResults != null && !locationResults.isEmpty()) {
                    return createMapFromLocationResults(locationResults, results);
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking map context: " + e.getMessage());
        }

        return null;
    }

    /**
     * Process map request from search agent
     */
    private Map<String, Object> processMapRequest(Map<String, Object> mapRequest) {
        Map<String, Object> processedData = new HashMap<>();

        String query = (String) mapRequest.get("query");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> locations = (List<Map<String, Object>>) mapRequest.get("locations");
        @SuppressWarnings("unchecked")
        Map<String, Object> sentiment = (Map<String, Object>) mapRequest.get("sentiment");

        processedData.put("query", query);
        processedData.put("locations", locations != null ? locations : new ArrayList<>());
        processedData.put("sentiment", sentiment != null ? sentiment : new HashMap<>());
        processedData.put("visualizationType", mapRequest.get("visualizationType"));
        processedData.put("zoomLevel", mapRequest.get("zoomLevel"));
        processedData.put("showPOI", mapRequest.get("showPOI"));
        processedData.put("source", "search-agent");

        return processedData;
    }

    /**
     * Create map data from location results
     */
    private Map<String, Object> createMapFromLocationResults(Map<String, Object> locationResults, Map<String, Object> searchResults) {
        Map<String, Object> mapData = new HashMap<>();

        List<Map<String, Object>> locations = new ArrayList<>();
        locationResults.forEach((name, data) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> locationInfo = (Map<String, Object>) data;
            locations.add(locationInfo);
        });

        mapData.put("locations", locations);
        mapData.put("query", searchResults.get("query"));
        mapData.put("visualizationType", "roadmap");
        mapData.put("zoomLevel", 10);
        mapData.put("showPOI", true);
        mapData.put("source", "location-results");

        return mapData;
    }

    /**
     * Create map from task parameters
     */
    private Map<String, Object> createMapFromTask(Task task, Map<String, Object> parameters) {
        Map<String, Object> mapData = new HashMap<>();

        // Extract location from task description or parameters
        String location = (String) parameters.get("location");
        if (location == null && task.getDescription() != null) {
            location = extractLocationFromDescription(task.getDescription());
        }

        if (location != null) {
            Map<String, Object> locationInfo = createLocationInfo(location);
            mapData.put("locations", Arrays.asList(locationInfo));
        } else {
            mapData.put("locations", new ArrayList<>());
        }

        mapData.put("query", task.getDescription());
        mapData.put("visualizationType", parameters.getOrDefault("visualizationType", "roadmap"));
        mapData.put("zoomLevel", parameters.getOrDefault("zoomLevel", 10));
        mapData.put("showPOI", parameters.getOrDefault("showPOI", true));
        mapData.put("source", "direct-task");

        return mapData;
    }

    /**
     * Generate comprehensive map visualization
     */
    private Map<String, Object> generateMapVisualization(Map<String, Object> mapData) {
        Map<String, Object> visualization = new HashMap<>();

        try {
            String query = (String) mapData.get("query");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> locations = (List<Map<String, Object>>) mapData.get("locations");
            String visualizationType = (String) mapData.getOrDefault("visualizationType", "roadmap");
            Integer zoomLevel = (Integer) mapData.getOrDefault("zoomLevel", 10);
            Boolean showPOI = (Boolean) mapData.getOrDefault("showPOI", true);

            // Main map configuration
            Map<String, Object> mapConfig = new HashMap<>();
            mapConfig.put("provider", defaultMapProvider);
            mapConfig.put("type", visualizationType);
            mapConfig.put("zoom", zoomLevel);
            mapConfig.put("showTraffic", enableRealTimeTraffic);
            mapConfig.put("showPOI", showPOI && enablePointsOfInterest);

            // Calculate map center and bounds
            if (!locations.isEmpty()) {
                Map<String, Object> center = calculateMapCenter(locations);
                Map<String, Object> bounds = calculateMapBounds(locations);
                mapConfig.put("center", center);
                mapConfig.put("bounds", bounds);
            } else {
                // Default to a general location if no specific locations
                mapConfig.put("center", Map.of("lat", 40.7128, "lng", -74.0060)); // NYC
            }

            // Prepare markers for locations
            List<Map<String, Object>> markers = createMapMarkers(locations, query);

            // Additional map layers based on query sentiment
            @SuppressWarnings("unchecked")
            Map<String, Object> sentiment = (Map<String, Object>) mapData.get("sentiment");
            List<Map<String, Object>> layers = createMapLayers(query, sentiment);

            // Interactive features
            Map<String, Object> interactivity = createInteractivityConfig(query, locations);

            // Compile final visualization
            visualization.put("mapConfig", mapConfig);
            visualization.put("markers", markers);
            visualization.put("layers", layers);
            visualization.put("interactivity", interactivity);
            visualization.put("query", query);
            visualization.put("timestamp", LocalDateTime.now().toString());
            visualization.put("hasMultipleLocations", locations.size() > 1);
            visualization.put("totalLocations", locations.size());

            // Add metadata for frontend
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("searchQuery", query);
            metadata.put("mapProvider", defaultMapProvider);
            metadata.put("generatedAt", LocalDateTime.now().toString());
            metadata.put("source", mapData.get("source"));
            metadata.put("confidence", calculateMapConfidence(mapData));
            visualization.put("metadata", metadata);

        } catch (Exception e) {
            System.err.println("Error generating map visualization: " + e.getMessage());
            visualization.put("error", "Failed to generate map: " + e.getMessage());
            visualization.put("fallbackMap", createFallbackMap());
        }

        return visualization;
    }

    /**
     * Calculate map center from multiple locations
     */
    private Map<String, Object> calculateMapCenter(List<Map<String, Object>> locations) {
        if (locations.isEmpty()) {
            return Map.of("lat", 40.7128, "lng", -74.0060); // Default NYC
        }

        double totalLat = 0;
        double totalLng = 0;
        int validLocations = 0;

        for (Map<String, Object> location : locations) {
            @SuppressWarnings("unchecked")
            Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");
            if (coordinates != null) {
                Double lat = (Double) coordinates.get("lat");
                Double lng = (Double) coordinates.get("lng");
                if (lat != null && lng != null) {
                    totalLat += lat;
                    totalLng += lng;
                    validLocations++;
                }
            }
        }

        if (validLocations == 0) {
            return Map.of("lat", 40.7128, "lng", -74.0060); // Fallback
        }

        return Map.of(
            "lat", totalLat / validLocations,
            "lng", totalLng / validLocations
        );
    }

    /**
     * Calculate map bounds for multiple locations
     */
    private Map<String, Object> calculateMapBounds(List<Map<String, Object>> locations) {
        if (locations.isEmpty()) {
            return Map.of(
                "northeast", Map.of("lat", 40.8, "lng", -73.9),
                "southwest", Map.of("lat", 40.6, "lng", -74.1)
            );
        }

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;

        for (Map<String, Object> location : locations) {
            @SuppressWarnings("unchecked")
            Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");
            if (coordinates != null) {
                Double lat = (Double) coordinates.get("lat");
                Double lng = (Double) coordinates.get("lng");
                if (lat != null && lng != null) {
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                    minLng = Math.min(minLng, lng);
                    maxLng = Math.max(maxLng, lng);
                }
            }
        }

        // Add some padding
        double latPadding = (maxLat - minLat) * 0.1;
        double lngPadding = (maxLng - minLng) * 0.1;

        return Map.of(
            "northeast", Map.of("lat", maxLat + latPadding, "lng", maxLng + lngPadding),
            "southwest", Map.of("lat", minLat - latPadding, "lng", minLng - lngPadding)
        );
    }

    /**
     * Create markers for map locations
     */
    private List<Map<String, Object>> createMapMarkers(List<Map<String, Object>> locations, String query) {
        List<Map<String, Object>> markers = new ArrayList<>();

        for (int i = 0; i < locations.size(); i++) {
            Map<String, Object> location = locations.get(i);
            @SuppressWarnings("unchecked")
            Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");

            if (coordinates != null) {
                Map<String, Object> marker = new HashMap<>();
                marker.put("id", "marker_" + i);
                marker.put("position", coordinates);
                marker.put("title", location.get("name"));
                marker.put("description", location.get("description"));
                marker.put("type", location.getOrDefault("type", "location"));
                marker.put("icon", determineMarkerIcon(location, query));
                marker.put("infoWindow", createInfoWindow(location, query));
                markers.add(marker);
            }
        }

        return markers;
    }

    /**
     * Create map layers based on sentiment and query
     */
    private List<Map<String, Object>> createMapLayers(String query, Map<String, Object> sentiment) {
        List<Map<String, Object>> layers = new ArrayList<>();

        if (query == null) return layers;

        // Traffic layer for navigation queries
        if (query.toLowerCase().contains("traffic") || query.toLowerCase().contains("directions")) {
            layers.add(Map.of(
                "type", "traffic",
                "visible", true,
                "opacity", 0.8
            ));
        }

        // Transit layer for public transport queries
        if (query.toLowerCase().contains("transit") || query.toLowerCase().contains("subway") ||
            query.toLowerCase().contains("bus")) {
            layers.add(Map.of(
                "type", "transit",
                "visible", true,
                "opacity", 0.7
            ));
        }

        // Weather layer based on sentiment
        if (sentiment != null) {
            Double sentimentScore = (Double) sentiment.get("sentimentScore");
            if (sentimentScore != null && Math.abs(sentimentScore) > 0.3) {
                layers.add(Map.of(
                    "type", "weather",
                    "visible", false, // Hidden by default, can be toggled
                    "opacity", 0.6
                ));
            }
        }

        return layers;
    }

    /**
     * Create interactivity configuration
     */
    private Map<String, Object> createInteractivityConfig(String query, List<Map<String, Object>> locations) {
        Map<String, Object> config = new HashMap<>();

        config.put("enableZoom", true);
        config.put("enablePan", true);
        config.put("enableStreetView", locations.size() <= 3); // Limit for performance
        config.put("enableDirections", query != null &&
            (query.toLowerCase().contains("directions") || query.toLowerCase().contains("route")));
        config.put("enableSearch", true);
        config.put("enableLayerToggle", true);

        // Custom controls based on query type
        List<String> customControls = new ArrayList<>();
        if (query != null) {
            if (query.toLowerCase().contains("restaurant") || query.toLowerCase().contains("food")) {
                customControls.add("restaurant_filter");
            }
            if (query.toLowerCase().contains("hotel") || query.toLowerCase().contains("accommodation")) {
                customControls.add("accommodation_filter");
            }
            if (query.toLowerCase().contains("weather")) {
                customControls.add("weather_toggle");
            }
        }
        config.put("customControls", customControls);

        return config;
    }

    // Helper methods
    private String extractLocationFromDescription(String description) {
        // Simple location extraction - in production, use NER
        String[] cities = {"new york", "london", "paris", "tokyo", "berlin"};
        String lowerDesc = description.toLowerCase();

        for (String city : cities) {
            if (lowerDesc.contains(city)) {
                return city;
            }
        }
        return null;
    }

    private Map<String, Object> createLocationInfo(String location) {
        // Mock location data - in production, use geocoding service
        Map<String, Object> info = new HashMap<>();
        info.put("name", location);
        info.put("coordinates", Map.of("lat", 40.7128, "lng", -74.0060)); // Default to NYC
        info.put("type", "city");
        info.put("description", "Location: " + location);
        return info;
    }

    private String determineMarkerIcon(Map<String, Object> location, String query) {
        String type = (String) location.get("type");
        if ("restaurant".equals(type)) return "restaurant";
        if ("hotel".equals(type)) return "lodging";
        if ("hospital".equals(type)) return "hospital";
        if ("school".equals(type)) return "school";

        if (query != null) {
            String lowerQuery = query.toLowerCase();
            if (lowerQuery.contains("restaurant")) return "restaurant";
            if (lowerQuery.contains("hotel")) return "lodging";
            if (lowerQuery.contains("hospital")) return "hospital";
        }

        return "location"; // Default
    }

    private Map<String, Object> createInfoWindow(Map<String, Object> location, String query) {
        Map<String, Object> infoWindow = new HashMap<>();

        String name = (String) location.get("name");
        String description = (String) location.get("description");

        StringBuilder content = new StringBuilder();
        content.append("<div class='map-info-window'>");
        content.append("<h3>").append(name != null ? name : "Location").append("</h3>");
        if (description != null) {
            content.append("<p>").append(description).append("</p>");
        }
        content.append("</div>");

        infoWindow.put("content", content.toString());
        infoWindow.put("maxWidth", 300);

        return infoWindow;
    }

    private double calculateMapConfidence(Map<String, Object> mapData) {
        double confidence = 0.5; // Base confidence

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> locations = (List<Map<String, Object>>) mapData.get("locations");
        if (locations != null && !locations.isEmpty()) {
            confidence += 0.3;

            // Higher confidence for more specific locations
            if (locations.size() == 1) {
                confidence += 0.2;
            }
        }

        String source = (String) mapData.get("source");
        if ("search-agent".equals(source)) {
            confidence += 0.1; // Higher confidence from search agent
        }

        return Math.min(confidence, 1.0);
    }

    private Map<String, Object> createFallbackMap() {
        Map<String, Object> fallback = new HashMap<>();

        fallback.put("mapConfig", Map.of(
            "provider", "google",
            "type", "roadmap",
            "zoom", 10,
            "center", Map.of("lat", 40.7128, "lng", -74.0060)
        ));
        fallback.put("markers", new ArrayList<>());
        fallback.put("layers", new ArrayList<>());
        fallback.put("message", "Default map view - no specific location detected");

        return fallback;
    }

    /**
     * Share map visualization with frontend and other agents
     */
    private void shareMapVisualization(Map<String, Object> visualization) {
        if (contextStore == null) return;

        try {
            // Store for frontend consumption
            contextStore.storeContext(getName(), "currentMap", visualization);

            // Share with other agents
            contextStore.storeSharedData(
                "activeMapVisualization",
                visualization,
                getName(),
                Map.of(
                    "dataType", "map-visualization",
                    "hasInteractivity", true,
                    "canEmbed", true,
                    "timestamp", LocalDateTime.now().toString()
                )
            );

        } catch (Exception e) {
            System.err.println("Failed to share map visualization: " + e.getMessage());
        }
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        this.status = AgentStatus.READY;

        // Initialize API keys from config
        if (config != null && config.getProperties() != null) {
            String mapProvider = (String) config.getProperties().get("map.default.provider");
            this.defaultMapProvider = mapProvider != null ? mapProvider : "google";
        }
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
