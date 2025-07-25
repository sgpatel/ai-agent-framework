import React, { useEffect, useState } from 'react';
import { taskApi } from '../services/api';
import { Card, Spinner } from 'react-bootstrap';
import JsonView from '@uiw/react-json-view';

export default function SystemMetrics() {
    const [metrics, setMetrics] = useState(null);

    useEffect(() => {
        taskApi.getMetrics().then(res => setMetrics(res.data));
    }, []);

    if (!metrics) return <Spinner animation="border" />;

    return (
        <Card>
            <Card.Header>System Metrics</Card.Header>
            <Card.Body>
                <JsonView value={metrics} />
            </Card.Body>
        </Card>
    );
}
