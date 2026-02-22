package it.unipi.MarketLens.utils;

import it.unipi.MarketLens.dto.TopicCoOccurrenceDTO;

import java.util.*;
import java.util.stream.Collectors;

/*
 Utility class per il Topic Clustering
 Implementa Connected Components sul grafo Topic–Topic.
 */
public class TopicClusteringUtils {

    private static final long MIN_EDGE_WEIGHT = 3;

    public static Map<String, Map<String, Long>> buildWeightedGraph(List<TopicCoOccurrenceDTO> edges) {

        if (edges == null || edges.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Long>> graph = new HashMap<>();

        for (TopicCoOccurrenceDTO e : edges) {
            if (e == null) continue;
            if (e.getStrength() < MIN_EDGE_WEIGHT) continue;

            String t1 = e.getTopic1();
            String t2 = e.getTopic2();

            if (t1 == null || t1.isBlank() || t2 == null || t2.isBlank()) {
                continue;
            }

            graph
                    .computeIfAbsent(t1, k -> new HashMap<>())
                    .put(t2, e.getStrength());

            graph
                    .computeIfAbsent(t2, k -> new HashMap<>())
                    .put(t1, e.getStrength());
        }

        if (graph.isEmpty()) {
            return Collections.emptyMap();
        }

        return graph;
    }

    public static Map<String, Set<String>> buildPrunedEgoNetwork(
            String hub,
            Map<String, Map<String, Long>> graph,
            double alpha
    ) {
        if (hub == null || hub.isBlank() || graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Set<String>> egoGraph = new HashMap<>();

        Map<String, Long> neighbors = graph.get(hub);
        if (neighbors == null || neighbors.isEmpty()) {
            return egoGraph; // vuoto
        }

        long maxWeight = neighbors.values().stream()
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        if (maxWeight <= 0) {
            return egoGraph;
        }

        long secondMax = neighbors.values().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .orElse(2L);

        // alpha può essere 0 o negativo: protezione
        double safeAlpha = (alpha <= 0) ? 0.3 : alpha;

        long threshold = Math.max(2, Math.min(secondMax, (long) (safeAlpha * maxWeight)));

        // solo neighbors forti
        Set<String> strongNeighbors = neighbors.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .filter(e -> e.getValue() != null && e.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (strongNeighbors.isEmpty()) {
            return egoGraph;
        }

        egoGraph.putIfAbsent(hub, new HashSet<>());

        // collega hub ↔ neighbors forti (cluster hub-centric)
        for (String n : strongNeighbors) {
            egoGraph.get(hub).add(n);
            egoGraph.computeIfAbsent(n, k -> new HashSet<>()).add(hub);
        }

        return egoGraph;
    }

    /**
     * Seleziona gli "hub" come topic con degree >= cutoff (percentile 85%).
     * Versione safe: gestisce grafo vuoto e impedisce IndexOutOfBounds.
     */
    public static Set<String> findHubTopics(Map<String, Map<String, Long>> graph) {

        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }

        Map<String, Integer> degree = new HashMap<>();
        for (var entry : graph.entrySet()) {
            int d = (entry.getValue() == null) ? 0 : entry.getValue().size();
            degree.put(entry.getKey(), d);
        }

        if (degree.isEmpty()) {
            return Collections.emptySet();
        }

        List<Integer> degrees = degree.values().stream()
                .sorted()
                .toList();

        if (degrees.isEmpty()) {
            return Collections.emptySet();
        }

        // percentile 85% (top ~15%). Indice sempre valido.
        int idx = (int) Math.floor((degrees.size() - 1) * 0.85);
        int cutoff = degrees.get(idx);

        return degree.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .filter(e -> e.getValue() >= cutoff)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Ego-network clustering hub-centrico.
     * Versione safe:
     * - se edges/grafo vuoti -> emptyMap
     * - se non ci sono hub -> emptyMap
     * - se egoGraph vuoto per un hub -> cluster singleton {hub}
     */
    public static Map<Integer, Set<String>> egoNetworkClustering(List<TopicCoOccurrenceDTO> edges) {

        Map<String, Map<String, Long>> graph = buildWeightedGraph(edges);
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<String> hubs = findHubTopics(graph);
        if (hubs == null || hubs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, Set<String>> finalClusters = new HashMap<>();
        int globalClusterId = 0;

        for (String hub : hubs) {
            if (hub == null || hub.isBlank()) continue;

            // Costruisci ego-network (pruned)
            Map<String, Set<String>> egoGraph =
                    TopicClusteringUtils.buildPrunedEgoNetwork(
                            hub,
                            graph,
                            0.3   // alpha
                    );

            if (egoGraph == null || egoGraph.isEmpty()) {
                finalClusters.put(
                        globalClusterId++,
                        new HashSet<>(List.of(hub))
                );
                continue;
            }

            // Hub deve avere almeno un vicino nel grafo ego
            Set<String> hubNeighbors = egoGraph.getOrDefault(hub, Set.of()).stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

            if (hubNeighbors.isEmpty()) {
                finalClusters.put(
                        globalClusterId++,
                        new HashSet<>(List.of(hub))
                );
                continue;
            }

            // Clusterizzazione hub-centrica: ogni neighbor -> cluster locale distinto
            Map<String, Integer> localClusters = new HashMap<>();
            int cid = 0;

            for (String n : hubNeighbors) {
                localClusters.put(n, cid++);
            }

            if (localClusters.isEmpty()) {
                finalClusters.put(
                        globalClusterId++,
                        new HashSet<>(List.of(hub))
                );
                continue;
            }

            // Costruisci cluster globali
            Map<Integer, Set<String>> tmp = new HashMap<>();

            for (var e : localClusters.entrySet()) {
                tmp
                        .computeIfAbsent(e.getValue(), k -> new HashSet<>())
                        .add(e.getKey());
            }

            // Aggiungi l’hub a OGNI cluster locale
            for (Set<String> clusterTopics : tmp.values()) {
                clusterTopics.add(hub);
                finalClusters.put(globalClusterId++, clusterTopics);
            }
        }

        if (finalClusters.isEmpty()) {
            return Collections.emptyMap();
        }

        return finalClusters;
    }
}