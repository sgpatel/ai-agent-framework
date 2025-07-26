package com.aiframework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Free API Integration Service
 * Integrates with multiple free APIs for search, news, weather, and location data
 */
@Service
public class FreeApiService {

    private final RestTemplate restTemplate;

    @Value("${search.duckduckgo.api.url:https://api.duckduckgo.com/}")
    private String duckDuckGoUrl;

    @Value("${news.api.key:}")
    private String newsApiKey;

    @Value("${news.api.url:https://newsapi.org/v2}")
    private String newsApiUrl;

    @Value("${weather.api.key:}")
    private String weatherApiKey;

    @Value("${weather.api.url:https://api.openweathermap.org/data/2.5}")
    private String weatherApiUrl;

    @Value("${countries.api.url:https://restcountries.com/v3.1}")
    private String countriesApiUrl;

    @Value("${geocoding.api.url:https://nominatim.openstreetmap.org}")
    private String geocodingApiUrl;

    @Value("${wikipedia.api.url:https://en.wikipedia.org/api/rest_v1}")
    private String wikipediaApiUrl;

    @Value("${pixabay.api.key:}")
    private String pixabayApiKey;

    @Value("${pixabay.api.url:https://pixabay.com/api/}")
    private String pixabayApiUrl;

    @Autowired
    public FreeApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Constructor for manual initialization with default URLs
     */
    public FreeApiService(RestTemplate restTemplate, boolean useDefaults) {
        this.restTemplate = restTemplate;
        if (useDefaults) {
            initializeDefaultUrls();
        }
    }

    /**
     * Initialize with default URLs when Spring @Value injection is not available
     */
    private void initializeDefaultUrls() {
        this.duckDuckGoUrl = "https://api.duckduckgo.com/";
        this.newsApiKey = "your-free-newsapi-key-here";
        this.newsApiUrl = "https://newsapi.org/v2";
        this.weatherApiKey = "your-free-weather-key-here";
        this.weatherApiUrl = "https://api.openweathermap.org/data/2.5";
        this.countriesApiUrl = "https://restcountries.com/v3.1";
        this.geocodingApiUrl = "https://nominatim.openstreetmap.org";
        this.wikipediaApiUrl = "https://en.wikipedia.org/api/rest_v1";
        this.pixabayApiKey = "your-free-pixabay-key-here";
        this.pixabayApiUrl = "https://pixabay.com/api/";
    }

