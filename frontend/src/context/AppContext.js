import React, { createContext, useContext, useReducer, useEffect, useCallback } from 'react';
import { agentApi, taskApi } from '../services/api';

const initialState = {
    agents: [],
    tasks: [],
    metrics: null,
    loading: false,
    error: null,
    // Enhanced context management
    agentContexts: {},
    sharedData: {},
    collaborativeWorkflows: [],
    contextSubscriptions: {},
    agentRecommendations: {}
};

const ActionTypes = {
    SET_LOADING: 'SET_LOADING',
    SET_ERROR: 'SET_ERROR',
    SET_AGENTS: 'SET_AGENTS',
    ADD_TASK: 'ADD_TASK',
    UPDATE_TASK: 'UPDATE_TASK',
    SET_METRICS: 'SET_METRICS',
    // Enhanced context actions
    SET_AGENT_CONTEXT: 'SET_AGENT_CONTEXT',
    UPDATE_SHARED_DATA: 'UPDATE_SHARED_DATA',
    ADD_COLLABORATIVE_WORKFLOW: 'ADD_COLLABORATIVE_WORKFLOW',
    SUBSCRIBE_TO_CONTEXT: 'SUBSCRIBE_TO_CONTEXT',
    SET_AGENT_RECOMMENDATIONS: 'SET_AGENT_RECOMMENDATIONS',
    CLEAR_CONTEXT: 'CLEAR_CONTEXT'
};

function reducer(state, action) {
    switch (action.type) {
        case ActionTypes.SET_LOADING:
            return { ...state, loading: action.payload };
        case ActionTypes.SET_ERROR:
            return { ...state, error: action.payload, loading: false };
        case ActionTypes.SET_AGENTS:
            return { ...state, agents: action.payload, loading: false };
        case ActionTypes.ADD_TASK:
            return { ...state, tasks: [...state.tasks, action.payload] };
        case ActionTypes.UPDATE_TASK:
            return {
                ...state,
                tasks: state.tasks.map(t =>
                    t.taskId === action.payload.taskId ? action.payload : t
                )
            };
        case ActionTypes.SET_METRICS:
            return { ...state, metrics: action.payload };
        case ActionTypes.SET_AGENT_CONTEXT:
            return {
                ...state,
                agentContexts: {
                    ...state.agentContexts,
                    [action.payload.agentId]: {
                        ...state.agentContexts[action.payload.agentId],
                        ...action.payload.context,
                        lastUpdated: new Date().toISOString()
                    }
                }
            };
        case ActionTypes.UPDATE_SHARED_DATA:
            return {
                ...state,
                sharedData: {
                    ...state.sharedData,
                    [action.payload.key]: {
                        data: action.payload.data,
                        sourceAgent: action.payload.sourceAgent,
                        timestamp: new Date().toISOString(),
                        metadata: action.payload.metadata || {}
                    }
                }
            };
        case ActionTypes.ADD_COLLABORATIVE_WORKFLOW:
            return {
                ...state,
                collaborativeWorkflows: [...state.collaborativeWorkflows, action.payload]
            };
        case ActionTypes.SUBSCRIBE_TO_CONTEXT:
            return {
                ...state,
                contextSubscriptions: {
                    ...state.contextSubscriptions,
                    [action.payload.subscriberAgent]: [
                        ...(state.contextSubscriptions[action.payload.subscriberAgent] || []),
                        action.payload.contextKey
                    ]
                }
            };
        case ActionTypes.SET_AGENT_RECOMMENDATIONS:
            return {
                ...state,
                agentRecommendations: {
                    ...state.agentRecommendations,
                    [action.payload.agentId]: action.payload.recommendations
                }
            };
        case ActionTypes.CLEAR_CONTEXT:
            return {
                ...state,
                agentContexts: action.payload.agentId
                    ? { ...state.agentContexts, [action.payload.agentId]: {} }
                    : {},
                sharedData: action.payload.dataKey
                    ? { ...state.sharedData, [action.payload.dataKey]: undefined }
                    : {}
            };
        default:
            return state;
    }
}

const AppContext = createContext();

