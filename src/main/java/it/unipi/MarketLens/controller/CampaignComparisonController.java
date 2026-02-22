package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.CampaignCompetitorComparisonResponse;
import it.unipi.MarketLens.service.CampaignCompetitorComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyst/campaigns")
public class CampaignComparisonController {

    @Autowired
    private CampaignCompetitorComparisonService service;

    @GetMapping("/competitor-benchmark")
    public ResponseEntity<?> compareWithCompetitors(
            @RequestParam String campaignName,
            @RequestParam String username
    ) {
        try {
            CampaignCompetitorComparisonResponse resp = service.compareCampaignCompetitors(campaignName, username);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error in the benchmark: " + e.getMessage());
        }
    }
}