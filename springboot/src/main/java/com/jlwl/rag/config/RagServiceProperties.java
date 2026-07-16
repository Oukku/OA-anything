package com.jlwl.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 服务连接配置。
 * application.yml:
 *   rag:
 *     service-url: http://localhost:8001
 *     timeout-ms: 60000
 */
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagServiceProperties {
    private String serviceUrl = "http://localhost:8001";
    private int timeoutMs = 60_000;

    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
}
