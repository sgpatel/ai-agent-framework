package com.aiframework.plugin;

import com.aiframework.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example agent for customer service tasks
 */
public class CustomerServiceAgent implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceAgent.class);

    private AgentStatus status = AgentStatus.INITIALIZING;
    private AgentConfig config;

    // Predefined responses for demo
    private static final List<String> RESPONSES = List.of(
        "Thank you for contacting us. I understand your concern and will help resolve it.",
        "I apologize for any inconvenience. Let me check our records to assist you better.",
        "Your issue has been noted. We'll process your request within 24 hours.",
        "I've escalated your case to our specialized team for immediate attention.",
        "Thank you for your patience. Your request has been successfully processed."
    );

    @Override
    public String getName() {
        return "CustomerServiceAgent";
    }

    @Override
    public String getDescription() {
        return "Handles customer inquiries, complaints, and support requests";
    }

    @Override
    public boolean canHandle(Task task) {
        return "customer_service".equalsIgnoreCase(task.getType()) ||
               "support".equalsIgnoreCase(task.getType()) ||
               "inquiry".equalsIgnoreCase(task.getType()) ||
               "complaint".equalsIgnoreCase(task.getType());
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        logger.info("CustomerServiceAgent executing task: {}", task.getId());

        long startTime = System.currentTimeMillis();

        try {
            // Simulate customer service processing time
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 1500));

            // Generate response based on task description
            String response = generateResponse(task.getDescription());

            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("response", response);
            serviceResult.put("ticketId", "CS-" + System.currentTimeMillis());
            serviceResult.put("priority", task.getPriority().toString());
            serviceResult.put("category", task.getType());
            serviceResult.put("status", "resolved");
            serviceResult.put("satisfactionScore", ThreadLocalRandom.current().nextInt(3, 6)); // 3-5 stars

            AgentResult result = AgentResult.success(task.getId(), getName(), serviceResult);
            result.setMessage("Customer service request processed successfully");
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AgentResult.failure(task.getId(), getName(), "Task interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error in CustomerServiceAgent", e);
            return AgentResult.failure(task.getId(), getName(), "Service request failed: " + e.getMessage());
        }
    }

    private String generateResponse(String inquiry) {
        // Simple response generation based on keywords
        if (inquiry != null) {
            String lowerInquiry = inquiry.toLowerCase();
            if (lowerInquiry.contains("refund") || lowerInquiry.contains("money")) {
                return "I understand you're asking about a refund. I've initiated the refund process for you.";
            } else if (lowerInquiry.contains("delivery") || lowerInquiry.contains("shipping")) {
                return "I've checked your delivery status. Your package is on the way and should arrive soon.";
            } else if (lowerInquiry.contains("account") || lowerInquiry.contains("login")) {
                return "I can help you with your account issue. Please check your email for reset instructions.";
            }
        }

        // Return random response if no specific keywords found
        return RESPONSES.get(ThreadLocalRandom.current().nextInt(RESPONSES.size()));
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        this.status = AgentStatus.READY;
        logger.info("CustomerServiceAgent initialized");
    }

    @Override
    public void shutdown() {
        this.status = AgentStatus.SHUTDOWN;
        logger.info("CustomerServiceAgent shutdown");
    }
}
