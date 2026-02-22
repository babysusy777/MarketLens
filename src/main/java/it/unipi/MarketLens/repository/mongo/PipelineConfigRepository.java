package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.config.PipelineConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PipelineConfigRepository
        extends MongoRepository<PipelineConfig, String> {

    Optional<PipelineConfig> findTopByOrderByLastUpdatedAtDesc();
}

