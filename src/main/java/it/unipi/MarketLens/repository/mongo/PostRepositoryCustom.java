package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.model.Post;

import java.time.Instant;
import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPosts(
            String keyword,
            String author,
            String platform,
            Instant dateFrom,
            Instant dateTo
    );

}
