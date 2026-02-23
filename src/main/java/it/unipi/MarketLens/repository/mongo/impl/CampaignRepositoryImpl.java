package it.unipi.MarketLens.repository.mongo.impl;

import it.unipi.MarketLens.dto.CampaignResponse;
import it.unipi.MarketLens.dto.BrandMetricsResponse.TimePoint;
import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.repository.mongo.CampaignRepositoryCustom;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class CampaignRepositoryImpl implements CampaignRepositoryCustom {

    private final MongoTemplate fastMongoTemplate;
    private final MongoTemplate strongMongoTemplate;

    public CampaignRepositoryImpl(
            @Qualifier("fastMongoTemplate") MongoTemplate fastMongoTemplate,
            @Qualifier("strongMongoTemplate") MongoTemplate strongMongoTemplate
    ) {
        this.fastMongoTemplate = fastMongoTemplate;
        this.strongMongoTemplate = strongMongoTemplate;
    }


    private MongoTemplate readMongo() {
        return strongMongoTemplate;
    }


    @Override
    public CampaignResponse analyzeCampaign(Campaign config) {

        // ---- SAFETY: brandRegex robusto + gestione null/blank
        String brand = config.getBrandAuthor();
        String brandRegex = null;
        if (brand != null && !brand.isBlank()) {
            brandRegex = ".*" + Pattern.quote(brand) + ".*";
        }

        Date start = (config.getStartDate() != null) ? Date.from(config.getStartDate()) : null;
        Date end   = (config.getEndDate() != null)   ? Date.from(config.getEndDate())   : null;

        List<String> hashtags;
        List<String> keywords;
        Document extraFilter = new Document();
        boolean fallbackOnlyDate = false;

        boolean hasExplicitHashtags =
                config.getHashtags() != null && !config.getHashtags().isEmpty();

        boolean hasExplicitKeywords =
                config.getKeywords() != null && !config.getKeywords().isEmpty();

        if (hasExplicitHashtags || hasExplicitKeywords) {

            hashtags = hasExplicitHashtags ? config.getHashtags() : List.of();
            keywords = hasExplicitKeywords ? config.getKeywords() : List.of();

            extraFilter = buildOrFilter(hashtags, keywords);

            // Se l’utente ha passato liste “vuote” mascherate: evita $or:[]
            if (extraFilter.isEmpty()) {
                fallbackOnlyDate = true;
            }

        } else {

            // Se non ho brand, non posso trovare ricorrenze basate su user_posted regex
            if (brandRegex == null) {
                fallbackOnlyDate = true;
                hashtags = null;
                keywords = null;
                extraFilter = new Document();
            } else {
                hashtags = findRecurringHashtags(brandRegex, start, end);
                keywords = findRecurringEntities(brandRegex, start, end);

                if ((hashtags == null || hashtags.isEmpty())
                        && (keywords == null || keywords.isEmpty())) {

                    fallbackOnlyDate = true;
                    extraFilter = new Document();
                    hashtags = null;
                    keywords = null;

                } else {
                    extraFilter = buildOrFilter(hashtags, keywords);
                }
            }
        }

        Document metrics =
                computeMetrics(brandRegex, start, end, extraFilter);

        List<Document> sentimentStats =
                computeSentimentDistribution(brandRegex, start, end, extraFilter);

        List<Document> timelineStats =
                computeSentimentEvolution(brandRegex, start, end, extraFilter);

        CampaignResponse response =
                mapResultsToDTO(metrics, sentimentStats, timelineStats, config);

        response.setHashtags(hashtags);
        response.setKeywords(keywords);

        if (fallbackOnlyDate) {
            response.setMessage("campaign filtered only by date");
        }

        return response;
    }

    private Criteria baseCriteria(String brandRegex, Date startDate, Date endDate) {
        List<Criteria> and = new ArrayList<>();

        // Brand filter (solo se presente)
        if (brandRegex != null && !brandRegex.isBlank()) {
            and.add(Criteria.where("user_posted").regex(brandRegex, "i"));
        }

        // Date filter (gestione null)
        if (startDate != null && endDate != null) {
            and.add(Criteria.where("date_posted").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            and.add(Criteria.where("date_posted").gte(startDate));
        } else if (endDate != null) {
            and.add(Criteria.where("date_posted").lte(endDate));
        }

        // Platform
        and.add(Criteria.where("platform").is("instagram"));

        return new Criteria().andOperator(and.toArray(new Criteria[0]));
    }

    private List<String> findRecurringHashtags(String brandRegex, Date startDate, Date endDate) {

        Aggregation agg = Aggregation.newAggregation(

                Aggregation.match(baseCriteria(brandRegex, startDate, endDate)),

                Aggregation.unwind("hashtags"),

                Aggregation.group("hashtags")
                        .addToSet("_id").as("posts"),

                Aggregation.project("_id")
                        .and("posts").size().as("cnt"),

                Aggregation.match(Criteria.where("cnt").gte(2))
        );

        return readMongo()
                .aggregate(agg, "posts", Document.class)
                .getMappedResults()
                .stream()
                .map(d -> d.getString("_id"))
                .toList();
    }

    private List<String> findRecurringEntities(String brandRegex, Date startDate, Date endDate) {

        Aggregation agg = Aggregation.newAggregation(

                Aggregation.match(baseCriteria(brandRegex, startDate, endDate)),

                Aggregation.unwind("nlp.entities"),

                Aggregation.group("nlp.entities.entity")
                        .addToSet("_id").as("posts"),

                Aggregation.project("_id")
                        .and("posts").size().as("cnt"),

                Aggregation.match(Criteria.where("cnt").gte(2))
        );

        return readMongo()
                .aggregate(agg, "posts", Document.class)
                .getMappedResults()
                .stream()
                .map(d -> d.getString("_id"))
                .toList();
    }

    private Document computeMetrics(String brandRegex, Date startDate, Date endDate, Document extraFilter) {

        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(Aggregation.match(baseCriteria(brandRegex, startDate, endDate)));

        if (extraFilter != null && !extraFilter.isEmpty()) {
            pipeline.add(ctx -> new Document("$match", extraFilter));
        }

        pipeline.add(
                Aggregation.group()
                        .count().as("totalPosts")
                        .sum("likes").as("totLikes")
                        .sum("num_comments").as("totComments")
        );

        Aggregation agg = Aggregation.newAggregation(pipeline);

        return readMongo()
                .aggregate(agg, "posts", Document.class)
                .getUniqueMappedResult();
    }

    private Document buildOrFilter(List<String> hashtags, List<String> keywords) {
        List<Document> orConditions = new ArrayList<>();

        if (hashtags != null && !hashtags.isEmpty()) {
            orConditions.add(new Document("hashtags", new Document("$in", hashtags)));
        }

        if (keywords != null && !keywords.isEmpty()) {
            orConditions.add(new Document("nlp.entities.entity", new Document("$in", keywords)));
        }

        // IMPORTANT: se non ci sono condizioni, non generare $or:[]
        if (orConditions.isEmpty()) {
            return new Document();
        }

        return new Document("$or", orConditions);
    }

    private List<Document> computeSentimentDistribution(String brandRegex, Date startDate, Date endDate, Document extraFilter) {

        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(Aggregation.match(baseCriteria(brandRegex, startDate, endDate)));

        if (extraFilter != null && !extraFilter.isEmpty()) {
            pipeline.add(ctx -> new Document("$match", extraFilter));
        }

        pipeline.add(
                Aggregation.group("nlp.sentiment.label")
                        .count().as("count")
        );

        return readMongo()
                .aggregate(Aggregation.newAggregation(pipeline), "posts", Document.class)
                .getMappedResults();
    }

    private List<Document> computeSentimentEvolution(String brandRegex, Date startDate, Date endDate, Document extraFilter) {

        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(Aggregation.match(baseCriteria(brandRegex, startDate, endDate)));

        if (extraFilter != null && !extraFilter.isEmpty()) {
            pipeline.add(ctx -> new Document("$match", extraFilter));
        }

        pipeline.add(
                Aggregation.project()
                        .and(
                                DateOperators.DateToString
                                        .dateOf("date_posted")
                                        .toString("%Y-%m-%d")
                        ).as("date")
                        .and("nlp.sentiment.score").as("score")
        );

        pipeline.add(
                Aggregation.group("date")
                        .avg("score").as("value")
        );

        pipeline.add(Aggregation.sort(Sort.Direction.ASC, "_id"));

        return readMongo()
                .aggregate(Aggregation.newAggregation(pipeline), "posts", Document.class)
                .getMappedResults();
    }

    private CampaignResponse mapResultsToDTO(
            Document metrics,
            List<Document> sentimentStats,
            List<Document> timelineStats,
            Campaign config
    ) {

        CampaignResponse response = new CampaignResponse();

        response.setId(config.getId());
        response.setName(config.getName());
        response.setCreatedAt(config.getCreatedAt());
        response.setBrandAuthor(config.getBrandAuthor());
        response.setStartDate(config.getStartDate());
        response.setEndDate(config.getEndDate());

        if (metrics == null) {
            response.setTotalPosts(0);
            response.setTotalEngagement(0);
            response.setSentimentDistribution(
                    new CampaignResponse.SentimentDistribution(0, 0, 0)
            );
            response.setSentimentEvolution(List.of());
            return response;
        }

        // totalPosts potrebbe essere Integer o Long -> usa Number
        long posts = 0L;
        Object tp = metrics.get("totalPosts");
        if (tp instanceof Number n) posts = n.longValue();

        long likes = 0L;
        Object tl = metrics.get("totLikes");
        if (tl instanceof Number n) likes = n.longValue();

        long comments = 0L;
        Object tc = metrics.get("totComments");
        if (tc instanceof Number n) comments = n.longValue();

        response.setTotalPosts(posts);
        response.setTotalEngagement(likes + comments);

        long pos = 0, neg = 0, neu = 0, total = 0;

        for (Document d : sentimentStats) {
            String label = d.getString("_id");
            long count = 0L;
            Object c = d.get("count");
            if (c instanceof Number n) count = n.longValue();

            if (label == null) continue;

            switch (label.toLowerCase()) {
                case "positive" -> pos += count;
                case "negative" -> neg += count;
                default -> neu += count;
            }
            total += count;
        }

        if (total > 0) {
            response.setSentimentDistribution(
                    new CampaignResponse.SentimentDistribution(
                            (pos * 100.0) / total,
                            (neg * 100.0) / total,
                            (neu * 100.0) / total
                    )
            );
        } else {
            response.setSentimentDistribution(
                    new CampaignResponse.SentimentDistribution(0, 0, 0)
            );
        }

        List<TimePoint> timeline = new ArrayList<>();

        for (Document d : timelineStats) {
            String date = d.getString("_id");
            double value = 0.0;
            Object v = d.get("value");
            if (v instanceof Number n) value = n.doubleValue();

            if (date != null) {
                timeline.add(new TimePoint(date, value));
            }
        }

        response.setSentimentEvolution(timeline);

        return response;
    }

    public CampaignResponse analyzeVirtualCampaign(String brand, Instant start, Instant end) {

        Campaign comp = new Campaign();
        comp.setBrandAuthor(brand);
        comp.setStartDate(start);
        comp.setEndDate(end);

        return analyzeCampaign(comp);
    }
}