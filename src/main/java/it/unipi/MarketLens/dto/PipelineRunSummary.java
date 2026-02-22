package it.unipi.MarketLens.dto;

import java.time.Instant;

public record PipelineRunSummary(
        String runId,
        String status,
        Instant startTime,
        Instant endTime,
        Long durationMs,
        String error
) {}
