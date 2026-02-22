package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.graph.CompetitorGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CompetitorService {

    @Autowired
    private CompetitorGraphRepository competitorGraphRepository;

    @Autowired
    private BrandRepository brandRepository;


    public List<Brand.Competitor> computeAndPersistCompetitors(String brandName, Industry industry) {

        List<Map<String, Object>> rows =
                competitorGraphRepository.getTopCompetitors(brandName, industry);

        List<Brand.Competitor> competitors = rows.stream()
                .map(r -> new Brand.Competitor(
                        (String) r.get("brand"),
                        ((Number) r.get("score")).doubleValue()
                ))
                .toList();

        Brand brand = brandRepository
                .findByBrandNameIgnoreCase(brandName)
                .orElseThrow(() -> new RuntimeException("Brand not found in Mongo: " + brandName));

        brand.setCompetitors(competitors);
        brandRepository.save(brand);

        return competitors;
    }
}


