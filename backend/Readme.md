# AI Agent Framework - Backend

This is the backend implementation of the AI Agent Framework built with Java and Spring Boot.

## Features

- **Plugin Architecture**: Hot-pluggable agents using Java SPI
- **Agent Orchestration**: LLM-powered task delegation and coordination
- **Context Management**: Persistent context sharing between agents
- **Communication Protocols**: Agent-to-agent communication
- **REST API**: Complete REST API for task submission and agent management
- **Async Processing**: Non-blocking task execution
- **Monitoring**: Built-in metrics and health checks

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Optional: OpenAI API key for LLM integration

### Running the Application

1. Clone the repository
2. Navigate to the backend directory
3. Run the application:

```bash
cd backend
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### API Endpoints

#### Task Management
- `POST /api/tasks` - Submit a task for processing
- `POST /api/tasks/batch` - Submit multiple tasks
- `GET /api/tasks/{taskId}/status` - Get task status
- `DELETE /api/tasks/{taskId}` - Cancel a task
- `GET /api/tasks/metrics` - Get processing metrics

#### Agent Management
- `GET /api/agents` - List all agents
- `GET /api/agents/{name}` - Get agent details
- `GET /api/agents/capable/{taskType}` - Find capable agents
- `POST /api/agents/reload` - Reload plugin agents

### Configuration

Edit `src/main/resources/application.yml` to configure:
- Server port and context path
- Plugin directory
- LLM integration settings
- Security settings
- Logging levels

### Creating Custom Agents

1. Implement the `Agent` interface
2. Add your agent class name to `META-INF/services/com.aiframework.core.Agent`
3. Package as JAR and place in plugins directory

Example:
```java
public class MyCustomAgent implements Agent {
    @Override
    public String getName() { return "MyCustomAgent"; }

    @Override
    public boolean canHandle(Task task) { 
        return "my_task_type".equals(task.getType()); 
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        // Your agent logic here
        return AgentResult.success(task.getId(), getName(), result, "Success", executionTime);
    }

    // Implement other required methods...
}
```

### Testing

Run tests with:
```bash
mvn test
```

### Building

Build the application:
```bash
mvn clean package
```

This creates an executable JAR in the `target/` directory.

## Architecture

The framework follows a modular architecture:

- **Core**: Base interfaces and models
- **Manager**: Agent lifecycle management
- **Orchestrator**: Task coordination and delegation
- **Communication**: Agent messaging protocols
- **Context**: Shared memory and knowledge management
- **Plugin**: Example agent implementations
- **API**: REST controllers and DTOs

## Plugin Development

See the example agents in `src/main/java/com/aiframework/plugin/` for reference implementations.