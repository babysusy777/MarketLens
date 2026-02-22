package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.CampaignCompetitorComparisonResponse;
import it.unipi.MarketLens.dto.CampaignResponse;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.CampaignRepository;
import it.unipi.MarketLens.repository.mongo.CampaignRepositoryCustom;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignCompetitorComparisonService {

    private final CampaignRepositoryCustom campaignRepositoryCustom;
    private final CampaignRepository campaignRepository;
    private final BrandRepository brandRepository;

    public CampaignCompetitorComparisonService(
            CampaignRepositoryCustom campaignRepositoryCustom,
            CampaignRepository campaignRepository,
            BrandRepository brandRepository
    ) {
        this.campaignRepositoryCustom = campaignRepositoryCustom;
        this.campaignRepository = campaignRepository;
        this.brandRepository = brandRepository;
    }

    public CampaignCompetitorComparisonResponse compareCampaignCompetitors(
            String campaignName,
            String username
    ) {

        Campaign campaign = campaignRepository
                .findByNameAndAnalystUsername(campaignName, username)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Brand mainBrand = brandRepository
                .findByBrandNameIgnoreCase(campaign.getBrandAuthor())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Instant start = campaign.getStartDate();
        Instant end   = campaign.getEndDate();

        CampaignCompetitorComparisonResponse resp =
                new CampaignCompetitorComparisonResponse();

        resp.setCampaignId(campaign.getId());
        resp.setCampaignName(campaign.getName());
        resp.setMainBrand(campaign.getBrandAuthor());
        resp.setStartDate(start);
        resp.setEndDate(end);
        resp.setHashtags(campaign.getHashtags());
        resp.setKeywords(campaign.getKeywords());
        resp.setTotalPosts(campaign.getTotalPosts());
        resp.setTotalEngagement(campaign.getTotalEngagement());
        resp.setMessage(campaign.getMessage());

        List<CampaignCompetitorComparisonResponse.CompetitorStats> out =
                new ArrayList<>();

        if (mainBrand.getCompetitors() != null) {
            for (Brand.Competitor c : mainBrand.getCompetitors()) {

                CampaignResponse virtual =
                        campaignRepositoryCustom.analyzeVirtualCampaign(
                                c.getBrand(),
                                start,
                                end
                        );

                CampaignCompetitorComparisonResponse.CompetitorStats cs =
                        new CampaignCompetitorComparisonResponse.CompetitorStats();

                cs.setBrand(c.getBrand());
                cs.setSimilarityScore(
                        c.getSimilarityScore() != null ? c.getSimilarityScore() : 0.0
                );

                cs.setTotalPosts(virtual.getTotalPosts());
                cs.setTotalEngagement(virtual.getTotalEngagement());
                cs.setHashtags(virtual.getHashtags());
                cs.setKeywords(virtual.getKeywords());
                cs.setMessage(virtual.getMessage());

                cs.setAvgSentiment(
                        (virtual.getSentimentDistribution() != null)
                                ? virtual.getSentimentDistribution().getPositivePercent()
                                : 0.0
                );

                out.add(cs);
            }
        }

        resp.setCompetitors(out);
        return resp;
    }
}
