package it.unipi.MarketLens.repository.mongo.impl;

import it.unipi.MarketLens.dto.BrandMetricsResponse;
import it.unipi.MarketLens.repository.mongo.PostAnalyticsRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PostAnalyticsRepositoryImpl implements PostAnalyticsRepository {


    private final MongoTemplate mongoTemplate;

    public PostAnalyticsRepositoryImpl(@Qualifier("fastMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public BrandMetricsResponse computeBrandMetrics(List<String> authors) {


        MatchOperation match = Aggregation.match(
                Criteria.where("user_posted").in(authors)
        );


        GroupOperation group = Aggregation.group()
                .count().as("postCount")
                .sum("total_engagement").as("totalEngagement")
                .avg("sentiment_score").as("avgSentiment"); // CORRETTO: Tolto lo spazio iniziale!

        Aggregation globalAgg = Aggregation.newAggregation(match, group);

        Document global = mongoTemplate
                .aggregate(globalAgg, "posts", Document.class)
                .getUniqueMappedResult();

        long postCount = 0L;
        double totalEngagement = 0.0;
        double avgSentiment = 0.0;

        if (global != null) {
            if (global.get("postCount") != null) {
                postCount = ((Number) global.get("postCount")).longValue();
            }
            if (global.get("totalEngagement") != null) {
                totalEngagement = ((Number) global.get("totalEngagement")).doubleValue();
            }
            if (global.get("avgSentiment") != null) {
                avgSentiment = ((Number) global.get("avgSentiment")).doubleValue();
            }
        }


        ProjectionOperation project = Aggregation.project()
                .and("date_posted").as("date")
                .and("sentiment_score").as("sentiment")   // CORRETTO: Tolto lo spazio iniziale!
                .and("total_engagement").as("engagement"); // CORRETTO: Tolto lo spazio iniziale!

        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "date");

        Aggregation timeAgg = Aggregation.newAggregation(match, project, sort);

        List<Document> timeline = mongoTemplate
                .aggregate(timeAgg, "posts", Document.class)
                .getMappedResults();

        List<BrandMetricsResponse.TimePoint> sentimentEvolution = new ArrayList<>();
        List<BrandMetricsResponse.TimePoint> engagementEvolution = new ArrayList<>();

        for (Document d : timeline) {

            if (d.get("date") == null) continue;

            Instant date;
            try {
                if (d.get("date") instanceof java.util.Date) {
                    date = ((java.util.Date) d.get("date")).toInstant();
                } else {
                    date = d.getDate("date").toInstant();
                }
            } catch (Exception e) {
                continue;
            }

            double sentimentVal = (d.get("sentiment") != null)
                    ? ((Number) d.get("sentiment")).doubleValue()
                    : 0.0;

            double engagementVal = (d.get("engagement") != null)
                    ? ((Number) d.get("engagement")).doubleValue()
                    : 0.0;

            sentimentEvolution.add(new BrandMetricsResponse.TimePoint(date, sentimentVal));
            engagementEvolution.add(new BrandMetricsResponse.TimePoint(date, engagementVal));
        }

        return new BrandMetricsResponse(
                postCount,
                (long) totalEngagement,
                avgSentiment,
                sentimentEvolution,
                engagementEvolution
        );
    }
}