package it.unipi.MarketLens.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PipelineConfigUpdateRequest {

    @NotNull
    private Boolean ingestionEnabled;

    @NotBlank
    private String ingestionSchedule;

    @Min(1)
    private Integer batchSize;

    public Boolean getIngestionEnabled() { return ingestionEnabled; }
    public void setIngestionEnabled(Boolean ingestionEnabled) { this.ingestionEnabled = ingestionEnabled; }

    public String getIngestionSchedule() { return ingestionSchedule; }
    public void setIngestionSchedule(String ingestionSchedule) { this.ingestionSchedule = ingestionSchedule; }

    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
}