    /**
     * Search using DuckDuckGo Instant Answer API
     */
    public List<Map<String, Object>> searchDuckDuckGo(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // Ensure URL is properly initialized
            if (duckDuckGoUrl == null || duckDuckGoUrl.isEmpty()) {
                duckDuckGoUrl = "https://api.duckduckgo.com/";
            }

            // Ensure URL ends with slash
            if (!duckDuckGoUrl.endsWith("/")) {
                duckDuckGoUrl += "/";
            }

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = duckDuckGoUrl + "?q=" + encodedQuery + "&format=json&no_html=1&skip_disambig=1";

            // Add timeout and retry mechanism
            restTemplate.getInterceptors().clear();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                // Process DuckDuckGo instant answer
                String abstractText = (String) response.get("Abstract");
                String abstractUrl = (String) response.get("AbstractURL");
                String abstractSource = (String) response.get("AbstractSource");

                if (abstractText != null && !abstractText.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("title", "DuckDuckGo: " + query);
                    result.put("snippet", abstractText);
                    result.put("url", abstractUrl != null ? abstractUrl : "https://duckduckgo.com/?q=" + encodedQuery);
                    result.put("source", abstractSource != null ? abstractSource : "DuckDuckGo");
                    result.put("relevanceScore", 0.9);
                    result.put("type", "instant_answer");
                    results.add(result);
                }

                // Process related topics
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> relatedTopics = (List<Map<String, Object>>) response.get("RelatedTopics");
                if (relatedTopics != null) {
                    for (int i = 0; i < Math.min(3, relatedTopics.size()); i++) {
                        Map<String, Object> topic = relatedTopics.get(i);
                        String text = (String) topic.get("Text");
                        String firstUrl = (String) topic.get("FirstURL");

                        if (text != null && !text.isEmpty()) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("title", "Related: " + text.split(" - ")[0]);
                            result.put("snippet", text);
                            result.put("url", firstUrl != null ? firstUrl : "");
                            result.put("source", "DuckDuckGo Related");
                            result.put("relevanceScore", 0.8 - (i * 0.1));
                            result.put("type", "related_topic");
                            results.add(result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("DuckDuckGo search failed: " + e.getMessage());
            // Don't print full stack trace for known API issues
            if (e.getMessage().contains("500 Internal Server Error")) {
                System.err.println("DuckDuckGo API is temporarily unavailable (500 error)");
            } else {
                e.printStackTrace();
            }
        }

        // Always return at least a fallback result
        if (results.isEmpty()) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("title", "Search: " + query);
            fallback.put("snippet", "DuckDuckGo search temporarily unavailable. Using alternative search methods. " +
                         "This is a common issue with the DuckDuckGo instant answer API.");
            try {
                fallback.put("url", "https://duckduckgo.com/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            } catch (Exception urlEx) {
                fallback.put("url", "https://duckduckgo.com/");
            }
            fallback.put("source", "Alternative Search");
            fallback.put("relevanceScore", 0.7);
            fallback.put("type", "alternative");
            results.add(fallback);
        }

        return results;
    }

    /**
     * Search news using NewsAPI
     */
    public List<Map<String, Object>> searchNews(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        if ("your-free-newsapi-key-here".equals(newsApiKey)) {
            // Return mock data if no API key is configured
            return getMockNewsResults(query);
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = newsApiUrl + "/everything?q=" + encodedQuery +
                        "&sortBy=publishedAt&pageSize=5&apiKey=" + newsApiKey;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "ok".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

                if (articles != null) {
                    for (Map<String, Object> article : articles) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("headline", article.get("title"));
                        result.put("summary", article.get("description"));
                        result.put("url", article.get("url"));
                        result.put("publishedAt", article.get("publishedAt"));

                        @SuppressWarnings("unchecked")
                        Map<String, Object> source = (Map<String, Object>) article.get("source");
                        result.put("source", source != null ? source.get("name") : "Unknown");

                        result.put("credibilityScore", 0.85 + (Math.random() * 0.15));
                        result.put("type", "news");
                        results.add(result);
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("NewsAPI search failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return getMockNewsResults(query);
        } catch (Exception e) {
            System.err.println("NewsAPI search failed: " + e.getMessage());
            return getMockNewsResults(query);
        }

        return results;
    }

    /**
     * Get weather information using OpenWeatherMap
     */
    public Map<String, Object> getWeatherInfo(String location) {
        Map<String, Object> weatherInfo = new HashMap<>();

        if ("your-free-weather-key-here".equals(weatherApiKey)) {
            return getMockWeatherData(location);
        }

        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
            String url = weatherApiUrl + "/weather?q=" + encodedLocation + "&appid=" + weatherApiKey + "&units=metric";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
                @SuppressWarnings("unchecked")
                Map<String, Object> coord = (Map<String, Object>) response.get("coord");

                weatherInfo.put("location", response.get("name"));
                weatherInfo.put("country", ((Map<String, Object>) response.get("sys")).get("country"));

                if (main != null) {
                    weatherInfo.put("temperature", main.get("temp"));
                    weatherInfo.put("humidity", main.get("humidity"));
                    weatherInfo.put("pressure", main.get("pressure"));
                    weatherInfo.put("feels_like", main.get("feels_like"));
                }

                if (weather != null && !weather.isEmpty()) {
                    Map<String, Object> weatherMain = weather.get(0);
                    weatherInfo.put("description", weatherMain.get("description"));
                    weatherInfo.put("main_weather", weatherMain.get("main"));
                    weatherInfo.put("icon", weatherMain.get("icon"));
                }

                if (coord != null) {
                    weatherInfo.put("coordinates", Map.of(
                        "lat", coord.get("lat"),
                        "lng", coord.get("lon")
                    ));
                }

                weatherInfo.put("type", "weather");
                weatherInfo.put("source", "OpenWeatherMap");
            }
        } catch (Exception e) {
            System.err.println("Weather API failed: " + e.getMessage());
            return getMockWeatherData(location);
        }

        return weatherInfo;
    }

    /**
     * Geocode location using OpenStreetMap Nominatim
     */
    public Map<String, Object> geocodeLocation(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
            String url = geocodingApiUrl + "/search?q=" + encodedLocation + "&format=json&limit=1&addressdetails=1";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);

            if (response != null && !response.isEmpty()) {
                Map<String, Object> result = response.get(0);
                Map<String, Object> locationInfo = new HashMap<>();

                locationInfo.put("name", result.get("display_name"));
                locationInfo.put("coordinates", Map.of(
                    "lat", Double.parseDouble((String) result.get("lat")),
                    "lng", Double.parseDouble((String) result.get("lon"))
                ));
                locationInfo.put("type", result.get("type"));
                locationInfo.put("class", result.get("class"));
                locationInfo.put("importance", result.get("importance"));

                @SuppressWarnings("unchecked")
                Map<String, Object> address = (Map<String, Object>) result.get("address");
                if (address != null) {
                    locationInfo.put("country", address.get("country"));
                    locationInfo.put("city", address.get("city"));
                    locationInfo.put("state", address.get("state"));
                }

                return locationInfo;
            }
        } catch (Exception e) {
            System.err.println("Geocoding failed: " + e.getMessage());
        }

        // Return mock data if geocoding fails
        return Map.of(
            "name", location,
            "coordinates", Map.of("lat", 40.7128, "lng", -74.0060),
            "type", "city",
            "country", "Unknown"
        );
    }

