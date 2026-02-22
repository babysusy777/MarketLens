package it.unipi.MarketLens.repository.graph;

import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.model.graph.BrandNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CompetitorGraphRepository extends Neo4jRepository<BrandNode, String> {

    @Query("""
        MATCH (b:Brand {name: $brand})-[r:SIMILAR_TO {industry: $industry}]->(other:Brand)
        RETURN {brand: other.name, score: r.score} AS row
        ORDER BY r.score DESC
        LIMIT 4
    """)
    List<Map<String, Object>> getTopCompetitors(String brand, Industry industry);

}