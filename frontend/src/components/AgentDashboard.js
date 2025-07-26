import React, { useState, useEffect } from 'react';
import { useAppContext } from '../context/AppContext';
import { Alert } from 'react-bootstrap';
import { Helmet } from 'react-helmet-async';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import {
  Card, CardHeader, CardTitle, CardBody, CardFooter,
  Button, Badge, Status, Grid, Container, Skeleton,
} from './common/UIComponents';

// Dashboard Stats Component
const DashboardStats = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="dashboard-stats">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="metric-card">
            <Skeleton height="1rem" width="60%" />
            <Skeleton height="2.5rem" width="80%" className="my-3" />
            <Skeleton height="0.75rem" width="40%" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="dashboard-stats">
      <motion.div
        className="metric-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="metric-label">Active Agents</div>
        <div className="metric-value">{stats.activeAgents || 0}</div>
        <div className="metric-trend metric-trend--up">
          ↗ +2 from last hour
        </div>
      </motion.div>

      <motion.div
        className="metric-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <div className="metric-label">Tasks Completed</div>
        <div className="metric-value">{stats.completedTasks || 0}</div>
        <div className="metric-trend metric-trend--up">
          ↗ +12 today
        </div>
      </motion.div>

      <motion.div
        className="metric-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <div className="metric-label">System Health</div>
        <div className="metric-value">{stats.systemHealth || '98'}%</div>
        <div className="metric-trend metric-trend--neutral">
          → Stable
        </div>
      </motion.div>

      <motion.div
        className="metric-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <div className="metric-label">API Calls</div>
        <div className="metric-value">{stats.apiCalls || '1.2k'}</div>
        <div className="metric-trend metric-trend--up">
          ↗ +15% this week
        </div>
      </motion.div>
    </div>
  );
};

// Agent Card Component
const AgentCard = ({ agent, index }) => {
  const getStatusVariant = (status) => {
    switch (status?.toLowerCase()) {
    case 'active': case 'running': return 'success';
    case 'error': case 'failed': return 'error';
    case 'paused': case 'waiting': return 'warning';
    default: return 'neutral';
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
    case 'active': case 'running': return '🟢';
    case 'error': case 'failed': return '🔴';
    case 'paused': case 'waiting': return '🟡';
    default: return '⚪';
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.1 }}
      whileHover={{ y: -4 }}
    >
      <Card className="agent-card h-100">
        <CardHeader className="agent-card__header">
          <CardTitle className="agent-card__title">
            <span className="me-2">{agent.icon || '🤖'}</span>
            {agent.name || `Agent ${index + 1}`}
          </CardTitle>
          <div className="agent-card__subtitle">
            {agent.description || 'AI-powered automation agent'}
          </div>
        </CardHeader>

        <CardBody className="agent-card__body">
          <div className="d-flex align-items-center justify-content-between mb-3">
            <Status
              status={agent.status?.toLowerCase() || 'offline'}
              label={agent.status || 'Offline'}
            />
            <Badge variant={getStatusVariant(agent.status)}>
              {getStatusIcon(agent.status)} {agent.status || 'Inactive'}
            </Badge>
          </div>

          <div className="agent-metrics">
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Tasks Completed:</span>
              <strong>{agent.tasksCompleted || 0}</strong>
            </div>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Success Rate:</span>
              <strong>{agent.successRate || '95'}%</strong>
            </div>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Uptime:</span>
              <strong>{agent.uptime || '2h 45m'}</strong>
            </div>
          </div>

          {agent.capabilities && (
            <div className="agent-capabilities mt-3">
              <small className="text-muted d-block mb-2">Capabilities:</small>
              <div className="d-flex flex-wrap gap-1">
                {agent.capabilities.slice(0, 3).map((capability, idx) => (
                  <Badge key={idx} variant="neutral" size="sm">
                    {capability}
                  </Badge>
                ))}
                {agent.capabilities.length > 3 && (
                  <Badge variant="neutral" size="sm">
                    +{agent.capabilities.length - 3} more
                  </Badge>
                )}
              </div>
            </div>
          )}
        </CardBody>

        <CardFooter className="agent-card__footer">
          <div className="d-flex gap-2">
            <Button
              variant="primary"
              size="sm"
              onClick={() => toast.success(`${agent.name || 'Agent'} details opened`)}
            >
              View Details
            </Button>
            <Button
              variant="secondary"
              size="sm"
              onClick={() => toast.success(`${agent.name || 'Agent'} configured`)}
            >
              Configure
            </Button>
            {agent.status?.toLowerCase() === 'active' ? (
              <Button
                variant="warning"
                size="sm"
                onClick={() => toast.success(`${agent.name || 'Agent'} paused`)}
              >
                Pause
              </Button>
            ) : (
              <Button
                variant="success"
                size="sm"
                onClick={() => toast.success(`${agent.name || 'Agent'} started`)}
              >
                Start
              </Button>
            )}
          </div>
        </CardFooter>
      </Card>
    </motion.div>
  );
};

