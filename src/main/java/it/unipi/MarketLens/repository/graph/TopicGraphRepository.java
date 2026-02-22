package it.unipi.MarketLens.repository.graph;

import it.unipi.MarketLens.dto.*;
import it.unipi.MarketLens.model.graph.TopicNode; // Assicurati di avere questo nodo
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TopicGraphRepository extends Neo4jRepository<TopicNode, String> {
    // Questa query crea archi "CO_OCCURS" tra topic che appaiono nello stesso post
    // MA SOLO per i brand dell'industry specificata.
    @Query("MATCH (b:Brand)-[:POSTED]->(p:Post) " +
            "WHERE toUpper(b.industry) = toUpper($industry) " +

            "MATCH (t1:Topic)<-[:TOPIC_EXTRACTION]-(p)-[:TOPIC_EXTRACTION]->(t2:Topic) " +
            "WHERE elementId(t1) < elementId(t2) " +
            "MERGE (t1)-[r:CO_OCCURS_WITH {industry: toUpper($industry)}]->(t2) " +
            "ON CREATE SET r.weight = 1 " +
            "ON MATCH SET r.weight = r.weight + 1")
    void createCoOccurrenceGraph(String industry);

    @Query("""
            MATCH (t1:Topic)-[r:CO_OCCURS_WITH {industry: $industry}]-(t2:Topic)
            RETURN t1.name AS topic1,
                   t2.name AS topic2,
                   r.weight AS strength
            """)
    List<TopicCoOccurrenceDTO> getAllCoOccurrences(String industry);


    @Query("""
        MATCH (t:Topic {name: $topic})
        SET
          t.clusterIds =
            CASE
              WHEN t.clusterIds IS NULL THEN [$clusterId]
              ELSE t.clusterIds + $clusterId
            END,
          t.industries =
            CASE
              WHEN t.industries IS NULL THEN [$industry]
              ELSE t.industries + $industry
            END
    """)
    void persistTopicClusterMembership(String topic, String industry, Integer clusterId);
    //topic più ricorrenti per industry
    @Query("""
    MATCH (t:Topic)-[r:CO_OCCURS_WITH {industry: toUpper($industry)}]-(neighbor:Topic)
    RETURN t.name AS topic, 
           count(r) AS CoOccurences, 
           sum(r.weight) AS totalFrequency
    ORDER BY totalFrequency DESC
    LIMIT $limit
    """)
    List<TopicTrendDTO> findTopRecurringTopics(String industry, int limit);
    @Query("""
    MATCH (b:Brand {name: $brandName})-[:POSTED]->(:Post)-[:TOPIC_EXTRACTION]->(t:Topic)
    RETURN t.name AS topic, 
           count(*) AS occurrences
    ORDER BY occurrences DESC
    LIMIT $limit
""")
    List<BrandFocusDTO> findBrandSemanticFocus(String brandName, int limit);
    // Query per misurare la diversità tematica dei brand (analisi data science)
    @Query("""
    MATCH (b:Brand)-[:POSTED]->(:Post)-[:TOPIC_EXTRACTION]->(t:Topic)
    WHERE b.industry = $industry
    RETURN b.name AS brand, 
           count(DISTINCT t) AS topicDiversity
    ORDER BY topicDiversity DESC
""")
    List<BrandDiversityDTO> findBrandTopicDiversity(String industry);
    // Query per identificare topic di nicchia (usati da pochi brand)
    @Query("""
    MATCH (b:Brand)-[:POSTED]->(:Post)-[:TOPIC_EXTRACTION]->(t:Topic)
    WHERE b.industry = $industry
    WITH t, count(DISTINCT b) AS brandCount, collect(DISTINCT b.name) AS brands
    RETURN t.name AS topic, 
           brandCount, 
           brands[0] AS brandName
    ORDER BY brandCount ASC
    LIMIT 10
""")
    List<TopicExclusivityDTO> findExclusiveTopics(String industry);
}
