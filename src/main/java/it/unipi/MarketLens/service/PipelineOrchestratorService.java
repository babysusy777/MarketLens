package it.unipi.MarketLens.service;

import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.model.PipelineStepType;
import it.unipi.MarketLens.repository.graph.BrandGraphRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PipelineOrchestratorService {

    private final PostIngestionService postIngestionService;
    private final AnalyticsRefreshService analyticsRefreshService;
    private final GraphRecomputeService graphRecomputeService;
    private final CampaignRefreshService campaignRefreshService;
    private final PipelineRunLogger pipelineRunLogger;
    private final BrandGraphRepository brandGraphRepository;

    public PipelineOrchestratorService(
            PostIngestionService postIngestionService,
            AnalyticsRefreshService analyticsRefreshService,
            GraphRecomputeService graphRecomputeService,
            CampaignRefreshService campaignRefreshService,
            PipelineRunLogger pipelineRunLogger,
            BrandGraphRepository brandGraphRepository
    ) {
        this.postIngestionService = postIngestionService;
        this.analyticsRefreshService = analyticsRefreshService;
        this.graphRecomputeService = graphRecomputeService;
        this.campaignRefreshService = campaignRefreshService;
        this.pipelineRunLogger = pipelineRunLogger;
        this.brandGraphRepository = brandGraphRepository;
    }

    public void runIngestionPipeline(int batchSize) throws Exception {
        long t0 = System.currentTimeMillis();
        System.out.println("[Orchestrator] START pipeline batchSize=" + batchSize);

        Map<String, Object> configSnapshot = Map.of(
                "batchSize", batchSize
        );

        Map<String, Object> metadata = Map.of(
                "service", "MarketLens",
                "instanceId", System.getenv().getOrDefault("HOSTNAME", "local")
        );

        String runId = pipelineRunLogger.startRun(configSnapshot, metadata);

        try {
            pipelineRunLogger.startStep(runId, PipelineStepType.INGESTION);
            long t1 = System.currentTimeMillis();
            PostIngestionService.IngestionStats ingStats = postIngestionService.ingestPostsWithStats(batchSize);
            pipelineRunLogger.succeedStep(
                    runId,
                    PipelineStepType.INGESTION,
                    "Ingestion completed",
                    ingStats.toCounters()
            );
            System.out.println("[Orchestrator] Ingestion done in " + (System.currentTimeMillis() - t1) + " ms");

            pipelineRunLogger.startStep(runId, PipelineStepType.ANALYTICS);
            long t2 = System.currentTimeMillis();
            AnalyticsRefreshService.AnalyticsStats anStats = analyticsRefreshService.refreshAllWithStats();
            pipelineRunLogger.succeedStep(
                    runId,
                    PipelineStepType.ANALYTICS,
                    "Analytics refresh completed",
                    anStats.toCounters()
            );
            System.out.println("[Orchestrator] Analytics refresh done in " + (System.currentTimeMillis() - t2) + " ms");


            pipelineRunLogger.startStep(runId, PipelineStepType.GRAPH);
            long t3 = System.currentTimeMillis();


            for (Industry ind : Industry.values()){
                brandGraphRepository.deleteAllSimilarities(ind.name());
                brandGraphRepository.deleteTopicClusters(ind.name());
            }

            GraphRecomputeService.GraphStats grStats = graphRecomputeService.recomputeAllWithStats();

            pipelineRunLogger.startStep(runId, PipelineStepType.CAMPAIGN);
            long t4 = System.currentTimeMillis();
            CampaignRefreshService.CampaignStats cStats = campaignRefreshService.refreshSavedCampaignsWithStats();

            pipelineRunLogger.succeedStep(
                    runId,
                    PipelineStepType.CAMPAIGN,
                    cStats.failed > 0 ? "Campaign refresh completed with some failures" : "Campaign refresh completed",
                    cStats.toCounters()
            );

            System.out.println("[Orchestrator] Campaign refresh done in " + (System.currentTimeMillis() - t4) + " ms");

            pipelineRunLogger.closeRunSuccess(runId);

        } catch (Exception ex) {
            pipelineRunLogger.closeRunFailed(runId, ex);
            throw ex;
        } finally {
            System.out.println("[Orchestrator] END pipeline total " + (System.currentTimeMillis() - t0) + " ms");
        }
    }
}