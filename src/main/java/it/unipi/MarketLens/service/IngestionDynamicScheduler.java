package it.unipi.MarketLens.service;

import it.unipi.MarketLens.config.PipelineConfig;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import it.unipi.MarketLens.events.PipelineConfigUpdatedEvent;
import org.springframework.context.event.EventListener;

@Service
public class IngestionDynamicScheduler {

    private final TaskScheduler taskScheduler;
    private final PipelineConfigService configService;


    private final PipelineOrchestratorService orchestratorService;

    private volatile ScheduledFuture<?> scheduledTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public IngestionDynamicScheduler(
            TaskScheduler taskScheduler,
            PipelineConfigService configService,
            PipelineOrchestratorService orchestratorService
    ) {
        this.taskScheduler = taskScheduler;
        this.configService = configService;
        this.orchestratorService = orchestratorService;
    }

    @PostConstruct
    public void init() {
        System.out.println("[Scheduler] init() called");
        PipelineConfig config = configService.getCurrentConfig();
        reschedule(config);
    }

    public synchronized void reschedule(PipelineConfig config) {
        System.out.println("[Scheduler] reschedule() called -> enabled="
                + config.isIngestionEnabled()
                + " cron=" + config.getIngestionSchedule()
                + " batchSize=" + config.getBatchSize());

        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }


        if (!config.isIngestionEnabled()) {
            return;
        }


        CronTrigger trigger = new CronTrigger(config.getIngestionSchedule());
        scheduledTask = taskScheduler.schedule(() -> safeRun(config.getBatchSize()), trigger);
    }

    private void safeRun(int batchSize) {
        System.out.println("[Scheduler] tick -> batchSize=" + batchSize);


        if (!running.compareAndSet(false, true)) {
            System.out.println("[Scheduler] SKIP: previous run still running");
            return;
        }

        System.out.println("[Scheduler] ENTER run (lock acquired)");
        System.out.flush();

        try {
            System.out.println("[Scheduler] Calling orchestrator...");
            System.out.flush();

            orchestratorService.runIngestionPipeline(batchSize);

            System.out.println("[Scheduler] Orchestrator returned OK");
            System.out.flush();
        } catch (Exception e) {
            System.err.println("[Scheduler] Run failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            running.set(false);
            System.out.println("[Scheduler] EXIT run (lock released)");
            System.out.flush();
        }
    }


    @EventListener
    public void onConfigUpdated(PipelineConfigUpdatedEvent event) {
        reschedule(event.getConfig());
    }
}
