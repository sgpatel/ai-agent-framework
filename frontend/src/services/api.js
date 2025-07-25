import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    headers: { 'Content-Type': 'application/json' }
});

export const agentApi = {
    getAllAgents: () => api.get('/agents'),
    reloadAgents: () => api.post('/agents/reload')
};

export const taskApi = {
    submitTask: data => api.post('/tasks', data),
    getMetrics: () => api.get('/tasks/metrics')
};

export const stockApi = {
    // Basic endpoints
    getRealTimeQuote: (symbol) => api.get(`/stocks/${symbol}/quote`),
    getHistoricalData: (symbol, period = '1y') => api.get(`/stocks/${symbol}/history`, { params: { period } }),
    getAdvancedHistoricalData: (symbol, interval = 'daily', outputSize = 'compact') =>
        api.get(`/stocks/${symbol}/history/advanced`, { params: { interval, outputSize } }),
    getTechnicalIndicators: (symbol) => api.get(`/stocks/${symbol}/indicators`),
    getTradingSignal: (symbol) => api.get(`/stocks/${symbol}/signal`),
    getCompanyInfo: (symbol) => api.get(`/stocks/${symbol}/info`),
    addToWatchlist: (symbol) => api.post('/stocks/watchlist', { symbol }),
    getMarketStatus: () => api.get('/stocks/market/status'),

    // Advanced endpoints
    getTechnicalPatterns: (symbol) => api.get(`/stocks/${symbol}/patterns`),
    getRiskAssessment: (symbol) => api.get(`/stocks/${symbol}/risk`),
    getPricePrediction: (symbol) => api.get(`/stocks/${symbol}/prediction`),
    getCompleteAnalysis: (symbol) => api.get(`/stocks/${symbol}/analysis/complete`),
    compareWithMarket: (symbol, benchmark = 'SPY') => api.get(`/stocks/${symbol}/comparison`, { params: { benchmarkSymbol: benchmark } }),

    // Alert management
    createAlert: (symbol, alertData) => api.post(`/stocks/${symbol}/alerts`, alertData),
    getActiveAlerts: (symbol) => api.get(`/stocks/${symbol}/alerts`),
    removeAlert: (alertId) => api.delete(`/stocks/alerts/${alertId}`),

    // Portfolio analysis
    analyzePortfolio: (holdings) => api.post('/stocks/portfolio/analyze', { holdings })
};

// New Context Management API
export const contextApi = {
    // Basic context operations
    setAgentContext: (agentId, context) => api.post(`/context/agent/${agentId}/context`, context),
    getAgentContext: (agentId) => api.get(`/context/agent/${agentId}/context`),
    getAgentContextValue: (agentId, key) => api.get(`/context/agent/${agentId}/context/${key}`),
    clearAgentContext: (agentId) => api.delete(`/context/agent/${agentId}/context`),

    // Shared data operations
    setSharedData: (dataKey, data, sourceAgent, metadata = {}) =>
        api.post(`/context/shared-data/${dataKey}`, { data, sourceAgent, metadata }),
    getSharedData: (dataKey) => api.get(`/context/shared-data/${dataKey}`),
    getAllSharedData: () => api.get('/context/shared-data'),
    clearSharedData: (dataKey) => api.delete(`/context/shared-data/${dataKey}`),

    // Subscription management
    subscribeToContext: (subscriberAgent, contextKey) =>
        api.post('/context/subscribe', { subscriberAgent, contextKey }),
    unsubscribeFromContext: (subscriberAgent, contextKey) =>
        api.delete('/context/subscribe', { data: { subscriberAgent, contextKey } }),
    getSubscribers: (contextKey) => api.get(`/context/subscribers/${contextKey}`),

    // Workflow management
    createWorkflow: (workflow) => api.post('/context/workflows', workflow),
    getWorkflow: (workflowId) => api.get(`/context/workflows/${workflowId}`),
    getActiveWorkflows: () => api.get('/context/workflows'),
    updateWorkflowStatus: (workflowId, status) =>
        api.put(`/context/workflows/${workflowId}/status`, { status }),

    // Agent recommendations
    getAgentRecommendations: (agentId) => api.get(`/context/agent/${agentId}/recommendations`),
    clearAgentRecommendations: (agentId) => api.delete(`/context/agent/${agentId}/recommendations`),
    executeRecommendation: (agentId, recommendationId, action) =>
        api.post(`/context/agent/${agentId}/execute-recommendation`, { recommendationId, action }),

    // Advanced queries
    findAgentsWithDataType: (dataType) => api.get(`/context/agents/with-data-type/${dataType}`),
    findCompatibleAgents: (sourceAgent, dataType) =>
        api.get(`/context/agents/${sourceAgent}/compatible/${dataType}`),
    getCollaborationSuggestions: (agentId) => api.get(`/context/agent/${agentId}/collaboration-suggestions`),

    // Chart and visualization helpers
    getChartSuggestions: (dataType, dataStructure) =>
        api.post('/context/chart-suggestions', { dataType, dataStructure }),
    triggerCollaboration: (sourceAgent, targetAgent, dataKey, action) =>
        api.post('/context/trigger-collaboration', { sourceAgent, targetAgent, dataKey, action })
};

// Plugin Management API
export const pluginApi = {
    // Get plugins
    getInstalledPlugins: () => api.get('/plugins/installed'),
    getAvailablePlugins: () => api.get('/plugins/available'),
    getPlugin: (pluginId) => api.get(`/plugins/${pluginId}`),

    // Plugin lifecycle management
    installPlugin: (pluginId) => api.post(`/plugins/${pluginId}/install`),
    togglePlugin: (pluginId) => api.post(`/plugins/${pluginId}/toggle`),
    uninstallPlugin: (pluginId) => api.delete(`/plugins/${pluginId}`),
    updatePlugin: (pluginId) => api.post(`/plugins/${pluginId}/update`),

    // Plugin configuration
    configurePlugin: (pluginId, config) => api.post(`/plugins/${pluginId}/configure`, config),
    getPluginConfiguration: (pluginId) => api.get(`/plugins/${pluginId}/configuration`),

    // Plugin upload and installation
    uploadPlugin: (formData) => api.post('/plugins/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),

    // Dependencies and updates
    getPluginDependencies: (pluginId) => api.get(`/plugins/${pluginId}/dependencies`),
    checkForUpdates: () => api.post('/plugins/check-updates'),

    // Marketplace and search
    getFeaturedPlugins: () => api.get('/plugins/marketplace/featured'),
    searchPlugins: (query, category = null) => api.get('/plugins/search', {
        params: { query, ...(category && { category }) }
    })
};

export default api;
