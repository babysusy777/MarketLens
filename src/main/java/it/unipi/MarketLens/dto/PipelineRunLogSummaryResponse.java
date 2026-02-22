package it.unipi.MarketLens.dto;

import it.unipi.MarketLens.model.PipelineStepType;
import it.unipi.MarketLens.model.RunStatus;
import it.unipi.MarketLens.model.StepStatus;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public class PipelineRunLogSummaryResponse {

    public String runId;
    public RunStatus status;
    public Instant startTime;
    public Instant endTime;
    public Long durationMs;

    public Map<PipelineStepType, StepStatus> stepStatuses = new EnumMap<>(PipelineStepType.class);

    public static PipelineRunLogSummaryResponse of(
            String runId,
            RunStatus status,
            Instant startTime,
            Instant endTime,
            Long durationMs,
            Map<PipelineStepType, StepStatus> stepStatuses
    ) {
        PipelineRunLogSummaryResponse r = new PipelineRunLogSummaryResponse();
        r.runId = runId;
        r.status = status;
        r.startTime = startTime;
        r.endTime = endTime;
        r.durationMs = durationMs;
        r.stepStatuses = stepStatuses;
        return r;
    }
}
