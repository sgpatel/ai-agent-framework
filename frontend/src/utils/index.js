// Formatting utilities
export const formatCurrency = (value, currency = 'USD') => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

export const formatNumber = (value, decimals = 2) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
};

export const formatPercentage = (value, decimals = 2) => {
  return `${(value * 100).toFixed(decimals)}%`;
};

export const formatDate = (date, format = 'short') => {
  const options = {
    short: { month: 'short', day: 'numeric', year: 'numeric' },
    long: {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    time: {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    },
  };

  return new Intl.DateTimeFormat('en-US', options[format]).format(new Date(date));
};

// Validation utilities
export const validateEmail = email => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

export const validateStockSymbol = symbol => {
  const re = /^[A-Z]{1,5}$/;
  return re.test(symbol?.toUpperCase());
};

export const validateRequired = value => {
  return value !== null && value !== undefined && value !== '';
};

// Data manipulation utilities
export const groupBy = (array, key) => {
  return array.reduce((result, item) => {
    const group = item[key];
    if (!result[group]) {
      result[group] = [];
    }
    result[group].push(item);
    return result;
  }, {});
};

export const sortBy = (array, key, direction = 'asc') => {
  return array.sort((a, b) => {
    const aVal = a[key];
    const bVal = b[key];

    if (direction === 'asc') {
      return aVal > bVal ? 1 : -1;
    } else {
      return aVal < bVal ? 1 : -1;
    }
  });
};

export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

export const throttle = (func, limit) => {
  let inThrottle;
  return function () {
    const args = arguments;
    const context = this;
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

// Color utilities for charts and data visualization
export const generateColorPalette = count => {
  const colors = [
    '#007bff',
    '#28a745',
    '#dc3545',
    '#ffc107',
    '#17a2b8',
    '#6f42c1',
    '#e83e8c',
    '#fd7e14',
    '#20c997',
    '#6c757d',
  ];

  if (count <= colors.length) {
    return colors.slice(0, count);
  }

  // Generate additional colors if needed
  const additionalColors = [];
  for (let i = colors.length; i < count; i++) {
    const hue = (i * 137.508) % 360; // Golden angle approximation
    additionalColors.push(`hsl(${hue}, 70%, 50%)`);
  }

  return [...colors, ...additionalColors];
};

export const getChangeColor = value => {
  if (value > 0) return '#28a745'; // Green for positive
  if (value < 0) return '#dc3545'; // Red for negative
  return '#6c757d'; // Gray for neutral
};

// Performance utilities
export const memoize = fn => {
  const cache = new Map();
  return (...args) => {
    const key = JSON.stringify(args);
    if (cache.has(key)) {
      return cache.get(key);
    }
    const result = fn(...args);
    cache.set(key, result);
    return result;
  };
};

export const deepClone = obj => {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj);
  if (obj instanceof Array) return obj.map(item => deepClone(item));
  if (typeof obj === 'object') {
    const clonedObj = {};
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj;
  }
};

// Error handling utilities
export const handleApiError = (error, fallbackMessage = 'An error occurred') => {
  console.error('API Error:', error);

  if (error.response) {
    // Server responded with error status
    return error.response.data?.message || `Server error: ${error.response.status}`;
  } else if (error.request) {
    // Request was made but no response received
    return 'Network error: Please check your connection';
  } else {
    // Something else happened
    return error.message || fallbackMessage;
  }
};

export const createErrorHandler = context => error => {
  const message = handleApiError(error);
  console.error(`Error in ${context}:`, message);
  return message;
};

// Storage utilities
export const storage = {
  get: (key, defaultValue = null) => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : defaultValue;
    } catch (error) {
      console.error(`Error reading from localStorage: ${key}`, error);
      return defaultValue;
    }
  },

  set: (key, value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error(`Error writing to localStorage: ${key}`, error);
      return false;
    }
  },

  remove: key => {
    try {
      localStorage.removeItem(key);
      return true;
    } catch (error) {
      console.error(`Error removing from localStorage: ${key}`, error);
      return false;
    }
  },

  clear: () => {
    try {
      localStorage.clear();
      return true;
    } catch (error) {
      console.error('Error clearing localStorage', error);
      return false;
    }
  },
};

// Chart utilities
export const prepareChartData = (data, xKey, yKey) => {
  return data.map(item => ({
    x: item[xKey],
    y: item[yKey],
    ...item,
  }));
};

export const calculateMovingAverage = (data, period) => {
  const result = [];
  for (let i = period - 1; i < data.length; i++) {
    const sum = data.slice(i - period + 1, i + 1).reduce((a, b) => a + b, 0);
    result.push(sum / period);
  }
  return result;
};
