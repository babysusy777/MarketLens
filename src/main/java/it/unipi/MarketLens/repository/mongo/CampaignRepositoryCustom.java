package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.dto.CampaignResponse;
import it.unipi.MarketLens.model.Campaign;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;

import java.time.Instant;
import java.util.List;

public interface CampaignRepositoryCustom {

    CampaignResponse analyzeCampaign(Campaign campaignConfig);

    CampaignResponse analyzeVirtualCampaign(String brand, Instant start, Instant end);
}