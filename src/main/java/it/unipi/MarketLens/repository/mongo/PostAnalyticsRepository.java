package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.dto.BrandMetricsResponse;
import java.util.List;



public interface PostAnalyticsRepository {

    BrandMetricsResponse computeBrandMetrics(List<String> authors);


}
