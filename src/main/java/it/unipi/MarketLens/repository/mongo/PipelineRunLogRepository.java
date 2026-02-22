package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.model.PipelineRunLog;
import it.unipi.MarketLens.model.RunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;


public interface PipelineRunLogRepository extends MongoRepository<PipelineRunLog, String> {

    Optional<PipelineRunLog> findByRunId(String runId);

    Page<PipelineRunLog> findAllByOrderByStartTimeDesc(Pageable pageable);

    Page<PipelineRunLog> findByStatusOrderByStartTimeDesc(RunStatus status, Pageable pageable);

    Page<PipelineRunLog> findByStartTimeBetweenOrderByStartTimeDesc(Instant from, Instant to, Pageable pageable);

    Page<PipelineRunLog> findByStatusAndStartTimeBetweenOrderByStartTimeDesc(
            RunStatus status, Instant from, Instant to, Pageable pageable
    );
}
