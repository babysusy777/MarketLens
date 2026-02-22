package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.PostSearchResponse;
import it.unipi.MarketLens.model.Post;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/analyst/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BrandRepository brandRepository;


    @GetMapping("/search")
    public ResponseEntity<?> searchPostsUnified(
            // PARAMETRO OBBLIGATORIO
            @RequestParam String username,

           // FILTRI OPZIONALI
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String platform,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        // RICERCA MANUALE.
        boolean hasSearchFilters = (keyword != null && !keyword.isBlank()) ||
                (author != null && !author.isBlank()) ||
                (platform != null && !platform.isBlank()) ||
                (dateFrom != null) ||
                (dateTo != null);

        if (hasSearchFilters) {
            // RICERCA MANUALE (Filtri attivi)
            Instant from = (dateFrom != null) ? dateFrom.atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.EPOCH;
            Instant to = (dateTo != null) ? dateTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.now();

            String searchKeyword = (keyword != null) ? keyword : "";
            String searchAuthor = (author != null) ? author : "";
            String searchPlatform = (platform != null) ? platform : "";

            List<Post> posts = postRepository.searchPosts(searchKeyword, searchAuthor, searchPlatform, from, to);
            return ResponseEntity.ok(calculateAnalytics(posts));

        } else {
            var brandOpt = brandRepository.findByUsername(username);

            if (brandOpt.isPresent()) {
                var brand = brandOpt.get();

                List<String> rawKeywords = brand.getMonitoringKeywords() != null ? brand.getMonitoringKeywords() : new ArrayList<>();
                List<String> topics = brand.getMonitoringTopics() != null ? brand.getMonitoringTopics() : new ArrayList<>();

                // Se il brand non ha preferenze salvate, restituisci vuoto
                if (rawKeywords.isEmpty() && topics.isEmpty()) {
                    return ResponseEntity.ok(new PostSearchResponse(new ArrayList<>(), 0, 0.0));
                }

                List<String> smartKeywords = generateVariations(rawKeywords);
                List<String> smartTopics = generateVariations(topics);

                List<Post> posts = postRepository.findByPreferences(smartKeywords, smartTopics);

                return ResponseEntity.ok(calculateAnalytics(posts));
            }

            // Se l'utente non è associato a un brand o non esiste
            return ResponseEntity.badRequest().body("no brand configured for the user: " + username);
        }
    }


    private PostSearchResponse calculateAnalytics(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return new PostSearchResponse(new ArrayList<>(), 0, 0.0);
        }

        double totalEngagement = posts.stream()
                .mapToDouble(p -> (p.getTotalEngagement() != null ? p.getTotalEngagement() : 0))
                .sum();

        return new PostSearchResponse(posts, posts.size(), totalEngagement);
    }

    private List<String> generateVariations(List<String> inputs) {
        Set<String> variations = new HashSet<>();
        for (String input : inputs) {
            if (input == null || input.isBlank()) continue;
            String term = input.trim();
            variations.add(term);
            variations.add(term.toLowerCase());
            if (term.length() > 1) {
                variations.add(term.substring(0, 1).toUpperCase() + term.substring(1).toLowerCase());
            }
            variations.add(term.toUpperCase());

            if (!term.startsWith("#")) {
                variations.add("#" + term);
                variations.add("#" + term.toLowerCase());
            } else {
                variations.add(term.substring(1));
            }
        }
        return new ArrayList<>(variations);
    }
}