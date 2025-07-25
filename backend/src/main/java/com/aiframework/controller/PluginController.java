package com.aiframework.controller;

import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for Plugin Management
 */
@RestController
@RequestMapping("/api/plugins")
@CrossOrigin(origins = "*")
public class PluginController {

    @Autowired
    private ContextStore contextStore;

    // Mock data storage for demonstration
    private final Map<String, PluginInfo> installedPlugins = new HashMap<>();
    private final Map<String, PluginInfo> availablePlugins = new HashMap<>();

    public PluginController() {
        initializeMockData();
    }

    private void initializeMockData() {
        // Initialize installed plugins
        installedPlugins.put("stock-analyzer-pro", new PluginInfo(
            "stock-analyzer-pro", "Stock Analyzer Pro", "2.1.4", "Financial",
            "Advanced stock analysis with ML predictions", "FinTech Solutions",
            "active", "12.5 MB", "2025-07-20", Arrays.asList("numpy", "pandas", "tensorflow"),
            Arrays.asList("market-data", "file-system"), 4.8, 15420, true, "2.2.0"
        ));

        installedPlugins.put("risk-assessor", new PluginInfo(
            "risk-assessor", "AI Risk Assessor", "1.3.2", "Analytics",
            "Intelligent risk assessment and portfolio optimization", "Risk Analytics Inc",
            "active", "8.2 MB", "2025-07-18", Arrays.asList("scipy", "matplotlib"),
            Arrays.asList("portfolio-data"), 4.6, 8934, false, null
        ));

        installedPlugins.put("chart-visualizer", new PluginInfo(
            "chart-visualizer", "Advanced Chart Visualizer", "3.0.1", "Visualization",
            "Interactive charts and technical indicators", "DataViz Pro",
            "inactive", "15.8 MB", "2025-07-15", Arrays.asList("d3", "plotly", "chart.js"),
            Arrays.asList("display"), 4.9, 23145, false, null
        ));

        // Initialize available plugins
        availablePlugins.put("sentiment-analyzer", new PluginInfo(
            "sentiment-analyzer", "Market Sentiment Analyzer", "1.5.0", "AI/ML",
            "Real-time market sentiment analysis using NLP", "SentimentAI",
            null, "25.3 MB", null, Arrays.asList("nltk", "transformers"),
            Arrays.asList("network-access"), 4.7, 12847, false, null,
            "Free", "✓ Compatible", Arrays.asList("Real-time analysis", "Multiple sources", "API integration")
        ));

        availablePlugins.put("crypto-tracker", new PluginInfo(
            "crypto-tracker", "Cryptocurrency Tracker", "2.0.8", "Financial",
            "Comprehensive cryptocurrency portfolio tracking", "CryptoTools",
            null, "18.7 MB", null, Arrays.asList("web3", "requests"),
            Arrays.asList("network-access", "file-system"), 4.5, 9876, false, null,
            "$29.99", "✓ Compatible", Arrays.asList("Multi-exchange support", "DeFi tracking", "Tax reporting")
        ));
    }

    // Get all installed plugins
    @GetMapping("/installed")
    public ResponseEntity<Collection<PluginInfo>> getInstalledPlugins() {
        return ResponseEntity.ok(installedPlugins.values());
    }

    // Get all available plugins
    @GetMapping("/available")
    public ResponseEntity<Collection<PluginInfo>> getAvailablePlugins() {
        return ResponseEntity.ok(availablePlugins.values());
    }

    // Get specific plugin info
    @GetMapping("/{pluginId}")
    public ResponseEntity<PluginInfo> getPlugin(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null) {
            plugin = availablePlugins.get(pluginId);
        }
        
