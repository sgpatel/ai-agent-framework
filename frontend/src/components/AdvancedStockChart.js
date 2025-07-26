import React, { useEffect, useRef, useState } from 'react';
import { createChart } from 'lightweight-charts';
import { Card, Row, Col, Badge, Spinner } from 'react-bootstrap';

const AdvancedStockChart = ({ symbol, data, indicators, signal, realTimeQuote }) => {
  const chartContainerRef = useRef();
  const chartRef = useRef();
  const candlestickSeriesRef = useRef();
  const volumeSeriesRef = useRef();
  const smaSeriesRef = useRef();
  const emaSeriesRef = useRef();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!chartContainerRef.current || !data || data.length === 0) return;

    // Create chart
    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 500,
      layout: {
        background: { color: '#ffffff' },
        textColor: '#333',
      },
      grid: {
        vertLines: { color: '#f0f0f0' },
        horzLines: { color: '#f0f0f0' },
      },
      crosshair: {
        mode: 1,
      },
      rightPriceScale: {
        borderColor: '#cccccc',
      },
      timeScale: {
        borderColor: '#cccccc',
        timeVisible: true,
        secondsVisible: false,
      },
    });

    // Add candlestick series
    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#00C851',
      downColor: '#ff4444',
      borderDownColor: '#ff4444',
      borderUpColor: '#00C851',
      wickDownColor: '#ff4444',
      wickUpColor: '#00C851',
    });

    // Add volume series
    const volumeSeries = chart.addHistogramSeries({
      color: '#26a69a',
      priceFormat: {
        type: 'volume',
      },
      priceScaleId: 'volume',
      scaleMargins: {
        top: 0.8,
        bottom: 0,
      },
    });

    // Add moving average series
    const smaSeries = chart.addLineSeries({
      color: '#2196F3',
      lineWidth: 2,
      title: 'SMA 20',
    });

    const emaSeries = chart.addLineSeries({
      color: '#FF9800',
      lineWidth: 2,
      title: 'EMA 12',
    });

    // Process and set data
    const processedData = data.map(item => ({
      time: item.date,
      open: parseFloat(item.open),
      high: parseFloat(item.high),
      low: parseFloat(item.low),
      close: parseFloat(item.close),
    }));

    const volumeData = data.map(item => ({
      time: item.date,
      value: parseFloat(item.volume),
      color: parseFloat(item.close) >= parseFloat(item.open) ? '#00C851' : '#ff4444',
    }));

    candlestickSeries.setData(processedData);
    volumeSeries.setData(volumeData);

    // Add indicators if available
    if (indicators && indicators.sma20) {
      const smaData = data.map(item => ({
        time: item.date,
        value: indicators.sma20, // This would be dynamic in real implementation
      }));
      smaSeries.setData(smaData);
    }

    if (indicators && indicators.ema12) {
      const emaData = data.map(item => ({
        time: item.date,
        value: indicators.ema12, // This would be dynamic in real implementation
      }));
      emaSeries.setData(emaData);
    }

    // Store references
    chartRef.current = chart;
    candlestickSeriesRef.current = candlestickSeries;
    volumeSeriesRef.current = volumeSeries;
    smaSeriesRef.current = smaSeries;
    emaSeriesRef.current = emaSeries;

    setIsLoading(false);

    // Handle resize
    const handleResize = () => {
      chart.applyOptions({ width: chartContainerRef.current.clientWidth });
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
    };
  }, [data, indicators]);

  // Update real-time data
  useEffect(() => {
    if (realTimeQuote && candlestickSeriesRef.current) {
      const currentTime = new Date().toISOString().split('T')[0];
      candlestickSeriesRef.current.update({
        time: currentTime,
        open: realTimeQuote.open,
        high: realTimeQuote.high,
        low: realTimeQuote.low,
        close: realTimeQuote.price,
      });
    }
  }, [realTimeQuote]);

  const getSignalBadge = signal => {
    if (!signal) return null;

    const variants = {
      BUY: 'success',
      STRONG_BUY: 'success',
      SELL: 'danger',
      STRONG_SELL: 'danger',
      HOLD: 'warning',
    };

    return (
      <Badge bg={variants[signal.signal]} className='me-2'>
        {signal.signal.replace('_', ' ')} ({(signal.confidence * 100).toFixed(0)}%)
      </Badge>
    );
  };

  if (isLoading) {
    return (
      <Card>
        <Card.Body className='text-center'>
          <Spinner animation='border' />
          <p>Loading chart...</p>
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card>
      <Card.Header>
        <Row className='align-items-center'>
          <Col>
            <h5 className='mb-0'>{symbol} - Advanced Chart</h5>
          </Col>
          <Col xs='auto'>
            {getSignalBadge(signal)}
            {realTimeQuote && (
              <Badge bg={realTimeQuote.change >= 0 ? 'success' : 'danger'}>
                ${realTimeQuote.price.toFixed(2)} ({realTimeQuote.changePercent.toFixed(2)}%)
              </Badge>
            )}
          </Col>
        </Row>
      </Card.Header>
      <Card.Body>
        <div ref={chartContainerRef} style={{ width: '100%', height: '500px' }} />
        {signal && signal.reason && (
          <div className='mt-3'>
            <small className='text-muted'>
              <strong>Signal Reason:</strong> {signal.reason}
            </small>
          </div>
        )}
      </Card.Body>
    </Card>
  );
};

export default AdvancedStockChart;
