package it.unipi.MarketLens.dto;

import java.time.Instant;
import java.util.List;

public class BrandMetricsResponse {

    private String brandName;
    private long totalPosts;
    private long totalEngagement;
    private double avgSentiment;

    private List<TimePoint> sentimentEvolution;
    private List<TimePoint> engagementEvolution;

    public BrandMetricsResponse() {}

    public BrandMetricsResponse(long totalPosts,
                                long totalEngagement,
                                double avgSentiment,
                                List<TimePoint> sentimentEvolution,
                                List<TimePoint> engagementEvolution) {
        this.totalPosts = totalPosts;
        this.totalEngagement = totalEngagement;
        this.avgSentiment = avgSentiment;
        this.sentimentEvolution = sentimentEvolution;
        this.engagementEvolution = engagementEvolution;
    }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public long getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(long totalEngagement) { this.totalEngagement = totalEngagement; }

    public double getAvgSentiment() { return avgSentiment; }
    public void setAvgSentiment(double avgSentiment) { this.avgSentiment = avgSentiment; }

    public List<TimePoint> getSentimentEvolution() { return sentimentEvolution; }
    public void setSentimentEvolution(List<TimePoint> sentimentEvolution) { this.sentimentEvolution = sentimentEvolution; }

    public List<TimePoint> getEngagementEvolution() { return engagementEvolution; }
    public void setEngagementEvolution(List<TimePoint> engagementEvolution) { this.engagementEvolution = engagementEvolution; }


    public static class TimePoint {
        private String date;
        private Double value;

        public TimePoint(Instant date, double engagementVal) {}

        public TimePoint(String date, Double value) {
            this.date = date;
            this.value = value;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }
}