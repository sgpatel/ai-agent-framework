import React from 'react';
import { useAppContext } from '../context/AppContext';
import { Row, Col, Card, Button, Spinner, Alert } from 'react-bootstrap';

export default function AgentDashboard() {
    const { agents, loading, error, loadAgents } = useAppContext();

    if (loading) return <Spinner animation="border" />;

    return (
        <>
            <h2>Agents</h2>
            <Button onClick={loadAgents}>Reload</Button>
            {error && <Alert variant="danger">{error}</Alert>}
            <Row className="mt-3">
                {agents.map(agent => (
                    <Col md={4} key={agent.name} className="mb-3">
                        <Card>
                            <Card.Body>
                                <Card.Title>{agent.name}</Card.Title>
                                <Card.Text>{agent.description}</Card.Text>
                                <Button disabled>{agent.status}</Button>
                            </Card.Body>
                        </Card>
                    </Col>
                ))}
            </Row>
        </>
    );
}
