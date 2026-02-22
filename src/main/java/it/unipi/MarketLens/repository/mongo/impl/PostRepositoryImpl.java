package it.unipi.MarketLens.repository.mongo.impl;

import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.model.Post;
import it.unipi.MarketLens.repository.mongo.PostRepositoryCustom;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class PostRepositoryImpl implements PostRepositoryCustom {


    private final MongoTemplate mongoTemplate;

    public PostRepositoryImpl(@Qualifier("fastMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Post> searchPosts(
            String keyword,
            String author,
            String platform,
            Instant dateFrom,
            Instant dateTo
    ) {

        List<Criteria> criteriaList = new ArrayList<>();


        if (keyword != null && !keyword.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("description").regex(keyword, "i"),
                    Criteria.where("hashtags").regex(keyword, "i"),
                    Criteria.where("nlp.topics").regex(keyword, "i"),
                    Criteria.where("nlp.entities.entity").regex(keyword, "i")
            ));
        }


        if (author != null && !author.isBlank()) {
            criteriaList.add(
                    Criteria.where("userPosted").regex("^" + author + "$", "i")
            );
        }


        if (platform != null && !platform.isBlank()) {
            criteriaList.add(
                    Criteria.where("platform").regex("^" + platform + "$", "i")
            );
        }


        if (dateFrom != null || dateTo != null) {
            Criteria dateCriteria = Criteria.where("datePosted");
            if (dateFrom != null) dateCriteria.gte(dateFrom);
            if (dateTo != null) dateCriteria.lte(dateTo);
            criteriaList.add(dateCriteria);
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
        }

        query.with(Sort.by(Sort.Direction.DESC, "datePosted"));

        return mongoTemplate.find(query, Post.class);
    }
}