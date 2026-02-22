package it.unipi.MarketLens.dto;

import java.util.Map;

public record DashboardDbStatusResponse(
        boolean mongoUp,
        MongoStats mongoStats,
        boolean neo4jUp,
        Neo4jStats neo4jStats
) {
    public record MongoStats(
            Integer collections,
            Long objects,
            Double dataSizeMB,
            Double storageSizeMB,
            Double indexSizeMB
    ) {}

    public record Neo4jStats(
            long nodes,
            long relationships,
            Map<String, Long> countByLabel
    ) {}
}
