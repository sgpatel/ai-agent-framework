import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './IntelligentDashboard.css';

const IntelligentDashboard = () => {
  const [activeTab, setActiveTab] = useState('decisions');
  const [availableAgents, setAvailableAgents] = useState([]);
  const [decisionResult, setDecisionResult] = useState(null);
  const [supportResponse, setSupportResponse] = useState(null);
  const [agentCommunication, setAgentCommunication] = useState(null);
  const [loading, setLoading] = useState(false);
  const [symbol, setSymbol] = useState('AAPL');

  useEffect(() => {
    loadAvailableAgents();
  }, []);

  const loadAvailableAgents = async () => {
    try {
      const response = await fetch('/api/intelligent/agents/available');
      const data = await response.json();
      setAvailableAgents(data.agents || []);
    } catch (error) {
      console.error('Error loading agents:', error);
    }
  };

  const makeInvestmentDecision = async () => {
    setLoading(true);
    try {
      const marketData = {
        price: 150.25,
        volume: 1000000,
        rsi: 65,
        macd: 0.5,
        movingAverage20: 148.5,
        movingAverage50: 145.2,
      };

      const response = await fetch('/api/intelligent/decision/investment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbol, marketData }),
      });

      const data = await response.json();
      setDecisionResult(data);
    } catch (error) {
      console.error('Error making investment decision:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStockAlert = async (alertType) => {
    setLoading(true);
    try {
      const marketData = {
        currentPrice: 150.25,
        priceChange: -2.5,
        percentChange: -1.64,
        volume: 1200000,
        alert: alertType,
      };

      const response = await fetch('/api/intelligent/support/stock-alert', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbol, alertType, marketData }),
      });

      const data = await response.json();
      setSupportResponse(data);
    } catch (error) {
      console.error('Error handling stock alert:', error);
    } finally {
      setLoading(false);
    }
  };

  const facilitateAgentCommunication = async () => {
    setLoading(true);
    try {
      const context = {
        symbol,
        urgency: 'HIGH',
        analysisType: 'COMPREHENSIVE',
        riskTolerance: 'MEDIUM',
      };

      const response = await fetch('/api/intelligent/agents/communicate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fromAgent: 'user-interface',
          task: `Perform comprehensive analysis for ${symbol} with risk assessment and trading recommendations`,
          context,
        }),
      });

      const data = await response.json();
      setAgentCommunication(data);
    } catch (error) {
      console.error('Error facilitating agent communication:', error);
    } finally {
      setLoading(false);
    }
  };

  const explainIndicator = async (indicator) => {
    setLoading(true);
    try {
      const context = {
        currentValue: indicator === 'RSI' ? 65 : 0.5,
        trend: 'BULLISH',
        timeframe: '1D',
      };

      const response = await fetch('/api/intelligent/support/explain-indicator', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ indicator, symbol, context }),
      });

      const data = await response.json();
      setSupportResponse(data);
    } catch (error) {
      console.error('Error explaining indicator:', error);
    } finally {
      setLoading(false);
    }
  };

  const renderDecisionTab = () => (
    <div className="tab-content">
      <div className="decision-controls">
        <div className="input-group">
          <label htmlFor="symbol-input">Stock Symbol:</label>
          <input
            id="symbol-input"
            type="text"
            value={symbol}
            onChange={(e) => setSymbol(e.target.value.toUpperCase())}
            placeholder="Enter symbol"
          />
        </div>
        <button onClick={makeInvestmentDecision} disabled={loading}>
          {loading ? 'Analyzing...' : 'Make Investment Decision'}
        </button>
      </div>

      {decisionResult && (
        <div className="result-panel">
          <h3>🧠 AI Investment Decision</h3>
          <div className="decision-details">
            <div className="decision-header">
              <span className="recommendation">{decisionResult.decision?.recommendation || 'ANALYZING'}</span>
              <span className="confidence">Confidence: {(decisionResult.decision?.confidence * 100 || 50).toFixed(1)}%</span>
            </div>
            <div className="decision-reasoning">
              <h4>AI Reasoning:</h4>
              <div className="markdown-content">
                {decisionResult.decision?.rawResponse ? (
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {decisionResult.decision.rawResponse}
                  </ReactMarkdown>
                ) : (
                  <p>Processing decision logic...</p>
                )}
              </div>
            </div>
            <div className="action-items">
              <h4>Recommended Actions:</h4>
              <ul>
                {(decisionResult.decision?.actionItems || ['Monitor market conditions', 'Review risk tolerance']).map((action, index) => (
                  <li key={index}>{action}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  const renderSupportTab = () => (
    <div className="tab-content">
      <div className="support-controls">
        <h3>📞 Intelligent Customer Support</h3>
        <div className="support-actions">
          <button onClick={() => handleStockAlert('PRICE_DROP')} disabled={loading}>
            Handle Price Drop Alert
          </button>
          <button onClick={() => handleStockAlert('VOLUME_SPIKE')} disabled={loading}>
            Handle Volume Spike Alert
          </button>
          <button onClick={() => explainIndicator('RSI')} disabled={loading}>
            Explain RSI Indicator
          </button>
          <button onClick={() => explainIndicator('MACD')} disabled={loading}>
            Explain MACD Indicator
          </button>
        </div>
      </div>

      {supportResponse && (
        <div className="result-panel">
          <h3>🎯 Support Response</h3>
          <div className="support-details">
            <div className="urgency-level">
              <span className={`urgency ${supportResponse.urgencyLevel?.toLowerCase() || 'medium'}`}>
                {supportResponse.urgencyLevel || 'MEDIUM'} PRIORITY
              </span>
              {supportResponse.actionRequired && <span className="follow-up">Follow-up Required</span>}
            </div>
            <div className="support-content">
              <div className="markdown-content">
                {supportResponse.support?.content || supportResponse.explanation?.content ? (
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {supportResponse.support?.content || supportResponse.explanation?.content}
                  </ReactMarkdown>
                ) : (
                  <p>Generating response...</p>
                )}
              </div>
            </div>
            {supportResponse.support?.actionItems && (
              <div className="action-items">
                <h4>Action Items:</h4>
                <ul>
                  {supportResponse.support.actionItems.map((item, index) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );

  const renderAgentsTab = () => (
    <div className="tab-content">
      <div className="agents-section">
        <div className="agents-header">
          <h3>🤖 Dynamic Agent Communication</h3>
          <button onClick={facilitateAgentCommunication} disabled={loading}>
            {loading ? 'Coordinating...' : 'Start Agent Collaboration'}
          </button>
        </div>

        <div className="available-agents">
          <h4>Available Agents ({availableAgents.length})</h4>
          <div className="agents-grid">
            {availableAgents.map((agent, index) => (
              <div key={index} className="agent-card">
                <div className="agent-header">
                  <h5>{agent.agentId}</h5>
                  <span className="specialization">{agent.specialization}</span>
                </div>
                <p className="agent-description">{agent.description}</p>
                <div className="capabilities">
                  {agent.capabilities?.slice(0, 3).map((cap, capIndex) => (
                    <span key={capIndex} className="capability-tag">{cap}</span>
                  ))}
                  {agent.capabilities?.length > 3 && (
                    <span className="capability-tag more">+{agent.capabilities.length - 3} more</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {agentCommunication && (
          <div className="result-panel">
            <h3>🔄 Agent Communication Result</h3>
            <div className="communication-details">
              <div className="agents-involved">
                <h4>Agents Involved:</h4>
                <div className="agent-chips">
                  {Object.keys(agentCommunication.agentsInvolved || {}).map((agentId, index) => (
                    <span key={index} className="agent-chip">{agentId}</span>
                  ))}
                </div>
              </div>

              {/* Individual Agent Analysis - Collapsible */}
              <div className="individual-agent-analysis">
                <h4>🤔 Individual Agent Analysis (Thinking Mode)</h4>
                <div className="agent-thinking-panels">
                  {Object.entries(agentCommunication.communicationResult?.agentResults || {}).map(([agentId, result], index) => (
                    <AgentThinkingPanel
                      key={index}
                      agentId={agentId}
                      result={result}
                    />
                  ))}
                </div>
              </div>

              {/* Unified Analysis */}
              <div className="communication-result">
                <h4>🎯 Unified Collaborative Analysis:</h4>
                <div className="markdown-content unified-analysis">
                  {agentCommunication.communicationResult?.synthesizedResult ? (
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {agentCommunication.communicationResult.synthesizedResult}
                    </ReactMarkdown>
                  ) : (
                    <p>Agents are collaborating to provide comprehensive analysis...</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="intelligent-dashboard">
      <div className="dashboard-header">
        <h2>🧠 Intelligent AI Decision System</h2>
        <p>LLM-powered decision making, customer support, and dynamic agent communication</p>
      </div>

      <div className="tab-navigation">
        <button
          className={activeTab === 'decisions' ? 'active' : ''}
          onClick={() => setActiveTab('decisions')}
        >
          💡 Smart Decisions
        </button>
        <button
          className={activeTab === 'support' ? 'active' : ''}
          onClick={() => setActiveTab('support')}
        >
          📞 Intelligent Support
        </button>
        <button
          className={activeTab === 'agents' ? 'active' : ''}
          onClick={() => setActiveTab('agents')}
        >
          🤖 Agent Network
        </button>
      </div>

      <div className="tab-container">
        {activeTab === 'decisions' && renderDecisionTab()}
        {activeTab === 'support' && renderSupportTab()}
        {activeTab === 'agents' && renderAgentsTab()}
      </div>

      {loading && (
        <div className="loading-overlay">
          <div className="loading-spinner">
            <div className="spinner"></div>
            <p>AI processing...</p>
          </div>
        </div>
      )}
    </div>
  );
};

const AgentThinkingPanel = ({ agentId, result }) => {
  const [expanded, setExpanded] = useState(false);

  const handleToggle = () => {
    setExpanded(!expanded);
  };

  const handleKeyDown = (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      handleToggle();
    }
  };

  // Handle different response structures from backend
  const getAgentAnalysis = () => {
    if (!result) {
      return 'No analysis data available for this agent.';
    }

    // Try different possible response structures
    if (result.detailedAnalysis) {
      return result.detailedAnalysis;
    }

    if (result.analysis) {
      return result.analysis;
    }

    if (result.content) {
      return result.content;
    }

    if (result.response) {
      return result.response;
    }

    if (typeof result === 'string') {
      return result;
    }

    // If result is an object, try to extract meaningful content
    if (typeof result === 'object') {
      const possibleKeys = ['reasoning', 'recommendation', 'summary', 'output', 'result'];
      for (const key of possibleKeys) {
        if (result[key] && typeof result[key] === 'string') {
          return result[key];
        }
      }

      // Fallback: show formatted JSON
      return `**Agent Analysis:**\n\n${JSON.stringify(result, null, 2)}`;
    }

    return 'Agent analysis in progress...';
  };

  return (
    <div className="agent-thinking-panel">
      <div
        className="panel-header"
        onClick={handleToggle}
        onKeyDown={handleKeyDown}
        role="button"
        tabIndex={0}
        aria-expanded={expanded}
        aria-controls={`panel-content-${agentId}`}
      >
        <h5>{agentId}</h5>
        <span className="toggle-icon">{expanded ? '▼' : '►'}</span>
      </div>
      {expanded && (
        <div id={`panel-content-${agentId}`} className="panel-content">
          <div className="markdown-content">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {getAgentAnalysis()}
            </ReactMarkdown>
          </div>
        </div>
      )}
    </div>
  );
};

export default IntelligentDashboard;
