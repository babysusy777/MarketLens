package it.unipi.MarketLens.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CampaignCompetitorComparisonResponse {

    private String campaignId;
    private String campaignName;
    private String mainBrand;
    private Instant startDate;
    private Instant endDate;
    private long totalPosts;
    private double totalEngagement;
    private double avgSentiment;

    private List<String> hashtags;
    private List<String> keywords;
    private String message;


    private List<CompetitorStats> competitors = new ArrayList<>();

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getMainBrand() { return mainBrand; }
    public void setMainBrand(String mainBrand) { this.mainBrand = mainBrand; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public double getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(double totalEngagement) { this.totalEngagement = totalEngagement; }

    public double getAvgSentiment() { return avgSentiment; }
    public void setAvgSentiment(double avgSentiment) { this.avgSentiment = avgSentiment; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<CompetitorStats> getCompetitors() { return competitors; }
    public void setCompetitors(List<CompetitorStats> competitors) { this.competitors = competitors; }


    public static class CompetitorStats {
        private String brand;
        private Double similarityScore;
        private long totalPosts;
        private double totalEngagement;
        private double avgSentiment;
        private List<String> hashtags;
        private List<String> keywords;
        private String message;

        public CompetitorStats() {}

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public Double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

        public long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

        public double getTotalEngagement() { return totalEngagement; }
        public void setTotalEngagement(double totalEngagement) { this.totalEngagement = totalEngagement; }

        public double getAvgSentiment() { return avgSentiment; }
        public void setAvgSentiment(double avgSentiment) { this.avgSentiment = avgSentiment; }

        public List<String> getHashtags() { return hashtags; }
        public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}