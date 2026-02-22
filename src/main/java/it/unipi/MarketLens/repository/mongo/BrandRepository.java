package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.dto.BrandRankingDTO;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.Industry;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<Brand, String> {

    Optional<Brand> findByBrandName(String brandName);

    Optional<Brand> findByUsername(String username);

    Optional<Brand> findByBrandNameIgnoreCase(String brandName);
    @Aggregation(pipeline = {
            "{ '$match': { 'industry': ?0 } }",
            "{ '$project': { " +
                    "'name': 1, " + // Usiamo il nome originale del DB
                    "'totalPosts': 1, " +
                    "'totalEngagement': 1, " +
                    "'avgSentiment': 1, " +
                    "'engagementPerPost': { '$cond': [ { '$gt': ['$totalPosts', 0] }, { '$divide': ['$totalEngagement', '$totalPosts'] }, 0 ] }" +
                    "} }",
            "{ '$sort': { 'engagementPerPost': -1 } }",
            "{ '$limit': 10 }"
    })
    List<BrandRankingDTO> findTopBrandsByEngagement(String industryName);
}