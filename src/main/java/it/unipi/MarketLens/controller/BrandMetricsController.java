package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.BrandFocusDTO;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.repository.graph.TopicGraphRepository;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/analyst/brands")
public class BrandMetricsController {

    @Autowired
    private final BrandRepository brandRepository;
    private final TopicGraphRepository topicGraphRepository;


    public BrandMetricsController(BrandRepository brandRepository, TopicGraphRepository topicGraphRepository) {
        this.brandRepository = brandRepository;
        this.topicGraphRepository = topicGraphRepository;
    }

    @GetMapping("/{brandName}")
    public ResponseEntity<Brand> getBrandDetails(@PathVariable String brandName) {

        return brandRepository.findByBrandName(brandName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/{brandName}/semantic-focus")
    public List<BrandFocusDTO> getBrandSemanticFocus(
            @PathVariable String brandName,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // Ora il controller restituisce una lista di oggetti tipizzati invece di una mappa generica
        return topicGraphRepository.findBrandSemanticFocus(brandName, limit);
    }


}