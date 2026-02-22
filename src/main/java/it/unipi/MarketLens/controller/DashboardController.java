package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.DashboardDbStatusResponse;
import it.unipi.MarketLens.dto.DashboardPipelineStatusResponse;
import it.unipi.MarketLens.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /*
     * Stato DB (Mongo + Neo4j) con stats sintetiche.
     * Ideale per Swagger come "dashboard semplice".
     */
    @GetMapping("/db-status")
    public DashboardDbStatusResponse getDbStatus() {
        return dashboardService.getDbStatus();
    }

    /*
     * Stato pipeline: ultimo run, run in corso (se presente), ultimi run, ultimi failure.
     */
    @GetMapping("/pipeline-status")
    public DashboardPipelineStatusResponse getPipelineStatus() {
        return dashboardService.getPipelineStatus();
    }
}

