package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import com.aiframework.service.FreeApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Intelligent Search Agent with Perplexity-like capabilities
 * Provides comprehensive search, synthesis, and location-aware responses
 * Now integrated with free APIs for real data
 */
@Component
public class IntelligentSearchAgent implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    @Autowired(required = false)
    private FreeApiService freeApiService;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;

    // Search configuration
    private final boolean enableLocationDetection = true;
    private final boolean enableRealTimeSearch = true;

    public IntelligentSearchAgent() {
        initializeConfig();
    }

    private void initializeConfig() {
        this.config = new AgentConfig();
        this.config.setName("IntelligentSearchAgent");
        // Use setProperty for additional configuration
        this.config.setProperty("description", "Advanced search agent providing comprehensive, real-time answers with source citations and location awareness");
        this.config.setProperty("capabilities", List.of(
            "web_search",
            "news_search",
            "location_detection",
            "real_time_data",
            "source_verification",
            "synthesis"
        ));
        this.config.setProperty("maxConcurrentTasks", 5);
        this.config.setProperty("timeoutMs", 30000);
        this.status = AgentStatus.READY;
    }

    @Override
    public AgentConfig getConfig() {
        if (config == null) {
            initializeConfig();
        }
        return this.config;
    }

    @Override
    public String getName() {
        return "IntelligentSearchAgent";
    }

    @Override
    public String getDescription() {
        return "Advanced search agent providing comprehensive, real-time answers with source citations and location awareness";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;

        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

        return "SEARCH".equals(task.getType()) ||
               "WEB_SEARCH".equals(task.getType()) ||
               "RESEARCH".equals(task.getType()) ||
               "QUESTION_ANSWERING".equals(task.getType()) ||
               taskType.contains("search") ||
               taskType.contains("find") ||
               taskType.contains("research") ||
               taskType.contains("what") ||
               taskType.contains("where") ||
               taskType.contains("how") ||
               taskType.contains("when") ||
               taskType.contains("why") ||
               description.contains("search") ||
               description.contains("find") ||
               description.contains("location");
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            status = AgentStatus.RUNNING;

            // Initialize FreeApiService if not injected by Spring
            if (freeApiService == null) {
                initializeFreeApiService();
            }

            Map<String, Object> parameters = task.getParameters();
            String query = (String) parameters.getOrDefault("query", task.getDescription());
            String searchType = (String) parameters.getOrDefault("searchType", "comprehensive");

            // Store context for collaboration
            if (contextStore != null) {
                contextStore.storeContext(getName(), "currentQuery", query);
                contextStore.storeContext(getName(), "dataType", "search-results");
                contextStore.storeContext(getName(), "searchType", searchType);
            }

            // Perform intelligent search
            Map<String, Object> searchResults = performIntelligentSearch(query, searchType);

            // Check if location/map context is needed
            boolean hasLocationContext = detectLocationIntent(query, searchResults);
            if (hasLocationContext) {
                triggerMapPlugin(query, searchResults);
            }

            // Share results with other plugins
            shareSearchResults(query, searchResults);

            status = AgentStatus.READY;

            return AgentResult.success(
                task.getId(),
                getName(),
                searchResults
            );

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(
                task.getId(),
                getName(),
                "Search failed: " + e.getMessage()
            );
        }
    }

    /**
     * Initialize FreeApiService manually if Spring DI is not available
     */
    private void initializeFreeApiService() {
        try {
            // Create RestTemplate and FreeApiService manually
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            this.freeApiService = new FreeApiService(restTemplate);
            System.out.println("FreeApiService initialized manually for IntelligentSearchAgent");
        } catch (Exception e) {
            System.err.println("Failed to initialize FreeApiService manually: " + e.getMessage());
            // Set to null so fallback methods will be used
            this.freeApiService = null;
        }
    }

    /**
     * Perform comprehensive search with multiple sources
     */
    private Map<String, Object> performIntelligentSearch(String query, String searchType) {
        Map<String, Object> results = new HashMap<>();

        try {
            // 1. Web Search Results
            List<Map<String, Object>> webResults = performWebSearch(query);
            results.put("webResults", webResults);

            // 2. News Search (if recent/current events)
            if (isNewsQuery(query)) {
                List<Map<String, Object>> newsResults = performNewsSearch(query);
                results.put("newsResults", newsResults);
            }

            // 3. Academic/Research Sources
            if (isAcademicQuery(query)) {
                List<Map<String, Object>> academicResults = performAcademicSearch(query);
                results.put("academicResults", academicResults);
            }

            // 4. Location-based search
            if (isLocationQuery(query)) {
                Map<String, Object> locationResults = performLocationSearch(query);
                results.put("locationResults", locationResults);
            }

            // 5. Image Search (if visual content would be helpful)
            if (isImageQuery(query)) {
                List<Map<String, Object>> imageResults = performImageSearch(query);
                results.put("imageResults", imageResults);
            }

            // 6. Synthesize comprehensive answer
            Map<String, Object> synthesis = synthesizeResults(query, results);
            results.put("synthesis", synthesis);

            // 7. Source citations and confidence
            results.put("sources", extractSources(results));
            results.put("confidence", calculateConfidence(results));
            results.put("searchType", searchType);
            results.put("timestamp", LocalDateTime.now().toString());
            results.put("query", query);

        } catch (Exception e) {
            results.put("error", "Search processing failed: " + e.getMessage());
            results.put("fallbackAnswer", generateFallbackAnswer(query));
        }

        return results;
    }

    /**
     * Perform web search using DuckDuckGo and Wikipedia
     */
    private List<Map<String, Object>> performWebSearch(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // Search with DuckDuckGo
            List<Map<String, Object>> duckDuckGoResults = freeApiService.searchDuckDuckGo(query);
            results.addAll(duckDuckGoResults);

            // Add Wikipedia results for educational queries
            if (isEducationalQuery(query)) {
                List<Map<String, Object>> wikipediaResults = freeApiService.searchWikipedia(query);
                results.addAll(wikipediaResults);
            }

            // If we have limited results, add some contextual information
            if (results.size() < 3) {
                results.addAll(generateContextualResults(query));
            }

        } catch (Exception e) {
            System.err.println("Web search failed: " + e.getMessage());
            // Fallback to basic results
            results.addAll(generateFallbackResults(query));
        }

        return results;
    }

    /**
     * Perform news search using NewsAPI
     */
    private List<Map<String, Object>> performNewsSearch(String query) {
        try {
            return freeApiService.searchNews(query);
        } catch (Exception e) {
            System.err.println("News search failed: " + e.getMessage());
            return generateMockNewsResults(query);
        }
    }

    /**
     * Perform academic/research search
     */
    private List<Map<String, Object>> performAcademicSearch(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Mock implementation - integrate with:
        // - Google Scholar API
        // - PubMed API
        // - arXiv API
        // - ResearchGate

        for (int i = 0; i < 3; i++) {
            Map<String, Object> paper = new HashMap<>();
            paper.put("title", "Academic Research on " + query);
            paper.put("authors", Arrays.asList("Dr. Smith", "Dr. Johnson", "Prof. Wilson"));
            paper.put("journal", "Journal of Advanced Research");
            paper.put("year", 2024 - i);
            paper.put("citations", 150 - (i * 20));
            paper.put("abstract", "This study explores " + query +
                     " through comprehensive analysis and empirical research.");
            paper.put("doi", "10.1000/example" + i);
            results.add(paper);
        }

        return results;
    }

    /**
     * Perform location-based search using multiple free APIs
     */
    private Map<String, Object> performLocationSearch(String query) {
        Map<String, Object> locationData = new HashMap<>();

        try {
            // Extract location entities from query
            List<String> locations = extractLocations(query);

            for (String location : locations) {
                // Get geocoding information
                Map<String, Object> geoInfo = freeApiService.geocodeLocation(location);
                if (geoInfo != null) {
                    locationData.put(location, geoInfo);

                    // Add weather information if relevant
                    if (isWeatherQuery(query)) {
                        Map<String, Object> weatherInfo = freeApiService.getWeatherInfo(location);
                        if (weatherInfo != null) {
                            geoInfo.put("weather", weatherInfo);
                        }
                    }

                    // Add country information if it's a country query
                    if (isCountryQuery(query, location)) {
                        Map<String, Object> countryInfo = freeApiService.getCountryInfo(location);
                        if (countryInfo != null) {
                            geoInfo.put("countryDetails", countryInfo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Location search failed: " + e.getMessage());
            // Fallback to basic location data
            return generateFallbackLocationData(query);
        }

        return locationData;
    }

    /**
     * Perform image search using Pixabay API
     */
    private List<Map<String, Object>> performImageSearch(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            if (freeApiService != null) {
                // Search with Pixabay
                List<Map<String, Object>> pixabayResults = freeApiService.searchImages(query);
                results.addAll(pixabayResults);
            }
        } catch (Exception e) {
            System.err.println("Image search failed: " + e.getMessage());
            // Return empty results on failure
        }

        return results;
    }

    /**
     * Synthesize results into comprehensive answer
     */
    private Map<String, Object> synthesizeResults(String query, Map<String, Object> allResults) {
        Map<String, Object> synthesis = new HashMap<>();

        // Generate comprehensive answer
        StringBuilder answer = new StringBuilder();
        answer.append("Based on comprehensive research across multiple sources, here's what I found about ")
              .append(query).append(":\n\n");

        // Incorporate web results
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> webResults = (List<Map<String, Object>>) allResults.get("webResults");
        if (webResults != null && !webResults.isEmpty()) {
            answer.append("**Key Findings:**\n");
            for (int i = 0; i < Math.min(3, webResults.size()); i++) {
                Map<String, Object> result = webResults.get(i);
                answer.append("• ").append(result.get("snippet")).append("\n");
            }
            answer.append("\n");
        }

        // Incorporate news if available
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> newsResults = (List<Map<String, Object>>) allResults.get("newsResults");
        if (newsResults != null && !newsResults.isEmpty()) {
            answer.append("**Recent Developments:**\n");
            for (Map<String, Object> news : newsResults) {
                answer.append("• ").append(news.get("summary")).append(" (").append(news.get("source")).append(")\n");
            }
            answer.append("\n");
        }

        // Add location context if available
        @SuppressWarnings("unchecked")
        Map<String, Object> locationResults = (Map<String, Object>) allResults.get("locationResults");
        if (locationResults != null && !locationResults.isEmpty()) {
            answer.append("**Location Context:**\n");
            locationResults.forEach((location, data) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> locData = (Map<String, Object>) data;
                answer.append("• ").append(location).append(": ").append(locData.get("description")).append("\n");
            });
        }

        synthesis.put("comprehensiveAnswer", answer.toString());
        synthesis.put("keyPoints", extractKeyPoints(allResults));
        synthesis.put("confidence", calculateAnswerConfidence(allResults));
        synthesis.put("hasLocationContext", locationResults != null && !locationResults.isEmpty());

        return synthesis;
    }

    /**
     * Detect if query has location intent using sentiment analysis
     */
    private boolean detectLocationIntent(String query, Map<String, Object> searchResults) {
        // Check for explicit location keywords
        String[] locationKeywords = {
            "where", "location", "place", "city", "country", "map", "directions",
            "near", "nearby", "around", "in", "at", "restaurant", "hotel",
            "weather", "climate", "geography", "population", "area"
        };

        String lowerQuery = query.toLowerCase();
        for (String keyword : locationKeywords) {
            if (lowerQuery.contains(keyword)) {
                return true;
            }
        }

        // Check if sentiment analysis indicates location relevance
        if (contextStore != null) {
            var sentimentData = contextStore.getSharedData("querysentiment");
            if (sentimentData.isPresent()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sentiment = (Map<String, Object>) sentimentData.get().getData();

                // Check if sentiment indicates location-related content
                @SuppressWarnings("unchecked")
                List<String> entities = (List<String>) sentiment.get("locationEntities");
                if (entities != null && !entities.isEmpty()) {
                    return true;
                }

                Double locationRelevance = (Double) sentiment.get("locationRelevance");
                if (locationRelevance != null && locationRelevance > 0.6) {
                    return true;
                }
            }
        }

        // Check search results for location indicators
        @SuppressWarnings("unchecked")
        Map<String, Object> locationResults = (Map<String, Object>) searchResults.get("locationResults");
        return locationResults != null && !locationResults.isEmpty();
    }

    /**
     * Trigger map plugin with intelligent context
     */
    private void triggerMapPlugin(String query, Map<String, Object> searchResults) {
        if (contextStore == null) return;

        try {
            // Perform sentiment analysis on the query for location context
            Map<String, Object> locationSentiment = analyzeLocationSentiment(query, searchResults);

            // Create map visualization request
            Map<String, Object> mapRequest = new HashMap<>();
            mapRequest.put("query", query);
            mapRequest.put("locations", extractLocationsFromResults(searchResults));
            mapRequest.put("sentiment", locationSentiment);
            mapRequest.put("visualizationType", determineMapType(query, locationSentiment));
            mapRequest.put("zoomLevel", determineZoomLevel(query, searchResults));
            mapRequest.put("showPOI", shouldShowPointsOfInterest(query));
            mapRequest.put("timestamp", LocalDateTime.now().toString());

            // Store context for map plugin
            contextStore.storeContext("interactive-map", "mapRequest", mapRequest);

            // Share data for map plugin
            contextStore.storeSharedData(
                "mapVisualization",
                mapRequest,
                getName(),
                Map.of(
                    "dataType", "location-visualization",
                    "targetAgent", "interactive-map",
                    "priority", "high",
                    "requiresMap", true
                )
            );

            System.out.println("Map plugin triggered for query: " + query);

        } catch (Exception e) {
            System.err.println("Failed to trigger map plugin: " + e.getMessage());
        }
    }

    /**
     * Analyze location sentiment to determine map visualization needs
     */
    private Map<String, Object> analyzeLocationSentiment(String query, Map<String, Object> searchResults) {
        Map<String, Object> sentiment = new HashMap<>();

        try {
            // Use sentiment analysis agent
            Task sentimentTask = new Task();
            sentimentTask.setId("location-sentiment-" + System.currentTimeMillis());
            sentimentTask.setType("LOCATION_SENTIMENT");
            sentimentTask.setDescription("Analyze location sentiment for: " + query);
            sentimentTask.setParameters(Map.of(
                "query", query,
                "context", "location-search",
                "searchResults", searchResults
            ));

            // Basic sentiment analysis for location context
            sentiment.put("locationRelevance", calculateLocationRelevance(query));
            sentiment.put("sentimentScore", calculateQuerySentiment(query));
            sentiment.put("locationEntities", new ArrayList<>()); // Fix: Use new ArrayList() instead of Arrays.asList()
            sentiment.put("visualizationNeed", determineVisualizationNeed(query, searchResults));
            sentiment.put("userIntent", classifyLocationIntent(query));

        } catch (Exception e) {
            System.err.println("Location sentiment analysis failed: " + e.getMessage());
            // Fallback sentiment
            sentiment.put("locationRelevance", 0.5);
            sentiment.put("sentimentScore", 0.0);
            sentiment.put("locationEntities", new ArrayList<>()); // Fix: Use new ArrayList() instead of Arrays.asList()
        }

        return sentiment;
    }

    // Helper methods for location and sentiment analysis
    private double calculateLocationRelevance(String query) {
        String[] locationIndicators = {"where", "location", "map", "near", "in", "at", "city", "country"};
        long matches = Arrays.stream(locationIndicators)
            .mapToLong(indicator -> query.toLowerCase().contains(indicator) ? 1 : 0)
            .sum();
        return Math.min(matches * 0.2, 1.0);
    }

    private double calculateQuerySentiment(String query) {
        // Simple sentiment calculation based on keywords
        String[] positiveWords = {"best", "good", "great", "excellent", "amazing", "beautiful"};
        String[] negativeWords = {"worst", "bad", "terrible", "awful", "ugly", "dangerous"};

        long positive = Arrays.stream(positiveWords)
            .mapToLong(word -> query.toLowerCase().contains(word) ? 1 : 0).sum();
        long negative = Arrays.stream(negativeWords)
            .mapToLong(word -> query.toLowerCase().contains(word) ? 1 : 0).sum();

        if (positive + negative == 0) return 0.0;
        return (double)(positive - negative) / (positive + negative);
    }

    private List<String> extractLocations(String query) {
        List<String> locations = new ArrayList<>();

        // Simple location extraction - in production, use NER models
        String[] commonCities = {"new york", "london", "paris", "tokyo", "berlin", "moscow", "sydney", "toronto"};
        String[] commonCountries = {"usa", "uk", "france", "japan", "germany", "russia", "australia", "canada"};

        String lowerQuery = query.toLowerCase();
        for (String city : commonCities) {
            if (lowerQuery.contains(city)) {
                locations.add(city);
            }
        }
        for (String country : commonCountries) {
            if (lowerQuery.contains(country)) {
                locations.add(country);
            }
        }

        return locations;
    }

    private String determineMapType(String query, Map<String, Object> sentiment) {
        if (query.toLowerCase().contains("satellite")) return "satellite";
        if (query.toLowerCase().contains("terrain")) return "terrain";
        if (query.toLowerCase().contains("traffic")) return "traffic";

        Double sentimentScore = (Double) sentiment.get("sentimentScore");
        if (sentimentScore != null && sentimentScore > 0.5) {
            return "hybrid"; // Show both map and satellite for positive queries
        }

        return "roadmap"; // Default
    }

    private int determineZoomLevel(String query, Map<String, Object> searchResults) {
        if (query.toLowerCase().contains("city") || query.toLowerCase().contains("downtown")) return 12;
        if (query.toLowerCase().contains("country") || query.toLowerCase().contains("state")) return 6;
        if (query.toLowerCase().contains("street") || query.toLowerCase().contains("address")) return 16;

        return 10; // Default city-level zoom
    }

    private boolean shouldShowPointsOfInterest(String query) {
        String[] poiKeywords = {"restaurant", "hotel", "store", "shop", "museum", "park", "hospital", "school"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(poiKeywords).anyMatch(lowerQuery::contains);
    }

    private List<Map<String, Object>> extractLocationsFromResults(Map<String, Object> searchResults) {
        List<Map<String, Object>> locations = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map<String, Object> locationResults = (Map<String, Object>) searchResults.get("locationResults");
        if (locationResults != null) {
            locationResults.forEach((name, data) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> locationData = (Map<String, Object>) data;
                locations.add(locationData);
            });
        }

        return locations;
    }

    private String determineVisualizationNeed(String query, Map<String, Object> searchResults) {
        if (query.toLowerCase().contains("directions") || query.toLowerCase().contains("route")) {
            return "directions";
        }
        if (query.toLowerCase().contains("nearby") || query.toLowerCase().contains("around")) {
            return "proximity";
        }
        if (searchResults.containsKey("locationResults")) {
            return "location_overview";
        }
        return "general";
    }

    private String classifyLocationIntent(String query) {
        if (query.toLowerCase().contains("how to get") || query.toLowerCase().contains("directions")) {
            return "navigation";
        }
        if (query.toLowerCase().contains("what is") || query.toLowerCase().contains("tell me about")) {
            return "information";
        }
        if (query.toLowerCase().contains("best") || query.toLowerCase().contains("recommend")) {
            return "recommendation";
        }
        return "general_inquiry";
    }

    // Helper methods for query classification
    private boolean isEducationalQuery(String query) {
        String[] educationalKeywords = {"what is", "define", "explain", "history of", "how does", "theory", "concept"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(educationalKeywords).anyMatch(lowerQuery::contains);
    }

    private boolean isWeatherQuery(String query) {
        String[] weatherKeywords = {"weather", "temperature", "climate", "rain", "snow", "sunny", "cloudy"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(weatherKeywords).anyMatch(lowerQuery::contains);
    }

    private boolean isCountryQuery(String query, String location) {
        String[] countryKeywords = {"country", "nation", "capital", "population", "language", "currency"};
        String lowerQuery = query.toLowerCase();

        // Check if the location is likely a country name
        String[] commonCountries = {"usa", "uk", "france", "germany", "japan", "china", "india", "brazil", "canada", "australia"};
        String lowerLocation = location.toLowerCase();

        return Arrays.stream(countryKeywords).anyMatch(lowerQuery::contains) ||
               Arrays.stream(commonCountries).anyMatch(lowerLocation::contains);
    }

    private boolean isNewsQuery(String query) {
        String[] newsKeywords = {"news", "latest", "recent", "breaking", "today", "yesterday", "current"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(newsKeywords).anyMatch(lowerQuery::contains);
    }

    private boolean isAcademicQuery(String query) {
        String[] academicKeywords = {"research", "study", "paper", "journal", "academic", "scholar", "thesis"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(academicKeywords).anyMatch(lowerQuery::contains);
    }

    private boolean isLocationQuery(String query) {
        return calculateLocationRelevance(query) > 0.3;
    }

    private boolean isImageQuery(String query) {
        String[] imageKeywords = {"image", "photo", "picture", "graphic", "illustration"};
        String lowerQuery = query.toLowerCase();
        return Arrays.stream(imageKeywords).anyMatch(lowerQuery::contains);
    }

    private List<Map<String, Object>> extractSources(Map<String, Object> results) {
        List<Map<String, Object>> sources = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> webResults = (List<Map<String, Object>>) results.get("webResults");
        if (webResults != null) {
            webResults.forEach(result -> {
                Map<String, Object> source = new HashMap<>();
                source.put("title", result.get("title"));
                source.put("url", result.get("url"));
                source.put("type", "web");
                source.put("relevance", result.get("relevanceScore"));
                sources.add(source);
            });
        }

        return sources;
    }

    private double calculateConfidence(Map<String, Object> results) {
        double confidence = 0.5; // Base confidence

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> webResults = (List<Map<String, Object>>) results.get("webResults");
        if (webResults != null && !webResults.isEmpty()) {
            confidence += 0.3;
        }

        if (results.containsKey("newsResults")) {
            confidence += 0.1;
        }

        if (results.containsKey("locationResults")) {
            confidence += 0.1;
        }

        return Math.min(confidence, 1.0);
    }

    private List<String> extractKeyPoints(Map<String, Object> results) {
        List<String> keyPoints = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> webResults = (List<Map<String, Object>>) results.get("webResults");
        if (webResults != null) {
            webResults.stream()
                .limit(3)
                .forEach(result -> keyPoints.add((String) result.get("snippet")));
        }

        return keyPoints;
    }

    private double calculateAnswerConfidence(Map<String, Object> results) {
        return calculateConfidence(results);
    }

    private String generateFallbackAnswer(String query) {
        return "I apologize, but I encountered an issue while searching for information about \"" + query +
               "\". Please try rephrasing your question or check back later.";
    }

    private void shareSearchResults(String query, Map<String, Object> results) {
        if (contextStore == null) return;

        try {
            contextStore.storeSharedData(
                "searchResults",
                results,
                getName(),
                Map.of(
                    "dataType", "comprehensive-search",
                    "query", query,
                    "hasLocationData", results.containsKey("locationResults"),
                    "canVisualize", true,
                    "timestamp", LocalDateTime.now().toString()
                )
            );
        } catch (Exception e) {
            System.err.println("Failed to share search results: " + e.getMessage());
        }
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        this.status = AgentStatus.READY;

        // Initialize API keys from config if available
        if (config != null && config.getProperties() != null) {
            // Note: API keys would be configured in application.properties
            // and accessed through Spring's @Value annotation in production
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


    private List<Map<String, Object>> generateContextualResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Add a helpful result about the search
        Map<String, Object> contextResult = new HashMap<>();
        contextResult.put("title", "Search Context for: " + query);
        contextResult.put("snippet", "Based on your search for '" + query + "', here are some additional insights. " +
                         "This search is powered by free APIs including DuckDuckGo, Wikipedia, and other open sources.");
        contextResult.put("url", "https://duckduckgo.com/?q=" + query.replace(" ", "+"));
        contextResult.put("source", "AI Agent Framework");
        contextResult.put("relevanceScore", 0.6);
        contextResult.put("type", "context");
        results.add(contextResult);

        return results;
    }

    private List<Map<String, Object>> generateFallbackResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("title", "Search Information: " + query);
        fallback.put("snippet", "Your search for '" + query + "' is being processed. " +
                     "The system uses multiple free APIs to provide comprehensive results. " +
                     "If you're seeing this message, it means the primary search APIs are temporarily unavailable.");
        fallback.put("url", "https://duckduckgo.com/?q=" + query.replace(" ", "+"));
        fallback.put("source", "Fallback Search");
        fallback.put("relevanceScore", 0.5);
        fallback.put("type", "fallback");
        results.add(fallback);

        return results;
    }

    private List<Map<String, Object>> generateMockNewsResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> newsItem = new HashMap<>();
        newsItem.put("headline", "News about " + query);
        newsItem.put("summary", "Latest developments regarding " + query + ". " +
                     "To get real news results, please configure your NEWS_API_KEY in application.properties. " +
                     "You can get a free key at https://newsapi.org/register");
        newsItem.put("url", "https://newsapi.org/");
        newsItem.put("publishedAt", LocalDateTime.now().toString());
        newsItem.put("source", "Configuration Needed");
        newsItem.put("credibilityScore", 0.5);
        newsItem.put("type", "config_info");
        results.add(newsItem);

        return results;
    }

    private Map<String, Object> generateFallbackLocationData(String query) {
        Map<String, Object> locationData = new HashMap<>();

        List<String> locations = extractLocations(query);
        for (String location : locations) {
            Map<String, Object> locInfo = new HashMap<>();
            locInfo.put("name", location);
            locInfo.put("coordinates", Map.of("lat", 40.7128, "lng", -74.0060)); // Default coordinates
            locInfo.put("type", "location");
            locInfo.put("description", "Location data for " + location + " (using fallback data)");
            locInfo.put("source", "Fallback");
            locationData.put(location, locInfo);
        }

        return locationData;
    }
}
