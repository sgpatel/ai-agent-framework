// Application Constants
export const API_ENDPOINTS = {
  AGENTS: '/agents',
  TASKS: '/tasks',
  METRICS: '/metrics',
  STOCKS: '/stocks',
  INTELLIGENT: '/intelligent',
  PLUGINS: '/plugins',
  WEBSOCKET: '/ws',
};

export const AGENT_TYPES = {
  STOCK_ANALYZER: 'stock-analyzer',
  RISK_ASSESSOR: 'risk-assessor',
  STRATEGY_GENERATOR: 'strategy-generator',
  PATTERN_DETECTOR: 'pattern-detector',
  SEARCH_AGENT: 'search-agent',
  LOCATION_AGENT: 'location-agent',
};

export const AGENT_STATUS = {
  ACTIVE: 'active',
  INACTIVE: 'inactive',
  ERROR: 'error',
  LOADING: 'loading',
};

export const TASK_STATUS = {
  PENDING: 'pending',
  RUNNING: 'running',
  COMPLETED: 'completed',
  FAILED: 'failed',
};

export const CHART_TYPES = {
  CANDLESTICK: 'candlestick',
  LINE: 'line',
  AREA: 'area',
  OHLC: 'ohlc',
};

export const THEMES = {
  LIGHT: 'light',
  DARK: 'dark',
};

export const DEFAULT_VALUES = {
  STOCK_SYMBOL: 'AAPL',
  REFRESH_INTERVAL: 30000,
  REQUEST_TIMEOUT: 30000,
  CHART_TYPE: CHART_TYPES.CANDLESTICK,
  THEME: THEMES.DARK,
};

export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network connection failed. Please check your internet connection.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'Access to this resource is forbidden.',
  NOT_FOUND: 'The requested resource was not found.',
  SERVER_ERROR: 'Internal server error. Please try again later.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  TIMEOUT_ERROR: 'Request timed out. Please try again.',
};

export const TOAST_CONFIG = {
  SUCCESS: {
    duration: 3000,
    position: 'top-right',
    style: {
      background: '#10b981',
      color: 'white',
    },
  },
  ERROR: {
    duration: 5000,
    position: 'top-right',
    style: {
      background: '#ef4444',
      color: 'white',
    },
  },
  WARNING: {
    duration: 4000,
    position: 'top-right',
    style: {
      background: '#f59e0b',
      color: 'white',
    },
  },
};

export const VALIDATION_RULES = {
  STOCK_SYMBOL: {
    minLength: 1,
    maxLength: 5,
    pattern: /^[A-Z]+$/,
  },
  EMAIL: {
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  },
  PASSWORD: {
    minLength: 8,
    pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
  },
};

export const BREAKPOINTS = {
  XS: 0,
  SM: 576,
  MD: 768,
  LG: 992,
  XL: 1200,
  XXL: 1400,
};
