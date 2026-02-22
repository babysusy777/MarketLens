package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.PipelineRunLogSummaryResponse;
import it.unipi.MarketLens.model.PipelineRunLog;
import it.unipi.MarketLens.model.RunStatus;
import it.unipi.MarketLens.service.PipelineRunLogAdminService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@RequestMapping("/admin/pipeline-runs")
public class PipelineRunLogAdminController {

    private final PipelineRunLogAdminService service;

    public PipelineRunLogAdminController(PipelineRunLogAdminService service) {
        this.service = service;
    }

    /*
     * LISTA: filtri per status e intervallo (startTime tra from/to).
     * Esempio:
     * /admin/pipeline-runs?status=SUCCESS&from=2026-01-18T00:00:00Z&to=2026-01-18T23:59:59Z&page=0&size=20
     */
    @GetMapping
    public Page<PipelineRunLogSummaryResponse> list(
            @RequestParam(required = false) RunStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(status, from, to, page, size);
    }

    /*
     * DETTAGLIO: ritorna l'intero documento PipelineRunLog (include configSnapshot, steps, error, metadata).
     */
    @GetMapping("/{runId}")
    public PipelineRunLog get(@PathVariable String runId) {
        return service.getByRunId(runId);
    }

    /*
     * EXPORT: CSV o JSON.
     * - format=csv -> attachment
     * - format=json -> attachment con json list (riusiamo la lista page=0)
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) RunStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        if ("json".equalsIgnoreCase(format)) {
            var page = service.list(status, from, to, 0, Math.max(1, Math.min(limit, 5000)));
            byte[] body = page.getContent().toString().getBytes(StandardCharsets.UTF_8); // vedi nota sotto

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pipeline_runs.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(page.getContent());
        }

        String csv = service.exportCsv(status, from, to, limit);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pipeline_runs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}
