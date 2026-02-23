package it.unipi.MarketLens.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexBootstrap implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mappingContext;

    public MongoIndexBootstrap(@Qualifier("strongMongoTemplate") MongoTemplate mongoTemplate, MongoMappingContext mappingContext) {
        this.mongoTemplate = mongoTemplate;
        this.mappingContext = mappingContext;
    }




    @Override
    public void run(ApplicationArguments args) {
        var resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        mappingContext.getPersistentEntities().stream()
                .filter(e -> e.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Document.class))
                .forEach(e -> {
                    var indexOps = mongoTemplate.indexOps(e.getType());
                    resolver.resolveIndexFor(e.getType()).forEach(indexOps::ensureIndex);
                });
    }
}