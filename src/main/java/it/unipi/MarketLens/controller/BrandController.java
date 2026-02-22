package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.BrandSetupRequest;
import it.unipi.MarketLens.dto.BrandSetupResponse;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.User;
import it.unipi.MarketLens.model.UserRole;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.PostRepository;
import it.unipi.MarketLens.repository.mongo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyst/brand-setup")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping
    public ResponseEntity<?> saveBrandConfiguration(@RequestBody BrandSetupRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User does not exist"));

            if (user.getRole() != UserRole.MARKETING_ANALYST) {
                return ResponseEntity.status(403).body("Only marketing analysts can configure brands");
            }

            // Cerca il brand--> Se non esiste nel DB 'brands', controlla se ci sono i post
            Brand brand = brandRepository.findByBrandName(request.getBrandName())
                    .orElseGet(() -> {
                        boolean postsExist = postRepository.existsByUserPostedIgnoreCase(request.getBrandName());

                        if (!postsExist) {
                            throw new RuntimeException("Error: no posts are currently available for " + request.getBrandName() + "'. Please ingest data before proceeding.");
                        }

                        // Se i post ci sono, creiamo l'oggetto Brand
                        Brand newBrand = new Brand();
                        newBrand.setBrandName(request.getBrandName());
                        return newBrand;
                    });

            // Il brand è già di qualcun altro?
            if (brand.getId() != null && brand.getUsername() != null && !brand.getUsername().equals(user.getUsername())) {
                return ResponseEntity.badRequest().body("Brand already configured by another analyst");
            }

            // L'utente ha già un altro brand?
            if (user.getBrandId() != null && brand.getId() != null && !user.getBrandId().equals(brand.getId())) {
                return ResponseEntity.badRequest().body("Marketing analyst already linked to another brand");
            }

            // SALVATAGGIO CONFIGURAZIONE
            brand.setUsername(request.getUsername());
            brand.setMonitoringKeywords(request.getMonitoringKeywords());
            brand.setMonitoringTopics(request.getMonitoringTopics());

            Brand savedBrand = brandRepository.save(brand);


            user.setBrandId(savedBrand.getId());
            userRepository.save(user);

            BrandSetupResponse response = new BrandSetupResponse();
            response.setId(savedBrand.getId());
            response.setUsername(savedBrand.getUsername());
            response.setBrandName(savedBrand.getBrandName());
            response.setIndustry(savedBrand.getIndustry());
            response.setMonitoringKeywords(savedBrand.getMonitoringKeywords());
            response.setMonitoringTopics(savedBrand.getMonitoringTopics());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}