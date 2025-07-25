import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Badge, Button, Modal, Form, Alert, Dropdown, ButtonGroup } from 'react-bootstrap';
import { useAppContext } from '../context/AppContext';

export default function ContextManager() {
    const {
        agentContexts,
        sharedData,
        agentRecommendations,
        collaborativeWorkflows,
        setAgentContext,
        updateSharedData,
        createCollaborativeWorkflow,
        generateAgentRecommendations,
        suggestChartTypes,
        clearContext
    } = useAppContext();

    const [showWorkflowModal, setShowWorkflowModal] = useState(false);
    const [selectedAgent, setSelectedAgent] = useState(null);
    const [workflowData, setWorkflowData] = useState({
        name: '',
        description: '',
        agents: [],
        dataFlow: []
    });

    const formatTimestamp = (timestamp) => {
        return new Date(timestamp).toLocaleString();
    };

    const getDataTypeBadge = (dataType) => {
        const variants = {
            'stock-analysis': 'primary',
            'financial-data': 'success',
            'market-sentiment': 'warning',
            'technical-indicators': 'info',
            'chart-data': 'secondary'
        };
        return variants[dataType] || 'outline-dark';
    };

    const handleCreateWorkflow = () => {
        const workflow = createCollaborativeWorkflow(workflowData);
        setShowWorkflowModal(false);
        setWorkflowData({ name: '', description: '', agents: [], dataFlow: [] });
    };

    const executeRecommendation = (recommendation, agentId) => {
        switch (recommendation.action) {
            case 'create-chart':
                // Trigger chart creation with available data
                const stockData = sharedData['stockData'];
                if (stockData) {
                    const chartSuggestions = suggestChartTypes('time-series', stockData.data);
                    updateSharedData('chartSuggestions', chartSuggestions, 'context-manager', {
                        sourceRecommendation: recommendation.id,
                        targetAgent: agentId
                    });
                }
                break;
            case 'assess-risk':
                // Trigger risk assessment workflow
                const financialData = sharedData['financialMetrics'];
                if (financialData) {
                    setAgentContext('risk-assessor', {
                        dataType: 'risk-analysis',
                        inputData: financialData.data,
                        analysisType: 'comprehensive'
                    });
                }
                break;
            default:
                console.log('Unknown recommendation action:', recommendation.action);
        }
    };

    return (
        <div className="context-manager p-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>🔄 Agent Context Manager</h2>
                <div>
                    <Button
                        variant="outline-primary"
                        className="me-2"
                        onClick={() => setShowWorkflowModal(true)}
                    >
                        ➕ Create Workflow
                    </Button>
                    <Button
                        variant="outline-secondary"
                        onClick={() => clearContext()}
                    >
                        🗑️ Clear All Context
                    </Button>
                </div>
            </div>

            <Row>
                {/* Agent Contexts */}
                <Col md={6}>
                    <Card className="mb-4">
                        <Card.Header>
                            <h5>🤖 Agent Contexts</h5>
                        </Card.Header>
                        <Card.Body>
                            {Object.keys(agentContexts).length === 0 ? (
                                <Alert variant="info">No active agent contexts</Alert>
                            ) : (
                                Object.entries(agentContexts).map(([agentId, context]) => (
                                    <Card key={agentId} className="mb-3 border-start border-primary border-3">
                                        <Card.Body className="p-3">
                                            <div className="d-flex justify-content-between align-items-start mb-2">
                                                <h6 className="mb-1">{agentId}</h6>
                                                <small className="text-muted">
                                                    {formatTimestamp(context.lastUpdated)}
                                                </small>
                                            </div>
                                            {context.dataType && (
                                                <Badge bg={getDataTypeBadge(context.dataType)} className="mb-2">
                                                    {context.dataType}
                                                </Badge>
                                            )}
                                            <div className="small text-muted">
                                                {Object.entries(context)
                                                    .filter(([key]) => key !== 'lastUpdated')
                                                    .map(([key, value]) => (
                                                        <div key={key}>
                                                            <strong>{key}:</strong> {JSON.stringify(value)}
                                                        </div>
                                                    ))
                                                }
                                            </div>
                                        </Card.Body>
                                    </Card>
                                ))
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Shared Data */}
                <Col md={6}>
                    <Card className="mb-4">
                        <Card.Header>
                            <h5>📊 Shared Data</h5>
                        </Card.Header>
                        <Card.Body>
                            {Object.keys(sharedData).length === 0 ? (
                                <Alert variant="info">No shared data available</Alert>
                            ) : (
                                Object.entries(sharedData).map(([key, dataInfo]) => (
                                    <Card key={key} className="mb-3 border-start border-success border-3">
                                        <Card.Body className="p-3">
                                            <div className="d-flex justify-content-between align-items-start mb-2">
                                                <h6 className="mb-1">{key}</h6>
                                                <small className="text-muted">
                                                    {formatTimestamp(dataInfo.timestamp)}
                                                </small>
                                            </div>
                                            <Badge bg="outline-secondary" className="mb-2">
                                                Source: {dataInfo.sourceAgent}
                                            </Badge>
                                            {dataInfo.metadata && Object.keys(dataInfo.metadata).length > 0 && (
                                                <div className="small text-muted mt-2">
                                                    <strong>Metadata:</strong>
                                                    {Object.entries(dataInfo.metadata).map(([metaKey, metaValue]) => (
                                                        <div key={metaKey} className="ms-2">
                                                            {metaKey}: {JSON.stringify(metaValue)}
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </Card.Body>
                                    </Card>
                                ))
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Row>
                {/* Agent Recommendations */}
                <Col md={8}>
                    <Card className="mb-4">
                        <Card.Header>
                            <h5>💡 Agent Recommendations</h5>
                        </Card.Header>
                        <Card.Body>
                            {Object.keys(agentRecommendations).length === 0 ? (
                                <Alert variant="info">No recommendations available</Alert>
                            ) : (
                                Object.entries(agentRecommendations).map(([agentId, recommendations]) => (
                                    <div key={agentId} className="mb-4">
                                        <h6 className="border-bottom pb-2">{agentId}</h6>
                                        {recommendations.map((rec) => (
                                            <Card key={rec.id} className="mb-2 border-start border-warning border-3">
                                                <Card.Body className="p-3">
                                                    <div className="d-flex justify-content-between align-items-start">
                                                        <div>
                                                            <h6 className="mb-1">{rec.title}</h6>
                                                            <p className="mb-2 text-muted small">{rec.description}</p>
                                                            <Badge bg={rec.priority === 'high' ? 'danger' : rec.priority === 'medium' ? 'warning' : 'secondary'}>
                                                                {rec.priority} priority
                                                            </Badge>
                                                        </div>
                                                        <Button
                                                            size="sm"
                                                            variant="outline-primary"
                                                            onClick={() => executeRecommendation(rec, agentId)}
                                                        >
                                                            Execute
                                                        </Button>
                                                    </div>
                                                </Card.Body>
                                            </Card>
                                        ))}
                                    </div>
                                ))
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Collaborative Workflows */}
                <Col md={4}>
                    <Card className="mb-4">
                        <Card.Header>
                            <h5>🔄 Active Workflows</h5>
                        </Card.Header>
                        <Card.Body>
                            {collaborativeWorkflows.length === 0 ? (
                                <Alert variant="info">No active workflows</Alert>
                            ) : (
                                collaborativeWorkflows.map((workflow) => (
                                    <Card key={workflow.id} className="mb-3 border-start border-info border-3">
                                        <Card.Body className="p-3">
                                            <h6 className="mb-1">{workflow.name}</h6>
                                            <p className="mb-2 small text-muted">{workflow.description}</p>
                                            <Badge bg="info" className="mb-2">{workflow.status}</Badge>
                                            <div className="small text-muted">
                                                Created: {formatTimestamp(workflow.createdAt)}
                                            </div>
                                        </Card.Body>
                                    </Card>
                                ))
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Create Workflow Modal */}
            <Modal show={showWorkflowModal} onHide={() => setShowWorkflowModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Create Collaborative Workflow</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Workflow Name</Form.Label>
                            <Form.Control
                                type="text"
                                value={workflowData.name}
                                onChange={(e) => setWorkflowData({...workflowData, name: e.target.value})}
                                placeholder="e.g., Stock Analysis to Chart Visualization"
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Description</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                value={workflowData.description}
                                onChange={(e) => setWorkflowData({...workflowData, description: e.target.value})}
                                placeholder="Describe the workflow and expected outputs"
                            />
                        </Form.Group>
                        <Alert variant="info">
                            <small>
                                This workflow will automatically coordinate between agents based on data availability and context.
                            </small>
                        </Alert>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowWorkflowModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleCreateWorkflow}
                        disabled={!workflowData.name || !workflowData.description}
                    >
                        Create Workflow
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
