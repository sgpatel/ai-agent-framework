package com.aiframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llm.local")
public class GPT4AllConfig {

    private Api api = new Api();
    private int maxTokens = 1024;
    private float temperature = 0.7f;

    public static class Api {
        private String url = "http://localhost:4891/v1";
        private boolean enabled = true;
        private int timeout = 30000;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
}
