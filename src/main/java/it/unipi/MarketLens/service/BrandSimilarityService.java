package it.unipi.MarketLens.service;

import it.unipi.MarketLens.repository.graph.BrandGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BrandSimilarityService {

    private final BrandGraphRepository brandRepo;

    public BrandSimilarityService(BrandGraphRepository brandRepo) {
        this.brandRepo = brandRepo;
    }


    @Transactional("transactionManager")
    @SuppressWarnings("unchecked")
    public void rebuildSimilarity(String industry, int topK) {

        String clusterIdsProp = "clusterIds_" + industry;
        String clusterFreqsProp = "clusterFreqs_" + industry;


        List<Map<String, Object>> rows = brandRepo.getAllBrandClusterVectors(industry, clusterIdsProp, clusterFreqsProp);
        Map<String, Map<Long, Long>> vectors = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name");

            List<?> idsRaw = (List<?>) row.get("clusterIds");
            List<?> freqsRaw = (List<?>) row.get("clusterFreqs");

            if (idsRaw == null || freqsRaw == null) continue;

            Map<Long, Long> v = new HashMap<>();

            int n = Math.min(idsRaw.size(), freqsRaw.size());
            for (int i = 0; i < n; i++) {
                Object idObj = idsRaw.get(i);
                Object frObj = freqsRaw.get(i);

                Long id = (idObj instanceof Number)
                        ? ((Number) idObj).longValue()
                        : Long.valueOf(idObj.toString());

                Long fr = (frObj instanceof Number)
                        ? ((Number) frObj).longValue()
                        : Long.valueOf(frObj.toString());

                v.put(id, fr);
            }
            vectors.put(name, v);
        }


        for (String b1 : vectors.keySet()) {

            PriorityQueue<Map.Entry<String, Double>> top =
                    new PriorityQueue<>(Map.Entry.comparingByValue());

            for (String b2 : vectors.keySet()) {
                if (b1.equals(b2)) continue;

                double sim = cosine(vectors.get(b1), vectors.get(b2));
                if (sim <= 0) continue;

                top.offer(Map.entry(b2, sim));
                if (top.size() > topK) top.poll();
            }

            while (!top.isEmpty()) {
                var e = top.poll();
                brandRepo.createSimilarity(b1, e.getKey(), industry, e.getValue());
            }
        }
    }

    private double cosine(Map<Long, Long> v1, Map<Long, Long> v2) {
        double dot = 0, n1 = 0, n2 = 0;

        for (long k : v1.keySet()) {
            long x = v1.get(k);
            n1 += x * x;
            if (v2.containsKey(k)) {
                dot += x * v2.get(k);
            }
        }

        for (long y : v2.values()) {
            n2 += y * y;
        }

        if (n1 == 0 || n2 == 0) return 0;
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }
}