package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.*;
import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.repository.graph.TopicGraphRepository;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.TrendAnalyticsRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/analyst/trends")
public class TrendController {

    private final TrendAnalyticsRepository trendRepository;
    private final BrandRepository brandRepository;
    private final TopicGraphRepository topicGraphRepository; // 1. Dichiarazione

    // 2. Costruttore per l'iniezione (Dependency Injection)
    public TrendController(TrendAnalyticsRepository trendRepository,
                           BrandRepository brandRepository,
                           TopicGraphRepository topicGraphRepository) {
        this.trendRepository = trendRepository;
        this.brandRepository = brandRepository;
        this.topicGraphRepository = topicGraphRepository;
    }

    @GetMapping("/hashtags")
    public List<HashtagTrendDTO> getHashtagTrends(
            @RequestParam Industry industry,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam String platform
    ) {

        var start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        String platformRegex = "^" + platform + "$";

        return trendRepository.findTopHashtagsByIndustry(
                industry.name(),
                Date.from(start),
                Date.from(end),
                platformRegex
        );
    }
    @GetMapping("/brands-ranking")
    public List<BrandRankingDTO> getBrandRanking(@RequestParam Industry industry) {

        return brandRepository.findTopBrandsByEngagement(String.valueOf(industry));
    }
    @GetMapping("/top-topics")
    public List<TopicTrendDTO> getTopTopics(
            @RequestParam Industry industry,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return topicGraphRepository.findTopRecurringTopics(industry.name(), limit);
    }
    @GetMapping("/diversity")
    public List<BrandDiversityDTO> getBrandTopicDiversity(
            @RequestParam Industry industry
    ) {

        return topicGraphRepository.findBrandTopicDiversity(industry.name());
    }

    @GetMapping("/exclusive-topics")
    public List<TopicExclusivityDTO> getExclusiveTopics(
            @RequestParam Industry industry
    ) {

        return topicGraphRepository.findExclusiveTopics(industry.name());
    }
}