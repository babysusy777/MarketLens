package it.unipi.MarketLens.model.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.ArrayList;
import java.util.List;

@Node("Brand")
public class BrandNode {

    @Id
    private String name;

    private String industry;

    @Relationship(type = "POSTED", direction = Relationship.Direction.OUTGOING)
    private List<PostNode> posts = new ArrayList<>();

    public BrandNode(String name) {
        this.name = name;
    }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getName() { return name; }

    public void addPost(PostNode post) {
        this.posts.add(post);
    }
}