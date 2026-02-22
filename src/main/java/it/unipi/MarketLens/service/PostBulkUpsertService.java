package it.unipi.MarketLens.service;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import it.unipi.MarketLens.model.Post;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostBulkUpsertService {

    private final MongoTemplate fastMongoTemplate;

    public PostBulkUpsertService(@Qualifier("fastMongoTemplate") MongoTemplate fastMongoTemplate) {
        this.fastMongoTemplate = fastMongoTemplate;
    }

    /**
     * Upsert idempotente per postId.
     * Ritorna il numero di INSERT reali (upsert).
     */
    public long bulkUpsertPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return 0;

        List<WriteModel<Document>> ops = new ArrayList<>(posts.size());
        UpdateOptions options = new UpdateOptions().upsert(true);

        for (Post p : posts) {
            if (p == null || p.getPostId() == null || p.getPostId().isBlank()) continue;

            // filtro sul NOME CAMPO MONGO: post_id
            Document filter = new Document("post_id", p.getPostId());

            // converte Post -> Document con i nomi corretti (@Field già applicati)
            Document replacement = new Document();
            fastMongoTemplate.getConverter().write(p, replacement);
            replacement.remove("_id"); // non toccare l'_id

            // NB: se vuoi evitare di sovrascrivere post_id stesso, potresti rimuoverlo qui, ma non è necessario.
            Document update = new Document("$set", replacement);

            ops.add(new UpdateOneModel<>(filter, update, options));
        }

        if (ops.isEmpty()) return 0;

        BulkWriteResult res = fastMongoTemplate.getCollection("posts").bulkWrite(ops);
        return res.getUpserts().size();
    }

}
