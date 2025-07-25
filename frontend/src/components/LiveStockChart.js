import React, { useState, useEffect } from 'react';
import { Card, Button, Badge } from 'react-bootstrap';

export default function LiveStockChart({ symbol }) {
    const [priceData, setPriceData] = useState(null);
    const [isLive, setIsLive] = useState(false);

    useEffect(() => {
        if (isLive && symbol) {
            const interval = setInterval(() => {
                fetchLivePrice();
            }, 5000); // Update every 5 seconds

            return () => clearInterval(interval);
        }
    }, [isLive, symbol]);

    const fetchLivePrice = async () => {
        try {
            const response = await fetch(`/api/stocks/${symbol}/price`);
            const data = await response.json();
            setPriceData(data);
        } catch (error) {
            console.error('Failed to fetch live price:', error);
        }
    };

    const toggleLiveFeed = () => {
        setIsLive(!isLive);
        if (!isLive) {
            fetchLivePrice();
        }
    };

    const formatPrice = (price) => {
        return price ? `$${price.toFixed(2)}` : 'N/A';
    };

    const formatChange = (change, changePercent) => {
        if (!change || !changePercent) return 'N/A';
        const sign = change >= 0 ? '+' : '';
        return `${sign}${change.toFixed(2)} (${sign}${changePercent.toFixed(2)}%)`;
    };

    const getChangeColor = (change) => {
        if (!change) return 'secondary';
        return change >= 0 ? 'success' : 'danger';
    };

    return (
        <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
                <span>Live Price Feed - {symbol}</span>
                <Button 
                    variant={isLive ? 'danger' : 'success'} 
                    size="sm" 
                    onClick={toggleLiveFeed}
                >
                    {isLive ? 'Stop Live Feed' : 'Start Live Feed'}
                </Button>
            </Card.Header>
            <Card.Body>
                {priceData ? (
                    <div>
                        <h4>{formatPrice(priceData.price)}</h4>
                        <Badge bg={getChangeColor(priceData.change)}>
                            {formatChange(priceData.change, priceData.change_percent)}
                        </Badge>
                        <div className="mt-2">
                            <small className="text-muted">
                                Volume: {priceData.volume?.toLocaleString()} | 
                                Market Cap: {priceData.market_cap} | 
                                Currency: {priceData.currency}
                            </small>
                        </div>
                        {isLive && (
                            <div className="mt-2">
                                <Badge bg="info">🔴 LIVE</Badge>
                                <small className="text-muted ms-2">Updates every 5 seconds</small>
                            </div>
                        )}
                    </div>
                ) : (
                    <p>Click "Start Live Feed" to begin real-time price updates</p>
                )}
            </Card.Body>
        </Card>
    );
}