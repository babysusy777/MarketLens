package it.unipi.MarketLens.service;

import it.unipi.MarketLens.config.PipelineConfig;
import it.unipi.MarketLens.dto.PipelineConfigUpdateRequest;
import it.unipi.MarketLens.repository.mongo.PipelineConfigRepository;
import org.springframework.stereotype.Service;
import it.unipi.MarketLens.events.PipelineConfigUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;

@Service
public class PipelineConfigService {

    private static final String SINGLETON_ID = "pipeline_config";

    private final PipelineConfigRepository repository;

    private final ApplicationEventPublisher eventPublisher;

    public PipelineConfigService(PipelineConfigRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public PipelineConfig getCurrentConfig() {
        return repository.findTopByOrderByLastUpdatedAtDesc()
                .orElseGet(this::createDefaultConfig);
    }

    public PipelineConfig updateConfig(PipelineConfigUpdateRequest request, String admin) {
        PipelineConfig current = getCurrentConfig();

        if (!CronExpression.isValidExpression(request.getIngestionSchedule())) {
            throw new IllegalArgumentException("Cron expression invalid: " + request.getIngestionSchedule());
        }

        current.setIngestionEnabled(Boolean.TRUE.equals(request.getIngestionEnabled()));
        current.setIngestionSchedule(request.getIngestionSchedule());
        current.setBatchSize(request.getBatchSize());

        current.setLastUpdatedAt(LocalDateTime.now());


        PipelineConfig saved = repository.save(current);
        eventPublisher.publishEvent(new PipelineConfigUpdatedEvent(saved));
        return saved;
    }
    private PipelineConfig createDefaultConfig() {
        PipelineConfig config = new PipelineConfig();
        config.setId(SINGLETON_ID);
        config.setIngestionEnabled(true);
        config.setIngestionSchedule("0 0 */24 * * * ");
        config.setBatchSize(1000);
        //questo lo fa nella realta, nel nostro caso lui prende tutti i post senza considerare la piattaforma, perchè non ce nel post ingestion service
        config.setLastUpdatedAt(LocalDateTime.now());

        return repository.save(config);
    }
}