        return plugin != null ? ResponseEntity.ok(plugin) : ResponseEntity.notFound().build();
    }

    // Install a plugin
    @PostMapping("/{pluginId}/install")
    public ResponseEntity<Map<String, Object>> installPlugin(@PathVariable String pluginId) {
        PluginInfo plugin = availablePlugins.get(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Simulate installation process
            Thread.sleep(2000); // Simulate installation time

            // Move from available to installed
            plugin.setStatus("active");
            plugin.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            installedPlugins.put(pluginId, plugin);
            availablePlugins.remove(pluginId);

            // Update context
            contextStore.storeContext("plugin-manager", "lastInstalled", pluginId);
            contextStore.storeContext("plugin-manager", "installedCount", installedPlugins.size());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Plugin installed successfully");
            response.put("plugin", plugin);

            return ResponseEntity.ok(response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Activate/Deactivate plugin
    @PostMapping("/{pluginId}/toggle")
    public ResponseEntity<Map<String, Object>> togglePlugin(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        String newStatus = "active".equals(plugin.getStatus()) ? "inactive" : "active";
        plugin.setStatus(newStatus);

        // Update context
        contextStore.storeContext("plugin-manager", "lastToggled", pluginId);
        contextStore.storeContext("plugin-manager", "toggledTo", newStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Plugin " + newStatus);
        response.put("newStatus", newStatus);

        return ResponseEntity.ok(response);
    }

    // Uninstall plugin
    @DeleteMapping("/{pluginId}")
    public ResponseEntity<Map<String, Object>> uninstallPlugin(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.remove(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        // Move back to available if it was from marketplace
        plugin.setStatus(null);
        plugin.setLastUpdated(null);
        availablePlugins.put(pluginId, plugin);

        // Update context
        contextStore.storeContext("plugin-manager", "lastUninstalled", pluginId);
        contextStore.storeContext("plugin-manager", "installedCount", installedPlugins.size());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Plugin uninstalled successfully");

        return ResponseEntity.ok(response);
    }

    // Update plugin
    @PostMapping("/{pluginId}/update")
    public ResponseEntity<Map<String, Object>> updatePlugin(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null || !plugin.isHasUpdate()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Simulate update process
            Thread.sleep(3000);

            // Update version
            plugin.setVersion(plugin.getNewVersion());
            plugin.setHasUpdate(false);
            plugin.setNewVersion(null);
            plugin.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // Update context
            contextStore.storeContext("plugin-manager", "lastUpdated", pluginId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Plugin updated successfully");
            response.put("newVersion", plugin.getVersion());

            return ResponseEntity.ok(response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Configure plugin
    @PostMapping("/{pluginId}/configure")
    public ResponseEntity<Map<String, Object>> configurePlugin(@PathVariable String pluginId,
                                                               @RequestBody Map<String, Object> config) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        plugin.setConfiguration(config);

        // Update context
        contextStore.storeContext("plugin-manager", "lastConfigured", pluginId);
        contextStore.storeContext("plugin-manager", "configurationKeys", config.keySet());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Plugin configuration saved");

        return ResponseEntity.ok(response);
    }

    // Get plugin configuration
    @GetMapping("/{pluginId}/configuration")
    public ResponseEntity<Map<String, Object>> getPluginConfiguration(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(plugin.getConfiguration() != null ? plugin.getConfiguration() : new HashMap<>());
    }

    // Upload custom plugin
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPlugin(@RequestParam("file") MultipartFile file,
                                                            @RequestParam(value = "url", required = false) String url) {
        try {
            String pluginId = "custom-" + System.currentTimeMillis();
            String filename = file != null ? file.getOriginalFilename() : url;

            // Simulate plugin validation and installation
            Thread.sleep(2000);

            PluginInfo customPlugin = new PluginInfo(
                pluginId, "Custom Plugin", "1.0.0", "Custom",
                "User uploaded custom plugin", "User",
                "active", file != null ? formatFileSize(file.getSize()) : "Unknown",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                Arrays.asList("custom"), Arrays.asList("custom-permissions"),
                0.0, 0, false, null
            );

            installedPlugins.put(pluginId, customPlugin);

            // Update context
            contextStore.storeContext("plugin-manager", "lastUploaded", pluginId);
            contextStore.storeContext("plugin-manager", "uploadSource", file != null ? "file" : "url");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Custom plugin uploaded and installed successfully");
            response.put("pluginId", pluginId);

            return ResponseEntity.ok(response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get plugin dependencies
    @GetMapping("/{pluginId}/dependencies")
    public ResponseEntity<Map<String, Object>> getPluginDependencies(@PathVariable String pluginId) {
        PluginInfo plugin = installedPlugins.get(pluginId);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("pluginId", pluginId);
        response.put("dependencies", plugin.getDependencies());
        response.put("status", "satisfied"); // Mock status
        response.put("checkTimestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // Check for plugin updates
    @PostMapping("/check-updates")
    public ResponseEntity<Map<String, Object>> checkForUpdates() {
        int updatesAvailable = 0;
        for (PluginInfo plugin : installedPlugins.values()) {
            if (plugin.isHasUpdate()) {
                updatesAvailable++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("updatesAvailable", updatesAvailable);
        response.put("lastChecked", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // Get plugin marketplace info
    @GetMapping("/marketplace/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedPlugins() {
        Map<String, Object> marketplace = new HashMap<>();
        
        marketplace.put("trending", Arrays.asList("sentiment-analyzer", "crypto-tracker", "news-aggregator"));
        marketplace.put("categories", Map.of(
            "Financial", 25,
            "AI/ML", 18,
            "Analytics", 12,
            "Visualization", 15
        ));
        marketplace.put("totalPlugins", 70);
        marketplace.put("lastUpdated", LocalDateTime.now());

        return ResponseEntity.ok(marketplace);
    }

    // Plugin search
    @GetMapping("/search")
    public ResponseEntity<List<PluginInfo>> searchPlugins(@RequestParam String query,
                                                          @RequestParam(required = false) String category) {
        List<PluginInfo> results = new ArrayList<>();
        
        // Search in both installed and available plugins
        Stream.concat(installedPlugins.values().stream(), availablePlugins.values().stream())
            .filter(plugin -> plugin.getName().toLowerCase().contains(query.toLowerCase()) ||
                            plugin.getDescription().toLowerCase().contains(query.toLowerCase()))
            .filter(plugin -> category == null || plugin.getCategory().equals(category))
            .forEach(results::add);

        return ResponseEntity.ok(results);
    }

    // Helper method to format file size
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // Plugin Info class
    public static class PluginInfo {
        private String id;
        private String name;
        private String version;
        private String category;
        private String description;
        private String author;
        private String status;
        private String size;
        private String lastUpdated;
        private List<String> dependencies;
        private List<String> permissions;
        private double rating;
        private int downloads;
        private boolean hasUpdate;
        private String newVersion;
        private String price;
        private String compatibility;
        private List<String> features;
        private Map<String, Object> configuration;

        // Constructor for installed plugins
        public PluginInfo(String id, String name, String version, String category, String description,
                         String author, String status, String size, String lastUpdated,
                         List<String> dependencies, List<String> permissions, double rating,
                         int downloads, boolean hasUpdate, String newVersion) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.category = category;
            this.description = description;
            this.author = author;
            this.status = status;
            this.size = size;
            this.lastUpdated = lastUpdated;
            this.dependencies = dependencies;
            this.permissions = permissions;
            this.rating = rating;
            this.downloads = downloads;
            this.hasUpdate = hasUpdate;
            this.newVersion = newVersion;
            this.configuration = new HashMap<>();
        }

        // Constructor for available plugins
        public PluginInfo(String id, String name, String version, String category, String description,
                         String author, String status, String size, String lastUpdated,
                         List<String> dependencies, List<String> permissions, double rating,
                         int downloads, boolean hasUpdate, String newVersion, String price,
                         String compatibility, List<String> features) {
            this(id, name, version, category, description, author, status, size, lastUpdated,
                 dependencies, permissions, rating, downloads, hasUpdate, newVersion);
            this.price = price;
            this.compatibility = compatibility;
            this.features = features;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public int getDownloads() { return downloads; }
        public void setDownloads(int downloads) { this.downloads = downloads; }

        public boolean isHasUpdate() { return hasUpdate; }
        public void setHasUpdate(boolean hasUpdate) { this.hasUpdate = hasUpdate; }

        public String getNewVersion() { return newVersion; }
        public void setNewVersion(String newVersion) { this.newVersion = newVersion; }

        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }

        public String getCompatibility() { return compatibility; }
        public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    }
}
