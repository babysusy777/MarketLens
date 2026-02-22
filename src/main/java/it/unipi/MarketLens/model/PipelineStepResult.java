package it.unipi.MarketLens.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class PipelineStepResult {

    @Field("status")
    private StepStatus status = StepStatus.PENDING;

    @Field("startTime")
    private Instant startTime;

    @Field("endTime")
    private Instant endTime;

    @Field("durationMs")
    private Long durationMs;

    @Field("counters")
    private Map<String, Long> counters = new LinkedHashMap<>();

    @Field("message")
    private String message;

    @Field("error")
    private String error;

    @Field("metadata")
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

   public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Map<String, Long> getCounters() { return counters; }
    public void setCounters(Map<String, Long> counters) { this.counters = counters; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public void incCounter(String key, long delta) {
        this.counters.put(key, this.counters.getOrDefault(key, 0L) + delta);
    }
}
