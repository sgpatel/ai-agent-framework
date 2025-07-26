import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAppContext } from '../context/AppContext';

const HomePage = () => {
  const { user } = useAppContext();

  const features = [
    {
      title: 'AI Agent Dashboard',
      description: 'Monitor and manage your intelligent agents',
      icon: '🤖',
      link: '/dashboard'
    },
    {
      title: 'Stock Analysis',
      description: 'Advanced stock market analysis with AI insights',
      icon: '📈',
      link: '/stocks'
    },
    {
      title: 'Task Management',
      description: 'Create and track automated tasks',
      icon: '📋',
      link: '/task-management'
    },
    {
      title: 'System Monitoring',
      description: 'Real-time system health and metrics',
      icon: '🔍',
      link: '/monitoring'
    },
    {
      title: 'AI Chat',
      description: 'Chat with local AI models',
      icon: '💬',
      link: '/ai-chat'
    },
    {
      title: 'Plugin Manager',
      description: 'Manage and configure plugins',
      icon: '🔌',
      link: '/plugin-manager'
    }
  ];

  return (
    <Container fluid className="py-5">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        {/* Hero Section */}
        <Row className="text-center mb-5">
          <Col lg={8} className="mx-auto">
            <motion.h1
              className="display-4 fw-bold mb-4"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.6 }}
            >
              Welcome to AI Agent Framework
            </motion.h1>
            <motion.p
              className="lead text-muted mb-4"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4, duration: 0.6 }}
            >
              A powerful platform for creating, managing, and orchestrating intelligent agents for task automation and analysis.
            </motion.p>
            {!user && (
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.6, duration: 0.6 }}
              >
                <Link to="/login">
                  <Button variant="primary" size="lg" className="me-3">
                    Get Started
                  </Button>
                </Link>
                <Link to="/register">
                  <Button variant="outline-primary" size="lg">
                    Sign Up
                  </Button>
                </Link>
              </motion.div>
            )}
          </Col>
        </Row>

        {/* Features Grid */}
        <Row className="g-4">
          {features.map((feature, index) => (
            <Col key={index} lg={4} md={6}>
              <motion.div
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 * index, duration: 0.6 }}
                whileHover={{ y: -5 }}
              >
                <Card className="h-100 shadow-sm border-0">
                  <Card.Body className="text-center p-4">
                    <div className="fs-1 mb-3">{feature.icon}</div>
                    <Card.Title className="h5 mb-3">{feature.title}</Card.Title>
                    <Card.Text className="text-muted mb-4">
                      {feature.description}
                    </Card.Text>
                    <Link to={feature.link}>
                      <Button variant="outline-primary">
                        Explore
                      </Button>
                    </Link>
                  </Card.Body>
                </Card>
              </motion.div>
            </Col>
          ))}
        </Row>

        {/* Quick Stats */}
        {user && (
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 1, duration: 0.6 }}
            className="mt-5"
          >
            <Row className="text-center">
              <Col md={3}>
                <Card className="bg-primary text-white">
                  <Card.Body>
                    <h3 className="mb-0">2</h3>
                    <small>Active Agents</small>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={3}>
                <Card className="bg-success text-white">
                  <Card.Body>
                    <h3 className="mb-0">0</h3>
                    <small>Completed Tasks</small>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={3}>
                <Card className="bg-info text-white">
                  <Card.Body>
                    <h3 className="mb-0">Online</h3>
                    <small>System Status</small>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={3}>
                <Card className="bg-warning text-white">
                  <Card.Body>
                    <h3 className="mb-0">5</h3>
                    <small>Available Plugins</small>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </motion.div>
        )}
      </motion.div>
    </Container>
  );
};

export default HomePage;
