package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.Post;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

@Service
public class GraphIngestionService {

    private final Driver driver;

    private static final Set<String> STOP_TOPICS = Set.of(
            "fashion"
    );

    public GraphIngestionService(Driver driver) {
        this.driver = driver;
    }

    public void savePostToGraph(Post post) {


        String industryRaw = post.getIndustry();
        String industry =
                (industryRaw == null || industryRaw.isBlank())
                        ? "UNKNOWN"
                        : industryRaw.toUpperCase(Locale.ROOT);

        Map<String, Object> params = new HashMap<>();
        params.put("brandName", post.getUserPosted());
        params.put("industry", industry);
        params.put("postId", post.getPostId());
        params.put("platform", post.getPlatform());
        params.put("topics", post.getNlp() != null && post.getNlp().getTopics() != null
                ? post.getNlp().getTopics()
                : List.of());

        String cypherQuery =
                // ===== BRAND =====
                "MERGE (b:Brand {name: $brandName})\n" +
                        "ON CREATE SET b.industry =\n" +
                        "  CASE\n" +
                        "    WHEN $industry = 'UNKNOWN' THEN 'UNKNOWN'\n" +
                        "    ELSE $industry\n" +
                        "  END\n" +
                        "ON MATCH SET b.industry =\n" +
                        "  CASE\n" +
                        "    WHEN b.industry IS NULL OR b.industry = '' OR b.industry = 'UNKNOWN'\n" +
                        "      THEN CASE\n" +
                        "        WHEN $industry = 'UNKNOWN' THEN b.industry\n" +
                        "        ELSE $industry\n" +
                        "      END\n" +
                        "    ELSE b.industry\n" +
                        "  END\n" +

                        // ===== POST =====
                        "MERGE (p:Post {postId: $postId})\n" +
                        "SET p.platform = $platform\n" +

                        // ===== BRAND → POST =====
                        "MERGE (b)-[:POSTED]->(p)\n" +

                        // ===== TOPICS =====
                        "WITH p, b, $topics AS topicList\n" +
                        "UNWIND topicList AS topicName\n" +
                        "MERGE (t:Topic {name: topicName})\n" +
                        "MERGE (p)-[:TOPIC_EXTRACTION]->(t)\n" +
                        "MERGE (b)-[r:DISCUSSES]->(t)\n" +
                        "ON CREATE SET r.weight = 1\n" +
                        "ON MATCH SET r.weight = r.weight + 1";

        try (Session session = driver.session()) {
            session.run(cypherQuery, params);
        }
    }
}