package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.BrandMetricsResponse;
import it.unipi.MarketLens.dto.BrandMetricsResponse.TimePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class BrandMetricsService {


    private final MongoTemplate mongoTemplate;

    public BrandMetricsService(@Qualifier("fastMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public BrandMetricsResponse getMetrics(String brandName) {


        BrandMetricsResponse globalStats = calculateGlobalStats(brandName);


        List<DailyMetricResult> dailyResults = calculateDailyEvolution(brandName);

        List<TimePoint> sentimentEvo = new ArrayList<>();
        List<TimePoint> engagementEvo = new ArrayList<>();

        for (DailyMetricResult day : dailyResults) {

            double dailyEng = (day.getDailyLikes() != null ? day.getDailyLikes() : 0) +
                    (day.getDailyComments() != null ? day.getDailyComments() : 0);


            double dailySent = day.getDailySentiment() != null ? day.getDailySentiment() : 0.0;

            engagementEvo.add(new TimePoint(day.getDate(), dailyEng));
            sentimentEvo.add(new TimePoint(day.getDate(), dailySent));
        }


        globalStats.setEngagementEvolution(engagementEvo);
        globalStats.setSentimentEvolution(sentimentEvo);

        return globalStats;
    }


    private BrandMetricsResponse calculateGlobalStats(String brandName) {
        var matchStage = match(Criteria.where("user_posted").regex("^" + brandName + "$", "i"));

        var groupStage = group()
                .count().as("totalPosts")
                .sum("likes").as("totalLikes")
                .sum("num_comments").as("totalComments")
                .avg("nlp.sentiment.score").as("avgSentiment");

        var projectStage = project("totalPosts", "avgSentiment")
                .andExpression("totalLikes + totalComments").as("totalEngagement");

        Aggregation aggregation = newAggregation(matchStage, groupStage, projectStage);

        BrandMetricsResponse result = mongoTemplate.aggregate(aggregation, "posts", BrandMetricsResponse.class)
                .getUniqueMappedResult();

        if (result == null) {
            result = new BrandMetricsResponse();
            result.setBrandName(brandName);
            result.setTotalPosts(0L);
            result.setTotalEngagement(0L);
            result.setAvgSentiment(0.0);
        } else {
            result.setBrandName(brandName);
        }
        return result;
    }


    private List<DailyMetricResult> calculateDailyEvolution(String brandName) {
        var matchStage = match(Criteria.where("user_posted").regex("^" + brandName + "$", "i"));


        var projectDate = project()
                .and(DateOperators.DateToString.dateOf("date_posted").toString("%Y-%m-%d")).as("dateStr")
                .and("likes").as("likes")
                .and("num_comments").as("num_comments")
                .and("nlp.sentiment.score").as("sentiment");


        var groupStage = group("dateStr")
                .sum("likes").as("dailyLikes")
                .sum("num_comments").as("dailyComments")
                .avg("sentiment").as("dailySentiment");


        var sortStage = sort(Sort.Direction.ASC, "_id");

        Aggregation aggregation = newAggregation(matchStage, projectDate, groupStage, sortStage);


        return mongoTemplate.aggregate(aggregation, "posts", DailyMetricResult.class).getMappedResults();
    }


    private static class DailyMetricResult {
        @org.springframework.data.annotation.Id
        private String date; // Corrisponde a "dateStr" usato nel group

        private Long dailyLikes;
        private Long dailyComments;
        private Double dailySentiment;


        public String getDate() { return date; }
       public void setDate(String date) { this.date = date; }
        public Long getDailyLikes() { return dailyLikes; }
        public void setDailyLikes(Long dailyLikes) { this.dailyLikes = dailyLikes; }
        public Long getDailyComments() { return dailyComments; }
       public void setDailyComments(Long dailyComments) { this.dailyComments = dailyComments; }
        public Double getDailySentiment() { return dailySentiment; }
         public void setDailySentiment(Double dailySentiment) { this.dailySentiment = dailySentiment; }
    }
}