package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.IngestionCursorState;
import it.unipi.MarketLens.repository.mongo.IngestionCursorStateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IngestionCursorService {

    private final IngestionCursorStateRepository repo;
    private final MongoTemplate strongMongoTemplate;

    public IngestionCursorService(IngestionCursorStateRepository repo,
                                  @Qualifier("strongMongoTemplate") MongoTemplate strongMongoTemplate) {
        this.repo = repo;
        this.strongMongoTemplate = strongMongoTemplate;
    }

    public IngestionCursorState getOrCreate(String sourceKey) {
        return repo.findBySourceKey(sourceKey).orElseGet(() -> {
            IngestionCursorState s = new IngestionCursorState();
            s.setSourceKey(sourceKey);
            s.setLastProcessedIndex(0);
            s.setUpdatedAt(Instant.now());
            try {
                return repo.save(s);
            } catch (Exception e) {
                // in caso di race (unique), ricarica
                return repo.findBySourceKey(sourceKey).orElseThrow();
            }
        });
    }

    /**
     * Advance atomico (STRONG): imposta lastProcessedIndex = newIndex e updatedAt=now in un'unica operazione.
     * Upsert=true: se non esiste lo crea.
     */
    public IngestionCursorState advance(String sourceKey, long newIndex) {
        Query q = new Query(Criteria.where("sourceKey").is(sourceKey));
        Update u = new Update()
                .set("lastProcessedIndex", newIndex)
                .set("updatedAt", Instant.now())
                .setOnInsert("sourceKey", sourceKey);

        FindAndModifyOptions opt = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        return strongMongoTemplate.findAndModify(q, u, opt, IngestionCursorState.class);
    }
}

