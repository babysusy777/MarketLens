package it.unipi.MarketLens.repository.graph;

import it.unipi.MarketLens.model.graph.BrandNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BrandGraphRepository extends Neo4jRepository<BrandNode, String> {

    @Query("""
        MATCH (b:Brand)
        WHERE toUpper(b.industry) = toUpper($industry)
        RETURN b.name
    """)
    List<String> getBrandNamesByIndustry(String industry);


    @Query("""
        MATCH (b:Brand {name: $brand})-[:POSTED]->(:Post)-[:TOPIC_EXTRACTION]->(t:Topic)
        WHERE $industry IN coalesce(t.industries, [])
        UNWIND coalesce(t.clusterIds, []) AS cid
        WITH cid, count(*) AS freq
        RETURN {
          clusterId: cid,
          frequency: freq
        } AS row
        ORDER BY cid
    """)
    List<Map<String, Object>> getBrandClusterVector(String brand, String industry);


    @Query("""
        MATCH (b:Brand)
        WHERE toUpper(b.industry) = toUpper($industry)
        RETURN {
          name: b.name,
          clusterIds: b[$clusterIdsProp],
          clusterFreqs: b[$clusterFreqsProp]
        } AS row
    """)
    List<Map<String, Object>> getAllBrandClusterVectors(String industry, String clusterIdsProp, String clusterFreqsProp);

    @Query("""
        MATCH (:Brand)-[r:SIMILAR_TO]->(:Brand)
        WHERE toUpper(r.industry) = toUpper($industry)
        DELETE r
    """)
    void deleteAllSimilarities(String industry);

    @Query("""
        MATCH (t:Topic)
        REMOVE
          t.clusterIds,
          t.industries
    """)
    void deleteTopicClusters(String industry);


    @Query("""
        MATCH (b1:Brand {name: $from})
        MATCH (b2:Brand {name: $to})
        MERGE (b1)-[r:SIMILAR_TO {industry: $industry}]->(b2)
        SET r.score = $score
    """)
    void createSimilarity(String from, String to, String industry, Double score);
}