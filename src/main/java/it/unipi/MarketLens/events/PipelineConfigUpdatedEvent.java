package it.unipi.MarketLens.events;

import it.unipi.MarketLens.config.PipelineConfig;

public class PipelineConfigUpdatedEvent {
    private final PipelineConfig config;

    public PipelineConfigUpdatedEvent(PipelineConfig config) {
        this.config = config;
    }

    public PipelineConfig getConfig() {
        return config;
    }
}
