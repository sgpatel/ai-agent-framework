package com.aiframework.communication;

import com.aiframework.core.AgentResult;
import com.aiframework.core.Task;

/**
 * Agent Communication Protocol for inter-agent communication
 */
public interface AgentCommunicationProtocol {
    /**
     * Notify that a task has been started
     */
    void notifyTaskStarted(Task task);

    /**
     * Notify that a task has been completed
     */
    void notifyTaskCompleted(Task task, AgentResult result);

    /**
     * Send a message between agents
     */
    void sendMessage(String fromAgent, String toAgent, Object message);

    /**
     * Broadcast a message to all agents
     */
    void broadcastMessage(String fromAgent, Object message);
}
