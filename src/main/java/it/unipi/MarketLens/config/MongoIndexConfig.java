package it.unipi.MarketLens.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexCreator;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoIndexConfig {

    //Crea gli indici sul database STRONG.
//    @Bean
//    public MongoPersistentEntityIndexCreator strongIndexCreator(
//            MongoMappingContext mappingContext,
//            @Qualifier("strongMongoTemplate") MongoTemplate strongMongoTemplate) {
//
//        return new MongoPersistentEntityIndexCreator(mappingContext, strongMongoTemplate);
//    }

    // @Bean
//    public MongoPersistentEntityIndexCreator fastIndexCreator(
//            MongoMappingContext mappingContext,
//            @Qualifier("fastMongoTemplate") MongoTemplate fastMongoTemplate) {
//
//        return new MongoPersistentEntityIndexCreator(mappingContext, fastMongoTemplate);
//    }

    @Bean
    @Primary
    public MongoMappingContext mongoMappingContext(
            MongoCustomConversions conversions,
            ApplicationContext applicationContext) {

        MongoMappingContext ctx = new MongoMappingContext();
        ctx.setApplicationContext(applicationContext);
        ctx.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        ctx.setAutoIndexCreation(true);
        return ctx;
    }
}