export function AppContextProvider({ children }) {
    const [state, dispatch] = useReducer(reducer, initialState);

    const loadAgents = useCallback(async () => {
        dispatch({ type: ActionTypes.SET_LOADING, payload: true });
        try {
            const res = await agentApi.getAllAgents();
            dispatch({ type: ActionTypes.SET_AGENTS, payload: res.data });
        } catch (e) {
            dispatch({ type: ActionTypes.SET_ERROR, payload: e.message });
        }
    }, []);

    const submitTask = useCallback(async data => {
        dispatch({ type: ActionTypes.SET_LOADING, payload: true });
        try {
            const res = await taskApi.submitTask(data);
            dispatch({ type: ActionTypes.ADD_TASK, payload: res.data });
            dispatch({ type: ActionTypes.SET_LOADING, payload: false });
            return res.data;
        } catch (e) {
            dispatch({ type: ActionTypes.SET_ERROR, payload: e.message });
            throw e;
        }
    }, []);

    const loadMetrics = useCallback(async () => {
        try {
            const res = await taskApi.getMetrics();
            dispatch({ type: ActionTypes.SET_METRICS, payload: res.data });
        } catch {}
    }, []);

    // Enhanced Context Management Functions
    const setAgentContext = useCallback((agentId, context) => {
        dispatch({
            type: ActionTypes.SET_AGENT_CONTEXT,
            payload: { agentId, context }
        });

        // Notify subscribed agents about context updates
        const subscribers = state.contextSubscriptions[agentId] || [];
        subscribers.forEach(subscriberAgent => {
            // Trigger context update notification for subscribers
            generateAgentRecommendations(subscriberAgent);
        });
    }, [state.contextSubscriptions]);

    const updateSharedData = useCallback((key, data, sourceAgent, metadata = {}) => {
        dispatch({
            type: ActionTypes.UPDATE_SHARED_DATA,
            payload: { key, data, sourceAgent, metadata }
        });

        // Auto-generate recommendations for related agents
        generateCollaborativeRecommendations(key, data, sourceAgent);
    }, []);

    const subscribeToContext = useCallback((subscriberAgent, contextKey) => {
        dispatch({
            type: ActionTypes.SUBSCRIBE_TO_CONTEXT,
            payload: { subscriberAgent, contextKey }
        });
    }, []);

    const createCollaborativeWorkflow = useCallback((workflowData) => {
        const workflow = {
            id: Date.now().toString(),
            ...workflowData,
            createdAt: new Date().toISOString(),
            status: 'active'
        };

        dispatch({
            type: ActionTypes.ADD_COLLABORATIVE_WORKFLOW,
            payload: workflow
        });

        return workflow;
    }, []);

    const generateAgentRecommendations = useCallback((agentId) => {
        const agentContext = state.agentContexts[agentId] || {};
        const availableData = Object.keys(state.sharedData);
        const recommendations = [];

        // Example: If agent has stock data, recommend chart visualization
        if (agentContext.dataType === 'stock-analysis' || availableData.includes('stockData')) {
            recommendations.push({
                id: 'chart-visualization',
                type: 'agent-suggestion',
                title: 'Create Interactive Charts',
                description: 'Visualize stock data with interactive charts',
                suggestedAgent: 'chart-visualizer',
                action: 'create-chart',
                priority: 'high',
                dataKeys: ['stockData']
            });
        }

        // Example: If agent has financial data, recommend risk analysis
        if (agentContext.dataType === 'financial-data' || availableData.includes('financialMetrics')) {
            recommendations.push({
                id: 'risk-analysis',
                type: 'agent-suggestion',
                title: 'Perform Risk Assessment',
                description: 'Analyze risk factors based on financial data',
                suggestedAgent: 'risk-assessor',
                action: 'assess-risk',
                priority: 'medium',
                dataKeys: ['financialMetrics']
            });
        }

        dispatch({
            type: ActionTypes.SET_AGENT_RECOMMENDATIONS,
            payload: { agentId, recommendations }
        });
    }, [state.agentContexts, state.sharedData]);

    const generateCollaborativeRecommendations = useCallback((dataKey, data, sourceAgent) => {
        const recommendations = [];

        // Smart recommendations based on data type
        if (dataKey === 'stockData') {
            recommendations.push({
                type: 'visualization',
                agents: ['chart-agent', 'technical-analyzer'],
                chartTypes: ['candlestick', 'line', 'volume', 'bollinger-bands'],
                description: 'Create interactive charts for stock analysis'
            });
        }

        if (dataKey === 'marketSentiment') {
            recommendations.push({
                type: 'analysis',
                agents: ['sentiment-analyzer', 'news-aggregator'],
                analysisTypes: ['trend-prediction', 'sentiment-correlation'],
                description: 'Correlate sentiment with price movements'
            });
        }

        // Store recommendations for all relevant agents
        const relevantAgents = state.agents.filter(agent =>
            recommendations.some(rec => rec.agents.includes(agent.type))
        );

        relevantAgents.forEach(agent => {
            dispatch({
                type: ActionTypes.SET_AGENT_RECOMMENDATIONS,
                payload: {
                    agentId: agent.id,
                    recommendations: recommendations.filter(rec =>
                        rec.agents.includes(agent.type)
                    )
                }
            });
        });
    }, [state.agents]);

    const getContextForAgent = useCallback((agentId) => {
        return state.agentContexts[agentId] || {};
    }, [state.agentContexts]);

    const getSharedData = useCallback((key) => {
        return state.sharedData[key];
    }, [state.sharedData]);

    const getAllSharedData = useCallback(() => {
        return state.sharedData;
    }, [state.sharedData]);

    const clearContext = useCallback((agentId = null, dataKey = null) => {
        dispatch({
            type: ActionTypes.CLEAR_CONTEXT,
            payload: { agentId, dataKey }
        });
    }, []);

    // Chart-specific helper functions
    const suggestChartTypes = useCallback((dataType, dataStructure) => {
        const suggestions = [];

        if (dataType === 'time-series') {
            suggestions.push(
                { type: 'line', suitable: true, description: 'Best for trend analysis' },
                { type: 'candlestick', suitable: true, description: 'Ideal for OHLC data' },
                { type: 'area', suitable: true, description: 'Good for volume visualization' }
            );
        }

        if (dataType === 'comparative') {
            suggestions.push(
                { type: 'bar', suitable: true, description: 'Compare multiple stocks' },
                { type: 'radar', suitable: true, description: 'Multi-dimensional comparison' }
            );
        }

        if (dataType === 'correlation') {
            suggestions.push(
                { type: 'scatter', suitable: true, description: 'Show correlation patterns' },
                { type: 'heatmap', suitable: true, description: 'Correlation matrix' }
            );
        }

        return suggestions;
    }, []);

    useEffect(() => {
        loadAgents();
        loadMetrics();
    }, [loadAgents, loadMetrics]);

    const contextValue = {
        ...state,
        // Original functions
        loadAgents,
        submitTask,
        // Enhanced context functions
        setAgentContext,
        updateSharedData,
        subscribeToContext,
        createCollaborativeWorkflow,
        generateAgentRecommendations,
        getContextForAgent,
        getSharedData,
        getAllSharedData,
        clearContext,
        suggestChartTypes
    };

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
}

export function useAppContext() {
    return useContext(AppContext);
}
