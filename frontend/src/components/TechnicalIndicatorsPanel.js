import React from 'react';
import { Card, Row, Col, Badge, ProgressBar, Table } from 'react-bootstrap';

const TechnicalIndicatorsPanel = ({ indicators, signal }) => {
    if (!indicators) {
        return (
            <Card>
                <Card.Body>
                    <Card.Title>Technical Indicators</Card.Title>
                    <p className="text-muted">Loading indicators...</p>
                </Card.Body>
            </Card>
        );
    }

    const getRSIColor = (rsi) => {
        if (rsi < 30) return 'success'; // Oversold
        if (rsi > 70) return 'danger';  // Overbought
        return 'primary';
    };

    const getSignalVariant = (signalType) => {
        const variants = {
            'BUY': 'success',
            'STRONG_BUY': 'success',
            'SELL': 'danger',
            'STRONG_SELL': 'danger',
            'HOLD': 'warning'
        };
        return variants[signalType] || 'secondary';
    };

    const formatNumber = (num) => {
        return num ? num.toFixed(2) : 'N/A';
    };

    return (
        <Row>
            {/* Trading Signal Card */}
            <Col lg={4} className="mb-3">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">Trading Signal</h6>
                    </Card.Header>
                    <Card.Body>
                        {signal ? (
                            <>
                                <div className="text-center mb-3">
                                    <Badge
                                        bg={getSignalVariant(signal.signal)}
                                        className="p-2 fs-5"
                                    >
                                        {signal.signal.replace('_', ' ')}
                                    </Badge>
                                </div>
                                <div className="mb-2">
                                    <small className="text-muted">Confidence:</small>
                                    <ProgressBar
                                        now={signal.confidence * 100}
                                        label={`${(signal.confidence * 100).toFixed(0)}%`}
                                        variant={signal.confidence > 0.7 ? 'success' : signal.confidence > 0.4 ? 'warning' : 'danger'}
                                    />
                                </div>
                                <div className="mb-2">
                                    <small className="text-muted">Strength:</small>
                                    <Badge bg="info" className="ms-1">{signal.strength}</Badge>
                                </div>
                                {signal.targetPrice && (
                                    <div className="mb-2">
                                        <small className="text-muted">Target Price:</small>
                                        <span className="fw-bold ms-2">${formatNumber(signal.targetPrice)}</span>
                                    </div>
                                )}
                                {signal.stopLoss && (
                                    <div className="mb-2">
                                        <small className="text-muted">Stop Loss:</small>
                                        <span className="fw-bold ms-2">${formatNumber(signal.stopLoss)}</span>
                                    </div>
                                )}
                                {signal.riskRewardRatio && (
                                    <div className="mb-2">
                                        <small className="text-muted">Risk/Reward:</small>
                                        <span className="fw-bold ms-2">{formatNumber(signal.riskRewardRatio)}:1</span>
                                    </div>
                                )}
                                {signal.reason && (
                                    <div className="mt-3">
                                        <small className="text-muted">Reason:</small>
                                        <p className="small mt-1">{signal.reason}</p>
                                    </div>
                                )}
                            </>
                        ) : (
                            <p className="text-muted">No signal available</p>
                        )}
                    </Card.Body>
                </Card>
            </Col>

            {/* Momentum Indicators */}
            <Col lg={4} className="mb-3">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">Momentum Indicators</h6>
                    </Card.Header>
                    <Card.Body>
                        <Table size="sm" className="mb-0">
                            <tbody>
                                <tr>
                                    <td>RSI (14)</td>
                                    <td className="text-end">
                                        <Badge bg={getRSIColor(indicators.rsi)}>
                                            {formatNumber(indicators.rsi)}
                                        </Badge>
                                    </td>
                                </tr>
                                <tr>
                                    <td>MACD</td>
                                    <td className="text-end">
                                        <span className={indicators.macd > 0 ? 'text-success' : 'text-danger'}>
                                            {formatNumber(indicators.macd)}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>MACD Signal</td>
                                    <td className="text-end">{formatNumber(indicators.macdSignal)}</td>
                                </tr>
                                <tr>
                                    <td>MACD Histogram</td>
                                    <td className="text-end">
                                        <span className={indicators.macdHistogram > 0 ? 'text-success' : 'text-danger'}>
                                            {formatNumber(indicators.macdHistogram)}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Stochastic %K</td>
                                    <td className="text-end">{formatNumber(indicators.stochasticK)}</td>
                                </tr>
                                <tr>
                                    <td>Stochastic %D</td>
                                    <td className="text-end">{formatNumber(indicators.stochasticD)}</td>
                                </tr>
                            </tbody>
                        </Table>
                    </Card.Body>
                </Card>
            </Col>

            {/* Moving Averages & Volatility */}
            <Col lg={4} className="mb-3">
                <Card className="h-100">
                    <Card.Header>
                        <h6 className="mb-0">Moving Averages & Volatility</h6>
                    </Card.Header>
                    <Card.Body>
                        <Table size="sm" className="mb-3">
                            <tbody>
                                <tr>
                                    <td>SMA 20</td>
                                    <td className="text-end">{formatNumber(indicators.sma20)}</td>
                                </tr>
                                <tr>
                                    <td>SMA 50</td>
                                    <td className="text-end">{formatNumber(indicators.sma50)}</td>
                                </tr>
                                <tr>
                                    <td>SMA 200</td>
                                    <td className="text-end">{formatNumber(indicators.sma200)}</td>
                                </tr>
                                <tr>
                                    <td>EMA 12</td>
                                    <td className="text-end">{formatNumber(indicators.ema12)}</td>
                                </tr>
                                <tr>
                                    <td>EMA 26</td>
                                    <td className="text-end">{formatNumber(indicators.ema26)}</td>
                                </tr>
                            </tbody>
                        </Table>

                        <h6 className="fs-6 text-muted mb-2">Bollinger Bands</h6>
                        <Table size="sm" className="mb-3">
                            <tbody>
                                <tr>
                                    <td>Upper</td>
                                    <td className="text-end">{formatNumber(indicators.bollingerUpper)}</td>
                                </tr>
                                <tr>
                                    <td>Middle</td>
                                    <td className="text-end">{formatNumber(indicators.bollingerMiddle)}</td>
                                </tr>
                                <tr>
                                    <td>Lower</td>
                                    <td className="text-end">{formatNumber(indicators.bollingerLower)}</td>
                                </tr>
                            </tbody>
                        </Table>

                        <div>
                            <small className="text-muted">ATR (14):</small>
                            <span className="fw-bold ms-2">{formatNumber(indicators.atr)}</span>
                        </div>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    );
};

export default TechnicalIndicatorsPanel;
