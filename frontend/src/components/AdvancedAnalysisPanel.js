import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Badge, Alert, Table, ProgressBar } from 'react-bootstrap';
import { stockApi } from '../services/api';

const AdvancedAnalysisPanel = ({ symbol }) => {
    const [patterns, setPatterns] = useState([]);
    const [riskMetrics, setRiskMetrics] = useState(null);
    const [prediction, setPrediction] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (symbol) {
            loadAdvancedAnalysis();
        }
    }, [symbol]);

    const loadAdvancedAnalysis = async () => {
        setLoading(true);
        setError(null);

        try {
            const [patternsRes, riskRes, predictionRes] = await Promise.all([
                stockApi.getTechnicalPatterns(symbol),
                stockApi.getRiskAssessment(symbol),
                stockApi.getPricePrediction(symbol)
            ]);

            setPatterns(patternsRes.data);
            setRiskMetrics(riskRes.data);
            setPrediction(predictionRes.data);
        } catch (err) {
            setError('Failed to load advanced analysis: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    const getPatternBadgeVariant = (signal) => {
        switch (signal) {
            case 'BULLISH': return 'success';
            case 'BEARISH': return 'danger';
            default: return 'warning';
        }
    };

    const getRiskLevelVariant = (level) => {
        switch (level) {
            case 'LOW': return 'success';
            case 'MEDIUM': return 'warning';
            case 'HIGH': return 'danger';
            case 'EXTREME': return 'dark';
            default: return 'secondary';
        }
    };

    const formatCurrency = (value) => {
        return value ? `$${value.toFixed(2)}` : 'N/A';
    };

    const formatPercentage = (value) => {
        return value ? `${(value * 100).toFixed(1)}%` : 'N/A';
    };

    if (loading) return <div>Loading advanced analysis...</div>;
    if (error) return <Alert variant="danger">{error}</Alert>;

    return (
        <Row>
            {/* Technical Patterns */}
            <Col lg={4} className="mb-4">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">🔍 Technical Patterns</h6>
                    </Card.Header>
                    <Card.Body>
                        {patterns.length > 0 ? (
                            patterns.map((pattern, index) => (
                                <div key={index} className="mb-3 p-2 border rounded">
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <strong>{pattern.type?.replace(/_/g, ' ')}</strong>
                                        <Badge bg={getPatternBadgeVariant(pattern.signal)}>
                                            {pattern.signal}
                                        </Badge>
                                    </div>
                                    <p className="small text-muted mb-2">{pattern.description}</p>
                                    <div className="mb-2">
                                        <small>Confidence:</small>
                                        <ProgressBar
                                            now={pattern.confidence * 100}
                                            variant={pattern.confidence > 0.7 ? 'success' : 'warning'}
                                            size="sm"
                                        />
                                    </div>
                                    {pattern.implications && pattern.implications.length > 0 && (
                                        <div>
                                            <small className="text-muted">Implications:</small>
                                            <ul className="small mb-0">
                                                {pattern.implications.map((implication, i) => (
                                                    <li key={i}>{implication}</li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            ))
                        ) : (
                            <p className="text-muted">No significant patterns detected</p>
                        )}
                    </Card.Body>
                </Card>
            </Col>

            {/* Risk Assessment */}
            <Col lg={4} className="mb-4">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">⚠️ Risk Assessment</h6>
                    </Card.Header>
                    <Card.Body>
                        {riskMetrics ? (
                            <>
                                <div className="text-center mb-3">
                                    <Badge
                                        bg={getRiskLevelVariant(riskMetrics.riskLevel)}
                                        className="p-2 fs-6"
                                    >
                                        {riskMetrics.riskLevel} RISK
                                    </Badge>
                                    <div className="mt-2">
                                        <small>Overall Risk Score:</small>
                                        <h5>{formatPercentage(riskMetrics.overallRisk)}</h5>
                                    </div>
                                </div>

                                <Table size="sm" className="mb-3">
                                    <tbody>
                                        <tr>
                                            <td>Volatility Risk</td>
                                            <td className="text-end">{formatPercentage(riskMetrics.volatilityRisk)}</td>
                                        </tr>
                                        <tr>
                                            <td>Liquidity Risk</td>
                                            <td className="text-end">{formatPercentage(riskMetrics.liquidityRisk)}</td>
                                        </tr>
                                        <tr>
                                            <td>Market Risk</td>
                                            <td className="text-end">{formatPercentage(riskMetrics.marketRisk)}</td>
                                        </tr>
                                        <tr>
                                            <td>Technical Risk</td>
                                            <td className="text-end">{formatPercentage(riskMetrics.technicalRisk)}</td>
                                        </tr>
                                    </tbody>
                                </Table>

                                {riskMetrics.riskFactors && riskMetrics.riskFactors.length > 0 && (
                                    <div>
                                        <h6 className="text-muted">Risk Factors:</h6>
                                        <ul className="small">
                                            {riskMetrics.riskFactors.map((factor, index) => (
                                                <li key={index}>{factor}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </>
                        ) : (
                            <p className="text-muted">Risk assessment not available</p>
                        )}
                    </Card.Body>
                </Card>
            </Col>

            {/* Price Prediction */}
            <Col lg={4} className="mb-4">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">🔮 Price Prediction</h6>
                    </Card.Header>
                    <Card.Body>
                        {prediction ? (
                            <>
                                <div className="text-center mb-3">
                                    <div className="mb-2">
                                        <strong>Current: {formatCurrency(prediction.currentPrice)}</strong>
                                    </div>
                                    <Badge
                                        bg={prediction.trend === 'BULLISH' ? 'success' :
                                            prediction.trend === 'BEARISH' ? 'danger' : 'warning'}
                                    >
                                        {prediction.trend} Trend
                                    </Badge>
                                    <div className="mt-1">
                                        <small>Strength: {formatPercentage(prediction.trendStrength)}</small>
                                    </div>
                                </div>

                                <Table size="sm" className="mb-3">
                                    <thead>
                                        <tr>
                                            <th>Timeframe</th>
                                            <th>Prediction</th>
                                            <th>Confidence</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.entries(prediction.predictions || {}).map(([timeframe, price]) => (
                                            <tr key={timeframe}>
                                                <td>{timeframe}</td>
                                                <td className={
                                                    price > prediction.currentPrice ? 'text-success' :
                                                    price < prediction.currentPrice ? 'text-danger' : ''
                                                }>
                                                    {formatCurrency(price)}
                                                </td>
                                                <td>
                                                    <small>{formatPercentage(prediction.confidence?.[timeframe])}</small>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>

                                {prediction.scenarioAnalysis && (
                                    <div>
                                        <h6 className="text-muted small">Scenario Analysis:</h6>
                                        {Object.entries(prediction.scenarioAnalysis).map(([scenario, probability]) => (
                                            <div key={scenario} className="d-flex justify-content-between small">
                                                <span>{scenario}</span>
                                                <span>{formatPercentage(probability)}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {prediction.predictionFactors && prediction.predictionFactors.length > 0 && (
                                    <div className="mt-3">
                                        <h6 className="text-muted small">Key Factors:</h6>
                                        <ul className="small mb-0">
                                            {prediction.predictionFactors.slice(0, 3).map((factor, index) => (
                                                <li key={index}>{factor}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </>
                        ) : (
                            <p className="text-muted">Price prediction not available</p>
                        )}
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    );
};

export default AdvancedAnalysisPanel;
