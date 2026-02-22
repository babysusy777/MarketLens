package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.model.IngestionCursorState;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IngestionCursorStateRepository extends MongoRepository<IngestionCursorState, String> {
    Optional<IngestionCursorState> findBySourceKey(String sourceKey);
}

