import axios from 'axios';
import toast from 'react-hot-toast';
import { API_ENDPOINTS, ERROR_MESSAGES, DEFAULT_VALUES, TOAST_CONFIG } from '../utils/constants';

// Create axios instance with enhanced configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Fixed: Point to backend server
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  timeout: DEFAULT_VALUES.REQUEST_TIMEOUT,
});

// Request interceptor for adding auth tokens and logging
api.interceptors.request.use(
  config => {
    // Add auth token if available - Fixed: Use correct token key
    const token = localStorage.getItem('token'); // Changed from 'authToken' to 'token'
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add request timestamp for debugging
    config.metadata = { startTime: new Date() };

    // Log requests in development
    if (process.env.NODE_ENV === 'development') {
      console.log(`🚀 ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }

    return config;
  },
  error => {
    console.error('Request error:', error);
    return Promise.reject(error);
  },
);

// Response interceptor for error handling and logging
api.interceptors.response.use(
  response => {
    // Calculate request duration
    const duration = new Date() - response.config.metadata.startTime;

    // Log successful responses in development
    if (process.env.NODE_ENV === 'development') {
      console.log(
        `✅ ${response.config.method?.toUpperCase()} ${response.config.url} (${duration}ms)`,
        response.data,
      );
    }

    return response;
  },
  error => {
    // Enhanced error handling with user-friendly messages
    const message = getErrorMessage(error);
    const status = error.response?.status;

    // Show appropriate error messages based on status
    switch (status) {
    case 401:
      toast.error(ERROR_MESSAGES.UNAUTHORIZED, TOAST_CONFIG.ERROR);
      // Clear auth token and redirect to login if needed
      localStorage.removeItem('token'); // Fixed: Use correct token key
      break;
    case 403:
      toast.error(ERROR_MESSAGES.FORBIDDEN, TOAST_CONFIG.ERROR);
      break;
    case 404:
      toast.error(ERROR_MESSAGES.NOT_FOUND, TOAST_CONFIG.ERROR);
      break;
    case 408:
      toast.error(ERROR_MESSAGES.TIMEOUT_ERROR, TOAST_CONFIG.ERROR);
      break;
    case 422:
      toast.error(ERROR_MESSAGES.VALIDATION_ERROR, TOAST_CONFIG.ERROR);
      break;
    case 500:
    case 502:
    case 503:
    case 504:
      toast.error(ERROR_MESSAGES.SERVER_ERROR, TOAST_CONFIG.ERROR);
      break;
    default:
      if (error.code === 'ECONNABORTED') {
        toast.error(ERROR_MESSAGES.TIMEOUT_ERROR, TOAST_CONFIG.ERROR);
      } else if (error.code === 'NETWORK_ERROR') {
        toast.error(ERROR_MESSAGES.NETWORK_ERROR, TOAST_CONFIG.ERROR);
      } else if (status >= 400) {
        toast.error(message, TOAST_CONFIG.ERROR);
      }
    }

    // Enhanced error logging
    const duration = error.config?.metadata
      ? new Date() - error.config.metadata.startTime
      : 'unknown';
    console.error(
      `❌ ${error.config?.method?.toUpperCase()} ${error.config?.url} (${duration}ms)`,
      {
        status,
        message,
        data: error.response?.data,
        stack: error.stack,
      },
    );

    return Promise.reject(error);
  },
);

// Utility function to extract meaningful error messages
const getErrorMessage = error => {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error.response?.data?.error) {
    return error.response.data.error;
  }
  if (error.message) {
    return error.message;
  }
  return 'An unexpected error occurred';
};

// Enhanced API service functions with better error handling and validation
export const agentApi = {
  getAllAgents: async () => {
    try {
      const response = await api.get(API_ENDPOINTS.AGENTS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch agents: ${getErrorMessage(error)}`);
    }
  },

  getAgent: async id => {
    if (!id) {
      throw new Error('Agent ID is required');
    }
    try {
      const response = await api.get(`${API_ENDPOINTS.AGENTS}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch agent ${id}: ${getErrorMessage(error)}`);
    }
  },

  createAgent: async agentData => {
    if (!agentData || !agentData.name || !agentData.type) {
      throw new Error('Agent name and type are required');
    }
    try {
      const response = await api.post(API_ENDPOINTS.AGENTS, agentData);
      toast.success('Agent created successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to create agent: ${getErrorMessage(error)}`);
    }
  },

  updateAgent: async (id, agentData) => {
    if (!id) {
      throw new Error('Agent ID is required');
    }
    try {
      const response = await api.put(`${API_ENDPOINTS.AGENTS}/${id}`, agentData);
      toast.success('Agent updated successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to update agent ${id}: ${getErrorMessage(error)}`);
    }
  },

  deleteAgent: async id => {
    if (!id) {
      throw new Error('Agent ID is required');
    }
    try {
      await api.delete(`${API_ENDPOINTS.AGENTS}/${id}`);
      toast.success('Agent deleted successfully', TOAST_CONFIG.SUCCESS);
      return true;
    } catch (error) {
      throw new Error(`Failed to delete agent ${id}: ${getErrorMessage(error)}`);
    }
  },

  executeAgent: async (id, taskData) => {
    if (!id) {
      throw new Error('Agent ID is required');
    }
    try {
      const response = await api.post(`${API_ENDPOINTS.AGENTS}/${id}/execute`, taskData);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to execute agent ${id}: ${getErrorMessage(error)}`);
    }
  },
};

export const taskApi = {
  submitTask: async data => {
    try {
      const response = await api.post('/tasks', data);
      toast.success('Task submitted successfully');
      return response.data;
    } catch (error) {
      throw new Error(`Failed to submit task: ${error.message}`);
    }
  },

  getMetrics: async () => {
    try {
      const response = await api.get('/tasks/metrics');
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch metrics: ${error.message}`);
    }
  },
};

export const stockApi = {
  // Basic endpoints with enhanced error handling
  getRealTimeQuote: async symbol => {
    try {
      const response = await api.get(`/stocks/${symbol}/quote`);
      return response.data;
    } catch (error) {
      toast.error(`Failed to get quote for ${symbol}`);
      throw error;
    }
  },

  getHistoricalData: async (symbol, period = '1y') => {
    try {
      const response = await api.get(`/stocks/${symbol}/history`, { params: { period } });
      return response.data;
    } catch (error) {
      toast.error(`Failed to get historical data for ${symbol}`);
      throw error;
    }
  },
  getAdvancedHistoricalData: (symbol, interval = 'daily', outputSize = 'compact') =>
    api.get(`/stocks/${symbol}/history/advanced`, { params: { interval, outputSize } }),
  getTechnicalIndicators: symbol => api.get(`/stocks/${symbol}/indicators`),
  getTradingSignal: symbol => api.get(`/stocks/${symbol}/signal`),
  getCompanyInfo: symbol => api.get(`/stocks/${symbol}/info`),
  addToWatchlist: symbol => api.post('/stocks/watchlist', { symbol }),
  getMarketStatus: () => api.get('/stocks/market/status'),
  getTechnicalPatterns: symbol => api.get(`/stocks/${symbol}/patterns`),
  getRiskAssessment: symbol => api.get(`/stocks/${symbol}/risk`),
  getPricePrediction: symbol => api.get(`/stocks/${symbol}/prediction`),
  getCompleteAnalysis: symbol => api.get(`/stocks/${symbol}/analysis/complete`),
  compareWithMarket: (symbol, benchmark = 'SPY') =>
    api.get(`/stocks/${symbol}/comparison`, { params: { benchmarkSymbol: benchmark } }),
  createAlert: (symbol, alertData) => api.post(`/stocks/${symbol}/alerts`, alertData),
  getActiveAlerts: symbol => api.get(`/stocks/${symbol}/alerts`),
  removeAlert: alertId => api.delete(`/stocks/alerts/${alertId}`),
  analyzePortfolio: holdings => api.post('/stocks/portfolio/analyze', { holdings }),
};

// Enhanced Context Management API with better error handling
export const contextApi = {
  setAgentContext: async (agentId, context) => {
    try {
      const response = await api.post(`/context/agent/${agentId}/context`, context);
      return response.data;
    } catch (error) {
      toast.error('Failed to set agent context');
      throw error;
    }
  },

  getAgentContext: async agentId => {
    try {
      const response = await api.get(`/context/agent/${agentId}/context`);
      return response.data;
    } catch (error) {
      toast.error('Failed to get agent context');
      throw error;
    }
  },
  getAgentContextValue: (agentId, key) => api.get(`/context/agent/${agentId}/context/${key}`),
};

// New GPT4All API for local intelligence
export const gpt4allApi = {
  sendMessage: async (message, conversationId = null) => {
    try {
      const response = await api.post('/gpt4all/chat', {
        message,
        conversationId,
        model: 'gpt4all-falcon-q4_0',
      });
      return response.data;
    } catch (error) {
      toast.error('Failed to send message to GPT4All');
      throw error;
    }
  },

  getModels: async () => {
    try {
      const response = await api.get('/gpt4all/models');
      return response.data;
    } catch (error) {
      toast.error('Failed to fetch available models');
      throw error;
    }
  },

  getConversations: async () => {
    try {
      const response = await api.get('/gpt4all/conversations');
      return response.data;
    } catch (error) {
      toast.error('Failed to fetch conversations');
      throw error;
    }
  },
};

// Free APIs integration
export const freeApiService = {
  searchDuckDuckGo: async query => {
    try {
      const response = await api.get('/search/duckduckgo', { params: { q: query } });
      return response.data;
    } catch (error) {
      toast.error('Search failed. Please try again.');
      throw error;
    }
  },

  getWeatherData: async location => {
    try {
      const response = await api.get('/weather', { params: { location } });
      return response.data;
    } catch (error) {
      toast.error('Failed to get weather data');
      throw error;
    }
  },

  getLocationData: async address => {
    try {
      const response = await api.get('/location/geocode', { params: { address } });
      return response.data;
    } catch (error) {
      toast.error('Failed to get location data');
      throw error;
    }
  },
};

export const pluginApi = {
  getInstalledPlugins: async () => {
    try {
      const response = await api.get(`${API_ENDPOINTS.PLUGINS}/installed`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch installed plugins: ${getErrorMessage(error)}`);
    }
  },

  getAvailablePlugins: async () => {
    try {
      const response = await api.get(`${API_ENDPOINTS.PLUGINS}/available`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch available plugins: ${getErrorMessage(error)}`);
    }
  },

  installPlugin: async pluginData => {
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/install`, pluginData);
      toast.success('Plugin installed successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to install plugin: ${getErrorMessage(error)}`);
    }
  },

  uninstallPlugin: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      await api.delete(`${API_ENDPOINTS.PLUGINS}/${pluginId}`);
      toast.success('Plugin uninstalled successfully', TOAST_CONFIG.SUCCESS);
      return true;
    } catch (error) {
      throw new Error(`Failed to uninstall plugin: ${getErrorMessage(error)}`);
    }
  },

  enablePlugin: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/${pluginId}/enable`);
      toast.success('Plugin enabled successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to enable plugin: ${getErrorMessage(error)}`);
    }
  },

  disablePlugin: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/${pluginId}/disable`);
      toast.success('Plugin disabled successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to disable plugin: ${getErrorMessage(error)}`);
    }
  },

  configurePlugin: async (pluginId, config) => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.put(`${API_ENDPOINTS.PLUGINS}/${pluginId}/config`, config);
      toast.success('Plugin configuration updated', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to configure plugin: ${getErrorMessage(error)}`);
    }
  },

  getPluginConfig: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.get(`${API_ENDPOINTS.PLUGINS}/${pluginId}/config`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get plugin configuration: ${getErrorMessage(error)}`);
    }
  },

  uploadPlugin: async formData => {
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      toast.success('Plugin uploaded successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to upload plugin: ${getErrorMessage(error)}`);
    }
  },

  validatePlugin: async pluginData => {
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/validate`, pluginData);
      return response.data;
    } catch (error) {
      throw new Error(`Plugin validation failed: ${getErrorMessage(error)}`);
    }
  },

  getPluginDependencies: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.get(`${API_ENDPOINTS.PLUGINS}/${pluginId}/dependencies`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get plugin dependencies: ${getErrorMessage(error)}`);
    }
  },

  checkPluginUpdates: async () => {
    try {
      const response = await api.get(`${API_ENDPOINTS.PLUGINS}/updates`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to check plugin updates: ${getErrorMessage(error)}`);
    }
  },

  updatePlugin: async pluginId => {
    if (!pluginId) {
      throw new Error('Plugin ID is required');
    }
    try {
      const response = await api.post(`${API_ENDPOINTS.PLUGINS}/${pluginId}/update`);
      toast.success('Plugin updated successfully', TOAST_CONFIG.SUCCESS);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to update plugin: ${getErrorMessage(error)}`);
    }
  },
};

export default api;
