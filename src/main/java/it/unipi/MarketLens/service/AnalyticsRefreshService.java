package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.BrandMetricsResponse;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsRefreshService {

    private final BrandRepository brandRepository;
    private final BrandMetricsService brandMetricsService;

    public AnalyticsRefreshService(BrandRepository brandRepository,
                                   BrandMetricsService brandMetricsService) {
        this.brandRepository = brandRepository;
        this.brandMetricsService = brandMetricsService;
    }

    public static class AnalyticsStats {
        public long brandsTotal = 0;
        public long brandsProcessed = 0;

        public Map<String, Long> toCounters() {
            return Map.of(
                    "brandsTotal", brandsTotal,
                    "brandsProcessed", brandsProcessed
            );
        }
    }

    public AnalyticsStats refreshAllWithStats() {
        List<Brand> brands = brandRepository.findAll();

        AnalyticsStats stats = new AnalyticsStats();
        stats.brandsTotal = brands.size();

        for (Brand b : brands) {

            if (b.getBrandName() == null) continue;


            BrandMetricsResponse m = brandMetricsService.getMetrics(b.getBrandName());


            b.setTotalPosts(m.getTotalPosts());
            b.setTotalEngagement(m.getTotalEngagement());
            b.setAvgSentiment(m.getAvgSentiment());


            b.setAnalyticsLastRefreshedAt(LocalDateTime.now());

            b.setEngagementEvolution(
                    m.getEngagementEvolution() == null ? List.of() :
                            m.getEngagementEvolution().stream()
                                    .map(tp -> new Brand.MetricsTimePoint(tp.getDate(), tp.getValue()))
                                    .collect(Collectors.toList())
            );

            b.setSentimentEvolution(
                    m.getSentimentEvolution() == null ? List.of() :
                            m.getSentimentEvolution().stream()
                                    .map(tp -> new Brand.MetricsTimePoint(tp.getDate(), tp.getValue()))
                                    .collect(Collectors.toList())
            );

            brandRepository.save(b);

            stats.brandsProcessed++;
        }

        return stats;
    }
}