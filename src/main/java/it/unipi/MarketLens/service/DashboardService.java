package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.DashboardDbStatusResponse;
import it.unipi.MarketLens.dto.DashboardDbStatusResponse.MongoStats;
import it.unipi.MarketLens.dto.DashboardDbStatusResponse.Neo4jStats;
import it.unipi.MarketLens.dto.DashboardPipelineStatusResponse;
import it.unipi.MarketLens.dto.PipelineRunSummary;
import it.unipi.MarketLens.model.PipelineRunLog;
import it.unipi.MarketLens.model.RunStatus;
import it.unipi.MarketLens.repository.mongo.PipelineRunLogRepository;
import org.bson.Document;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final MongoTemplate mongoTemplate;
    private final Driver neo4jDriver;
    private final PipelineRunLogRepository pipelineRunLogRepository;

    public DashboardService(
            @Qualifier("fastMongoTemplate") MongoTemplate mongoTemplate,
            Driver neo4jDriver,
            PipelineRunLogRepository pipelineRunLogRepository
    ) {
        this.mongoTemplate = mongoTemplate;
        this.neo4jDriver = neo4jDriver;
        this.pipelineRunLogRepository = pipelineRunLogRepository;
    }


    public DashboardDbStatusResponse getDbStatus() {
        boolean mongoUp = false;
        MongoStats mongoStats = null;

        try {

            mongoTemplate.getDb().runCommand(new Document("ping", 1));

            Document stats = mongoTemplate.getDb().runCommand(new Document("dbStats", 1));

            mongoStats = new MongoStats(
                    toInt(stats.get("collections")),
                    toLong(stats.get("objects")),
                    toMb(stats.get("dataSize")),
                    toMb(stats.get("storageSize")),
                    toMb(stats.get("indexSize"))
            );
            mongoUp = true;
        } catch (Exception ignored) {
            mongoUp = false;
            mongoStats = null;
        }

        boolean neo4jUp = false;
        Neo4jStats neo4jStats = null;

        try (Session session = neo4jDriver.session()) {

            session.run("RETURN 1").consume();

            long nodes = session.run("MATCH (n) RETURN count(n) AS c")
                    .single().get("c").asLong();

            long rels = session.run("MATCH ()-[r]->() RETURN count(r) AS c")
                    .single().get("c").asLong();


            Map<String, Long> byLabel = new LinkedHashMap<>();
            byLabel.put("Brand", safeCount(session, "MATCH (n:Brand) RETURN count(n) AS c"));
            byLabel.put("Topic", safeCount(session, "MATCH (n:Topic) RETURN count(n) AS c"));
            byLabel.put("Post", safeCount(session, "MATCH (n:Post) RETURN count(n) AS c"));

            neo4jStats = new Neo4jStats(nodes, rels, byLabel);
            neo4jUp = true;
        } catch (Exception ignored) {
            neo4jUp = false;
            neo4jStats = null;
        }

        return new DashboardDbStatusResponse(mongoUp, mongoStats, neo4jUp, neo4jStats);
    }

    public DashboardPipelineStatusResponse getPipelineStatus() {

        var latestPage = pipelineRunLogRepository.findAllByOrderByStartTimeDesc(PageRequest.of(0, 1));
        PipelineRunSummary latest = latestPage.hasContent()
                ? toSummary(latestPage.getContent().get(0))
                : null;


        PipelineRunSummary running = null;
        try {
            var runningPage = pipelineRunLogRepository.findByStatusOrderByStartTimeDesc(RunStatus.RUNNING, PageRequest.of(0, 1));
            if (runningPage.hasContent()) {
                running = toSummary(runningPage.getContent().get(0));
            }
        } catch (Exception ignored) {

        }


        var recentPage = pipelineRunLogRepository.findAllByOrderByStartTimeDesc(PageRequest.of(0, 10));
        List<PipelineRunSummary> recent = recentPage.getContent().stream().map(this::toSummary).toList();


        List<PipelineRunSummary> recentFailures;
        try {
            var failuresPage = pipelineRunLogRepository.findByStatusOrderByStartTimeDesc(RunStatus.FAILED, PageRequest.of(0, 10));
            recentFailures = failuresPage.getContent().stream().map(this::toSummary).toList();
        } catch (Exception ignored) {

            recentFailures = List.of();
        }

        return new DashboardPipelineStatusResponse(latest, running, recent, recentFailures);
    }

    private PipelineRunSummary toSummary(PipelineRunLog log) {
        return new PipelineRunSummary(
                log.getRunId(),
                log.getStatus() != null ? log.getStatus().name() : null,
                log.getStartTime(),
                log.getEndTime(),
                log.getDurationMs(),
                log.getError()
        );
    }

    private long safeCount(Session session, String cypher) {
        try {
            return session.run(cypher).single().get("c").asLong();
        } catch (Exception e) {
            return 0L;
        }
    }

    private static Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return null;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return null;
    }

    private static Double toMb(Object bytesValue) {
        if (bytesValue == null) return null;
        if (!(bytesValue instanceof Number n)) return null;
        double bytes = n.doubleValue();
        return bytes / (1024.0 * 1024.0);
    }
}
