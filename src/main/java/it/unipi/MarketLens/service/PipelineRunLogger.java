package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.*;
import it.unipi.MarketLens.repository.mongo.PipelineRunLogRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class PipelineRunLogger {

    private final PipelineRunLogRepository repo;

    public PipelineRunLogger(PipelineRunLogRepository repo) {
        this.repo = repo;
    }

    public String startRun(Map<String, Object> configSnapshot, Map<String, Object> metadata) {
        String runId = UUID.randomUUID().toString();

        PipelineRunLog log = new PipelineRunLog();
        log.setRunId(runId);
        log.setStatus(RunStatus.RUNNING);
        log.setStartTime(Instant.now());
        if (configSnapshot != null) log.setConfigSnapshot(configSnapshot);
        if (metadata != null) log.setMetadata(metadata);

        // init steps
        log.getSteps().put(PipelineStepType.INGESTION, new PipelineStepResult());
        log.getSteps().put(PipelineStepType.ANALYTICS, new PipelineStepResult());
        log.getSteps().put(PipelineStepType.GRAPH, new PipelineStepResult());
        log.getSteps().put(PipelineStepType.CAMPAIGN, new PipelineStepResult());

        repo.save(log);
        return runId;
    }

    public void startStep(String runId, PipelineStepType step) {
        PipelineRunLog log = repo.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("runId not found: " + runId));

        PipelineStepResult r = ensureStep(log, step);
        r.setStatus(StepStatus.RUNNING);
        r.setStartTime(Instant.now());
        r.setEndTime(null);
        r.setDurationMs(null);
        r.setError(null);

        repo.save(log);
    }

    public void succeedStep(String runId, PipelineStepType step, String message, Map<String, Long> counters) {
        PipelineRunLog log = repo.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("runId not found: " + runId));

        PipelineStepResult r = ensureStep(log, step);
        r.setStatus(StepStatus.SUCCESS);
        r.setEndTime(Instant.now());
        r.setDurationMs(calcDurationMs(r.getStartTime(), r.getEndTime()));
        if (message != null) r.setMessage(message);
        if (counters != null) r.setCounters(counters);

        repo.save(log);
    }


    public void closeRunSuccess(String runId) {
        PipelineRunLog log = repo.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("runId not found: " + runId));

        log.setStatus(RunStatus.SUCCESS);
        log.setEndTime(Instant.now());
        log.setDurationMs(calcDurationMs(log.getStartTime(), log.getEndTime()));
        repo.save(log);
    }

    public void closeRunFailed(String runId, Throwable ex) {
        PipelineRunLog log = repo.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("runId not found: " + runId));

        log.setStatus(RunStatus.FAILED);
        log.setError(compactError(ex));
        log.setEndTime(Instant.now());
        log.setDurationMs(calcDurationMs(log.getStartTime(), log.getEndTime()));
        repo.save(log);
    }

    private PipelineStepResult ensureStep(PipelineRunLog log, PipelineStepType step) {
        return log.getSteps().computeIfAbsent(step, s -> new PipelineStepResult());
    }

    private Long calcDurationMs(Instant start, Instant end) {
        if (start == null || end == null) return null;
        return Duration.between(start, end).toMillis();
    }

    private String compactError(Throwable ex) {
        if (ex == null) return null;
        String msg = ex.getMessage();
        if (msg == null) msg = "";
        msg = msg.replaceAll("\\s+", " ").trim();
        if (msg.length() > 400) msg = msg.substring(0, 400) + "...";
        return ex.getClass().getSimpleName() + ": " + msg;
    }
}