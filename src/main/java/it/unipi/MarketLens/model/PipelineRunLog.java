package it.unipi.MarketLens.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Document(collection = "pipeline_run_log")
public class PipelineRunLog {

    @Id
    private String id;

    @Indexed(unique = true)
    private String runId;

    @Indexed
    private RunStatus status;

    @Indexed
    private Instant startTime;

    private Instant endTime;
    private Long durationMs;

    /**
     * Snapshot config runtime usata per quella run (cron, batchSize, sources, ecc.)
     * Mantienila flessibile: è log/audit, non un model “core domain”.
     */
    private Map<String, Object> configSnapshot = new LinkedHashMap<>();

    /**
     * Risultati per step.
     */
    private Map<PipelineStepType, PipelineStepResult> steps = new EnumMap<>(PipelineStepType.class);

    /**
     * Error summary globale (se la run fallisce).
     */
    private String error;

    /**
     * Metadata operativa (hostname, instanceId, env, version, ecc.)
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public PipelineRunLog() {}

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Map<String, Object> getConfigSnapshot() { return configSnapshot; }
    public void setConfigSnapshot(Map<String, Object> configSnapshot) { this.configSnapshot = configSnapshot; }

    public Map<PipelineStepType, PipelineStepResult> getSteps() { return steps; }
    public void setSteps(Map<PipelineStepType, PipelineStepResult> steps) { this.steps = steps; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}