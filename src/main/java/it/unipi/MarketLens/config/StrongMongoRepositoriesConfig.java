package it.unipi.MarketLens.config;

import it.unipi.MarketLens.repository.mongo.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@Configuration
@EnableMongoRepositories(
        basePackages = "it.unipi.MarketLens.repository.mongo",
        mongoTemplateRef = "strongMongoTemplate",
        includeFilters = @Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        UserRepository.class,
                        BrandRepository.class,
                        PipelineConfigRepository.class,
                        IngestionCursorStateRepository.class,
                        //CampaignRepository.class,
                        PipelineRunLogRepository.class
                }
        )
)
public class StrongMongoRepositoriesConfig {}