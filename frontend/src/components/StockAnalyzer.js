import React, { useState, useEffect } from 'react';
import { Form, Button, Card, Row, Col, Alert, Badge, Nav, Tab, Spinner } from 'react-bootstrap';
import { useAppContext } from '../context/AppContext';
import { stockApi } from '../services/api';
import websocketService from '../services/websocket';
import AdvancedStockChart from './AdvancedStockChart';
import TechnicalIndicatorsPanel from './TechnicalIndicatorsPanel';
import AdvancedAnalysisPanel from './AdvancedAnalysisPanel';
import AdvancedChartDrawing from './AdvancedChartDrawing';
import Select from 'react-select';
import moment from 'moment';

export default function StockAnalyzer() {
  const {
    submitTask,
    loading,
    setAgentContext,
    updateSharedData,
    getSharedData,
    agentRecommendations,
    suggestChartTypes,
  } = useAppContext();

  const [symbol, setSymbol] = useState('TCS');
  const [period, setPeriod] = useState('1y');
  const [indicators, setIndicators] = useState('sma,rsi');
  const [analysisType, setAnalysisType] = useState('all');
  const [result, setResult] = useState(null);

  // Enhanced state for advanced features
  const [realTimeQuote, setRealTimeQuote] = useState(null);
  const [historicalData, setHistoricalData] = useState([]);
  const [technicalIndicators, setTechnicalIndicators] = useState(null);
  const [tradingSignal, setTradingSignal] = useState(null);
  const [companyInfo, setCompanyInfo] = useState(null);
  const [marketStatus, setMarketStatus] = useState(null);
  const [completeAnalysis, setCompleteAnalysis] = useState(null);
  const [marketComparison, setMarketComparison] = useState(null);
  const [watchlist, setWatchlist] = useState([
    'AAPL',
    'GOOGL',
    'MSFT',
    'TSLA',
    'AMZN',
    'NVDA',
    'META',
  ]);
  const [selectedWatchlistStock, setSelectedWatchlistStock] = useState(null);
  const [loadingData, setLoadingData] = useState(false);
  const [error, setError] = useState(null);
  const [isRealTimeEnabled, setIsRealTimeEnabled] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');
  const [benchmarkSymbol, setBenchmarkSymbol] = useState('SPY');
  const [detectedPatterns, setDetectedPatterns] = useState(null);

  // Enhanced state for context sharing
  const [contextData, setContextData] = useState(null);
  const [availableChartTypes, setAvailableChartTypes] = useState([]);
  const [recommendations, setRecommendations] = useState([]);

  // Initialize WebSocket connection (disabled for now)
  useEffect(() => {
    // WebSocket connection disabled to prevent 404 errors
    console.log('WebSocket connection disabled');
  }, []);

  // Load initial data when symbol changes
  useEffect(() => {
    if (symbol) {
      loadStockData(symbol);
    }
  }, [symbol]);

  // Real-time data subscription (disabled)
  useEffect(() => {
    // Real-time subscription disabled
  }, [isRealTimeEnabled, symbol]);

  // New useEffect for handling context and recommendations
  useEffect(() => {
    // Listen for recommendations from other agents
    const stockRecommendations = agentRecommendations['stock-analyzer'] || [];
    setRecommendations(stockRecommendations);
  }, [agentRecommendations]);

  // Enhanced loadStockData function
  const loadStockData = async stockSymbol => {
    setLoadingData(true);
    setError(null);

    try {
      const [
        quoteRes,
        historyRes,
        indicatorsRes,
        signalRes,
        infoRes,
        marketRes,
        completeRes,
        comparisonRes,
      ] = await Promise.all([
        stockApi.getRealTimeQuote(stockSymbol),
        stockApi.getAdvancedHistoricalData(stockSymbol, 'daily', 'compact'),
        stockApi.getTechnicalIndicators(stockSymbol),
        stockApi.getTradingSignal(stockSymbol),
        stockApi.getCompanyInfo(stockSymbol),
        stockApi.getMarketStatus(),
        stockApi.getCompleteAnalysis(stockSymbol),
        stockApi.compareWithMarket(stockSymbol, benchmarkSymbol),
      ]);

      const stockData = {
        symbol: stockSymbol,
        quote: quoteRes.data,
        historical: historyRes.data,
        indicators: indicatorsRes.data,
        signal: signalRes.data,
        companyInfo: infoRes.data,
        analysis: completeRes.data,
        comparison: comparisonRes.data,
      };

      setRealTimeQuote(quoteRes.data);
      setHistoricalData(historyRes.data);
      setTechnicalIndicators(indicatorsRes.data);
      setTradingSignal(signalRes.data);
      setCompanyInfo(infoRes.data);
      setMarketStatus(marketRes.data);
      setCompleteAnalysis(completeRes.data);
      setMarketComparison(comparisonRes.data);

      // Share context with other agents
      setAgentContext('stock-analyzer', {
        dataType: 'stock-analysis',
        currentSymbol: stockSymbol,
        lastAnalysis: new Date().toISOString(),
        hasHistoricalData: true,
        hasTechnicalIndicators: true,
        tradingSignal: signalRes.data?.signal,
      });

      // Update shared data for other agents to use
      updateSharedData('stockData', stockData, 'stock-analyzer', {
        dataType: 'time-series',
        recordCount: historyRes.data?.length || 0,
        indicators: Object.keys(indicatorsRes.data || {}),
        canVisualize: true,
      });

      // Generate chart type suggestions
      const chartSuggestions = suggestChartTypes('time-series', stockData);
      setAvailableChartTypes(chartSuggestions);

      // Update shared chart suggestions
      updateSharedData('chartSuggestions', chartSuggestions, 'stock-analyzer', {
        sourceData: 'stockData',
        recommendedFor: ['chart-visualizer', 'technical-analyzer'],
      });

      setContextData(stockData);
    } catch (error) {
      console.error('Failed to load stock data:', error);
      setError('Failed to load stock data: ' + error.message);
    } finally {
      setLoadingData(false);
    }
  };

  // Function to trigger chart creation workflow
  const triggerChartCreation = chartType => {
    if (contextData) {
      setAgentContext('chart-visualizer', {
        dataType: 'chart-creation',
        sourceData: 'stockData',
        chartType: chartType,
        symbol: symbol,
        requestedBy: 'stock-analyzer',
      });

      updateSharedData(
        'chartRequest',
        {
          type: chartType,
          data: contextData,
          symbol: symbol,
          timestamp: new Date().toISOString(),
        },
        'stock-analyzer',
        {
          priority: 'high',
          targetAgent: 'chart-visualizer',
        },
      );
    }
  };

  // Function to share analysis with other agents
  const shareAnalysisWithAgents = () => {
    if (completeAnalysis) {
      // Share with risk assessment agent
      setAgentContext('risk-assessor', {
        dataType: 'financial-data',
        sourceAnalysis: completeAnalysis,
        symbol: symbol,
        riskFactors: completeAnalysis.risks || [],
      });

      updateSharedData(
        'financialMetrics',
        {
          symbol: symbol,
          metrics: completeAnalysis,
          volatility: completeAnalysis.volatility,
          trends: completeAnalysis.trends,
        },
        'stock-analyzer',
        {
          analysisType: 'comprehensive',
          suitable_for: ['risk-assessment', 'portfolio-optimization'],
        },
      );
    }
  };

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      const res = await submitTask({
        type: 'STOCK_ANALYSIS',
        description: `Advanced analysis of ${symbol} stock`,
        parameters: { symbol, period, indicators, analysis_type: analysisType },
      });
      setResult(res);
    } catch (error) {
      console.error('Analysis failed:', error);
      setError('Analysis failed: ' + error.message);
    }
  };

  const addToWatchlist = async () => {
    try {
      await stockApi.addToWatchlist(symbol);
      if (!watchlist.includes(symbol)) {
        setWatchlist([...watchlist, symbol]);
      }
    } catch (error) {
      console.error('Failed to add to watchlist:', error);
    }
  };

  const toggleRealTime = () => {
    setIsRealTimeEnabled(!isRealTimeEnabled);
  };

  const renderSignal = signal => {
    if (!signal) return null;
    const variant =
      signal.signal === 'BUY' || signal.signal === 'STRONG_BUY'
        ? 'success'
        : signal.signal === 'SELL' || signal.signal === 'STRONG_SELL'
          ? 'danger'
          : 'warning';
    return <Badge bg={variant}>{signal.signal.replace('_', ' ')}</Badge>;
  };

  const watchlistOptions = watchlist.map(stock => ({
    value: stock,
    label: stock,
  }));

  return (
    <>
      <Row className='mb-4'>
        <Col>
          <h2>🚀 Advanced Stock Analyzer</h2>
          {marketStatus && (
            <Badge bg={marketStatus.isOpen ? 'success' : 'secondary'} className='mb-3'>
              Market {marketStatus.isOpen ? 'Open' : 'Closed'}
            </Badge>
          )}
        </Col>
        <Col xs='auto'>
          <Button
            variant={isRealTimeEnabled ? 'success' : 'outline-secondary'}
            onClick={toggleRealTime}
            disabled={true}
            className='me-2'
          >
            {isRealTimeEnabled ? '🟢 Real-time ON' : '⏸️ Real-time OFF'}
          </Button>
          <Button variant='outline-primary' onClick={() => loadStockData(symbol)}>
            🔄 Refresh
          </Button>
        </Col>
      </Row>

      {error && (
        <Alert variant='danger' dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSubmit} className='mb-4'>
        <Row>
          <Col md={3}>
            <Form.Group className='mb-3'>
              <Form.Label>Stock Symbol</Form.Label>
              <Form.Control
                value={symbol}
                onChange={e => setSymbol(e.target.value.toUpperCase())}
                placeholder='AAPL'
                required
              />
            </Form.Group>
          </Col>
          <Col md={3}>
            <Form.Group className='mb-3'>
              <Form.Label>Watchlist</Form.Label>
              <Select
                options={watchlistOptions}
                value={selectedWatchlistStock}
                onChange={option => {
                  setSelectedWatchlistStock(option);
                  if (option) setSymbol(option.value);
                }}
                placeholder='Select from watchlist...'
                isClearable
              />
            </Form.Group>
          </Col>
          <Col md={2}>
            <Form.Group className='mb-3'>
              <Form.Label>Benchmark</Form.Label>
              <Form.Select
                value={benchmarkSymbol}
                onChange={e => setBenchmarkSymbol(e.target.value)}
              >
                <option value='SPY'>S&P 500 (SPY)</option>
                <option value='QQQ'>NASDAQ (QQQ)</option>
                <option value='DIA'>Dow Jones (DIA)</option>
                <option value='IWM'>Russell 2000 (IWM)</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={2}>
            <Form.Group className='mb-3'>
              <Form.Label>Analysis Type</Form.Label>
              <Form.Select value={analysisType} onChange={e => setAnalysisType(e.target.value)}>
                <option value='all'>Complete Analysis</option>
                <option value='technical'>Technical Only</option>
                <option value='fundamental'>Fundamental Only</option>
                <option value='sentiment'>Sentiment Only</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={2} className='d-flex align-items-end'>
            <div className='mb-3 w-100'>
              <Button type='submit' disabled={loading || loadingData} className='w-100'>
                {loadingData ? <Spinner size='sm' /> : '🔍 Analyze'}
              </Button>
            </div>
          </Col>
        </Row>
      </Form>

      {/* Enhanced real-time quote display */}
      {realTimeQuote && (
        <Card className='mb-4'>
          <Card.Body>
            <Row className='align-items-center'>
              <Col md={6}>
                <h4 className='mb-1'>
                  {realTimeQuote.symbol} - ${realTimeQuote.price.toFixed(2)}
                  <Badge bg={realTimeQuote.change >= 0 ? 'success' : 'danger'} className='ms-2'>
                    {realTimeQuote.change >= 0 ? '+' : ''}
                    {realTimeQuote.change.toFixed(2)} ({realTimeQuote.changePercent.toFixed(2)}%)
                  </Badge>
                </h4>
                <small className='text-muted'>
                  Last updated:{' '}
                  {realTimeQuote.timestamp
                    ? moment(realTimeQuote.timestamp).format('HH:mm:ss')
                    : moment().format('HH:mm:ss')}
                </small>
              </Col>
              <Col md={3}>
                {renderSignal(tradingSignal)}
                {marketComparison && marketComparison.outperforming !== undefined && (
                  <Badge
                    bg={marketComparison.outperforming ? 'success' : 'danger'}
                    className='ms-2'
                  >
                    {marketComparison.outperforming ? '📈 Outperforming' : '📉 Underperforming'}{' '}
                    {benchmarkSymbol}
                  </Badge>
                )}
              </Col>
              <Col md={3} className='text-end'>
                <Button
                  variant='outline-primary'
                  size='sm'
                  onClick={addToWatchlist}
                  className='me-2'
                >
                  ⭐ Add to Watchlist
                </Button>
                <Button variant='outline-success' size='sm'>
                  🔔 Create Alert
                </Button>
              </Col>
            </Row>
            <Row className='mt-3'>
              <Col>
                <small>Open: ${realTimeQuote.open?.toFixed(2)}</small>
              </Col>
              <Col>
                <small>High: ${realTimeQuote.high?.toFixed(2)}</small>
              </Col>
              <Col>
                <small>Low: ${realTimeQuote.low?.toFixed(2)}</small>
              </Col>
              <Col>
                <small>Volume: {realTimeQuote.volume?.toLocaleString()}</small>
              </Col>
              <Col>
                <small>Prev Close: ${realTimeQuote.previousClose?.toFixed(2)}</small>
              </Col>
            </Row>
          </Card.Body>
        </Card>
      )}

      {/* Enhanced tabbed interface with new advanced features */}
      <Tab.Container activeKey={activeTab} onSelect={setActiveTab}>
        <Nav variant='tabs' className='mb-3'>
          <Nav.Item>
            <Nav.Link eventKey='overview'>📊 Overview</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='chart'>📈 Advanced Chart</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='indicators'>🔢 Technical Indicators</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='patterns'>🔍 Patterns & Predictions</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='comparison'>⚖️ Market Comparison</Nav.Link>
          </Nav.Item>
        </Nav>

        <Tab.Content>
          <Tab.Pane eventKey='overview'>
            <AdvancedAnalysisPanel symbol={symbol} />

            {completeAnalysis && (
              <Row className='mt-4'>
                <Col lg={8}>
                  <Card>
                    <Card.Header>
                      <h6 className='mb-0'>📋 Analysis Summary</h6>
                    </Card.Header>
                    <Card.Body>
                      <div className='mb-3'>
                        <strong>AI Analysis Completed:</strong> {new Date().toLocaleString()}
                      </div>
                      {completeAnalysis.alerts && completeAnalysis.alerts.length > 0 && (
                        <Alert variant='info'>
                          <strong>Active Alerts:</strong>
                          <ul className='mb-0 mt-2'>
                            {completeAnalysis.alerts.map((alert, index) => (
                              <li key={index}>{alert}</li>
                            ))}
                          </ul>
                        </Alert>
                      )}
                    </Card.Body>
                  </Card>
                </Col>
                <Col lg={4}>
                  {companyInfo && (
                    <Card>
                      <Card.Header>
                        <h6 className='mb-0'>🏢 Company Info</h6>
                      </Card.Header>
                      <Card.Body>
                        <h6>{companyInfo.name}</h6>
                        <p>
                          <strong>Sector:</strong> {companyInfo.sector}
                        </p>
                        <p>
                          <strong>Industry:</strong> {companyInfo.industry}
                        </p>
                        <p>
                          <strong>Employees:</strong> {companyInfo.employees?.toLocaleString()}
                        </p>
                        <p className='small'>{companyInfo.description}</p>
                      </Card.Body>
                    </Card>
                  )}
                </Col>
              </Row>
            )}
          </Tab.Pane>

          <Tab.Pane eventKey='chart'>
            {historicalData.length > 0 ? (
              <AdvancedStockChart
                symbol={symbol}
                data={historicalData}
                indicators={technicalIndicators}
                signal={tradingSignal}
                realTimeQuote={realTimeQuote}
              />
            ) : (
              <Card>
                <Card.Body className='text-center'>
                  {loadingData ? <Spinner animation='border' /> : 'No chart data available'}
                </Card.Body>
              </Card>
            )}
          </Tab.Pane>

          <Tab.Pane eventKey='indicators'>
            <TechnicalIndicatorsPanel indicators={technicalIndicators} signal={tradingSignal} />
          </Tab.Pane>

          <Tab.Pane eventKey='patterns'>
            <AdvancedChartDrawing
              data={historicalData}
              patterns={detectedPatterns}
              onDrawingComplete={drawing => console.log('Drawing completed:', drawing)}
            />

            {detectedPatterns && (
              <div className='mt-4'>
                <h6>🔍 Detected Patterns</h6>
                <div className='row'>
                  {detectedPatterns.chart_patterns?.map((pattern, index) => (
                    <div key={index} className='col-md-4 mb-3'>
                      <div className='card'>
                        <div className='card-body'>
                          <h6 className='card-title'>{pattern.name}</h6>
                          <p className='card-text'>
                            <Badge
                              bg={
                                pattern.signal === 'BULLISH'
                                  ? 'success'
                                  : pattern.signal === 'BEARISH'
                                    ? 'danger'
                                    : 'warning'
                              }
                            >
                              {pattern.signal}
                            </Badge>
                            <br />
                            <small>Confidence: {(pattern.confidence * 100).toFixed(0)}%</small>
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </Tab.Pane>

          <Tab.Pane eventKey='comparison'>
            {marketComparison ? (
              <Row>
                <Col lg={6}>
                  <Card>
                    <Card.Header>
                      <h6 className='mb-0'>
                        📊 {symbol} vs {benchmarkSymbol}
                      </h6>
                    </Card.Header>
                    <Card.Body>
                      <div className='text-center mb-3'>
                        <h4>
                          Relative Performance:
                          <Badge
                            bg={marketComparison.relativePerformance >= 0 ? 'success' : 'danger'}
                            className='ms-2'
                          >
                            {marketComparison.relativePerformance >= 0 ? '+' : ''}
                            {marketComparison.relativePerformance?.toFixed(2)}%
                          </Badge>
                        </h4>
                        <p className='text-muted'>
                          {symbol} is{' '}
                          {marketComparison.outperforming ? 'outperforming' : 'underperforming'}{' '}
                          {benchmarkSymbol} today
                        </p>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
                <Col lg={6}>
                  <Card>
                    <Card.Header>
                      <h6 className='mb-0'>📈 Benchmark Comparison</h6>
                    </Card.Header>
                    <Card.Body>
                      <p>
                        Detailed comparison analysis with {benchmarkSymbol} benchmark will be
                        displayed here.
                      </p>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>
            ) : (
              <Card>
                <Card.Body className='text-center'>
                  {loadingData ? (
                    <Spinner animation='border' />
                  ) : (
                    'Market comparison data not available'
                  )}
                </Card.Body>
              </Card>
            )}
          </Tab.Pane>
        </Tab.Content>
      </Tab.Container>
    </>
  );
}
