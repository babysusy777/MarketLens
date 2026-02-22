package it.unipi.MarketLens.dto;

import java.util.List;

public class BrandSetupRequest {
    private String username;
    private String brandName;
    private List<String> monitoringKeywords;
    private List<String> monitoringTopics;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public List<String> getMonitoringKeywords() { return monitoringKeywords; }
    public void setMonitoringKeywords(List<String> monitoringKeywords) { this.monitoringKeywords = monitoringKeywords; }

    public List<String> getMonitoringTopics() { return monitoringTopics; }
    public void setMonitoringTopics(List<String> monitoringTopics) { this.monitoringTopics = monitoringTopics; }
}