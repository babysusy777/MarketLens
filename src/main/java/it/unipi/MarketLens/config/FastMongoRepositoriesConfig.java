package it.unipi.MarketLens.config;

import it.unipi.MarketLens.repository.mongo.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "it.unipi.MarketLens.repository.mongo",
        mongoTemplateRef = "fastMongoTemplate",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        PostRepository.class,
                        //PipelineRunLogRepository.class,
                        //UserRepository.class,
                        //BrandRepository.class,
                        //CampaignRepository.class,
                        TrendAnalyticsRepository.class

                }
        )
)
public class FastMongoRepositoriesConfig {}

