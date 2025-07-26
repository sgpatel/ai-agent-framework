import React, { createContext, useContext, useReducer, useEffect, useCallback } from 'react';
import { agentApi, taskApi } from '../services/api';
import { storage } from '../utils';
import toast from 'react-hot-toast';

const initialState = {
  // Core state
  agents: [],
  tasks: [],
  metrics: null,
  loading: false,
  error: null,

  // Authentication state
  user: null,
  token: null,
  isAuthenticated: false,
  authLoading: false,

  // Enhanced context management
  agentContexts: {},
  sharedData: {},
  collaborativeWorkflows: [],
  contextSubscriptions: {},
  agentRecommendations: {},

  // UI state
  theme: storage.get('theme', 'dark'),
  sidebarCollapsed: storage.get('sidebarCollapsed', false),
  notifications: [],

  // User preferences
  preferences: storage.get('userPreferences', {
    defaultStockSymbol: 'AAPL',
    refreshInterval: 30000,
    chartType: 'candlestick',
    showAdvancedMetrics: true,
  }),
};

const ActionTypes = {
  // Loading and error states
  SET_LOADING: 'SET_LOADING',
  SET_ERROR: 'SET_ERROR',
  CLEAR_ERROR: 'CLEAR_ERROR',

  // Authentication actions
  LOGIN_START: 'LOGIN_START',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGIN_FAILURE: 'LOGIN_FAILURE',
  LOGOUT: 'LOGOUT',
  SET_AUTH_LOADING: 'SET_AUTH_LOADING',
  UPDATE_USER: 'UPDATE_USER',

  // Agent management
  SET_AGENTS: 'SET_AGENTS',
  ADD_AGENT: 'ADD_AGENT',
  UPDATE_AGENT: 'UPDATE_AGENT',
  REMOVE_AGENT: 'REMOVE_AGENT',

  // Task management
  SET_TASKS: 'SET_TASKS',
  ADD_TASK: 'ADD_TASK',
  UPDATE_TASK: 'UPDATE_TASK',
  REMOVE_TASK: 'REMOVE_TASK',

  // Metrics
  SET_METRICS: 'SET_METRICS',

  // Enhanced context actions
  SET_AGENT_CONTEXT: 'SET_AGENT_CONTEXT',
  UPDATE_SHARED_DATA: 'UPDATE_SHARED_DATA',
  ADD_COLLABORATIVE_WORKFLOW: 'ADD_COLLABORATIVE_WORKFLOW',
  SUBSCRIBE_TO_CONTEXT: 'SUBSCRIBE_TO_CONTEXT',
  SET_AGENT_RECOMMENDATIONS: 'SET_AGENT_RECOMMENDATIONS',
  CLEAR_CONTEXT: 'CLEAR_CONTEXT',

  // UI actions
  SET_THEME: 'SET_THEME',
  TOGGLE_SIDEBAR: 'TOGGLE_SIDEBAR',
  ADD_NOTIFICATION: 'ADD_NOTIFICATION',
  REMOVE_NOTIFICATION: 'REMOVE_NOTIFICATION',
  CLEAR_NOTIFICATIONS: 'CLEAR_NOTIFICATIONS',

  // Preferences
  UPDATE_PREFERENCES: 'UPDATE_PREFERENCES',
  RESET_PREFERENCES: 'RESET_PREFERENCES',
};

