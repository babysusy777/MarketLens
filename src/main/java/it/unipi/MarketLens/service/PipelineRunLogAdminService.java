package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.PipelineRunLogSummaryResponse;
import it.unipi.MarketLens.model.*;
import it.unipi.MarketLens.repository.mongo.PipelineRunLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

@Service
public class PipelineRunLogAdminService {

    private final PipelineRunLogRepository repo;

    public PipelineRunLogAdminService(PipelineRunLogRepository repo) {
        this.repo = repo;
    }

    public Page<PipelineRunLogSummaryResponse> list(
            RunStatus status,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 200));

        Page<PipelineRunLog> logs;
        boolean hasFromTo = (from != null && to != null);

        if (status != null && hasFromTo) {
            logs = repo.findByStatusAndStartTimeBetweenOrderByStartTimeDesc(status, from, to, pageable);
        } else if (status != null) {
            logs = repo.findByStatusOrderByStartTimeDesc(status, pageable);
        } else if (hasFromTo) {
            logs = repo.findByStartTimeBetweenOrderByStartTimeDesc(from, to, pageable);
        } else {
            logs = repo.findAllByOrderByStartTimeDesc(pageable);
        }

        return logs.map(this::toSummary);
    }

    public PipelineRunLog getByRunId(String runId) {
        return repo.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("runId not found: " + runId));
    }

    public String exportCsv(RunStatus status, Instant from, Instant to, int limit) {
        // Export “semplice”: usiamo list() con size=limit e page=0
        Page<PipelineRunLogSummaryResponse> page = list(status, from, to, 0, clamp(limit, 1, 5000));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);


        pw.println("runId,status,startTime,endTime,durationMs," +
                "ingestion,analytics,graph,campaign");

        for (PipelineRunLogSummaryResponse r : page.getContent()) {
            pw.print(escape(r.runId)); pw.print(",");
            pw.print(escape(String.valueOf(r.status))); pw.print(",");
            pw.print(escape(String.valueOf(r.startTime))); pw.print(",");
            pw.print(escape(String.valueOf(r.endTime))); pw.print(",");
            pw.print(r.durationMs == null ? "" : r.durationMs); pw.print(",");

            pw.print(escape(step(r, PipelineStepType.INGESTION))); pw.print(",");
            pw.print(escape(step(r, PipelineStepType.ANALYTICS))); pw.print(",");
            pw.print(escape(step(r, PipelineStepType.GRAPH))); pw.print(",");
            pw.print(escape(step(r, PipelineStepType.CAMPAIGN)));

            pw.println();
        }

        pw.flush();
        return sw.toString();
    }

    private PipelineRunLogSummaryResponse toSummary(PipelineRunLog log) {
        Map<PipelineStepType, StepStatus> steps = new EnumMap<>(PipelineStepType.class);
        if (log.getSteps() != null) {
            for (Map.Entry<PipelineStepType, PipelineStepResult> e : log.getSteps().entrySet()) {
                StepStatus ss = (e.getValue() != null && e.getValue().getStatus() != null)
                        ? e.getValue().getStatus()
                        : StepStatus.PENDING;
                steps.put(e.getKey(), ss);
            }
        }

        steps.putIfAbsent(PipelineStepType.INGESTION, StepStatus.PENDING);
        steps.putIfAbsent(PipelineStepType.ANALYTICS, StepStatus.PENDING);
        steps.putIfAbsent(PipelineStepType.GRAPH, StepStatus.PENDING);
        steps.putIfAbsent(PipelineStepType.CAMPAIGN, StepStatus.PENDING);

        return PipelineRunLogSummaryResponse.of(
                log.getRunId(),
                log.getStatus(),
                log.getStartTime(),
                log.getEndTime(),
                log.getDurationMs(),
                steps
        );
    }

    private String step(PipelineRunLogSummaryResponse r, PipelineStepType t) {
        StepStatus ss = r.stepStatuses.get(t);
        return ss == null ? "" : ss.name();
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private String escape(String s) {
        if (s == null) return "";
        // CSV escaping basico
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

