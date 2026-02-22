package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.TopicCoOccurrenceDTO;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.graph.BrandGraphRepository;
import it.unipi.MarketLens.repository.graph.TopicGraphRepository;
import it.unipi.MarketLens.utils.TopicClusteringUtils;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GraphRecomputeService {

    private final BrandSimilarityService brandSimilarityService;
    private final CompetitorService competitorService;
    private final BrandRepository brandRepository;
    private final TopicGraphRepository topicGraphRepository;
    private final BrandGraphRepository brandGraphRepository;
    private final Neo4jClient neo4jClient;

    private static final int TOP_K_SIMILARITY = 5;

    public GraphRecomputeService(BrandSimilarityService brandSimilarityService,
                                 CompetitorService competitorService,
                                 BrandRepository brandRepository,
                                 TopicGraphRepository topicGraphRepository,
                                 BrandGraphRepository brandGraphRepository,
                                 Neo4jClient neo4jClient) {
        this.brandSimilarityService = brandSimilarityService;
        this.competitorService = competitorService;
        this.brandRepository = brandRepository;
        this.topicGraphRepository = topicGraphRepository;
        this.brandGraphRepository = brandGraphRepository;
        this.neo4jClient = neo4jClient;
    }

    public static class GraphStats {
        public long industriesProcessed = 0;
        public long brandsTotal = 0;
        public long competitorsComputed = 0;

        public Map<String, Long> toCounters() {
            return Map.of(
                    "industriesProcessed", industriesProcessed,
                    "brandsTotal", brandsTotal,
                    "competitorsComputed", competitorsComputed
            );
        }
    }

    public GraphStats recomputeAllWithStats() {
        GraphStats stats = new GraphStats();

        for (Industry ind : Industry.values()) {
            String industryName = ind.name();
            String clusterIdsProp = "clusterIds_" + industryName;
            String clusterFreqsProp = "clusterFreqs_" + industryName;

            System.out.println("Processing Graph for Industry: " + industryName);

            // Clean vecchi cluster Brand
            String deleteQuery = "MATCH (b:Brand) WHERE toUpper(b.industry) = $ind " +
                    "SET b." + clusterIdsProp + " = null, b." + clusterFreqsProp + " = null";

            neo4jClient.query(deleteQuery)
                    .bind(industryName).to("ind")
                    .run();


            topicGraphRepository.createCoOccurrenceGraph(industryName);


            List<TopicCoOccurrenceDTO> edges = topicGraphRepository.getAllCoOccurrences(industryName);

            if (edges.isEmpty()) {
                continue;
            }


            Map<Integer, Set<String>> clusters = TopicClusteringUtils.egoNetworkClustering(edges);


            for (Map.Entry<Integer, Set<String>> entry : clusters.entrySet()) {
                Integer clusterId = entry.getKey();
                for (String topic : entry.getValue()) {
                    topicGraphRepository.persistTopicClusterMembership(topic, industryName, clusterId);
                }
            }


            List<String> brands = brandGraphRepository.getBrandNamesByIndustry(industryName);

            for (String brand : brands) {
                List<Map<String, Object>> rows = brandGraphRepository.getBrandClusterVector(brand, industryName);

                List<Long> clusterIds = new ArrayList<>();
                List<Long> clusterFreqs = new ArrayList<>();

                if (!rows.isEmpty()) {
                    for (Map<String, Object> r : rows) {
                        clusterIds.add(((Number) r.get("clusterId")).longValue());
                        clusterFreqs.add(((Number) r.get("frequency")).longValue());
                    }
                }


                String saveQuery = "MATCH (b:Brand {name: $bName}) " +
                        "SET b." + clusterIdsProp + " = $ids, " +
                        "    b." + clusterFreqsProp + " = $freqs";

                neo4jClient.query(saveQuery)
                        .bind(brand).to("bName")
                        .bind(clusterIds).to("ids")
                        .bind(clusterFreqs).to("freqs")
                        .run();
            }


            brandSimilarityService.rebuildSimilarity(industryName, TOP_K_SIMILARITY);
            stats.industriesProcessed++;
        }


        List<Brand> allBrands = brandRepository.findAll();
        stats.brandsTotal = allBrands.size();

        for (Brand b : allBrands) {
            if (b.getBrandName() == null || b.getIndustry() == null) continue;
            competitorService.computeAndPersistCompetitors(b.getBrandName(), b.getIndustry());
            stats.competitorsComputed++;
        }

        return stats;
    }
}