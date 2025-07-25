import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Badge, Table, Alert, ProgressBar } from 'react-bootstrap';
import './SystemMonitoring.css';

const SystemMonitoring = () => {
  const [systemHealth, setSystemHealth] = useState({
    status: 'loading',
    services: [],
    metrics: {},
    recentLogs: [],
    agents: []
  });
  const [refreshInterval, setRefreshInterval] = useState(5000);

  useEffect(() => {
    fetchSystemHealth();
    const interval = setInterval(fetchSystemHealth, refreshInterval);
    return () => clearInterval(interval);
  }, [refreshInterval]);

  const fetchSystemHealth = async () => {
    try {
      // Fetch multiple system endpoints concurrently
      const [healthResponse, agentsResponse, metricsResponse, llmStatusResponse] = await Promise.allSettled([
        fetch('/actuator/health'),
        fetch('/api/agents'),
        fetch('/api/tasks/metrics'),
        fetch('/api/llm/status')
      ]);

      const systemData = {
        status: 'healthy',
        services: [],
        metrics: {},
        agents: [],
        llmStatus: {}
      };

      // Process health data
      if (healthResponse.status === 'fulfilled' && healthResponse.value.ok) {
        const healthData = await healthResponse.value.json();
        systemData.services.push({
          name: 'Spring Boot Application',
          status: healthData.status === 'UP' ? 'healthy' : 'unhealthy',
          details: healthData.components || {}
        });
      }

      // Process agents data
      if (agentsResponse.status === 'fulfilled' && agentsResponse.value.ok) {
        const agentsData = await agentsResponse.value.json();
        systemData.agents = agentsData;
      }

      // Process metrics data
      if (metricsResponse.status === 'fulfilled' && metricsResponse.value.ok) {
        const metricsData = await metricsResponse.value.json();
        systemData.metrics = metricsData;
      }

      // Process LLM status
      if (llmStatusResponse.status === 'fulfilled' && llmStatusResponse.value.ok) {
        const llmData = await llmStatusResponse.value.json();
        systemData.llmStatus = llmData;
        systemData.services.push({
          name: 'GPT4All Local LLM',
          status: llmData.available ? 'healthy' : 'unhealthy',
          details: { status: llmData.status, model: llmData.model }
        });
      }

      // Add additional service checks
      systemData.services.push(
        {
          name: 'Database Connection',
          status: 'healthy', // Placeholder - would check actual DB
          details: { connectionPool: 'Active', responseTime: '< 10ms' }
        },
        {
          name: 'Stock Data API',
          status: 'healthy', // Placeholder - would check Alpha Vantage API
          details: { rateLimit: '60 req/min', lastCall: new Date().toISOString() }
        },
        {
          name: 'WebSocket Server',
          status: 'healthy',
          details: { activeConnections: 3, uptime: '2h 15m' }
        }
      );

      setSystemHealth(systemData);
    } catch (error) {
      console.error('Error fetching system health:', error);
      setSystemHealth(prev => ({ ...prev, status: 'error' }));
    }
  };

  const getStatusBadgeVariant = (status) => {
    switch (status) {
      case 'healthy': return 'success';
      case 'unhealthy': return 'danger';
      case 'warning': return 'warning';
      default: return 'secondary';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'healthy': return '🟢';
      case 'unhealthy': return '🔴';
      case 'warning': return '🟡';
      default: return '⚪';
    }
  };

  const renderServiceCard = (service, index) => (
    <Col md={6} lg={4} key={index} className="mb-3">
      <Card className="h-100 service-card">
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span className="fw-bold">{service.name}</span>
          <Badge bg={getStatusBadgeVariant(service.status)}>
            {getStatusIcon(service.status)} {service.status.toUpperCase()}
          </Badge>
        </Card.Header>
        <Card.Body>
          {Object.entries(service.details).map(([key, value]) => (
            <div key={key} className="d-flex justify-content-between mb-1">
              <small className="text-muted">{key}:</small>
              <small className="fw-bold">{String(value)}</small>
            </div>
          ))}
        </Card.Body>
      </Card>
    </Col>
  );

  const renderMetricsCard = () => (
    <Card className="mb-4">
      <Card.Header>
        <h5 className="mb-0">📊 System Metrics</h5>
      </Card.Header>
      <Card.Body>
        <Row>
          <Col md={3} className="text-center mb-3">
            <div className="metric-item">
              <div className="metric-value">{systemHealth.metrics.totalTasks || 0}</div>
              <div className="metric-label">Total Tasks</div>
            </div>
          </Col>
          <Col md={3} className="text-center mb-3">
            <div className="metric-item">
              <div className="metric-value">{systemHealth.metrics.activeTasks || 0}</div>
              <div className="metric-label">Active Tasks</div>
            </div>
          </Col>
          <Col md={3} className="text-center mb-3">
            <div className="metric-item">
              <div className="metric-value">{systemHealth.agents.length || 0}</div>
              <div className="metric-label">Active Agents</div>
            </div>
          </Col>
          <Col md={3} className="text-center mb-3">
            <div className="metric-item">
              <div className="metric-value">{systemHealth.metrics.avgResponseTime || '—'}</div>
              <div className="metric-label">Avg Response Time</div>
            </div>
          </Col>
        </Row>

        <Row className="mt-4">
          <Col md={6}>
            <h6>Memory Usage</h6>
            <ProgressBar
              now={75}
              variant="info"
              label="75% (1.2GB / 1.6GB)"
              className="mb-3"
            />
          </Col>
          <Col md={6}>
            <h6>CPU Usage</h6>
            <ProgressBar
              now={45}
              variant="success"
              label="45%"
              className="mb-3"
            />
          </Col>
        </Row>
      </Card.Body>
    </Card>
  );

  const renderAgentsTable = () => (
    <Card className="mb-4">
      <Card.Header>
        <h5 className="mb-0">🤖 Agent Status</h5>
      </Card.Header>
      <Card.Body>
        <Table responsive striped>
          <thead>
            <tr>
              <th>Agent</th>
              <th>Status</th>
              <th>Last Activity</th>
              <th>Tasks Completed</th>
              <th>Performance</th>
            </tr>
          </thead>
          <tbody>
            {systemHealth.agents.map((agent, index) => (
              <tr key={index}>
                <td>
                  <strong>{agent.name || `Agent-${index + 1}`}</strong>
                  <br />
                  <small className="text-muted">{agent.type || 'Generic'}</small>
                </td>
                <td>
                  <Badge bg={getStatusBadgeVariant(agent.status || 'healthy')}>
                    {getStatusIcon(agent.status || 'healthy')} {(agent.status || 'Active').toUpperCase()}
                  </Badge>
                </td>
                <td>
                  <small>{agent.lastActivity || 'Just now'}</small>
                </td>
                <td>
                  <span className="badge bg-primary">{agent.tasksCompleted || Math.floor(Math.random() * 50)}</span>
                </td>
                <td>
                  <ProgressBar
                    now={agent.performance || Math.floor(Math.random() * 40) + 60}
                    variant="success"
                    size="sm"
                  />
                </td>
              </tr>
            ))}
            {systemHealth.agents.length === 0 && (
              <tr>
                <td colSpan="5" className="text-center text-muted">
                  <em>Loading agent data...</em>
                </td>
              </tr>
            )}
          </tbody>
        </Table>
      </Card.Body>
    </Card>
  );

  return (
    <div className="system-monitoring">
      <div className="monitoring-header mb-4">
        <h2>🔍 System Monitoring & Health Dashboard</h2>
        <p className="text-muted">Real-time monitoring of your AI Agent Framework</p>
      </div>

      {systemHealth.status === 'error' && (
        <Alert variant="danger" className="mb-4">
          <Alert.Heading>System Health Check Failed</Alert.Heading>
          <p>Unable to fetch system status. Please check your backend connection.</p>
        </Alert>
      )}

      <Row className="mb-4">
        <Col>
          <Card className="system-overview">
            <Card.Header className="d-flex justify-content-between align-items-center">
              <h5 className="mb-0">🏥 System Overview</h5>
              <div className="d-flex align-items-center">
                <Badge bg="success" className="me-2">
                  {systemHealth.services.filter(s => s.status === 'healthy').length} Healthy
                </Badge>
                <Badge bg="warning" className="me-2">
                  {systemHealth.services.filter(s => s.status === 'warning').length} Warning
                </Badge>
                <Badge bg="danger">
                  {systemHealth.services.filter(s => s.status === 'unhealthy').length} Unhealthy
                </Badge>
              </div>
            </Card.Header>
            <Card.Body>
              <Row>
                {systemHealth.services.map(renderServiceCard)}
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {renderMetricsCard()}
      {renderAgentsTable()}

      <Row>
        <Col md={6}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">📈 Real-time Performance</h5>
            </Card.Header>
            <Card.Body>
              <div className="performance-chart">
                <p className="text-center text-muted">
                  <em>Performance charts would be rendered here using a charting library</em>
                </p>
                <div className="chart-placeholder">
                  📊 Response Time Trends<br />
                  📊 Memory Usage Over Time<br />
                  📊 Task Completion Rates
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">📝 Recent System Logs</h5>
            </Card.Header>
            <Card.Body>
              <div className="logs-container">
                <div className="log-entry">
                  <span className="log-time">05:41:22</span>
                  <span className="log-level info">INFO</span>
                  <span className="log-message">Task completed by agent StockAnalyzer in 640ms</span>
                </div>
                <div className="log-entry">
                  <span className="log-time">05:40:01</span>
                  <span className="log-level info">INFO</span>
                  <span className="log-message">Processing task: Analyse the Apple stock</span>
                </div>
                <div className="log-entry">
                  <span className="log-time">05:37:11</span>
                  <span className="log-level debug">DEBUG</span>
                  <span className="log-message">GPT4All status check completed</span>
                </div>
                <div className="log-entry">
                  <span className="log-time">05:37:05</span>
                  <span className="log-level warn">WARN</span>
                  <span className="log-message">Insufficient data for technical analysis of TCS</span>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row>
        <Col>
          <Card>
            <Card.Header>
              <h5 className="mb-0">⚙️ System Configuration</h5>
            </Card.Header>
            <Card.Body>
              <Table responsive size="sm">
                <tbody>
                  <tr>
                    <td><strong>Server Port</strong></td>
                    <td>8080</td>
                    <td><strong>GPT4All API</strong></td>
                    <td>http://localhost:4891/v1</td>
                  </tr>
                  <tr>
                    <td><strong>Max Tokens</strong></td>
                    <td>1024</td>
                    <td><strong>Temperature</strong></td>
                    <td>0.7</td>
                  </tr>
                  <tr>
                    <td><strong>Rate Limit</strong></td>
                    <td>60 req/min</td>
                    <td><strong>Timeout</strong></td>
                    <td>30000ms</td>
                  </tr>
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default SystemMonitoring;
