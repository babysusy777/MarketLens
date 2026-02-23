package it.unipi.MarketLens.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Document(collection = "brands")
public class Brand {

    @Id
    private String id;

    @Field("name")
    @Indexed(unique = true, name = "brand_name_unique")
    private String brandName;

    private String username;
    

    private Industry industry;

    private List<String> monitoringKeywords = new ArrayList<>();
    private List<String> monitoringTopics = new ArrayList<>();

    private Long totalPosts = 0L;
    private Long totalEngagement = 0L; // likes + comments
    private Double avgSentiment = 0.0;

    private LocalDateTime analyticsLastRefreshedAt;

    private List<MetricsTimePoint> sentimentEvolution = new ArrayList<>();
    private List<MetricsTimePoint> engagementEvolution = new ArrayList<>();

    private List<Competitor> competitors = new ArrayList<>();

    public Brand() {}

    public Brand(String brandName, Industry industry, String username) {
        this.brandName = brandName;
        this.industry = industry;
        this.username = username;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Industry getIndustry() { return industry; }
    public void setIndustry(Industry industry) { this.industry = industry; }

    public List<String> getMonitoringKeywords() { return monitoringKeywords; }
    public void setMonitoringKeywords(List<String> monitoringKeywords) { this.monitoringKeywords = monitoringKeywords; }

    public List<String> getMonitoringTopics() { return monitoringTopics; }
    public void setMonitoringTopics(List<String> monitoringTopics) { this.monitoringTopics = monitoringTopics; }

    public Long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(Long totalPosts) { this.totalPosts = totalPosts; }

    public Long getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(Long totalEngagement) { this.totalEngagement = totalEngagement; }

    public Double getAvgSentiment() { return avgSentiment; }
    public void setAvgSentiment(Double avgSentiment) { this.avgSentiment = avgSentiment; }

    public LocalDateTime getAnalyticsLastRefreshedAt() { return analyticsLastRefreshedAt; }
    public void setAnalyticsLastRefreshedAt(LocalDateTime analyticsLastRefreshedAt) { this.analyticsLastRefreshedAt = analyticsLastRefreshedAt; }

    public List<MetricsTimePoint> getSentimentEvolution() { return sentimentEvolution; }
    public void setSentimentEvolution(List<MetricsTimePoint> sentimentEvolution) { this.sentimentEvolution = sentimentEvolution; }

    public List<MetricsTimePoint> getEngagementEvolution() { return engagementEvolution; }
    public void setEngagementEvolution(List<MetricsTimePoint> engagementEvolution) { this.engagementEvolution = engagementEvolution; }

    public List<Competitor> getCompetitors() { return competitors; }
    public void setCompetitors(List<Competitor> competitors) { this.competitors = competitors; }


    public static class MetricsTimePoint {
        private String date;
        private Double value;

       // public MetricsTimePoint() {}
        public MetricsTimePoint(String date, Double value) {
            this.date = date;
            this.value = value;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }

    /*
     * Rappresenta un competitor con il punteggio di similarità
     */
    public static class Competitor {
        private String brand;          // Nome del competitor
        private Double similarityScore; // Score da 0.0 a 1.0
      //  public Competitor() {}
        public Competitor(String brand, Double similarityScore) {
            this.brand = brand;
            this.similarityScore = similarityScore;
        }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public Double getSimilarityScore() { return similarityScore; }
       // public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    }
}