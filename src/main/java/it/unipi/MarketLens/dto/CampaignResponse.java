package it.unipi.MarketLens.dto;

import it.unipi.MarketLens.dto.BrandMetricsResponse.TimePoint;
import java.time.Instant;
import java.util.List;

public class CampaignResponse {


    private String id;
    private String name;
    private Instant createdAt;


    private String brandAuthor;
    private List<String> hashtags;
    private List<String> keywords;
    private Instant startDate;
    private Instant endDate;
    private String message;


    private long totalPosts;
    private long totalEngagement;


    private SentimentDistribution sentimentDistribution;

    private List<TimePoint> sentimentEvolution;


    public static class SentimentDistribution {
        private double positivePercent;
        private double negativePercent;
        private double neutralPercent;

        public SentimentDistribution(double pos, double neg, double neu) {
            this.positivePercent = pos;
            this.negativePercent = neg;
            this.neutralPercent = neu;
        }


        public double getPositivePercent() { return positivePercent; }
        public double getNegativePercent() { return negativePercent; }
        public double getNeutralPercent() { return neutralPercent; }
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getBrandAuthor() { return brandAuthor; }
    public void setBrandAuthor(String brandAuthor) { this.brandAuthor = brandAuthor; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public long getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(long totalEngagement) { this.totalEngagement = totalEngagement; }

    public SentimentDistribution getSentimentDistribution() { return sentimentDistribution; }
    public void setSentimentDistribution(SentimentDistribution sentimentDistribution) { this.sentimentDistribution = sentimentDistribution; }

    public List<TimePoint> getSentimentEvolution() { return sentimentEvolution; }
    public void setSentimentEvolution(List<TimePoint> sentimentEvolution) { this.sentimentEvolution = sentimentEvolution; }


    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}