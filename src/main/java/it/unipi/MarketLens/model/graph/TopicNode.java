package it.unipi.MarketLens.model.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.*;

@Node("Topic")
public class TopicNode {

    @Id
    private String name;

    private List<Integer> clusterIds;
    private List<String> industries;

    @Relationship(type = "CO_OCCURS_WITH", direction = Relationship.Direction.OUTGOING)
    private Set<TopicNode> relatedTopics = new HashSet<>();

    public TopicNode(String name) {
        this.name = name;
        this.clusterIds = new ArrayList<>();
        this.industries = new ArrayList<>();
    }
}