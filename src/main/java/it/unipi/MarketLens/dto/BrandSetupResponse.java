package it.unipi.MarketLens.dto;

import it.unipi.MarketLens.model.Industry;
import java.util.List;

public class BrandSetupResponse {
    private String id;
    private String username;
    private String brandName;
    private Industry industry;
    private List<String> monitoringKeywords;
    private List<String> monitoringTopics;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Industry getIndustry() { return industry; }
    public void setIndustry(Industry industry) { this.industry = industry; }

    public List<String> getMonitoringKeywords() { return monitoringKeywords; }
    public void setMonitoringKeywords(List<String> monitoringKeywords) { this.monitoringKeywords = monitoringKeywords; }

    public List<String> getMonitoringTopics() { return monitoringTopics; }
    public void setMonitoringTopics(List<String> monitoringTopics) { this.monitoringTopics = monitoringTopics; }
}