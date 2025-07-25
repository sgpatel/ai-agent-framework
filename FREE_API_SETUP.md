# Free API Integration Setup Guide

This guide will help you set up and configure the free APIs integrated into your AI Agent Framework for enhanced search capabilities.

## 🆓 Free APIs Integrated

### 1. **DuckDuckGo Instant Answer API** (No API key required)
- **Purpose**: Web search and instant answers
- **Free Tier**: Unlimited (rate limited)
- **Setup**: No configuration needed - works out of the box!

### 2. **NewsAPI.org** (Optional - Enhanced news search)
- **Purpose**: Real-time news search
- **Free Tier**: 100 requests/day
- **Setup**: 
  1. Go to [https://newsapi.org/register](https://newsapi.org/register)
  2. Create a free account
  3. Get your API key
  4. Add to environment variables: `NEWS_API_KEY=your_api_key_here`

### 3. **OpenWeatherMap** (Optional - Weather information)
- **Purpose**: Weather data for location-based queries
- **Free Tier**: 1,000 calls/day
- **Setup**:
  1. Go to [https://openweathermap.org/api](https://openweathermap.org/api)
  2. Sign up for free account
  3. Get your API key
  4. Add to environment variables: `WEATHER_API_KEY=your_api_key_here`

### 4. **REST Countries API** (No API key required)
- **Purpose**: Country information and demographics
- **Free Tier**: Unlimited
- **Setup**: No configuration needed - works out of the box!

### 5. **OpenStreetMap Nominatim** (No API key required)
- **Purpose**: Geocoding and location data
- **Free Tier**: Rate limited (1 request/second)
- **Setup**: No configuration needed - works out of the box!

### 6. **Wikipedia API** (No API key required)
- **Purpose**: Educational and reference content
- **Free Tier**: Unlimited (rate limited)
- **Setup**: No configuration needed - works out of the box!

## 🚀 Quick Start

### Without API Keys (Basic functionality)
The system works immediately with these free APIs:
- ✅ DuckDuckGo search
- ✅ Wikipedia content
- ✅ Country information
- ✅ Location geocoding
- ✅ Basic map integration

### With API Keys (Enhanced functionality)
Add these environment variables for enhanced features:

```bash
export NEWS_API_KEY=your_newsapi_key_here
export WEATHER_API_KEY=your_openweather_key_here
```

Or add them to your application.properties:
```properties
news.api.key=your_newsapi_key_here
weather.api.key=your_openweather_key_here
```

## 🧪 Testing the Integration

### Example Queries to Test:

1. **Basic Search (DuckDuckGo + Wikipedia)**:
   - "What is artificial intelligence?"
   - "Explain quantum computing"

2. **Location Search (with Map)**:
   - "Best restaurants in Paris"
   - "Weather in London"
   - "Population of Tokyo"

3. **News Search** (requires NewsAPI key):
   - "Latest AI news"
   - "Recent technology developments"

4. **Country Information**:
   - "Tell me about France"
   - "Japan population and capital"

5. **Weather Queries** (enhanced with OpenWeatherMap):
   - "Current weather in New York"
   - "Temperature in Berlin today"

## 🔧 Configuration Options

### Rate Limiting
The system includes built-in rate limiting to respect API limits:
```properties
api.rate-limit.enabled=true
api.rate-limit.requests-per-minute=50
```

### Map Provider
Choose your map tile provider:
```properties
map.provider=openstreetmap
map.tile.url=https://tile.openstreetmap.org/{z}/{x}/{y}.png
```

## 🎯 Features Enabled

### ✅ Works Out of the Box:
- **Intelligent search** across multiple sources
- **Location detection** and geocoding
- **Map visualization** for location queries
- **Wikipedia integration** for educational content
- **Country information** lookup
- **Comprehensive answer synthesis**
- **Source citations** and confidence scoring

### ✅ Enhanced with API Keys:
- **Real-time news** from NewsAPI
- **Detailed weather** from OpenWeatherMap
- **Higher rate limits** for production use

## 🚦 API Status Indicators

The system provides clear feedback about API status:

- **🟢 Active**: API is configured and working
- **🟡 Fallback**: Using mock data (API key not configured)
- **🔴 Error**: API temporarily unavailable

## 📊 Usage Examples

### Frontend Integration
```javascript
// Search with automatic map integration
const searchResponse = await fetch('/api/search/intelligent', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    query: "best restaurants in Tokyo",
    searchType: "comprehensive"
  })
});

const results = await searchResponse.json();
// Results include both search data and map visualization if location detected
```

### Backend Usage
```java
// The system automatically:
// 1. Detects location intent in queries
// 2. Searches multiple free APIs
// 3. Synthesizes comprehensive answers
// 4. Triggers map visualization when appropriate
// 5. Provides source citations
```

## 🔒 Privacy & Rate Limits

### Respect for Free APIs:
- **Built-in rate limiting** to stay within free tiers
- **Fallback mechanisms** when APIs are unavailable
- **No API keys required** for basic functionality
- **Transparent error handling**

### Rate Limits:
- DuckDuckGo: Rate limited (handled automatically)
- NewsAPI: 100/day (free tier)
- OpenWeatherMap: 1,000/day (free tier)
- Wikipedia: Rate limited (handled automatically)
- REST Countries: Unlimited
- Nominatim: 1 request/second (handled automatically)

## 🎉 Ready to Use!

Your search system is now integrated with multiple free APIs and provides:

1. **Perplexity-like search experience**
2. **Automatic map integration**
3. **Multi-source data aggregation**
4. **Intelligent answer synthesis**
5. **Real-time results** (with API keys)
6. **Fallback capabilities** (without API keys)

Start searching and enjoy the enhanced capabilities! 🚀
