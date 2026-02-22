package it.unipi.MarketLens.model.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node("Post")
public class PostNode {

    @Id
    private String postId;

    @Relationship(type = "TOPIC_EXTRACTION", direction = Relationship.Direction.OUTGOING)
    private Set<TopicNode> topics = new HashSet<>();

    public PostNode(String postId) {
        this.postId = postId;
    }

   public void addTopic(TopicNode topic) {
        this.topics.add(topic);
    }

    public String getPostId() { return postId; }
}