function reducer(state, action) {
  switch (action.type) {
  case ActionTypes.SET_LOADING:
    return { ...state, loading: action.payload };

  case ActionTypes.SET_ERROR:
    return { ...state, error: action.payload, loading: false };

  case ActionTypes.CLEAR_ERROR:
    return { ...state, error: null };

  // Authentication cases
  case ActionTypes.LOGIN_START:
    return { ...state, authLoading: true, error: null };

  case ActionTypes.LOGIN_SUCCESS: {
    const { user, token } = action.payload;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    return {
      ...state,
      user,
      token,
      isAuthenticated: true,
      authLoading: false,
      error: null,
    };
  }

  case ActionTypes.LOGIN_FAILURE:
    return {
      ...state,
      user: null,
      token: null,
      isAuthenticated: false,
      authLoading: false,
      error: action.payload,
    };

  case ActionTypes.LOGOUT:
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    return {
      ...state,
      user: null,
      token: null,
      isAuthenticated: false,
      authLoading: false,
      error: null,
    };

  case ActionTypes.SET_AUTH_LOADING:
    return { ...state, authLoading: action.payload };

  case ActionTypes.UPDATE_USER: {
    const updatedUser = { ...state.user, ...action.payload };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    return { ...state, user: updatedUser };
  }

  case ActionTypes.SET_AGENTS:
    return { ...state, agents: action.payload, loading: false, error: null };

  case ActionTypes.ADD_AGENT:
    return { ...state, agents: [...state.agents, action.payload] };

  case ActionTypes.UPDATE_AGENT:
    return {
      ...state,
      agents: state.agents.map(agent =>
        agent.id === action.payload.id ? { ...agent, ...action.payload } : agent,
      ),
    };

  case ActionTypes.REMOVE_AGENT:
    return {
      ...state,
      agents: state.agents.filter(agent => agent.id !== action.payload),
    };

  case ActionTypes.SET_TASKS:
    return { ...state, tasks: action.payload };

  case ActionTypes.ADD_TASK:
    return { ...state, tasks: [...state.tasks, action.payload] };

  case ActionTypes.UPDATE_TASK:
    return {
      ...state,
      tasks: state.tasks.map(task =>
        task.taskId === action.payload.taskId ? { ...task, ...action.payload } : task,
      ),
    };

  case ActionTypes.REMOVE_TASK:
    return {
      ...state,
      tasks: state.tasks.filter(task => task.taskId !== action.payload),
    };

  case ActionTypes.SET_METRICS:
    return { ...state, metrics: action.payload };

    // Enhanced context management
  case ActionTypes.SET_AGENT_CONTEXT:
    return {
      ...state,
      agentContexts: {
        ...state.agentContexts,
        [action.payload.agentId]: action.payload.context,
      },
    };

  case ActionTypes.UPDATE_SHARED_DATA:
    return {
      ...state,
      sharedData: { ...state.sharedData, ...action.payload },
    };

  case ActionTypes.ADD_COLLABORATIVE_WORKFLOW:
    return {
      ...state,
      collaborativeWorkflows: [...state.collaborativeWorkflows, action.payload],
    };

  case ActionTypes.SUBSCRIBE_TO_CONTEXT:
    return {
      ...state,
      contextSubscriptions: {
        ...state.contextSubscriptions,
        [action.payload.subscriberId]: action.payload.subscriptions,
      },
    };

  case ActionTypes.SET_AGENT_RECOMMENDATIONS:
    return {
      ...state,
      agentRecommendations: {
        ...state.agentRecommendations,
        [action.payload.agentId]: action.payload.recommendations,
      },
    };

  case ActionTypes.CLEAR_CONTEXT:
    return {
      ...state,
      agentContexts: {},
      sharedData: {},
      collaborativeWorkflows: [],
      contextSubscriptions: {},
      agentRecommendations: {},
    };

    // UI state management
  case ActionTypes.SET_THEME:
    storage.set('theme', action.payload);
    return { ...state, theme: action.payload };

  case ActionTypes.TOGGLE_SIDEBAR: {
    const collapsed = !state.sidebarCollapsed;
    storage.set('sidebarCollapsed', collapsed);
    return { ...state, sidebarCollapsed: collapsed };
  }

  case ActionTypes.ADD_NOTIFICATION:
    return {
      ...state,
      notifications: [
        ...state.notifications,
        {
          id: Date.now(),
          timestamp: new Date(),
          ...action.payload,
        },
      ],
    };

  case ActionTypes.REMOVE_NOTIFICATION:
    return {
      ...state,
      notifications: state.notifications.filter(n => n.id !== action.payload),
    };

  case ActionTypes.CLEAR_NOTIFICATIONS:
    return { ...state, notifications: [] };

    // Preferences management
  case ActionTypes.UPDATE_PREFERENCES: {
    const newPreferences = { ...state.preferences, ...action.payload };
    storage.set('userPreferences', newPreferences);
    return { ...state, preferences: newPreferences };
  }

  case ActionTypes.RESET_PREFERENCES:
    storage.remove('userPreferences');
    return { ...state, preferences: initialState.preferences };

  default:
    return state;
  }
}

const AppContext = createContext();

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within an AppContextProvider');
  }
  return context;
};

