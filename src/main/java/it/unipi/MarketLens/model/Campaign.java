package it.unipi.MarketLens.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "campaigns")
@CompoundIndexes({
        @CompoundIndex(name = "unique_campaign_per_user", def = "{'name': 1, 'analystUsername': 1}", unique = true)
})
public class Campaign {

    @Id
    private String id;
    private String name;
    private String analystUsername;


    private String brandAuthor;
    private List<String> hashtags;
    private List<String> keywords;
    private List<String> platforms;
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt = Instant.now();
    private String message;

    private long totalPosts;
    private double totalEngagement;
    private double avgSentiment;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAnalystUsername() { return analystUsername; }
    public void setAnalystUsername(String analystUsername) { this.analystUsername = analystUsername; }

    public String getBrandAuthor() { return brandAuthor; }
    public void setBrandAuthor(String brandAuthor) { this.brandAuthor = brandAuthor; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Instant getCreatedAt() { return createdAt; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public double getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(double totalEngagement) { this.totalEngagement = totalEngagement; }

    public double getAvgSentiment() { return avgSentiment; }
    public void setAvgSentiment(double avgSentiment) { this.avgSentiment = avgSentiment; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}