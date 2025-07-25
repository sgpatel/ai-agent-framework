import React, { useRef, useEffect, useState } from 'react';
import { Card, Button, ButtonGroup, Badge } from 'react-bootstrap';

export default function AdvancedChartDrawing({ data, patterns, onDrawingComplete }) {
    const canvasRef = useRef(null);
    const [drawingMode, setDrawingMode] = useState('none');
    const [drawings, setDrawings] = useState([]);
    const [isDrawing, setIsDrawing] = useState(false);
    const [currentDrawing, setCurrentDrawing] = useState(null);

    useEffect(() => {
        if (data && data.length > 0) {
            drawChart();
        }
    }, [data, drawings, patterns]);

    const drawChart = () => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const { width, height } = canvas;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        
        // Draw price chart
        drawPriceChart(ctx, width, height);
        
        // Draw patterns
        drawPatterns(ctx, width, height);
        
        // Draw user drawings
        drawUserDrawings(ctx, width, height);
    };

    const drawPriceChart = (ctx, width, height) => {
        if (!data || data.length === 0) return;

        const margin = 40;
        const chartWidth = width - 2 * margin;
        const chartHeight = height - 2 * margin;

        // Calculate price range
        const prices = data.map(d => d.close);
        const minPrice = Math.min(...prices);
        const maxPrice = Math.max(...prices);
        const priceRange = maxPrice - minPrice;

        // Draw price line
        ctx.strokeStyle = '#2196F3';
        ctx.lineWidth = 2;
        ctx.beginPath();

        data.forEach((point, index) => {
            const x = margin + (index / (data.length - 1)) * chartWidth;
            const y = margin + ((maxPrice - point.close) / priceRange) * chartHeight;
            
            if (index === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        
        ctx.stroke();

        // Draw candlesticks
        data.forEach((candle, index) => {
            const x = margin + (index / (data.length - 1)) * chartWidth;
            const openY = margin + ((maxPrice - candle.open) / priceRange) * chartHeight;
            const closeY = margin + ((maxPrice - candle.close) / priceRange) * chartHeight;
            const highY = margin + ((maxPrice - candle.high) / priceRange) * chartHeight;
            const lowY = margin + ((maxPrice - candle.low) / priceRange) * chartHeight;

            // Candlestick body
            const bodyHeight = Math.abs(closeY - openY);
            const bodyY = Math.min(openY, closeY);
            
            ctx.fillStyle = candle.close > candle.open ? '#4CAF50' : '#F44336';
            ctx.fillRect(x - 2, bodyY, 4, bodyHeight);

            // Candlestick wicks
            ctx.strokeStyle = '#666';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(x, highY);
            ctx.lineTo(x, lowY);
            ctx.stroke();
        });
    };

    const drawPatterns = (ctx, width, height) => {
        if (!patterns) return;

        const margin = 40;
        const chartWidth = width - 2 * margin;
        const chartHeight = height - 2 * margin;

        // Draw support and resistance levels
        if (patterns.support_resistance) {
            const { support, resistance } = patterns.support_resistance;
            const prices = data.map(d => d.close);
            const minPrice = Math.min(...prices);
            const maxPrice = Math.max(...prices);
            const priceRange = maxPrice - minPrice;

            // Support line
            const supportY = margin + ((maxPrice - support) / priceRange) * chartHeight;
            ctx.strokeStyle = '#4CAF50';
            ctx.lineWidth = 2;
            ctx.setLineDash([5, 5]);
            ctx.beginPath();
            ctx.moveTo(margin, supportY);
            ctx.lineTo(margin + chartWidth, supportY);
            ctx.stroke();

            // Resistance line
            const resistanceY = margin + ((maxPrice - resistance) / priceRange) * chartHeight;
            ctx.strokeStyle = '#F44336';
            ctx.beginPath();
            ctx.moveTo(margin, resistanceY);
            ctx.lineTo(margin + chartWidth, resistanceY);
            ctx.stroke();
            ctx.setLineDash([]);
        }

        // Draw Fibonacci levels
        if (patterns.fibonacci_levels) {
            const { levels } = patterns.fibonacci_levels;
            const prices = data.map(d => d.close);
            const minPrice = Math.min(...prices);
            const maxPrice = Math.max(...prices);
            const priceRange = maxPrice - minPrice;

            ctx.strokeStyle = '#FF9800';
            ctx.lineWidth = 1;
            ctx.setLineDash([3, 3]);

            Object.entries(levels).forEach(([level, price]) => {
                const y = margin + ((maxPrice - price) / priceRange) * chartHeight;
                ctx.beginPath();
                ctx.moveTo(margin, y);
                ctx.lineTo(margin + chartWidth, y);
                ctx.stroke();

                // Label
                ctx.fillStyle = '#FF9800';
                ctx.font = '12px Arial';
                ctx.fillText(`${level}%`, margin + chartWidth + 5, y + 4);
            });
            ctx.setLineDash([]);
        }

        // Draw trend lines
        if (patterns.trend_lines) {
            const { start_price, end_price, trend } = patterns.trend_lines;
            const prices = data.map(d => d.close);
            const minPrice = Math.min(...prices);
            const maxPrice = Math.max(...prices);
            const priceRange = maxPrice - minPrice;

            const startY = margin + ((maxPrice - start_price) / priceRange) * chartHeight;
            const endY = margin + ((maxPrice - end_price) / priceRange) * chartHeight;

            ctx.strokeStyle = trend === 'UPTREND' ? '#4CAF50' : trend === 'DOWNTREND' ? '#F44336' : '#FFC107';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(margin, startY);
            ctx.lineTo(margin + chartWidth, endY);
            ctx.stroke();
        }
    };

    const drawUserDrawings = (ctx, width, height) => {
        drawings.forEach(drawing => {
            ctx.strokeStyle = drawing.color || '#9C27B0';
            ctx.lineWidth = drawing.width || 2;

            switch (drawing.type) {
                case 'line':
                    ctx.beginPath();
                    ctx.moveTo(drawing.startX, drawing.startY);
                    ctx.lineTo(drawing.endX, drawing.endY);
                    ctx.stroke();
                    break;
                
                case 'rectangle':
                    ctx.strokeRect(
                        drawing.startX,
                        drawing.startY,
                        drawing.endX - drawing.startX,
                        drawing.endY - drawing.startY
                    );
                    break;
                
                case 'circle':
                    const radius = Math.sqrt(
                        Math.pow(drawing.endX - drawing.startX, 2) + 
                        Math.pow(drawing.endY - drawing.startY, 2)
                    );
                    ctx.beginPath();
                    ctx.arc(drawing.startX, drawing.startY, radius, 0, 2 * Math.PI);
                    ctx.stroke();
                    break;
            }
        });

        // Draw current drawing in progress
        if (currentDrawing && isDrawing) {
            ctx.strokeStyle = '#9C27B0';
            ctx.lineWidth = 2;
            ctx.setLineDash([5, 5]);

            switch (drawingMode) {
                case 'line':
                    ctx.beginPath();
                    ctx.moveTo(currentDrawing.startX, currentDrawing.startY);
                    ctx.lineTo(currentDrawing.endX, currentDrawing.endY);
                    ctx.stroke();
                    break;
                
                case 'rectangle':
                    ctx.strokeRect(
                        currentDrawing.startX,
                        currentDrawing.startY,
                        currentDrawing.endX - currentDrawing.startX,
                        currentDrawing.endY - currentDrawing.startY
                    );
                    break;
            }
            ctx.setLineDash([]);
        }
    };

    const handleMouseDown = (e) => {
        if (drawingMode === 'none') return;

        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        setIsDrawing(true);
        setCurrentDrawing({
            type: drawingMode,
            startX: x,
            startY: y,
            endX: x,
            endY: y
        });
    };

    const handleMouseMove = (e) => {
        if (!isDrawing || drawingMode === 'none') return;

        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        setCurrentDrawing(prev => ({
            ...prev,
            endX: x,
            endY: y
        }));
    };

    const handleMouseUp = () => {
        if (!isDrawing || !currentDrawing) return;

        setDrawings(prev => [...prev, currentDrawing]);
        setIsDrawing(false);
        setCurrentDrawing(null);
        
        if (onDrawingComplete) {
            onDrawingComplete(currentDrawing);
        }
    };

    const clearDrawings = () => {
        setDrawings([]);
    };

    return (
        <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
                <span>📈 Advanced Chart with Drawing Tools</span>
                <div>
                    <ButtonGroup className="me-2">
                        <Button 
                            variant={drawingMode === 'line' ? 'primary' : 'outline-primary'}
                            size="sm"
                            onClick={() => setDrawingMode('line')}
                        >
                            📏 Line
                        </Button>
                        <Button 
                            variant={drawingMode === 'rectangle' ? 'primary' : 'outline-primary'}
                            size="sm"
                            onClick={() => setDrawingMode('rectangle')}
                        >
                            ⬜ Rectangle
                        </Button>
                        <Button 
                            variant={drawingMode === 'circle' ? 'primary' : 'outline-primary'}
                            size="sm"
                            onClick={() => setDrawingMode('circle')}
                        >
                            ⭕ Circle
                        </Button>
                    </ButtonGroup>
                    <ButtonGroup>
                        <Button 
                            variant="outline-secondary"
                            size="sm"
                            onClick={() => setDrawingMode('none')}
                        >
                            🖱️ Select
                        </Button>
                        <Button 
                            variant="outline-danger"
                            size="sm"
                            onClick={clearDrawings}
                        >
                            🗑️ Clear
                        </Button>
                    </ButtonGroup>
                </div>
            </Card.Header>
            <Card.Body>
                <div className="mb-3">
                    {patterns?.chart_patterns?.map((pattern, index) => (
                        <Badge key={index} bg="info" className="me-2">
                            {pattern.name} - {pattern.signal}
                        </Badge>
                    ))}
                </div>
                <canvas
                    ref={canvasRef}
                    width={800}
                    height={400}
                    style={{ 
                        border: '1px solid #ddd', 
                        cursor: drawingMode !== 'none' ? 'crosshair' : 'default',
                        width: '100%',
                        maxWidth: '800px'
                    }}
                    onMouseDown={handleMouseDown}
                    onMouseMove={handleMouseMove}
                    onMouseUp={handleMouseUp}
                />
                <div className="mt-3">
                    <small className="text-muted">
                        🎨 Drawing Mode: <strong>{drawingMode}</strong> | 
                        📊 Patterns Detected: <strong>{patterns?.chart_patterns?.length || 0}</strong> | 
                        ✏️ User Drawings: <strong>{drawings.length}</strong>
                    </small>
                </div>
            </Card.Body>
        </Card>
    );
}