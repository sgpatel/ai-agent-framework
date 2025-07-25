package com.aiframework.communication;

import com.aiframework.core.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Implementation of Model Context Protocol
 */
@Service
public class ModelContextProtocolImpl implements ModelContextProtocol {
    private static final Logger logger = LoggerFactory.getLogger(ModelContextProtocolImpl.class);

    private final Map<String, AgentContext> contextStore = new ConcurrentHashMap<>();

    @Override
    public AgentContext createContext(String sessionId, String userId) {
        AgentContext context = new AgentContext(sessionId, userId);
        contextStore.put(sessionId, context);
        logger.info("MCP: Created context for session: {}, user: {}", sessionId, userId);
        return context;
    }

    @Override
    public void updateContext(String sessionId, String key, Object value) {
        AgentContext context = contextStore.get(sessionId);
        if (context != null) {
            context.put(key, value);
            logger.debug("MCP: Updated context {} with key: {}", sessionId, key);
        } else {
            logger.warn("MCP: Context not found for session: {}", sessionId);
        }
    }

    @Override
    public Object getContextValue(String sessionId, String key) {
        AgentContext context = contextStore.get(sessionId);
        if (context != null) {
            return context.get(key);
        } else {
            logger.warn("MCP: Context not found for session: {}", sessionId);
            return null;
        }
    }

    @Override
    public void transferContext(String fromSessionId, String toSessionId) {
        AgentContext fromContext = contextStore.get(fromSessionId);
        if (fromContext != null) {
            AgentContext toContext = new AgentContext(toSessionId, fromContext.getUserId());
            toContext.setSharedData(new java.util.HashMap<>(fromContext.getSharedData()));
            toContext.setConfiguration(new java.util.HashMap<>(fromContext.getConfiguration()));
            contextStore.put(toSessionId, toContext);
            logger.info("MCP: Transferred context from {} to {}", fromSessionId, toSessionId);
        } else {
            logger.warn("MCP: Source context not found for session: {}", fromSessionId);
        }
    }

    @Override
    public void archiveContext(String sessionId) {
        AgentContext context = contextStore.remove(sessionId);
        if (context != null) {
            logger.info("MCP: Archived context for session: {}", sessionId);
            // TODO: Store in persistent archive
        } else {
            logger.warn("MCP: Context not found for archiving: {}", sessionId);
        }
    }
}