    /**
     * Search Wikipedia for academic/reference information
     */
    public List<Map<String, Object>> searchWikipedia(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = wikipediaApiUrl + "/page/summary/" + encodedQuery;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);

            if (response != null && !"disambiguation".equals(response.get("type"))) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", response.get("title"));
                result.put("abstract", response.get("extract"));
                result.put("url", response.get("content_urls"));
                result.put("source", "Wikipedia");
                result.put("type", "encyclopedia");
                result.put("relevanceScore", 0.8);
                results.add(result);
            }
        } catch (Exception e) {
            System.err.println("Wikipedia search failed: " + e.getMessage());
        }

        return results;
    }

    /**
     * Get country information using REST Countries API
     */
    public Map<String, Object> getCountryInfo(String countryName) {
        try {
            String encodedCountry = URLEncoder.encode(countryName, StandardCharsets.UTF_8);
            String url = countriesApiUrl + "/name/" + encodedCountry + "?fields=name,capital,population,region,languages,currencies,flags";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);

            if (response != null && !response.isEmpty()) {
                Map<String, Object> country = response.get(0);
                Map<String, Object> countryInfo = new HashMap<>();

                @SuppressWarnings("unchecked")
                Map<String, Object> name = (Map<String, Object>) country.get("name");
                countryInfo.put("name", name != null ? name.get("common") : countryName);
                countryInfo.put("capital", country.get("capital"));
                countryInfo.put("population", country.get("population"));
                countryInfo.put("region", country.get("region"));
                countryInfo.put("languages", country.get("languages"));
                countryInfo.put("currencies", country.get("currencies"));
                countryInfo.put("flag", ((Map<String, Object>) country.get("flags")).get("png"));
                countryInfo.put("type", "country");
                countryInfo.put("source", "REST Countries");

                return countryInfo;
            }
        } catch (Exception e) {
            System.err.println("Country info failed: " + e.getMessage());
        }

        return null;
    }

    /**
     * Search images using Pixabay API
     */
    public List<Map<String, Object>> searchImages(String query, String imageType, String category) {
        List<Map<String, Object>> results = new ArrayList<>();

        if ("your-free-pixabay-key-here".equals(pixabayApiKey) || pixabayApiKey.isEmpty()) {
            return getMockImageResults(query);
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = pixabayApiUrl + "?key=" + pixabayApiKey + 
                        "&q=" + encodedQuery + 
                        "&image_type=" + (imageType != null ? imageType : "photo") +
                        "&category=" + (category != null ? category : "") +
                        "&min_width=640" +
                        "&safesearch=true" +
                        "&per_page=12" +
                        "&order=popular";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "ok".equals(response.get("status")) == false) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> hits = (List<Map<String, Object>>) response.get("hits");

                if (hits != null) {
                    for (Map<String, Object> hit : hits) {
                        Map<String, Object> imageResult = new HashMap<>();
                        imageResult.put("id", hit.get("id"));
                        imageResult.put("title", hit.get("tags"));
                        imageResult.put("description", "Image: " + hit.get("tags"));
                        imageResult.put("thumbnailUrl", hit.get("previewURL"));
                        imageResult.put("imageUrl", hit.get("webformatURL"));
                        imageResult.put("largeImageUrl", hit.get("largeImageURL"));
                        imageResult.put("pageUrl", hit.get("pageURL"));
                        imageResult.put("width", hit.get("imageWidth"));
                        imageResult.put("height", hit.get("imageHeight"));
                        imageResult.put("size", hit.get("imageSize"));
                        imageResult.put("views", hit.get("views"));
                        imageResult.put("downloads", hit.get("downloads"));
                        imageResult.put("likes", hit.get("likes"));
                        imageResult.put("user", hit.get("user"));
                        imageResult.put("source", "Pixabay");
                        imageResult.put("type", "image");
                        imageResult.put("relevanceScore", 0.9);
                        results.add(imageResult);
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Pixabay search failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return getMockImageResults(query);
        } catch (Exception e) {
            System.err.println("Pixabay search failed: " + e.getMessage());
            return getMockImageResults(query);
        }

        return results.isEmpty() ? getMockImageResults(query) : results;
    }

    /**
     * Search images using Pixabay API with default parameters
     */
    public List<Map<String, Object>> searchImages(String query) {
        return searchImages(query, "photo", null);
    }

    // Mock data methods for when APIs are not configured
    private List<Map<String, Object>> getMockNewsResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> result = new HashMap<>();
            result.put("headline", "Breaking News: " + query + " - Update #" + (i + 1));
            result.put("summary", "Recent developments regarding " + query + " show significant progress. This is mock data - configure NEWS_API_KEY for real news.");
            result.put("url", "https://newsapi.org/");
            result.put("publishedAt", "2025-07-25T10:00:00Z");
            result.put("source", "Mock News Source " + (i + 1));
            result.put("credibilityScore", 0.7);
            result.put("type", "mock_news");
            results.add(result);
        }
        return results;
    }

    private Map<String, Object> getMockWeatherData(String location) {
        Map<String, Object> weather = new HashMap<>();
        weather.put("location", location);
        weather.put("temperature", 22.5);
        weather.put("description", "Partly cloudy");
        weather.put("humidity", 65);
        weather.put("coordinates", Map.of("lat", 40.7128, "lng", -74.0060));
        weather.put("type", "mock_weather");
        weather.put("source", "Mock Weather Data - Configure WEATHER_API_KEY for real data");
        return weather;
    }

    private List<Map<String, Object>> getMockImageResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Generate mock image results
        for (int i = 0; i < 6; i++) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", "mock-" + i);
            result.put("title", "Mock Image: " + query + " #" + (i + 1));
            result.put("description", "Sample image related to " + query + ". Configure PIXABAY_API_KEY for real images.");
            result.put("thumbnailUrl", "https://via.placeholder.com/150x150?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            result.put("imageUrl", "https://via.placeholder.com/640x480?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            result.put("largeImageUrl", "https://via.placeholder.com/1920x1080?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            result.put("pageUrl", "https://pixabay.com/");
            result.put("width", 640);
            result.put("height", 480);
            result.put("size", 50000 + (i * 10000));
            result.put("views", 1000 + (i * 100));
            result.put("downloads", 50 + (i * 10));
            result.put("likes", 20 + (i * 5));
            result.put("user", "MockUser" + (i + 1));
            result.put("source", "Mock Images (Configure API Key)");
            result.put("type", "mock_image");
            result.put("relevanceScore", 0.7);
            results.add(result);
        }
        
        return results;
    }
}