// Main Dashboard Component
export default function AgentDashboard() {
  const { agents, loading, error, loadAgents } = useAppContext();
  const [stats, setStats] = useState({});
  const [statsLoading, setStatsLoading] = useState(true);

  // Mock agents data if none available
  const mockAgents = [
    {
      name: 'Financial Analyzer',
      description: 'Real-time market analysis and trading insights',
      status: 'Active',
      icon: '📈',
      tasksCompleted: 156,
      successRate: 98,
      uptime: '5h 23m',
      capabilities: ['Market Analysis', 'Risk Assessment', 'Portfolio Optimization', 'News Analysis'],
    },
    {
      name: 'Content Generator',
      description: 'AI-powered content creation and optimization',
      status: 'Active',
      icon: '✍️',
      tasksCompleted: 89,
      successRate: 95,
      uptime: '3h 45m',
      capabilities: ['Text Generation', 'SEO Optimization', 'Translation', 'Summarization'],
    },
    {
      name: 'Data Processor',
      description: 'Large-scale data processing and analysis',
      status: 'Paused',
      icon: '🔄',
      tasksCompleted: 234,
      successRate: 92,
      uptime: '8h 12m',
      capabilities: ['Data Mining', 'ETL Operations', 'Pattern Recognition', 'Anomaly Detection'],
    },
    {
      name: 'Customer Support',
      description: 'Intelligent customer service automation',
      status: 'Active',
      icon: '🎧',
      tasksCompleted: 342,
      successRate: 97,
      uptime: '12h 30m',
      capabilities: ['Query Resolution', 'Sentiment Analysis', 'Escalation Management', 'Knowledge Base'],
    },
    {
      name: 'Security Monitor',
      description: 'Advanced threat detection and response',
      status: 'Active',
      icon: '🛡️',
      tasksCompleted: 67,
      successRate: 99,
      uptime: '24h 00m',
      capabilities: ['Threat Detection', 'Incident Response', 'Compliance Monitoring', 'Forensics'],
    },
    {
      name: 'Research Assistant',
      description: 'Intelligent research and knowledge synthesis',
      status: 'Error',
      icon: '🔬',
      tasksCompleted: 45,
      successRate: 88,
      uptime: '1h 15m',
      capabilities: ['Literature Review', 'Data Collection', 'Hypothesis Testing', 'Report Generation'],
    },
  ];

  const displayAgents = agents.length > 0 ? agents : mockAgents;

  useEffect(() => {
    // Load agents and stats
    if (loadAgents) {
      loadAgents();
    }

    // Mock stats loading
    setTimeout(() => {
      setStats({
        activeAgents: displayAgents.filter(a => a.status?.toLowerCase() === 'active').length,
        completedTasks: displayAgents.reduce((sum, a) => sum + (a.tasksCompleted || 0), 0),
        systemHealth: 98,
        apiCalls: '1.2k',
      });
      setStatsLoading(false);
    }, 1000);
  }, [loadAgents]);

  const handleRefreshDashboard = () => {
    setStatsLoading(true);
    if (loadAgents) {
      loadAgents();
    }

    setTimeout(() => {
      setStatsLoading(false);
      toast.success('Dashboard refreshed successfully');
    }, 1000);
  };

  return (
    <>
      <Helmet>
        <title>Agent Dashboard - AI Framework</title>
        <meta name="description" content="Monitor and manage your AI agents" />
      </Helmet>

      <Container>
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="page-header"
        >
          <div className="d-flex justify-content-between align-items-start">
            <div>
              <h1 className="page-title">
                <span className="me-3">🤖</span>
                Agent Dashboard
              </h1>
              <p className="page-subtitle">
                Monitor, configure, and manage your AI agents in real-time
              </p>
            </div>
            <div className="d-flex gap-3">
              <Button
                variant="secondary"
                onClick={handleRefreshDashboard}
                loading={statsLoading}
              >
                🔄 Refresh
              </Button>
              <Button variant="primary">
                ➕ Add Agent
              </Button>
            </div>
          </div>
        </motion.div>

        {error && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
          >
            <Alert variant="danger" className="mb-4">
              <strong>Error:</strong> {error}
            </Alert>
          </motion.div>
        )}

        <DashboardStats stats={stats} loading={statsLoading} />

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h3 mb-0">Active Agents</h2>
            <Badge variant="primary">
              {displayAgents.length} Total
            </Badge>
          </div>

          {loading ? (
            <div className="agent-grid">
              {[...Array(6)].map((_, i) => (
                <Card key={i} className="h-100">
                  <CardHeader>
                    <Skeleton height="1.5rem" width="70%" />
                    <Skeleton height="1rem" width="90%" className="mt-2" />
                  </CardHeader>
                  <CardBody>
                    <Skeleton height="1rem" width="100%" className="mb-2" />
                    <Skeleton height="1rem" width="80%" className="mb-2" />
                    <Skeleton height="1rem" width="60%" />
                  </CardBody>
                </Card>
              ))}
            </div>
          ) : (
            <div className="agent-grid">
              {displayAgents.map((agent, index) => (
                <AgentCard
                  key={agent.id || agent.name || index}
                  agent={agent}
                  index={index}
                />
              ))}
            </div>
          )}

          {!loading && displayAgents.length === 0 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center py-5"
            >
              <div className="mb-4" style={{ fontSize: '4rem' }}>🤖</div>
              <h3 className="h4 mb-3">No Agents Found</h3>
              <p className="text-muted mb-4">
                Get started by creating your first AI agent to automate tasks and workflows.
              </p>
              <Button variant="primary" size="lg">
                Create Your First Agent
              </Button>
            </motion.div>
          )}
        </motion.div>
      </Container>
    </>
  );
}
