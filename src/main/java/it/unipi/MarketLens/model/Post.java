package it.unipi.MarketLens.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;
import java.util.List;

@Document(collection = "posts")
@CompoundIndexes({
        @CompoundIndex(name = "brand_date_idx", def = "{'user_posted': 1, 'date_posted': -1}"),
})
public class Post {

    @Id
    private String id;

    @Field("post_id")
    @Indexed(unique = true, name = "post_id_unique")
    private String postId;

    @Field("user_posted")
    private String userPosted;

    @Field("description")
    private String content;

    private String platform;

    @Field("date_posted")
    @Indexed
    private Instant datePosted;

    private List<String> hashtags;

    @Field("num_comments")
    private Integer numComments;

    @Field("comments")
    @JsonProperty("comments")
    private List<CommentData> commentsList;

    private Integer likes;
    private String industry;

    private NlpData nlp;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserPosted() { return userPosted; }
    public void setUserPosted(String userPosted) { this.userPosted = userPosted; }

    @JsonProperty("description")
    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    @JsonIgnore
    public String getDescription() { return content; }
    public void setDescription(String description) { this.content = description; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public Instant getDatePosted() { return datePosted; }
    public void setDatePosted(Instant datePosted) { this.datePosted = datePosted; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public Integer getNumComments() { return numComments; }
    public void setNumComments(Integer numComments) { this.numComments = numComments; }

    public List<CommentData> getCommentsList() { return commentsList; }
    public void setCommentsList(List<CommentData> commentsList) { this.commentsList = commentsList; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public NlpData getNlp() { return nlp; }
    public void setNlp(NlpData nlp) { this.nlp = nlp; }

    public Long getTotalEngagement() {
        long l = (likes != null) ? likes : 0;
        long c = (numComments != null) ? numComments : 0;
        return l + c;
    }

    public static class CommentData {

        @JsonProperty("comments")
        private String text;
        private Integer likes;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public Integer getLikes() { return likes; }
        public void setLikes(Integer likes) { this.likes = likes; }
    }

    public static class NlpData {
        private SentimentData sentiment;
        private List<String> topics;
        private List<EntityData> entities;


        public SentimentData getSentiment() { return sentiment; }
        public void setSentiment(SentimentData sentiment) { this.sentiment = sentiment; }

        public List<String> getTopics() { return topics; }
        public void setTopics(List<String> topics) { this.topics = topics; }

        public List<EntityData> getEntities() { return entities; }
        public void setEntities(List<EntityData> entities) { this.entities = entities; }
    }

    public static class SentimentData {
        private Double score;
        private String label;

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class EntityData {
        @Field("entity")
        private String entity;
        private String type;

       public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}