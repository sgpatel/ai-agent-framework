# 🧠 AI Agent Framework

A powerful, configurable AI Agent Framework built with Spring Boot and React for intelligent task automation, real-time search, financial analysis, and multi-modal data processing.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![React](https://img.shields.io/badge/React-18.3.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🚀 Features

### 🤖 Intelligent Agents
- **Stock Analyzer Agent**: Real-time financial analysis with technical indicators
- **Intelligent Search Agent**: Perplexity-like search with multi-source integration
- **Sentiment Analysis Agent**: Advanced text sentiment analysis
- **Interactive Map Plugin**: Location-aware visualizations
- **Customer Service Agent**: Automated customer support
- **Data Analysis Agent**: Comprehensive data processing

### 🔍 Search & Intelligence
- **Multi-Source Search**: DuckDuckGo, Wikipedia, NewsAPI integration
- **Image Search**: Pixabay API integration for visual content
- **Location Services**: OpenStreetMap geocoding and weather data
- **Real-time News**: Breaking news and current events
- **Academic Research**: Scholarly content integration

### 💹 Financial Analysis
- **Real-time Stock Data**: Alpha Vantage API integration
- **Technical Indicators**: SMA, EMA, RSI, MACD, Bollinger Bands
- **Risk Assessment**: Comprehensive risk analysis
- **Trading Recommendations**: AI-powered investment insights
- **Portfolio Management**: Advanced portfolio tracking

### 🗺️ Interactive Mapping
- **OpenStreetMap Integration**: Free, high-quality maps
- **Location Intelligence**: Smart location detection and analysis
- **Weather Integration**: Real-time weather data
- **Points of Interest**: Restaurant, hotel, and landmark data

### 💬 Communication
- **GPT4All Integration**: Local LLM support
- **WebSocket Communication**: Real-time agent communication
- **Agent Orchestration**: Intelligent task coordination
- **Context Sharing**: Seamless data sharing between agents

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Frontend │    │  Spring Backend │    │   Free APIs     │
│                 │    │                 │    │                 │
│ • Dashboard     │◄──►│ • Agent Manager │◄──►│ • DuckDuckGo    │
│ • Search UI     │    │ • Orchestrator  │    │ • Pixabay       │
│ • Charts        │    │ • Context Store │    │ • NewsAPI       │
│ • Maps          │    │ • Free API Svc  │    │ • OpenWeather   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Security**: Spring Security
- **Communication**: WebSocket (STOMP)
- **APIs**: RESTful services
- **AI**: GPT4All local integration

### Frontend
- **Framework**: React 18.3.0
- **UI Library**: React Bootstrap 5.3.0
- **Charts**: Lightweight Charts, Recharts
- **Routing**: React Router 6.26.0
- **HTTP Client**: Axios

### Free APIs Integrated
- **Search**: DuckDuckGo Instant Answer API
- **Images**: Pixabay API (20,000 images/month)
- **News**: NewsAPI.org (100 requests/day)
- **Weather**: OpenWeatherMap (1000 calls/day)
- **Maps**: OpenStreetMap + Nominatim
- **Reference**: Wikipedia API
- **Geography**: REST Countries API
- **Finance**: Alpha Vantage

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- Maven 3.6+
- Git

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/ai-agent-framework.git
cd ai-agent-framework
```

### 2. Backend Setup
```bash
cd backend

# Configure API keys (optional - works with mock data)
export PIXABAY_API_KEY="your-pixabay-key"
export NEWS_API_KEY="your-newsapi-key"
export WEATHER_API_KEY="your-openweather-key"
export ALPHA_VANTAGE_API_KEY="your-alphavantage-key"

# Start backend
./mvnw spring-boot:run
```

### 3. Frontend Setup
```bash
cd ../frontend

# Install dependencies
npm install

# Start frontend
npm start
```

### 4. Access Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 🔧 Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080

# GPT4All Local API
llm.local.api.url=http://localhost:4891/v1
llm.local.api.enabled=true

# Free APIs (optional - works without keys)
pixabay.api.key=${PIXABAY_API_KEY:}
news.api.key=${NEWS_API_KEY:}
weather.api.key=${WEATHER_API_KEY:}
stock.api.alpha-vantage.key=${ALPHA_VANTAGE_API_KEY:}

# Security
spring.security.enabled=true
cors.allowed-origins=http://localhost:3000
```

### Environment Variables
```bash
# Optional API Keys (system works with mock data without these)
export PIXABAY_API_KEY="your-pixabay-api-key"
export NEWS_API_KEY="your-newsapi-key"
export WEATHER_API_KEY="your-openweather-key"
export ALPHA_VANTAGE_API_KEY="your-alphavantage-key"
export GPT4ALL_API_URL="http://localhost:4891/v1"
```

## 📚 API Documentation

### Search Endpoints
```http
POST /api/search/intelligent
Content-Type: application/json

{
  "query": "artificial intelligence trends",
  "searchType": "comprehensive"
}
```

### Stock Analysis Endpoints
```http
GET /api/stocks/{symbol}/analysis
GET /api/stocks/{symbol}/risk
POST /api/stocks/analyze
```

### Agent Management
```http
GET /api/agents/status
POST /api/agents/execute
GET /api/agents/tasks
```

## 🎯 Usage Examples

### 1. Intelligent Search
```javascript
// Search with automatic image and location detection
const searchResults = await api.post('/api/search/intelligent', {
  query: 'best restaurants in Paris with images',
  searchType: 'comprehensive'
});

// Results include:
// - Web search results
// - Images from Pixabay
// - Location data with maps
// - Reviews and ratings
```

### 2. Stock Analysis
```javascript
// Get comprehensive stock analysis
const analysis = await api.get('/api/stocks/AAPL/analysis');

// Returns:
// - Technical indicators
// - Risk assessment
// - Trading recommendations
// - Historical data
```

### 3. Agent Orchestration
```javascript
// Execute coordinated agent tasks
const result = await api.post('/api/agents/execute', {
  type: 'COMPREHENSIVE_ANALYSIS',
  description: 'Analyze TSLA stock with market sentiment',
  parameters: { symbol: 'TSLA', includeNews: true }
});
```

## 🔌 Plugin Development

### Creating a New Agent
```java
@Component
public class CustomAgent implements Agent {
    @Override
    public String getName() {
        return "CustomAgent";
    }
    
    @Override
    public AgentConfig getConfig() {
        AgentConfig config = new AgentConfig();
        config.setName("CustomAgent");
        return config;
    }
    
    @Override
    public boolean canHandle(Task task) {
        return "CUSTOM_TASK".equals(task.getType());
    }
    
    @Override
    public AgentResult execute(Task task, AgentContext context) {
        // Your custom logic here
        return AgentResult.success(task.getId(), getName(), result);
    }
}
```

### Registering Agents
Add your agent class to `META-INF/services/com.aiframework.core.Agent`:
```
com.aiframework.plugin.CustomAgent
```

## 🎨 UI Components

### Navigation Structure
```
🧠 AI Agent Framework
├── 📊 Dashboard
├── 🤖 AI Agents
│   ├── 📈 Stock Analyzer
│   ├── 🔍 Intelligent Search
│   ├── 🧠 Smart Decisions
│   ├── 💬 GPT4All Chat
│   └── 🗺️ Interactive Maps
├── 📊 Analytics
│   ├── 📊 System Metrics
│   └── 🔍 System Monitor
└── ⚙️ Settings
```

### Key Features
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Real-time Updates**: WebSocket-powered live data
- **Interactive Charts**: Financial charts with zoom and indicators
- **Map Integration**: Interactive maps with location intelligence
- **Dark/Light Theme**: User preference support

## 🔒 Security

### Authentication & Authorization
- Spring Security integration
- CORS configuration for frontend
- API key management
- Rate limiting

### API Security
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CORS and security configuration
    // Rate limiting for API endpoints
    // API key validation
}
```

## 📈 Monitoring & Metrics

### Health Checks
- Application health monitoring
- Database connectivity
- External API status
- Agent performance metrics

### Logging
```properties
# Comprehensive logging configuration
logging.level.com.aiframework=DEBUG
logging.file.name=logs/ai-agent-framework.log
```

## 🧪 Testing

### Running Tests
```bash
# Backend tests
cd backend
./mvnw test

