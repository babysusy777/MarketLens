package it.unipi.MarketLens.dto;

import java.util.List;

public record DashboardPipelineStatusResponse(
        PipelineRunSummary latestRun,
        PipelineRunSummary runningRun,
        List<PipelineRunSummary> recentRuns,
        List<PipelineRunSummary> recentFailures
) {}

