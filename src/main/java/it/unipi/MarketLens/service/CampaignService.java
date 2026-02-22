package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.CampaignRequest;
import it.unipi.MarketLens.dto.CampaignResponse;
import it.unipi.MarketLens.model.Campaign;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.CampaignRepository;
import it.unipi.MarketLens.repository.mongo.CampaignRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignRepositoryCustom campaignRepositoryCustom;

    @Autowired
    private BrandRepository brandRepository;


    public CampaignResponse createAndAnalyzeCampaign(CampaignRequest req, String username) {


        if (campaignRepository.existsByNameAndAnalystUsername(req.getName(), username)) {
            throw new IllegalArgumentException("You already have a campaign named '" + req.getName() + "'.");
        }


        if (req.getBrandName() != null && !req.getBrandName().isBlank()) {
            boolean brandExists = brandRepository
                    .findByBrandNameIgnoreCase(req.getBrandName())
                    .isPresent();

            if (!brandExists) {
                throw new IllegalArgumentException(
                        "Brand not found: " + req.getBrandName()
                );
            }
        }


        Campaign c = new Campaign();
        c.setName(req.getName());
        c.setAnalystUsername(req.getAnalystUsername());
        c.setBrandAuthor(req.getBrandName());
        c.setKeywords(req.getKeywords());
        c.setHashtags(req.getHashtags());
        c.setPlatforms(List.of("instagram"));
        c.setStartDate(req.getStartDate());
        c.setEndDate(req.getEndDate());


        c = campaignRepository.save(c);
        c = campaignRepository.findById(c.getId()).orElseThrow();
        CampaignResponse stats = campaignRepositoryCustom.analyzeCampaign(c);


        updateCampaignStatsInDb(c, stats);

        return stats;
    }

    private void updateCampaignStatsInDb(Campaign c, CampaignResponse stats) {

        c.setTotalPosts(stats.getTotalPosts());
        c.setTotalEngagement(stats.getTotalEngagement());
        c.setHashtags(stats.getHashtags());
        c.setKeywords(stats.getKeywords());

        double sentimentScore = 0.0;
        if (stats.getSentimentDistribution() != null) {
            double pos = stats.getSentimentDistribution().getPositivePercent();
            double neg = stats.getSentimentDistribution().getNegativePercent();

            sentimentScore = (pos - neg) / 100.0;
        }
        c.setAvgSentiment(sentimentScore);


        campaignRepository.save(c);
    }


    public List<Campaign> getUserCampaigns(String username) {
        return campaignRepository.findByAnalystUsername(username);
    }


    public byte[] exportCampaignByNameToCsv(String campaignName, String username) {

        Campaign c = campaignRepository.findByNameAndAnalystUsername(campaignName, username)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignName));


        CampaignResponse stats = campaignRepositoryCustom.analyzeCampaign(c);


        StringBuilder csv = new StringBuilder();


        csv.append("REPORT CAMPAIGN MARKETLENS\n");
        csv.append("Campaign name:,").append(escape(c.getName())).append("\n");
        csv.append("Analyst:,").append(c.getAnalystUsername()).append("\n");
        csv.append("Period:,").append(c.getStartDate()).append(" - ").append(c.getEndDate()).append("\n");
        csv.append("Brand:,").append(c.getBrandAuthor()).append("\n");


        List<String> platforms = c.getPlatforms() != null ? c.getPlatforms() : List.of();
        csv.append("Platforms:,").append(String.join(" | ", platforms)).append("\n");

        csv.append("\n");


        csv.append("METRICS\n");
        csv.append("Total Post,").append(stats.getTotalPosts()).append("\n");
        csv.append("Total Engagement,").append(stats.getTotalEngagement()).append("\n");

        if (stats.getSentimentDistribution() != null) {
            csv.append("Sentiment Positive (%),").append(stats.getSentimentDistribution().getPositivePercent()).append("\n");
            csv.append("Sentiment Negative (%),").append(stats.getSentimentDistribution().getNegativePercent()).append("\n");
            csv.append("Sentiment Neutral (%),").append(stats.getSentimentDistribution().getNeutralPercent()).append("\n");
        } else {
            csv.append("Avg Sentiment Score,").append(c.getAvgSentiment()).append("\n");
        }

        csv.append("\n");


        csv.append("CONFIGURATION\n");


        List<String> hashtags = c.getHashtags() != null ? c.getHashtags() : List.of();
        List<String> keywords = c.getKeywords() != null ? c.getKeywords() : List.of();

        csv.append("Hashtags,").append(String.join(" ", hashtags)).append("\n");
        csv.append("Keywords,").append(String.join(" ", keywords)).append("\n");

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }


    private String escape(String data) {
        if (data == null) return "";
        return data.replace(";", ",");
    }

    public void refreshCampaignStats(Campaign c) {
        CampaignResponse stats = campaignRepositoryCustom.analyzeCampaign(c);
        updateCampaignStatsInDb(c, stats);
    }


}