package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.CampaignRequest;
import it.unipi.MarketLens.dto.CampaignResponse;
import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/analyst/campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @PostMapping
    public ResponseEntity<?> createCampaign(
            @RequestParam String analystUsername,
            @RequestParam String name,
            @RequestParam String brandName,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) List<String> hashtags,
            @RequestParam(required = false) List<String> keywords
    ) {
        CampaignRequest request = new CampaignRequest();

        request.setAnalystUsername(analystUsername);
        request.setName(name);
        request.setBrandName(brandName);
        request.setHashtags(hashtags);
        request.setKeywords(keywords);


        request.setStartDate(startDate.atStartOfDay(ZoneOffset.UTC).toInstant());

        if (endDate != null) {
            request.setEndDate(endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        }

        try {
            CampaignResponse response =
                    campaignService.createAndAnalyzeCampaign(request, analystUsername);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> getUserCampaigns(@RequestParam String username) {
        return ResponseEntity.ok(campaignService.getUserCampaigns(username));
    }


    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCampaign(
            @RequestParam String campaignName,
            @RequestParam String username
    ) {
        try {
            System.out.println("Export richiesto per: " + campaignName + " User: " + username);

            byte[] csvContent = campaignService.exportCampaignByNameToCsv(campaignName, username);

            String filename = "report_" + campaignName.replaceAll("\\s+", "_") + ".csv";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvContent);

        } catch (Exception e) {
            System.err.println("ERRORE EXPORT: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}