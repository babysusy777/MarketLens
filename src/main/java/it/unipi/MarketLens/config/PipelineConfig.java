package it.unipi.MarketLens.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pipeline_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineConfig {

    @Id
    private String id;

    private boolean ingestionEnabled;

    private String ingestionSchedule;

     //Numero di post da ingerire per run
    private int batchSize;

    private LocalDateTime lastUpdatedAt;
}
