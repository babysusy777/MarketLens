package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.dto.HashtagTrendDTO;
import it.unipi.MarketLens.model.Post;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TrendAnalyticsRepository extends MongoRepository<Post, String> {

    @Aggregation(pipeline = {
            //MATCH BASE
            "{ '$match': { " +
                    "'datePosted': { '$gte': ?1, '$lt': ?2 }, " +
                    "'industry': ?0, " +
                    "'platform': { $regex: ?3, $options: 'i' } " +
                    "} }",
            //CALCOLO ENGAGEMENT + SENTIMENT
            "{ '$project': { " +
                    "'hashtags': 1, " +
                    "'engagement': { '$add': [ { '$ifNull': ['$likes', 0] }, { '$ifNull': ['$numComments', 0] } ] }, " +
                    "'sentiment': { '$ifNull': ['$nlp.sentiment.score', 0] } " +
                    "} }",
            //UNWIND HASHTAGS
            "{ '$unwind': '$hashtags' }",
            //GROUP (HashtagAccumulator logic)
            "{ '$group': { " +
                    "'_id': { '$toLower': { '$trim': { 'input': '$hashtags' } } }, " +
                    "'numPosts': { '$sum': 1 }, " +
                    "'totalEng': { '$sum': '$engagement' }, " +
                    "'totalSentiment': { '$sum': '$sentiment' } " +
                    "} }",
            //NORMALIZZAZIONE + AVG
            "{ '$project': { " +
                    "'hashtag': { " +
                    "'$cond': [ " +
                    "{ '$regexMatch': { 'input': '$_id', 'regex': '^#' } }, " +
                    "'$_id', " +
                    "{ '$concat': ['#', '$_id'] } " +
                    "] " +
                    "}, " +
                    "'numPosts': 1, " +
                    "'totalEng': 1, " +
                    "'avgSentiment': { '$divide': ['$totalSentiment', '$numPosts'] } " +
                    "} }",
            //SORT + LIMIT
            "{ '$sort': { 'totalEng': -1 } }",
            "{ '$limit': 10 }"
    })
    List<HashtagTrendDTO> findTopHashtagsByIndustry(
            String industry,
            Date from,
            Date to,
            String platform
    );
}