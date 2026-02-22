package it.unipi.MarketLens.dto;

import java.time.Instant;
import java.util.List;

public class CampaignRequest {
    private String analystUsername;
    private String name;
    private String brandName;
    private List<String> hashtags;
    private List<String> keywords;

    private Instant startDate;
    private Instant endDate;

    public String getAnalystUsername() { return analystUsername; }
    public void setAnalystUsername(String username) { this.analystUsername = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
}

