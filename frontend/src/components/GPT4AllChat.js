import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './GPT4AllChat.css';

const GPT4AllChat = () => {
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [modelStatus, setModelStatus] = useState('LOADING');
  const [selectedMode, setSelectedMode] = useState('general');

  useEffect(() => {
    checkModelStatus();
  }, []);

  const checkModelStatus = async () => {
    try {
      const response = await fetch('/api/llm/status');
      const data = await response.json();
      setModelStatus(data.status);
    } catch (error) {
      console.error('Error checking model status:', error);
      setModelStatus('ERROR');
    }
  };

  const sendMessage = async () => {
    if (!inputText.trim() || isLoading) return;

    const userMessage = { role: 'user', content: inputText, timestamp: new Date() };
    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsLoading(true);

    try {
      let endpoint = '/api/llm/generate';
      let requestBody = { prompt: inputText };

      // Adjust endpoint based on selected mode
      if (selectedMode === 'stock-analysis') {
        endpoint = '/api/llm/analyze-stock';
        requestBody = {
          symbol: extractSymbol(inputText),
          marketData: inputText,
          technicalIndicators: 'RSI, MACD, Moving Averages'
        };
      } else if (selectedMode === 'trading-insight') {
        endpoint = '/api/llm/trading-insight';
        requestBody = {
          symbol: extractSymbol(inputText),
          priceData: inputText,
          signals: 'Technical analysis signals'
        };
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();

      const assistantMessage = {
        role: 'assistant',
        content: data.response || data.analysis || data.insight || data.error,
        timestamp: new Date(),
        mode: selectedMode
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMessage = {
        role: 'assistant',
        content: 'Sorry, I encountered an error processing your request.',
        timestamp: new Date(),
        isError: true
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const extractSymbol = (text) => {
    const symbolMatch = text.match(/\b[A-Z]{1,5}\b/);
    return symbolMatch ? symbolMatch[0] : 'UNKNOWN';
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const clearChat = () => {
    setMessages([]);
  };

  const getStatusColor = () => {
    switch (modelStatus) {
      case 'READY': return '#4CAF50';
      case 'LOADING': return '#FF9800';
      case 'DISABLED': return '#9E9E9E';
      default: return '#F44336';
    }
  };

  return (
    <div className="gpt4all-chat">
      <div className="chat-header">
        <div className="header-content">
          <h3>GPT4All Intelligence</h3>
          <div className="model-status">
            <span
              className="status-indicator"
              style={{ backgroundColor: getStatusColor() }}
            />
            <span className="status-text">{modelStatus}</span>
          </div>
        </div>

        <div className="mode-selector">
          <select
            value={selectedMode}
            onChange={(e) => setSelectedMode(e.target.value)}
            className="mode-select"
          >
            <option value="general">General Chat</option>
            <option value="stock-analysis">Stock Analysis</option>
            <option value="trading-insight">Trading Insights</option>
          </select>
          <button onClick={clearChat} className="clear-btn">Clear</button>
        </div>
      </div>

      <div className="chat-messages">
        {messages.length === 0 && (
          <div className="welcome-message">
            <h4>Welcome to GPT4All Intelligence!</h4>
            <p>Select a mode and start chatting:</p>
            <ul>
              <li><strong>General Chat:</strong> Ask any questions</li>
              <li><strong>Stock Analysis:</strong> Get detailed stock analysis</li>
              <li><strong>Trading Insights:</strong> Receive trading recommendations</li>
            </ul>
          </div>
        )}

        {messages.map((message, index) => (
          <div
            key={index}
            className={`message ${message.role} ${message.isError ? 'error' : ''}`}
          >
            <div className="message-content">
              <div className="message-text">
                <ReactMarkdown
                  children={message.content}
                  remarkPlugins={[remarkGfm]}
                />
              </div>
              <div className="message-meta">
                <span className="timestamp">
                  {message.timestamp.toLocaleTimeString()}
                </span>
                {message.mode && (
                  <span className="mode-badge">{message.mode}</span>
                )}
              </div>
            </div>
          </div>
        ))}

        {isLoading && (
          <div className="message assistant loading">
            <div className="message-content">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="chat-input">
        <div className="input-container">
          <textarea
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={
              selectedMode === 'general'
                ? "Ask me anything..."
                : selectedMode === 'stock-analysis'
                ? "Enter stock symbol and market data for analysis..."
                : "Ask for trading insights on a specific stock..."
            }
            disabled={modelStatus !== 'READY' || isLoading}
            rows="2"
          />
          <button
            onClick={sendMessage}
            disabled={!inputText.trim() || modelStatus !== 'READY' || isLoading}
            className="send-btn"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
};

export default GPT4AllChat;