# Frontend tests
cd frontend
npm test
```

### Test Coverage
- Unit tests for all agents
- Integration tests for APIs
- End-to-end testing for workflows

## 🚀 Deployment

### Docker Support
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim
COPY target/ai-agent-framework-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Configuration
```properties
# Production settings
spring.profiles.active=production
logging.level.root=WARN
server.port=8080
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style
- Java: Follow Spring Boot conventions
- JavaScript: ESLint + Prettier configuration
- Documentation: Update README for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help
- **Documentation**: Check this README and inline comments
- **Issues**: Open GitHub issues for bugs and feature requests
- **Discussions**: Use GitHub Discussions for questions

### Common Issues

**DuckDuckGo API Errors**: The DuckDuckGo instant answer API frequently returns 500 errors. The system gracefully falls back to alternative search methods.

**Missing API Keys**: The system works with mock data when API keys are not configured. For production use, configure the optional API keys.

**CORS Issues**: Ensure `cors.allowed-origins` includes your frontend URL.

## 🔮 Roadmap

### Planned Features
- [ ] Additional LLM integrations (OpenAI, Claude)
- [ ] More financial data providers
- [ ] Advanced visualization tools
- [ ] Machine learning model integration
- [ ] Multi-language support
- [ ] Mobile app development

### Recent Updates
- ✅ Pixabay image search integration
- ✅ Enhanced error handling for DuckDuckGo API
- ✅ Improved agent orchestration
- ✅ Real-time WebSocket communication
- ✅ Interactive mapping capabilities

## 📊 Stats

- **Lines of Code**: ~15,000+
- **Agents**: 8 built-in agents
- **APIs**: 8+ free API integrations
- **Languages**: Java, JavaScript, TypeScript
- **Tests**: Comprehensive test coverage

---

**Built with ❤️ by the AI Agent Framework Team**

*Empowering intelligent automation through configurable AI agents*
