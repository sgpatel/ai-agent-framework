import { useState, useEffect, useCallback, useRef } from 'react';
import { agentApi, stockApi, contextApi } from '../services/api';
import toast from 'react-hot-toast';

// Custom hook for API calls with loading states
export const useApi = (apiCall, dependencies = []) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const mountedRef = useRef(true);

  const execute = useCallback(async (...args) => {
    try {
      setLoading(true);
      setError(null);
      const result = await apiCall(...args);
      if (mountedRef.current) {
        setData(result);
      }
      return result;
    } catch (err) {
      if (mountedRef.current) {
        setError(err.message);
      }
      throw err;
    } finally {
      if (mountedRef.current) {
        setLoading(false);
      }
    }
  }, dependencies);

  useEffect(() => {
    return () => {
      mountedRef.current = false;
    };
  }, []);

  return { data, loading, error, execute };
};

// Hook for managing agents
export const useAgents = () => {
  const [agents, setAgents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchAgents = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await agentApi.getAllAgents();
      setAgents(data);
    } catch (err) {
      setError(err.message);
      toast.error('Failed to fetch agents');
    } finally {
      setLoading(false);
    }
  }, []);

  const reloadAgents = useCallback(async () => {
    try {
      await agentApi.reloadAgents();
      await fetchAgents();
    } catch (err) {
      toast.error('Failed to reload agents');
    }
  }, [fetchAgents]);

  useEffect(() => {
    fetchAgents();
  }, [fetchAgents]);

  return {
    agents,
    loading,
    error,
    fetchAgents,
    reloadAgents,
  };
};

// Hook for stock data management
export const useStock = symbol => {
  const [stockData, setStockData] = useState({
    quote: null,
    history: null,
    indicators: null,
    analysis: null,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStockData = useCallback(
    async (dataType = 'all') => {
      if (!symbol) return;

      try {
        setLoading(true);
        setError(null);

        const promises = [];
        const updates = {};

        if (dataType === 'all' || dataType === 'quote') {
          promises.push(
            stockApi.getRealTimeQuote(symbol).then(data => {
              updates.quote = data;
            }),
          );
        }

        if (dataType === 'all' || dataType === 'history') {
          promises.push(
            stockApi.getHistoricalData(symbol).then(data => {
              updates.history = data;
            }),
          );
        }

        if (dataType === 'all' || dataType === 'indicators') {
          promises.push(
            stockApi.getTechnicalIndicators(symbol).then(data => {
              updates.indicators = data;
            }),
          );
        }

        if (dataType === 'all' || dataType === 'analysis') {
          promises.push(
            stockApi.getCompleteAnalysis(symbol).then(data => {
              updates.analysis = data;
            }),
          );
        }

        await Promise.allSettled(promises);
        setStockData(prev => ({ ...prev, ...updates }));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    },
    [symbol],
  );

  useEffect(() => {
    if (symbol) {
      fetchStockData();
    }
  }, [symbol, fetchStockData]);

  return {
    stockData,
    loading,
    error,
    refetch: fetchStockData,
  };
};

// Hook for local storage with JSON parsing
export const useLocalStorage = (key, initialValue) => {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error);
      return initialValue;
    }
  });

  const setValue = useCallback(
    value => {
      try {
        const valueToStore = value instanceof Function ? value(storedValue) : value;
        setStoredValue(valueToStore);
        window.localStorage.setItem(key, JSON.stringify(valueToStore));
      } catch (error) {
        console.error(`Error setting localStorage key "${key}":`, error);
      }
    },
    [key, storedValue],
  );

  const removeValue = useCallback(() => {
    try {
      window.localStorage.removeItem(key);
      setStoredValue(initialValue);
    } catch (error) {
      console.error(`Error removing localStorage key "${key}":`, error);
    }
  }, [key, initialValue]);

  return [storedValue, setValue, removeValue];
};

// Hook for debounced values
export const useDebounce = (value, delay) => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

// Hook for websocket connections
export const useWebSocket = (url, options = {}) => {
  const [socket, setSocket] = useState(null);
  const [lastMessage, setLastMessage] = useState(null);
  const [readyState, setReadyState] = useState(0);

  useEffect(() => {
    const ws = new WebSocket(url);

    ws.onopen = () => {
      setReadyState(ws.readyState);
      if (options.onOpen) options.onOpen();
    };

    ws.onmessage = event => {
      const message = JSON.parse(event.data);
      setLastMessage(message);
      if (options.onMessage) options.onMessage(message);
    };

    ws.onclose = () => {
      setReadyState(ws.readyState);
      if (options.onClose) options.onClose();
    };

    ws.onerror = error => {
      console.error('WebSocket error:', error);
      if (options.onError) options.onError(error);
    };

    setSocket(ws);

    return () => {
      ws.close();
    };
  }, [url]);

  const sendMessage = useCallback(
    message => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(message));
      }
    },
    [socket],
  );

  return {
    socket,
    lastMessage,
    readyState,
    sendMessage,
  };
};