export const AppContextProvider = ({ children }) => {
  const [state, dispatch] = useReducer(reducer, initialState);

  // Initialize authentication from localStorage
  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (token && user) {
      try {
        const parsedUser = JSON.parse(user);
        dispatch({
          type: ActionTypes.LOGIN_SUCCESS,
          payload: { user: parsedUser, token },
        });
      } catch (error) {
        console.error('Error parsing stored user data:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
  }, []);

  // Enhanced action creators with error handling
  const actions = {
    // Loading and error management
    setLoading: useCallback(loading => {
      dispatch({ type: ActionTypes.SET_LOADING, payload: loading });
    }, []),

    setError: useCallback(error => {
      dispatch({ type: ActionTypes.SET_ERROR, payload: error });
      toast.error(error);
    }, []),

    clearError: useCallback(() => {
      dispatch({ type: ActionTypes.CLEAR_ERROR });
    }, []),

    // Authentication actions
    login: useCallback((user, token) => {
      dispatch({ type: ActionTypes.LOGIN_SUCCESS, payload: { user, token } });
    }, []),

    logout: useCallback(() => {
      dispatch({ type: ActionTypes.LOGOUT });
    }, []),

    updateUser: useCallback((userUpdates) => {
      dispatch({ type: ActionTypes.UPDATE_USER, payload: userUpdates });
    }, []),

    toggleTheme: useCallback(() => {
      const newTheme = state.theme === 'dark' ? 'light' : 'dark';
      dispatch({ type: ActionTypes.SET_THEME, payload: newTheme });
    }, [state.theme]),

    // Agent management
    fetchAgents: useCallback(async () => {
      try {
        dispatch({ type: ActionTypes.SET_LOADING, payload: true });
        const agents = await agentApi.getAllAgents();
        dispatch({ type: ActionTypes.SET_AGENTS, payload: agents });
      } catch (error) {
        dispatch({ type: ActionTypes.SET_ERROR, payload: error.message });
      }
    }, []),

    reloadAgents: useCallback(async () => {
      try {
        await agentApi.reloadAgents();
        const agents = await agentApi.getAllAgents();
        dispatch({ type: ActionTypes.SET_AGENTS, payload: agents });
        toast.success('Agents reloaded successfully');
      } catch (error) {
        dispatch({ type: ActionTypes.SET_ERROR, payload: error.message });
      }
    }, []),

    // Task management
    submitTask: useCallback(async taskData => {
      try {
        const task = await taskApi.submitTask(taskData);
        dispatch({ type: ActionTypes.ADD_TASK, payload: task });
        toast.success('Task submitted successfully');
        return task;
      } catch (error) {
        dispatch({ type: ActionTypes.SET_ERROR, payload: error.message });
        throw error;
      }
    }, []),

    updateTask: useCallback(task => {
      dispatch({ type: ActionTypes.UPDATE_TASK, payload: task });
    }, []),

    // Context management
    setAgentContext: useCallback((agentId, context) => {
      dispatch({
        type: ActionTypes.SET_AGENT_CONTEXT,
        payload: { agentId, context },
      });
    }, []),

    updateSharedData: useCallback(data => {
      dispatch({ type: ActionTypes.UPDATE_SHARED_DATA, payload: data });
    }, []),

    addCollaborativeWorkflow: useCallback(workflow => {
      dispatch({ type: ActionTypes.ADD_COLLABORATIVE_WORKFLOW, payload: workflow });
    }, []),

    // UI management
    setTheme: useCallback(theme => {
      dispatch({ type: ActionTypes.SET_THEME, payload: theme });
    }, []),

    toggleSidebar: useCallback(() => {
      dispatch({ type: ActionTypes.TOGGLE_SIDEBAR });
    }, []),

    addNotification: useCallback(notification => {
      dispatch({ type: ActionTypes.ADD_NOTIFICATION, payload: notification });
    }, []),

    removeNotification: useCallback(id => {
      dispatch({ type: ActionTypes.REMOVE_NOTIFICATION, payload: id });
    }, []),

    // Preferences management
    updatePreferences: useCallback(preferences => {
      dispatch({ type: ActionTypes.UPDATE_PREFERENCES, payload: preferences });
    }, []),

    resetPreferences: useCallback(() => {
      dispatch({ type: ActionTypes.RESET_PREFERENCES });
    }, []),
  };

  // Initialize app data
  useEffect(() => {
    actions.fetchAgents();
  }, [actions.fetchAgents]);

  // Auto-remove notifications after 5 seconds
  useEffect(() => {
    const timeouts = state.notifications.map(notification => {
      if (notification.autoRemove !== false) {
        return setTimeout(() => {
          actions.removeNotification(notification.id);
        }, 5000);
      }
      return null;
    });

    return () => {
      timeouts.forEach(timeout => {
        if (timeout) clearTimeout(timeout);
      });
    };
  }, [state.notifications, actions]);

  const contextValue = {
    ...state,
    ...actions,
    // Computed values
    activeAgents: state.agents.filter(agent => agent.status === 'ACTIVE'),
    pendingTasks: state.tasks.filter(task => task.status === 'PENDING'),
    completedTasks: state.tasks.filter(task => task.status === 'COMPLETED'),
  };

  return <AppContext.Provider value={contextValue}>{children}</AppContext.Provider>;
};
