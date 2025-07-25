package com.aiframework.communication;

import com.aiframework.core.AgentContext;

/**
 * Model Context Protocol for maintaining and transferring context
 */
public interface ModelContextProtocol {
    /**
     * Create a new context session
     */
    AgentContext createContext(String sessionId, String userId);

    /**
     * Update context with new information
     */
    void updateContext(String sessionId, String key, Object value);

    /**
     * Retrieve context information
     */
    Object getContextValue(String sessionId, String key);

    /**
     * Transfer context between agents
     */
    void transferContext(String fromSessionId, String toSessionId);

    /**
     * Archive context session
     */
    void archiveContext(String sessionId);
}
