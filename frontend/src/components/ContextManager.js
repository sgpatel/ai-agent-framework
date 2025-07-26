import { useState } from 'react';
import { Card, Row, Col, Badge, Button, Modal, Form, Alert, Spinner } from 'react-bootstrap';
import { useAppContext } from '../context/AppContext';
import { motion, AnimatePresence } from 'framer-motion';

export default function ContextManager() {
  const {
    agentContexts,
    sharedData,
    agentRecommendations,
    collaborativeWorkflows,
    setAgentContext,
    updateSharedData,
    createCollaborativeWorkflow,
    suggestChartTypes,
    clearContext,
  } = useAppContext();

  const [showWorkflowModal, setShowWorkflowModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [workflowData, setWorkflowData] = useState({
    name: '',
    description: '',
    agents: [],
    dataFlow: [],
  });

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'N/A';
    try {
      return new Date(timestamp).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (error) {
      return 'Invalid date';
    }
  };

  const getDataTypeBadge = (dataType) => {
    const variants = {
      'stock-analysis': 'primary',
      'financial-data': 'success',
      'market-sentiment': 'warning',
      'technical-indicators': 'info',
      'chart-data': 'secondary',
      'risk-analysis': 'danger',
      'news-data': 'dark',
    };
    return variants[dataType] || 'outline-secondary';
  };

  const handleCreateWorkflow = async () => {
    if (!workflowData.name.trim() || !workflowData.description.trim()) {
      return;
    }

    setLoading(true);
    try {
      await createCollaborativeWorkflow(workflowData);
      setShowWorkflowModal(false);
      setWorkflowData({ name: '', description: '', agents: [], dataFlow: [] });
    } catch (error) {
      console.error('Failed to create workflow:', error);
    } finally {
      setLoading(false);
    }
  };

  const executeRecommendation = async (recommendation, agentId) => {
    setLoading(true);
    try {
      switch (recommendation.action) {
      case 'create-chart': {
        const stockData = sharedData['stockData'];
        if (stockData) {
          const chartSuggestions = await suggestChartTypes('time-series', stockData.data);
          updateSharedData('chartSuggestions', chartSuggestions, 'context-manager', {
            sourceRecommendation: recommendation.id,
            targetAgent: agentId,
          });
        }
        break;
      }
      case 'assess-risk': {
        const financialData = sharedData['financialMetrics'];
        if (financialData) {
          setAgentContext('risk-assessor', {
            dataType: 'risk-analysis',
            inputData: financialData.data,
            analysisType: 'comprehensive',
          });
        }
        break;
      }
      default:
        console.log('Unknown recommendation action:', recommendation.action);
      }
    } catch (error) {
      console.error('Failed to execute recommendation:', error);
    } finally {
      setLoading(false);
    }
  };

  const renderDataValue = (value) => {
    if (typeof value === 'object' && value !== null) {
      return JSON.stringify(value, null, 2);
    }
    return String(value);
  };

  return (
    <div className="context-manager p-4">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="d-flex justify-content-between align-items-center mb-4"
      >
        <div>
          <h2 className="text-gradient mb-1">🔄 Agent Context Manager</h2>
          <p className="text-muted mb-0">Monitor and manage AI agent communications and workflows</p>
        </div>
        <div className="d-flex gap-2">
          <Button
            variant="outline-primary"
            onClick={() => setShowWorkflowModal(true)}
            disabled={loading}
          >
                        ➕ Create Workflow
          </Button>
          <Button
            variant="outline-danger"
            onClick={() => clearContext()}
            disabled={loading}
          >
                        🗑️ Clear All Context
          </Button>
        </div>
      </motion.div>

      <Row className="g-4">
        {/* Agent Contexts */}
        <Col lg={6}>
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Card className="h-100 glass-effect">
              <Card.Header className="bg-transparent border-0 pb-0">
                <div className="d-flex align-items-center gap-2">
                  <span className="fs-5">🤖</span>
                  <h5 className="mb-0">Agent Contexts</h5>
                  <Badge bg="primary" className="ms-auto">
                    {Object.keys(agentContexts).length}
                  </Badge>
                </div>
              </Card.Header>
              <Card.Body className="pt-3">
                <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  <AnimatePresence>
                    {Object.keys(agentContexts).length === 0 ? (
                      <Alert variant="info" className="text-center">
                        <span className="fs-4 d-block mb-2">🤖</span>
                                                No active agent contexts
                      </Alert>
                    ) : (
                      Object.entries(agentContexts).map(([agentId, context], index) => (
                        <motion.div
                          key={agentId}
                          initial={{ opacity: 0, y: 20 }}
                          animate={{ opacity: 1, y: 0 }}
                          exit={{ opacity: 0, y: -20 }}
                          transition={{ delay: index * 0.1 }}
                          className="mb-3"
                        >
                          <Card className="border-start border-primary border-4 hover-card">
                            <Card.Body className="p-3">
                              <div className="d-flex justify-content-between align-items-start mb-2">
                                <h6 className="mb-1 fw-semibold">{agentId}</h6>
                                <small className="text-muted">
                                  {formatTimestamp(context.lastUpdated)}
                                </small>
                              </div>
                              {context.dataType && (
                                <Badge bg={getDataTypeBadge(context.dataType)} className="mb-2">
                                  {context.dataType.replace('-', ' ').toUpperCase()}
                                </Badge>
                              )}
                              <div className="small text-muted">
                                {Object.entries(context)
                                  .filter(([key]) => !['lastUpdated', 'dataType'].includes(key))
                                  .slice(0, 3) // Limit display items
                                  .map(([key, value]) => (
                                    <div key={key} className="mb-1">
                                      <strong className="text-capitalize">{key}:</strong>{' '}
                                      <span className="text-truncate d-inline-block" style={{ maxWidth: '200px' }}>
                                        {renderDataValue(value)}
                                      </span>
                                    </div>
                                  ))
                                }
                              </div>
                            </Card.Body>
                          </Card>
                        </motion.div>
                      ))
                    )}
                  </AnimatePresence>
                </div>
              </Card.Body>
            </Card>
          </motion.div>
        </Col>

        {/* Shared Data */}
        <Col lg={6}>
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Card className="h-100 glass-effect">
              <Card.Header className="bg-transparent border-0 pb-0">
                <div className="d-flex align-items-center gap-2">
                  <span className="fs-5">📊</span>
                  <h5 className="mb-0">Shared Data</h5>
                  <Badge bg="success" className="ms-auto">
                    {Object.keys(sharedData).length}
                  </Badge>
                </div>
              </Card.Header>
              <Card.Body className="pt-3">
                <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  <AnimatePresence>
                    {Object.keys(sharedData).length === 0 ? (
                      <Alert variant="info" className="text-center">
                        <span className="fs-4 d-block mb-2">📊</span>
                                                No shared data available
                      </Alert>
                    ) : (
                      Object.entries(sharedData).map(([key, dataInfo], index) => (
                        <motion.div
                          key={key}
                          initial={{ opacity: 0, y: 20 }}
                          animate={{ opacity: 1, y: 0 }}
                          exit={{ opacity: 0, y: -20 }}
                          transition={{ delay: index * 0.1 }}
                          className="mb-3"
                        >
                          <Card className="border-start border-success border-4 hover-card">
                            <Card.Body className="p-3">
                              <div className="d-flex justify-content-between align-items-start mb-2">
                                <h6 className="mb-1 fw-semibold">{key}</h6>
                                <small className="text-muted">
                                  {formatTimestamp(dataInfo.timestamp)}
                                </small>
                              </div>
                              <Badge variant="outline-secondary" className="mb-2">
                                                                Source: {dataInfo.sourceAgent || 'Unknown'}
                              </Badge>
                              {dataInfo.metadata && Object.keys(dataInfo.metadata).length > 0 && (
                                <div className="small text-muted mt-2">
                                  <strong>Metadata:</strong>
                                  <div className="ms-2">
                                    {Object.entries(dataInfo.metadata)
                                      .slice(0, 3)
                                      .map(([metaKey, metaValue]) => (
                                        <div key={metaKey} className="text-truncate">
                                          <span className="text-capitalize">{metaKey}:</span> {renderDataValue(metaValue)}
                                        </div>
                                      ))}
                                  </div>
                                </div>
                              )}
                            </Card.Body>
                          </Card>
                        </motion.div>
                      ))
                    )}
                  </AnimatePresence>
                </div>
              </Card.Body>
            </Card>
          </motion.div>
        </Col>
      </Row>

      <Row className="g-4 mt-2">
        {/* Agent Recommendations */}
        <Col lg={8}>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <Card className="glass-effect">
              <Card.Header className="bg-transparent border-0 pb-0">
                <div className="d-flex align-items-center gap-2">
                  <span className="fs-5">💡</span>
                  <h5 className="mb-0">Agent Recommendations</h5>
                  <Badge bg="warning" className="ms-auto">
                    {Object.values(agentRecommendations).flat().length}
                  </Badge>
                </div>
              </Card.Header>
              <Card.Body className="pt-3">
                <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                  <AnimatePresence>
                    {Object.keys(agentRecommendations).length === 0 ? (
                      <Alert variant="info" className="text-center">
                        <span className="fs-4 d-block mb-2">💡</span>
                                                No recommendations available
                      </Alert>
                    ) : (
                      Object.entries(agentRecommendations).map(([agentId, recommendations]) => (
                        <div key={agentId} className="mb-4">
                          <h6 className="border-bottom pb-2 mb-3 text-muted">{agentId}</h6>
                          {recommendations.map((rec, index) => (
                            <motion.div
                              key={rec.id}
                              initial={{ opacity: 0, x: -20 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ delay: index * 0.1 }}
                              className="mb-3"
                            >
                              <Card className="border-start border-warning border-4 hover-card">
                                <Card.Body className="p-3">
                                  <div className="d-flex justify-content-between align-items-start">
                                    <div className="flex-grow-1">
                                      <h6 className="mb-1 fw-semibold">{rec.title}</h6>
                                      <p className="mb-2 text-muted small">{rec.description}</p>
                                      <div className="d-flex gap-2 align-items-center">
                                        <Badge bg={rec.priority === 'high' ? 'danger' : rec.priority === 'medium' ? 'warning' : 'secondary'}>
                                          {rec.priority} priority
                                        </Badge>
                                        {rec.estimatedTime && (
                                          <Badge variant="outline-info">
                                                                                        ~{rec.estimatedTime}
                                          </Badge>
                                        )}
                                      </div>
                                    </div>
                                    <Button
                                      size="sm"
                                      variant="outline-primary"
                                      onClick={() => executeRecommendation(rec, agentId)}
                                      disabled={loading}
                                      className="ms-3"
                                    >
                                      {loading ? <Spinner size="sm" /> : 'Execute'}
                                    </Button>
                                  </div>
                                </Card.Body>
                              </Card>
                            </motion.div>
                          ))}
                        </div>
                      ))
                    )}
                  </AnimatePresence>
                </div>
              </Card.Body>
            </Card>
          </motion.div>
        </Col>

        {/* Collaborative Workflows */}
        <Col lg={4}>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <Card className="glass-effect">
              <Card.Header className="bg-transparent border-0 pb-0">
                <div className="d-flex align-items-center gap-2">
                  <span className="fs-5">🔄</span>
                  <h5 className="mb-0">Active Workflows</h5>
                  <Badge bg="info" className="ms-auto">
                    {collaborativeWorkflows?.length || 0}
                  </Badge>
                </div>
              </Card.Header>
              <Card.Body className="pt-3">
                <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                  <AnimatePresence>
                    {(!collaborativeWorkflows || collaborativeWorkflows.length === 0) ? (
                      <Alert variant="info" className="text-center">
                        <span className="fs-4 d-block mb-2">🔄</span>
                                                No active workflows
                      </Alert>
                    ) : (
                      collaborativeWorkflows.map((workflow, index) => (
                        <motion.div
                          key={workflow.id}
                          initial={{ opacity: 0, y: 20 }}
                          animate={{ opacity: 1, y: 0 }}
                          exit={{ opacity: 0, y: -20 }}
                          transition={{ delay: index * 0.1 }}
                          className="mb-3"
                        >
                          <Card className="border-start border-info border-4 hover-card">
                            <Card.Body className="p-3">
                              <h6 className="mb-1 fw-semibold">{workflow.name}</h6>
                              <p className="mb-2 small text-muted">{workflow.description}</p>
                              <div className="d-flex gap-2 mb-2">
                                <Badge bg={workflow.status === 'active' ? 'success' : workflow.status === 'pending' ? 'warning' : 'secondary'}>
                                  {workflow.status}
                                </Badge>
                                {workflow.progress && (
                                  <Badge variant="outline-info">
                                    {workflow.progress}%
                                  </Badge>
                                )}
                              </div>
                              <small className="text-muted">
                                                                Created: {formatTimestamp(workflow.createdAt)}
                              </small>
                            </Card.Body>
                          </Card>
                        </motion.div>
                      ))
                    )}
                  </AnimatePresence>
                </div>
              </Card.Body>
            </Card>
          </motion.div>
        </Col>
      </Row>

      {/* Create Workflow Modal */}
      <Modal
        show={showWorkflowModal}
        onHide={() => setShowWorkflowModal(false)}
        size="lg"
        centered
        backdrop="static"
      >
        <Modal.Header closeButton className="border-0">
          <Modal.Title className="d-flex align-items-center gap-2">
            <span className="fs-5">🔄</span>
                        Create Collaborative Workflow
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Workflow Name</Form.Label>
              <Form.Control
                type="text"
                value={workflowData.name}
                onChange={(e) => setWorkflowData({ ...workflowData, name: e.target.value })}
                placeholder="e.g., Stock Analysis to Chart Visualization"
                className="form-control-lg"
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                value={workflowData.description}
                onChange={(e) => setWorkflowData({ ...workflowData, description: e.target.value })}
                placeholder="Describe the workflow and expected outputs..."
              />
            </Form.Group>
            <Alert variant="info" className="d-flex align-items-start gap-3">
              <span className="fs-5">💡</span>
              <div>
                <strong>Tip:</strong> This workflow will automatically coordinate between agents based on data availability and context.
              </div>
            </Alert>
          </Form>
        </Modal.Body>
        <Modal.Footer className="border-0">
          <Button
            variant="outline-secondary"
            onClick={() => setShowWorkflowModal(false)}
            disabled={loading}
          >
                        Cancel
          </Button>
          <Button
            variant="primary"
            onClick={handleCreateWorkflow}
            disabled={!workflowData.name.trim() || !workflowData.description.trim() || loading}
          >
            {loading ? (
              <>
                <Spinner size="sm" className="me-2" />
                                Creating...
              </>
            ) : (
              'Create Workflow'
            )}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}