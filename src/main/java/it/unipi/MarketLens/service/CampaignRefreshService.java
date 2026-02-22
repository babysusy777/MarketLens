package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.repository.mongo.CampaignRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CampaignRefreshService {

    private final CampaignRepository campaignRepository;
    private final CampaignService campaignService;

    public CampaignRefreshService(CampaignRepository campaignRepository,
                                  CampaignService campaignService) {
        this.campaignRepository = campaignRepository;
        this.campaignService = campaignService;
    }

    public static class CampaignStats {
        public long total = 0;
        public long refreshed = 0;
        public long failed = 0;

        public Map<String, Long> toCounters() {
            return Map.of(
                    "campaignsTotal", total,
                    "campaignsRefreshed", refreshed,
                    "campaignsFailed", failed
            );
        }
    }

    public CampaignStats refreshSavedCampaignsWithStats() {
        List<Campaign> campaigns = campaignRepository.findAll();

        CampaignStats stats = new CampaignStats();
        stats.total = campaigns.size();

        for (Campaign c : campaigns) {
            try {
                campaignService.refreshCampaignStats(c);
                stats.refreshed++;
            } catch (Exception e) {
                stats.failed++;
                System.err.println("[CampaignRefresh] Failed for campaign="
                        + (c.getName() != null ? c.getName() : c.getId())
                        + " -> " + e.getMessage());
            }
        }
        return stats;
    }
}


