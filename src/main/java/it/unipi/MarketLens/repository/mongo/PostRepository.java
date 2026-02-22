package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {


    @Query(value = "{ '$or': [ " +
            "{ 'hashtags': { '$in': ?0 } }, " +
            "{ 'nlp.entities.text': { '$in': ?0 } }, " +
            "{ 'nlp.topics': { '$in': ?1 } } " +
            "] }", sort = "{ 'datePosted': -1 }")
    List<Post> findByPreferences(List<String> keywords, List<String> topics);

    boolean existsByUserPostedIgnoreCase(String userPosted);

    boolean existsByPostId(String postId);
}